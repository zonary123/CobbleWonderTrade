package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.model.UserInfo;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MongoDBDatabaseClient extends DatabaseClient {
  private final MongoClient mongoClient;
  private final MongoCollection<Document> pokemonsCollection;
  private final MongoCollection<Document> userInfoCollection;
  private final MongoCollection<Document> restartCollection;

  public MongoDBDatabaseClient(DataBaseConfig config) {
    String connectionString = config.getUrl();
    this.mongoClient = MongoClients.create(MongoClientSettings.builder()
      .applicationName("CobbleWonderTrade")
      .applyConnectionString(new ConnectionString(connectionString))
      .build());
    MongoDatabase database = mongoClient.getDatabase(config.getDatabase());
    this.pokemonsCollection = database.getCollection("pokemons");
    this.userInfoCollection = database.getCollection("user_info");
    this.restartCollection = database.getCollection("restart");
  }

  @Override
  public void connect() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Connecting to MongoDB Database");
    fixPool();
  }

  @Override
  public void disconnect() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Disconnecting from MongoDB Database");
    mongoClient.close();
  }

  @Override
  public UserInfo getUserInfo(ServerPlayerEntity player) {
    var userInfo = DatabaseClientFactory.userInfoMap.get(player.getUuid());
    if (userInfo != null) return userInfo;
    var userInfoDocument = userInfoCollection.find(new Document("playeruuid", player.getUuid().toString())).first();
    if (userInfoDocument != null) {
      UserInfo readUserInfo = Utils.newWithoutSpacingGson().fromJson(userInfoDocument.toJson(), UserInfo.class);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), readUserInfo);
    } else {
      UserInfo newUserInfo = new UserInfo(player);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), newUserInfo);
      updateUserInfo(player, newUserInfo);
    }
    return null;
  }

  @Override
  public Pokemon tradePokemon(ServerPlayerEntity player, Pokemon pokemon) {
    var randomPokemonDocument = pokemonsCollection.aggregate(List.of(new Document("$sample", new Document("size", 1)))).first();
    if (randomPokemonDocument == null) {
      CobbleUtils.LOGGER.warn(CobbleWonderTrade.MOD_ID, "No Pokémon available in the pool");
      return null;
    }

    Pokemon tradedPokemon = Utils.newWithoutSpacingGson().fromJson(randomPokemonDocument.toJson(), Pokemon.class);
    pokemonsCollection.deleteOne(randomPokemonDocument);
    pokemonsCollection.insertOne(Document.parse(Utils.newWithoutSpacingGson().toJson(pokemon)));
    return tradedPokemon;
  }

  @Override
  public List<Pokemon> getPokemonsAnimation() {
    var pokemonDocuments = pokemonsCollection.aggregate(List.of(new Document("$sample", new Document("size", DatabaseClientFactory.POKEMON_ANIMATION_SIZE)))).into(new ArrayList<>());
    List<Pokemon> pokemons = new ArrayList<>();
    for (var doc : pokemonDocuments) {
      pokemons.add(Utils.newWithoutSpacingGson().fromJson(doc.toJson(), Pokemon.class));
    }
    return pokemons;
  }

  @Override
  public List<Pokemon> getAllPokemons() {
    var pokemonDocuments = pokemonsCollection.find().into(new ArrayList<>());
    List<Pokemon> pokemons = new ArrayList<>();
    for (var doc : pokemonDocuments) {
      pokemons.add(Utils.newWithoutSpacingGson().fromJson(doc.toJson(), Pokemon.class));
    }
    return pokemons;
  }

  @Override
  public void restartPool() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Restarting pool in MongoDB");
    pokemonsCollection.deleteMany(new Document());
    List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getSizePool(), 0);
    List<Document> pokemonDocuments = newPokemons.stream()
      .map(pokemon -> Document.parse(Utils.newWithoutSpacingGson().toJson(pokemon)))
      .toList();
    pokemonsCollection.insertMany(pokemonDocuments);
  }

  @Override
  public void updateUserInfo(ServerPlayerEntity player, UserInfo userinfo) {
    var userInfoDocument = Document.parse(Utils.newWithoutSpacingGson().toJson(userinfo));
    userInfoCollection.replaceOne(
      new Document("playeruuid", player.getUuid().toString()),
      userInfoDocument,
      new ReplaceOptions().upsert(true)
    );
  }

  @Override
  public void fixPool() {
    long currentCount = pokemonsCollection.countDocuments();
    int sizePool = CobbleWonderTrade.config.getSizePool();
    if (currentCount < sizePool) {
      List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(sizePool, (int) currentCount);
      List<Document> pokemonDocuments = newPokemons.stream()
        .map(pokemon -> Document.parse(Utils.newWithoutSpacingGson().toJson(pokemon)))
        .toList();
      pokemonsCollection.insertMany(pokemonDocuments);
    } else {
      long excessCount = currentCount - sizePool;
      if (excessCount > 0) {
        var randomPokemonIds = pokemonsCollection.aggregate(List.of(
          new Document("$sample", new Document("size", excessCount)),
          new Document("$project", new Document("_id", 1))
        )).into(new ArrayList<>());

        List<Object> idsToDelete = randomPokemonIds.stream()
          .map(doc -> doc.get("_id"))
          .toList();

        pokemonsCollection.deleteMany(new Document("_id", new Document("$in", idsToDelete)));
      }
    }
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Size pool: " + sizePool + ", current count: " + currentCount);
  }

  @Override
  public boolean shouldRestartPool() {
    if (!super.shouldRestartPool()) return false;

    var restartDocument = restartCollection.find().first();

    long currentTime = System.currentTimeMillis();
    long nextRestartTime = TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getCooldownReset());

    if (restartDocument == null) {
      // Si no existe un registro, crear uno con el tiempo actual + cooldown
      restartCollection.insertOne(new Document("restartAt", currentTime + nextRestartTime));
      return false;
    }

    long restartAt = restartDocument.getLong("restartAt");

    if (currentTime >= restartAt) {
      // Si ya es hora de reiniciar, actualizar el tiempo de reinicio y reiniciar la pool
      restartCollection.updateOne(
        new Document("_id", restartDocument.get("_id")),
        new Document("$set", new Document("restartAt", currentTime + nextRestartTime))
      );
      return true;
    }

    return false;
  }
}