package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.mongodb.DocumentConverter;
import com.kingpixel.wondertrade.model.UserInfo;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import net.minecraft.server.level.ServerPlayer;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    resetPool(false);
  }

  public void createCollection(String collectionName) {
    try {
      database.createCollection(collectionName);
      CobbleWonderTrade.LOGGER.info("Collection " + collectionName + " created successfully");
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error while creating collection " + collectionName + ": " + e.getMessage());
    }
  }

  @Override
  public List<Pokemon> getSpecialPool(boolean special) {
    List<Document> documents = pool.find().into(new ArrayList<>());
    List<Pokemon> pokemons = new ArrayList<>();
    for (Document document : documents) {
      JsonObject jsonObject = DocumentConverter.documentToJsonObject(document);
      Pokemon pokemon = Pokemon.Companion.loadFromJSON(jsonObject);
      if (!special || WonderTradeUtil.isSpecial(pokemon)) {
        pokemons.add(pokemon);
      }
    }
    return pokemons;
  }

  @Override
  public List<JsonObject> getPokemonList(boolean special) {
    List<Document> documents = pool.find().into(new ArrayList<>());
    List<JsonObject> jsonObjects = new ArrayList<>();
    for (Document document : documents) {
      JsonObject jsonObject = DocumentConverter.documentToJsonObject(document);
      jsonObjects.add(jsonObject);
    }
    return jsonObjects;
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
  public Pokemon putPokemon(Pokemon pokemonPlayer) {
    Document newPokemonDoc = Document.parse(pokemonPlayer.saveToJSON(new JsonObject()).toString());

    // Selecciona un Pokémon aleatorio y lo reemplaza
    List<Document> documents = pool.aggregate(
      List.of(new Document("$sample", new Document("size", 1)))
    ).into(new ArrayList<>());

    if (!documents.isEmpty()) {
      Document selectedDocument = documents.get(0);
      pool.deleteOne(selectedDocument);
      pool.insertOne(newPokemonDoc);
      JsonObject selectedPokemonJson = DocumentConverter.documentToJsonObject(selectedDocument);
      return Pokemon.Companion.loadFromJSON(selectedPokemonJson);
    } else {
      pool.insertOne(newPokemonDoc);
      return pokemonPlayer;
    }
  }

  @Override
  public UserInfo getUserInfo(ServerPlayer player) {
    Document document = users.find(new Document("playeruuid", player.getUUID().toString())).first();
    return document != null ? UserInfo.fromDocument(document) : new UserInfo(player.getUUID());
  }

  @Override
  public UserInfo getUserinfo(UUID uuid) {
    ServerPlayer player = CobbleWonderTrade.server.getPlayerList().getPlayer(uuid);
    if (player == null) {
      return null;
    }
    return getUserInfo(player);
  }

  @Override
  public UserInfo putUserInfo(UserInfo userInfo) {
    return putUserInfo(userInfo, true);
  }

  @Override
  public UserInfo putUserInfo(UserInfo userInfo, boolean update) {
    if (update) {
      return updateUserInfo(userInfo);
    } else {
      userInfo.setDate(System.currentTimeMillis());
      Document existingDoc = users.find(new Document("playeruuid", userInfo.getPlayeruuid().toString())).first();
      if (existingDoc != null) {
        Bson filter = Filters.eq("playeruuid", userInfo.getPlayeruuid().toString());
        Bson updateOperation = Updates.combine(
          Updates.set("messagesend", userInfo.isMessagesend()),
          Updates.set("date", userInfo.getDate())
        );
        users.updateOne(filter, updateOperation, new UpdateOptions().upsert(true));
      } else {
        users.insertOne(userInfo.toDocument());
      }
      return userInfo;
    }
  }

  public UserInfo updateUserInfo(UserInfo userInfo) {
    Bson filter = Filters.eq("playeruuid", userInfo.getPlayeruuid().toString());
    Bson updateOperation = Updates.combine(
      Updates.set("messagesend", userInfo.isMessagesend()),
      Updates.set("date", UserInfo.getDateWithCooldown(CobbleWonderTrade.server.getPlayerList().getPlayer(userInfo.getPlayeruuid())))
    );
    users.updateOne(filter, updateOperation, new UpdateOptions().upsert(true));
    return userInfo;
  }

  @Override
  public void resetPool(boolean force) {
    List<Pokemon> a = getSpecialPool(false);
    if (a.isEmpty()) {
      force = true;
    } else if (!force && a.size() == CobbleWonderTrade.config.getSizePool()) {
      return;
    }

    long targetSize = CobbleWonderTrade.config.getSizePool();
    long count = pool.countDocuments();
    if (!force && count == targetSize) {
      return;
    }

    List<Document> documents = pool.find().into(new ArrayList<>());
    if (count > targetSize) {
      List<Document> sublist = documents.stream().limit(targetSize).collect(Collectors.toList());
      pool.drop();
      pool.insertMany(sublist);
    } else {
      for (int i = documents.size(); i < targetSize; i++) {
        try {
          Document randomPokemonDoc = Document.parse(WonderTradeUtil.getRandomPokemon().saveToJSON(new JsonObject()).toString());
          documents.add(randomPokemonDoc);
        } catch (ExecutionException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      pool.drop();
      pool.insertMany(documents);
    }
  }

  @Override
  public void disconnect() {
    client.close();
    CobbleWonderTrade.LOGGER.info("Disconnected from MongoDB");
  }

  @Override
  public void save() {
    // Implementación para guardar cambios, si es necesario
  }
}
