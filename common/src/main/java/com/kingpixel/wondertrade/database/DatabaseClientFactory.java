package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
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
      boolean replaced = false;

      // Mythical rate
      if (CobbleWonderTrade.config.getMythicalrate() > 0) {
        int mythical = Utils.RANDOM.nextInt(CobbleWonderTrade.config.getMythicalrate());
        if (mythical == 0 && !pokemon.isMythical()) {
          Pokemon newPokemon = DatabaseClientFactory.getMythical();
          applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getMythicalperfectivs());
          pokemons.set(i, newPokemon);
          replaced = true;
        }
      }

      // Legendary rate
      if (!replaced && CobbleWonderTrade.config.getLegendaryrate() > 0) {
        int legendary = Utils.RANDOM.nextInt(CobbleWonderTrade.config.getLegendaryrate());
        if (legendary == 0 && !pokemon.isLegendary()) {
          Pokemon newPokemon = DatabaseClientFactory.getLegendary();
          applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getLegendaryperfectivs());
          pokemons.set(i, newPokemon);
          replaced = true;
        }
      }

      // Ultra Beast rate
      if (!replaced && CobbleWonderTrade.config.getUltrabeastrate() > 0) {
        int ultraBeast = Utils.RANDOM.nextInt(CobbleWonderTrade.config.getUltrabeastrate());
        if (ultraBeast == 0 && !pokemon.isUltraBeast()) {
          Pokemon newPokemon = DatabaseClientFactory.getUltraBeast();
          applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getUltrabeastperfectivs());
          pokemons.set(i, newPokemon);
          replaced = true;
        }
      }

      // Paradox rate
      if (!replaced && CobbleWonderTrade.config.getParadoxrate() > 0) {
        int paradox = Utils.RANDOM.nextInt(CobbleWonderTrade.config.getParadoxrate());
        if (paradox == 0 && !pokemon.getForm().getLabels().contains(CobblemonPokemonLabels.PARADOX)) {
          Pokemon newPokemon = DatabaseClientFactory.getParadox();
          applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getParadoxperfectivs());
          pokemons.set(i, newPokemon);
        }
      }

      // Shiny rate
      if (CobbleWonderTrade.config.getShinyrate() > 0) {
        int shiny = Utils.RANDOM.nextInt(CobbleWonderTrade.config.getShinyrate());
        if (shiny == 0) {
          pokemons.get(i).setShiny(true);
        }
      }
    }
  }

  public static void applyPerfectIvs(Pokemon pokemon, int count) {
    if (count <= 0) return;
    PokemonProperties.Companion.parse("min_perfect_ivs=" + count).apply(pokemon);
  }

  private static Pokemon getRandomByLabel(String label) {
    List<Species> filtered = PokemonSpecies.getImplemented().stream()
            .filter(s -> s.getLabels().contains(label))
            .toList();
    return filtered.get(Utils.RANDOM.nextInt(filtered.size())).create(1);
  }

  public static Pokemon getLegendary() {
    return getRandomByLabel(CobblemonPokemonLabels.LEGENDARY);
  }

  public static Pokemon getMythical() {
    return getRandomByLabel(CobblemonPokemonLabels.MYTHICAL);
  }

  public static Pokemon getUltraBeast() {
    return getRandomByLabel(CobblemonPokemonLabels.ULTRA_BEAST);
  }

  public static Pokemon getParadox() {
    return getRandomByLabel(CobblemonPokemonLabels.PARADOX);
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
