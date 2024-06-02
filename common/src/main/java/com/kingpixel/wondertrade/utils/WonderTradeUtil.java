package com.kingpixel.wondertrade.utils;

import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.wondertrade.CobbleWonderTrade;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Carlos Varas Alonso - 26/05/2024 4:23
 */
public class WonderTradeUtil {
  public static ArrayList<Species> pokemons;
  public static ArrayList<Species> legendarys;

  public static void init() {
    Collection<Species> species = PokemonSpecies.INSTANCE.getSpecies();
    Set<String> pokeBlacklist = new HashSet<>(CobbleWonderTrade.config.getPokeblacklist());

    Map<Boolean, List<Species>> sortedSpecies = species.stream()
      .filter(species1 -> species1.getNationalPokedexNumber() != 9999)
      .filter(species1 -> !pokeBlacklist.contains(species1.getName().replace(" ", "").replace("-", "").replace(":",
        "").replace(".", "").trim()))
      .filter(species1 -> {
        Pokemon p = new Pokemon();
        p.setSpecies(species1);
        double rarity = CobbleWonderTrade.spawnRates.getRarity(p);
        return p.isLegendary() || rarity >= 0;
      })
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .collect(Collectors.partitioningBy(species1 -> {
        Pokemon p = new Pokemon();
        p.setSpecies(species1);
        return p.isLegendary();
      }));

    pokemons = new ArrayList<>(sortedSpecies.get(false));
    legendarys = new ArrayList<>(sortedSpecies.get(true));
  }

  public static void messagePool(List<Pokemon> pokemons) {
    int shinys = (int) pokemons.stream().filter(Pokemon::getShiny).count();
    int legendaries = (int) pokemons.stream().filter(Pokemon::isLegendary).count();
    int total = pokemons.size();

    CobbleWonderTrade.server.getPlayerList().getPlayers().forEach(player -> {
      player.sendSystemMessage(TextUtil.parseHexCodes(CobbleWonderTrade.language.getMessagepoolwondertrade()
        .replace("%shinys%", String.valueOf(shinys))
        .replace("%legendaries%", String.valueOf(legendaries))
        .replace("%total%", String.valueOf(total))
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
    });

  }

  private static final Random RANDOM = new Random();

  public static Pokemon getRandomPokemon() {
    Pokemon pokemon = new Pokemon();
    pokemon.createPokemonProperties(PokemonPropertyExtractor.ALL);
    List<Pokemon> listpokemons = CobbleWonderTrade.manager.getPokemonList();

    int shinyR = RANDOM.nextInt(CobbleWonderTrade.config.getShinyrate());
    int legendaryR = RANDOM.nextInt(CobbleWonderTrade.config.getLegendaryrate());

    if (shinyR == 0) { // shinyR será 0 con una probabilidad de 1/getShinyrate()
      pokemon.setSpecies(pokemons.get(RANDOM.nextInt(pokemons.size())));
      pokemon.setShiny(true);
    } else {
      pokemon.setSpecies(pokemons.get(RANDOM.nextInt(pokemons.size())));
      pokemon.setShiny(false);
    }

    if (legendaryR == 0) { // legendaryR será 0 con una probabilidad de 1/getLegendaryrate()
      pokemon.setSpecies(legendarys.get(RANDOM.nextInt(legendarys.size())));
      if (shinyR == 0) {
        pokemon.setShiny(true);
      }
    }

    return pokemon;
  }


  public static String getUserCooldown(UUID userId) {
    Date userDate = CobbleWonderTrade.manager.getUserInfo().get(userId);
    if (userDate == null) {
      return "No cooldown";
    }

    long diffInMillies = Math.abs(userDate.getTime() - new Date().getTime());
    long diffInSeconds = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    long hours = diffInSeconds / 3600;
    long minutes = (diffInSeconds % 3600) / 60;
    long seconds = diffInSeconds % 60;

    if (hours > 0) {
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    } else {
      return String.format("%02d:%02d", minutes, seconds);
    }
  }

  public static List<String> formatPokemonLore(Pokemon pokemon) {
    List<String> lore = new ArrayList<>();

    return lore;
  }

}
