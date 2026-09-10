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
import com.kingpixel.wondertrade.model.UserInfo;

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

    DataBaseType type = config.getType();
    if (type == null) type = DataBaseType.JSON;

    switch (type) {
      case JSON -> databaseClient = new JsonDatabaseClient(config);
      case MYSQL -> databaseClient = new MySQLDatabaseClient(config);
      case MONGODB -> databaseClient = new MongoDBDatabaseClient(config);
      case SQLITE -> databaseClient = new SQLiteDatabaseClient(config);
      default ->
        throw new IllegalArgumentException("Database type not supported -> " + Arrays.toString(DataBaseType.values()));
    }

    databaseClient.connect();
    CommandTree.invalidateStats();
  }

  public static List<Pokemon> getGeneratedPool(int sizePool, int currentSize) {
    int countToGenerate = Math.max(0, sizePool - currentSize);
    var pokemons = CobbleWonderTrade.config.getPool().getFilterGenerationPokemon().generateRandomPokemons(
      CobbleWonderTrade.MOD_ID,
      "pool",
      countToGenerate
    );

    applyProperties(pokemons);
    putLevels(pokemons);
    return pokemons;
  }

  private static void applyProperties(List<Pokemon> pokemons) {
    for (int i = 0; i < pokemons.size(); i++) {
      Pokemon pokemon = pokemons.get(i);
      boolean replaced = false;

      // Mythical rate
      if (CobbleWonderTrade.config.getRates().getMythicalrate() > 0) {
        int mythical = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getMythicalrate());
        if (mythical == 0 && !pokemon.isMythical()) {
          Pokemon newPokemon = getMythical();
          applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getRates().getMythicalperfectivs());
          pokemons.set(i, newPokemon);
          replaced = true;
        }
      }

      // Legendary rate
      if (!replaced && CobbleWonderTrade.config.getRates().getLegendaryrate() > 0) {
        int legendary = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getLegendaryrate());
        if (legendary == 0 && !pokemon.isLegendary()) {
          Pokemon newPokemon = getLegendary();
          applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getRates().getLegendaryperfectivs());
          pokemons.set(i, newPokemon);
          replaced = true;
        }
      }

      // Ultra Beast rate
      if (!replaced && CobbleWonderTrade.config.getRates().getUltrabeastrate() > 0) {
        int ultraBeast = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getUltrabeastrate());
        if (ultraBeast == 0 && !pokemon.isUltraBeast()) {
          Pokemon newPokemon = getUltraBeast();
          applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getRates().getUltrabeastperfectivs());
          pokemons.set(i, newPokemon);
          replaced = true;
        }
      }

      // Paradox rate
      if (!replaced && CobbleWonderTrade.config.getRates().getParadoxrate() > 0) {
        int paradox = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getParadoxrate());
        if (paradox == 0 && !pokemon.getForm().getLabels().contains(CobblemonPokemonLabels.PARADOX)) {
          Pokemon newPokemon = getParadox();
          applyPerfectIvs(newPokemon, CobbleWonderTrade.config.getRates().getParadoxperfectivs());
          pokemons.set(i, newPokemon);
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
      return PokemonSpecies.getImplemented().get(0).create(1);
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
    for (Pokemon pokemon : pokemons) {
      setLevel(pokemon);
    }
  }

  public static void setLevel(Pokemon pokemon) {
    int min = CobbleWonderTrade.config.getPool().getMinlv();
    int max = CobbleWonderTrade.config.getPool().getMaxlv();
    if (min > max) {
      int temp = min;
      min = max;
      max = temp;
    }
    pokemon.setLevel(Utils.getRandom().nextInt(min, max + 1));
    if (pokemon.getLevel() > Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel()) {
      pokemon.setLevel(Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel());
    }
  }
}
