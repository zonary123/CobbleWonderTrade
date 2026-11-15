package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.Model.DataBaseType;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.CommandTree;
import com.kingpixel.wondertrade.database.json.JsonDatabaseClient;
import com.kingpixel.wondertrade.database.mongodb.MongoDBDatabaseClient;
import com.kingpixel.wondertrade.database.sql.MySQLDatabaseClient;
import com.kingpixel.wondertrade.database.sql.SQLiteDatabaseClient;
import com.kingpixel.wondertrade.database.sql.SqlDatabaseClient;
import com.kingpixel.wondertrade.database.sql.dialect.H2Dialect;
import com.kingpixel.wondertrade.model.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Factory and helper methods for database lifecycle and WonderTrade Pokémon generation.
 * Features high-performance species caching and thread-safe operations.
 *
 * @author Carlos Varas Alonso
 */
public class DatabaseClientFactory {
  public static final Map<UUID, UserInfo> userInfoMap = new ConcurrentHashMap<>();
  private static final Map<String, List<Species>> SPECIES_BY_LABEL_CACHE = new ConcurrentHashMap<>();

  public static DatabaseClient databaseClient;
  public static Date cooldown;
  public static final int POKEMON_ANIMATION_SIZE = 8;
  public static final int MIN_POOL_SIZE = POKEMON_ANIMATION_SIZE + 4;

  private DatabaseClientFactory() {}

  public static void createDatabaseClient(DataBaseConfig config) {
    if (CobbleWonderTrade.config.getPool().isAutoReset() && cooldown == null) {
      cooldown = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getPool().getCooldownReset()));
    }
    if (databaseClient != null) {
      databaseClient.disconnect();
    }

    DataBaseType type = config != null ? config.getType() : DataBaseType.JSON;
    if (type == null) type = DataBaseType.JSON;

    switch (type) {
      case JSON -> databaseClient = new JsonDatabaseClient(config);
      case MYSQL, MARIADB -> databaseClient = new MySQLDatabaseClient(config);
      case MONGODB -> databaseClient = new MongoDBDatabaseClient(config);
      case SQLITE -> databaseClient = new SQLiteDatabaseClient(config);
      case H2 -> databaseClient = new SqlDatabaseClient(config, new H2Dialect());
      default -> {
        CobbleWonderTrade.LOGGER.error("Database type not supported: " + type + ", falling back to JSON");
        databaseClient = new JsonDatabaseClient(config);
      }
    }

    databaseClient.connect();
    CommandTree.invalidateStats();
  }

  public static List<Pokemon> getGeneratedPool(int sizePool, int currentSize) {
    int countToGenerate = Math.max(0, sizePool - currentSize);
    if (countToGenerate <= 0) return new ArrayList<>();

    var pokemons = CobbleWonderTrade.config.getPool().getFilterGenerationPokemon().generateRandomPokemons(
      CobbleWonderTrade.MOD_ID,
      "pool",
      countToGenerate
    );

    if (pokemons == null) pokemons = new ArrayList<>();

    applyProperties(pokemons);
    putLevels(pokemons);
    return pokemons;
  }

  private static void applyProperties(List<Pokemon> pokemons) {
    for (int i = 0; i < pokemons.size(); i++) {
      Pokemon pokemon = pokemons.get(i);
      if (pokemon == null) continue;
      boolean replaced = false;

      // Mythical rate
      if (CobbleWonderTrade.config.getRates().getMythicalrate() > 0) {
        int mythical = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getMythicalrate());
        if (mythical == 0 && !pokemon.isMythical()) {
          Pokemon newPokemon = getMythical();
          if (newPokemon != null) {
            applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getRates().getMythicalperfectivs());
            pokemons.set(i, newPokemon);
            replaced = true;
          }
        }
      }

      // Legendary rate
      if (!replaced && CobbleWonderTrade.config.getRates().getLegendaryrate() > 0) {
        int legendary = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getLegendaryrate());
        if (legendary == 0 && !pokemon.isLegendary()) {
          Pokemon newPokemon = getLegendary();
          if (newPokemon != null) {
            applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getRates().getLegendaryperfectivs());
            pokemons.set(i, newPokemon);
            replaced = true;
          }
        }
      }

      // Ultra Beast rate
      if (!replaced && CobbleWonderTrade.config.getRates().getUltrabeastrate() > 0) {
        int ultraBeast = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getUltrabeastrate());
        if (ultraBeast == 0 && !pokemon.isUltraBeast()) {
          Pokemon newPokemon = getUltraBeast();
          if (newPokemon != null) {
            applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getRates().getUltrabeastperfectivs());
            pokemons.set(i, newPokemon);
            replaced = true;
          }
        }
      }

      // Paradox rate
      if (!replaced && CobbleWonderTrade.config.getRates().getParadoxrate() > 0) {
        int paradox = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getParadoxrate());
        boolean isParadox = pokemon.getForm() != null && pokemon.getForm().getLabels() != null
            && pokemon.getForm().getLabels().contains(CobblemonPokemonLabels.PARADOX);
        if (paradox == 0 && !isParadox) {
          Pokemon newPokemon = getParadox();
          if (newPokemon != null) {
            applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getRates().getParadoxperfectivs());
            pokemons.set(i, newPokemon);
            replaced = true;
          }
        }
      }

      // Shiny rate
      if (CobbleWonderTrade.config.getRates().getShinyrate() > 0) {
        int shiny = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getShinyrate());
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

  private static List<Species> getSpeciesByLabel(String label) {
    return SPECIES_BY_LABEL_CACHE.computeIfAbsent(label, l ->
      PokemonSpecies.getImplemented().stream()
        .filter(s -> s.getLabels().contains(l))
        .toList()
    );
  }

  private static Pokemon getRandomByLabel(String label) {
    List<Species> filtered = getSpeciesByLabel(label);
    if (filtered.isEmpty()) {
      var implemented = PokemonSpecies.getImplemented();
      if (implemented.isEmpty()) return null;
      return implemented.get(0).create(1);
    }
    return filtered.get(Utils.getRandom().nextInt(filtered.size())).create(1);
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
    if (pokemons == null) return;
    for (Pokemon pokemon : pokemons) {
      if (pokemon != null) {
        setLevel(pokemon);
      }
    }
  }

  public static void setLevel(Pokemon pokemon) {
    if (pokemon == null) return;
    int min = CobbleWonderTrade.config.getPool().getMinlv();
    int max = CobbleWonderTrade.config.getPool().getMaxlv();
    if (min > max) {
      int temp = min;
      min = max;
      max = temp;
    }
    pokemon.setLevel(Utils.getRandom().nextInt(min, max + 1));
    try {
      if (Cobblemon.INSTANCE != null && Cobblemon.INSTANCE.getConfig() != null) {
        int maxLvl = Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel();
        if (pokemon.getLevel() > maxLvl) {
          pokemon.setLevel(maxLvl);
        }
      }
    } catch (Throwable ignored) {}
  }
}
