package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.CommandTree;
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

/**
 * MongoDB database client implementation.
 *
 * @author Carlos Varas Alonso
 */
public class MongoDBDatabaseClient extends DatabaseClient {
  private final MongoClient mongoClient;
  private final MongoCollection<Document> pokemonsCollection;
  private final MongoCollection<Document> userInfoCollection;
  private final MongoCollection<Document> restartCollection;

  public MongoDBDatabaseClient(DataBaseConfig config) {
    String connectionString = config.getUrl();
    this.mongoClient = MongoClients.create(MongoClientSettings.builder()
      .applicationName("UltraWondertrade")
      .applyConnectionString(new ConnectionString(connectionString))
      .build());
    MongoDatabase database = mongoClient.getDatabase(config.getDatabase());
    this.pokemonsCollection = database.getCollection("pokemons");
    this.userInfoCollection = database.getCollection("user_info");
    this.restartCollection = database.getCollection("restart");
  }

  @Override
  public void connect() {
    CobbleWonderTrade.LOGGER.info("Connecting to MongoDB Database");
    fixPool();
  }

  @Override
  public void disconnect() {
    CobbleWonderTrade.LOGGER.info("Disconnecting from MongoDB Database");
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  @Override
  public UserInfo getUserInfo(ServerPlayerEntity player) {
    if (player == null) return null;
    UserInfo userInfo = DatabaseClientFactory.userInfoMap.get(player.getUuid());
    if (userInfo != null) return userInfo;

    Document userInfoDocument = userInfoCollection.find(new Document("playeruuid", player.getUuid().toString())).first();
    if (userInfoDocument != null) {
      userInfo = UtilsFile.getGson().fromJson(userInfoDocument.toJson(), UserInfo.class);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
    } else {
      userInfo = new UserInfo(player);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
      updateUserInfo(player, userInfo);
    }
    return userInfo;
  }

  @Override
  public Pokemon tradePokemon(ServerPlayerEntity player, Pokemon pokemon) {
    Document randomPokemonDocument = pokemonsCollection.aggregate(List.of(new Document("$sample", new Document("size", 1)))).first();
    if (randomPokemonDocument == null) {
      CobbleWonderTrade.LOGGER.warn("No Pokémon available in the pool");
      return null;
    }

    Pokemon tradedPokemon = UtilsFile.getGson().fromJson(randomPokemonDocument.toJson(), Pokemon.class);
    pokemonsCollection.deleteOne(randomPokemonDocument);
    pokemonsCollection.insertOne(Document.parse(UtilsFile.getGson().toJson(pokemon)));
    CommandTree.invalidateStats();
    return tradedPokemon;
  }

  @Override
  public List<Pokemon> getPokemonsAnimation() {
    List<Document> pokemonDocuments = pokemonsCollection.aggregate(
      List.of(new Document("$sample", new Document("size", DatabaseClientFactory.POKEMON_ANIMATION_SIZE)))
    ).into(new ArrayList<>());

    List<Pokemon> pokemons = new ArrayList<>();
    for (Document doc : pokemonDocuments) {
      pokemons.add(UtilsFile.getGson().fromJson(doc.toJson(), Pokemon.class));
    }
    return pokemons;
  }

  @Override
  public List<Pokemon> getAllPokemons() {
    List<Document> pokemonDocuments = pokemonsCollection.find().into(new ArrayList<>());
    List<Pokemon> pokemons = new ArrayList<>();
    for (Document doc : pokemonDocuments) {
      pokemons.add(UtilsFile.getGson().fromJson(doc.toJson(), Pokemon.class));
    }
    return pokemons;
  }

  @Override
  public void restartPool() {
    CobbleWonderTrade.LOGGER.info("Restarting pool in MongoDB");
    pokemonsCollection.deleteMany(new Document());
    List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getPool().getSizePool(), 0);
    List<Document> pokemonDocuments = newPokemons.stream()
      .map(pokemon -> Document.parse(UtilsFile.getGson().toJson(pokemon)))
      .toList();
    if (!pokemonDocuments.isEmpty()) {
      pokemonsCollection.insertMany(pokemonDocuments);
    }
    CommandTree.invalidateStats();
  }

  @Override
  public void updateUserInfo(ServerPlayerEntity player, UserInfo userinfo) {
    if (player == null || userinfo == null) return;
    Document userInfoDocument = Document.parse(UtilsFile.getGson().toJson(userinfo));
    userInfoCollection.replaceOne(
      new Document("playeruuid", player.getUuid().toString()),
      userInfoDocument,
      new ReplaceOptions().upsert(true)
    );
  }

  @Override
  public void fixPool() {
    long currentCount = pokemonsCollection.countDocuments();
    int sizePool = CobbleWonderTrade.config.getPool().getSizePool();
    if (currentCount < sizePool) {
      List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(sizePool, (int) currentCount);
      List<Document> pokemonDocuments = newPokemons.stream()
        .map(pokemon -> Document.parse(UtilsFile.getGson().toJson(pokemon)))
        .toList();
      if (!pokemonDocuments.isEmpty()) {
        pokemonsCollection.insertMany(pokemonDocuments);
      }
    } else {
      long excessCount = currentCount - sizePool;
      if (excessCount > 0) {
        List<Document> randomPokemonIds = pokemonsCollection.aggregate(List.of(
          new Document("$sample", new Document("size", excessCount)),
          new Document("$project", new Document("_id", 1))
        )).into(new ArrayList<>());

        List<Object> idsToDelete = randomPokemonIds.stream()
          .map(doc -> doc.get("_id"))
          .toList();

        pokemonsCollection.deleteMany(new Document("_id", new Document("$in", idsToDelete)));
      }
    }
    CommandTree.invalidateStats();
    CobbleWonderTrade.LOGGER.info("Size pool: " + sizePool + ", current count: " + currentCount);
  }

  @Override
  public boolean shouldRestartPool() {
    if (!super.shouldRestartPool()) return false;

    Document restartDocument = restartCollection.find().first();
    long currentTime = System.currentTimeMillis();
    long nextRestartTime = TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getPool().getCooldownReset());

    if (restartDocument == null) {
      restartCollection.insertOne(new Document("restartAt", currentTime + nextRestartTime));
      return false;
    }

    long restartAt = restartDocument.getLong("restartAt");
    if (currentTime >= restartAt) {
      restartCollection.updateOne(
        new Document("_id", restartDocument.get("_id")),
        new Document("$set", new Document("restartAt", currentTime + nextRestartTime))
      );
      return true;
    }

    return false;
  }
}