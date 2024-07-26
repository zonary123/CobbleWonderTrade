package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.mongodb.DocumentConverter;
import com.kingpixel.wondertrade.model.UserInfo;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import net.minecraft.server.level.ServerPlayer;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MongoDBClient implements DatabaseClient {
  private MongoCollection<Document> pool;
  private MongoCollection<Document> users;
  private final MongoClient client;
  private final MongoDatabase database;

  public MongoDBClient(String uri, String databaseName, String user, String password) {
    this.client = MongoClients.create(uri);
    this.database = client.getDatabase(databaseName);
  }

  @Override
  public void connect() {
    CobbleWonderTrade.LOGGER.info("Connecting to MongoDB");
    try {
      createCollection("users");
      createCollection("pool");
      this.users = database.getCollection("users");
      this.pool = database.getCollection("pool");
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error while connecting to MongoDB: " + e.getMessage());
    }
  }

  private <T> CompletableFuture<T> toCompletableFuture(Publisher<T> publisher) {
    CompletableFuture<T> future = new CompletableFuture<>();
    publisher.subscribe(new Subscriber<T>() {
      @Override
      public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(T t) {
        if (!future.isDone()) {
          future.complete(t);
        }
      }

      @Override
      public void onError(Throwable t) {
        if (!future.isDone()) {
          future.completeExceptionally(t);
        }
      }

      @Override
      public void onComplete() {
        if (!future.isDone()) {
          future.complete(null);
        }
      }
    });
    return future;
  }

  private CompletableFuture<List<Document>> toCompletableFutureList(Publisher<Document> publisher) {
    CompletableFuture<List<Document>> future = new CompletableFuture<>();
    List<Document> documents = new ArrayList<>();
    publisher.subscribe(new Subscriber<Document>() {
      @Override
      public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Document document) {
        documents.add(document);
      }

      @Override
      public void onError(Throwable t) {
        future.completeExceptionally(t);
      }

      @Override
      public void onComplete() {
        future.complete(documents);
      }
    });
    return future;
  }

  public void createCollection(String collectionName) {
    database.createCollection(collectionName).subscribe(new Subscriber<Void>() {
      @Override
      public void onSubscribe(Subscription s) {
        s.request(1);
      }

      @Override
      public void onNext(Void aVoid) {
        // No-op
      }

      @Override
      public void onError(Throwable t) {
        CobbleWonderTrade.LOGGER.error("Error while creating collection " + collectionName + ": " + t.getMessage());
      }

      @Override
      public void onComplete() {
        CobbleWonderTrade.LOGGER.info("Collection " + collectionName + " created successfully");
      }
    });
  }

  @Override
  public CompletableFuture<List<Pokemon>> getSpecialPool(boolean special) {
    return toCompletableFutureList(pool.find())
      .thenApply(documents -> {
        List<Pokemon> pokemons = new ArrayList<>();
        for (Document document : documents) {
          JsonObject jsonObject = DocumentConverter.documentToJsonObject(document);
          Pokemon pokemon = Pokemon.Companion.loadFromJSON(jsonObject);
          if (!special) {
            pokemons.add(pokemon);
          } else {
            if (pokemon.getShiny() || pokemon.isLegendary() || pokemon.isUltraBeast() || PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31) {
              pokemons.add(pokemon);
            }
          }
        }
        return pokemons;
      });
  }

  @Override
  public CompletableFuture<List<JsonObject>> getPokemonList(boolean special) {
    return toCompletableFutureList(pool.find())
      .thenApply(documents -> {
        List<JsonObject> jsonObjects = new ArrayList<>();
        for (Document document : documents) {
          JsonObject jsonObject = DocumentConverter.documentToJsonObject(document);
          jsonObjects.add(jsonObject);
        }
        return jsonObjects;
      });
  }

  @Override
  public Pokemon getRandomPokemon() {
    try {
      return WonderTradeUtil.getRandomPokemon();
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CompletableFuture<Pokemon> putPokemon(Pokemon pokemonPlayer) {
    // Primero, prepara el documento del nuevo Pokémon
    Document newPokemonDoc = Document.parse(pokemonPlayer.saveToJSON(new JsonObject()).toString());

    // Ejecutar la operación de agregación para seleccionar un Pokémon aleatorio
    return toCompletableFutureList(pool.aggregate(
      List.of(
        new Document("$sample", new Document("size", 1)) // Selecciona un documento aleatorio
      )
    ))
      .thenCompose(documents -> {
        if (!documents.isEmpty()) {
          Document selectedDocument = documents.get(0);

          // Eliminar el Pokémon seleccionado aleatoriamente
          return toCompletableFuture(pool.deleteOne(selectedDocument))
            .thenCompose(deleteResult ->
              toCompletableFuture(pool.insertOne(newPokemonDoc))
                .thenApply(aVoid -> {
                  JsonObject selectedPokemonJson = DocumentConverter.documentToJsonObject(selectedDocument);
                  return Pokemon.Companion.loadFromJSON(selectedPokemonJson);
                })
            );
        } else {
          // Si no se encontraron documentos (aunque esto debería ser raro), simplemente insertar el nuevo Pokémon
          return toCompletableFuture(pool.insertOne(newPokemonDoc))
            .thenApply(aVoid -> pokemonPlayer);
        }
      })
      .exceptionally(e -> {
        // Manejo de excepciones
        CobbleWonderTrade.LOGGER.error("Error while putting Pokemon: " + e.getMessage());
        throw new RuntimeException(e);
      });
  }


  @Override
  public CompletableFuture<UserInfo> getUserInfo(ServerPlayer player) {
    CompletableFuture<UserInfo> future = new CompletableFuture<>();

    toCompletableFuture(users.find(new Document("playeruuid", player.getUUID().toString())).first())
      .thenAccept(document -> {
        UserInfo userInfo = document != null ? UserInfo.fromDocument(document) :
          new UserInfo(player.getUUID());
        future.complete(userInfo);
      })
      .exceptionally(t -> {
        future.completeExceptionally(t);
        return null;
      });

    return future;
  }

  @Override
  public CompletableFuture<UserInfo> getUserinfo(UUID uuid) {
    return CompletableFuture.supplyAsync(() -> {
      ServerPlayer player = CobbleWonderTrade.server.getPlayerList().getPlayer(uuid);
      if (player == null) {
        return null;
      }
      return getUserInfo(player).join();
    });
  }

  @Override public CompletableFuture<UserInfo> putUserInfo(UserInfo userInfo) {
    return putUserInfo(userInfo, true);
  }

  @Override
  public CompletableFuture<UserInfo> putUserInfo(UserInfo userInfo, boolean update) {
    if (update) {
      // Actualiza el documento si existe
      return updateUserInfo(userInfo);
    } else {
      userInfo.setDate(new Date(1));
      return toCompletableFuture(users.find(new Document("playeruuid", userInfo.getPlayeruuid().toString())).first())
        .thenCompose(existingDoc -> {
          if (existingDoc != null) {
            Bson filter = Filters.eq("playeruuid", userInfo.getPlayeruuid().toString());
            Bson updateOperation = Updates.combine(
              Updates.set("messagesend", userInfo.isMessagesend()),
              Updates.set("date", userInfo.getDate())
            );
            UpdateOptions options = new UpdateOptions().upsert(true);

            // Ejecuta la actualización o inserción
            return toCompletableFuture(users.updateOne(filter, updateOperation, options))
              .thenApply(updateResult -> userInfo);
          } else {
            // Si el documento no existe, inserta uno nuevo
            return toCompletableFuture(users.insertOne(userInfo.toDocument()))
              .thenApply(aVoid -> userInfo);
          }
        })
        .exceptionally(t -> {
          CobbleWonderTrade.LOGGER.error("Error while putting user info: " + t.getMessage());
          throw new RuntimeException(t);
        });
    }
  }

  public CompletableFuture<UserInfo> updateUserInfo(UserInfo userInfo) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        Bson filter = Filters.eq("playeruuid", userInfo.getPlayeruuid().toString());

        Bson updateOperation = Updates.combine(
          Updates.set("messagesend", userInfo.isMessagesend()),
          Updates.set("date", UserInfo.getDateWithCooldown())
        );

        UpdateOptions options = new UpdateOptions().upsert(true);

        var result = toCompletableFuture(users.updateOne(filter, updateOperation, options)).join();

        if (result.getMatchedCount() == 0 && result.getUpsertedId() == null) {
          throw new RuntimeException("No document matched the filter and no new document was inserted.");
        }

        if (CobbleWonderTrade.config.isDebug()) {
          CobbleWonderTrade.LOGGER.info("Updated or inserted userInfo: " + userInfo);
        }

        return userInfo;
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error updating or inserting userInfo", e);
      }
    });
  }


  @Override
  public void resetPool() {
    try {
      if (getSpecialPool(false).get().size() == CobbleWonderTrade.config.getSizePool()) return;

    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    toCompletableFuture(pool.countDocuments())
      .thenCompose(count -> {
        int targetSize = CobbleWonderTrade.config.getSizePool();

        if (count == targetSize) {
          return CompletableFuture.completedFuture(null);
        }

        return toCompletableFutureList(pool.find())
          .thenCompose(documents -> {
            if (count > targetSize) {
              List<Document> sublist = documents.stream()
                .limit(targetSize)
                .collect(Collectors.toList());
              return toCompletableFuture(pool.drop())
                .thenCompose(aVoid -> toCompletableFuture(pool.insertMany(sublist)));
            } else {
              List<Document> documentList = new ArrayList<>(documents);
              List<CompletableFuture<Document>> pokemonFutures = new ArrayList<>();
              for (int i = documentList.size(); i < targetSize; i++) {
                pokemonFutures.add(CompletableFuture.supplyAsync(() -> {
                  try {
                    return Document.parse(WonderTradeUtil.getRandomPokemon().saveToJSON(new JsonObject()).toString());
                  } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                }));
              }
              return CompletableFuture.allOf(pokemonFutures.toArray(new CompletableFuture[0]))
                .thenCompose(v -> {
                  pokemonFutures.forEach(future -> {
                    try {
                      documentList.add(future.get());
                    } catch (InterruptedException | ExecutionException e) {
                      CobbleWonderTrade.LOGGER.error("Error while getting Pokemon future: " + e.getMessage());
                    }
                  });
                  return toCompletableFuture(pool.drop())
                    .thenCompose(aVoid -> toCompletableFuture(pool.insertMany(documentList)));
                });
            }
          });
      })
      .exceptionally(e -> {
        CobbleWonderTrade.LOGGER.error("Error while resetting pool: " + e.getMessage());
        return null;
      });
  }

  @Override
  public void disconnect() {
    client.close();
    CobbleWonderTrade.LOGGER.info("Disconnected from MongoDB");
  }

  @Override
  public void save() {

  }
}
