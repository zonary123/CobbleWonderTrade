package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.Model.DataBaseType;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.model.UserInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:03
 */
public class DatabaseClientFactory {
  public static Map<UUID, UserInfo> userInfoMap = new ConcurrentHashMap<>();
  public static DatabaseClient databaseClient;
  public static Date cooldown;
  public static final int POKEMON_ANIMATION_SIZE = 8;
  public static final int MIN_POOL_SIZE = POKEMON_ANIMATION_SIZE + 4;


  public static void createDatabaseClient(DataBaseConfig config) {
    if (CobbleWonderTrade.config.isAutoReset() && cooldown == null) {
      cooldown = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getCooldownReset()));
    }
    if (databaseClient != null) {
      databaseClient.disconnect();
    }

    switch (config.getType()) {
      case JSON -> databaseClient = new JsonDatabaseClient(config);
      case MYSQL -> databaseClient = new MySQLDatabaseClient(config);
      case MONGODB -> databaseClient = new MongoDBDatabaseClient(config);
      case SQLITE -> databaseClient = new SQLiteDatabaseClient(config);
      default ->
        throw new IllegalArgumentException("Database type not supported -> " + Arrays.toString(DataBaseType.values()));
    }


    databaseClient.connect();
  }

  public static List<Pokemon> getGeneratedPool(int sizePool, int currentSize) {
    var pokemons = CobbleWonderTrade.config.getFilterGenerationPokemon().generateRandomPokemons(
      CobbleWonderTrade.MOD_ID,
      "pool",
      sizePool - currentSize);

    applyProperties(pokemons);
    putLevels(pokemons);
    return pokemons;
  }


  private static void applyProperties(List<Pokemon> pokemons) {
    for (int i = 0; i < pokemons.size(); i++) {
      Pokemon pokemon = pokemons.get(i);
      if (CobbleWonderTrade.config.getLegendaryrate() > 0) {
        int legendary = Utils.RANDOM.nextInt(CobbleWonderTrade.config.getLegendaryrate());
        if (legendary == 0 && !pokemon.getForm().getLabels().contains(CobblemonPokemonLabels.LEGENDARY)) {
          pokemons.set(i, DatabaseClientFactory.getLegendary());
        }
      }
      if (CobbleWonderTrade.config.getShinyrate() > 0) {
        int shiny = Utils.RANDOM.nextInt(CobbleWonderTrade.config.getShinyrate());
        if (shiny == 0) {
          pokemons.get(i).setShiny(true);
        }
      }
    }
  }

  public static Pokemon getLegendary() {
    var species = PokemonSpecies.INSTANCE.getImplemented();
    List<Species> legendaries = new ArrayList<>();
    for (Species s : species) {
      if (s.getLabels().contains(CobblemonPokemonLabels.LEGENDARY)) {
        legendaries.add(s);
      }
    }
    return legendaries.get(Utils.RANDOM.nextInt(legendaries.size())).create(1);
  }

  public static void putLevels(List<Pokemon> pokemons) {
    for (Pokemon pokemon : pokemons) {
      setLevel(pokemon);
    }
  }


  public static void setLevel(Pokemon pokemon) {
    pokemon.setLevel(Utils.RANDOM.nextInt(CobbleWonderTrade.config.getMinlv(), CobbleWonderTrade.config.getMaxlv() + 1));
    if (pokemon.getLevel() > Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel()) {
      pokemon.setLevel(Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel());
    }
  }
}
