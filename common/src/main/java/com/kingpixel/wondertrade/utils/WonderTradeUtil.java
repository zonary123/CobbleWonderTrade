package com.kingpixel.wondertrade.utils;

import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
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

    CobbleWonderTrade.server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(TextUtil.parseHexCodes(CobbleWonderTrade.language.getMessagepoolwondertrade()
      .replace("%shinys%", String.valueOf(shinys))
      .replace("%legends%", String.valueOf(legendaries))
      .replace("%total%", String.valueOf(total))
      .replace("%prefix%", CobbleWonderTrade.language.getPrefix()))));

  }

  private static final Random RANDOM = new Random();

  public static Pokemon getRandomPokemon() {
    Pokemon pokemon = new Pokemon();
    pokemon.createPokemonProperties(PokemonPropertyExtractor.ALL);
    List<Pokemon> listpokemons = CobbleWonderTrade.manager.getPokemonList();

    int shinyR = RANDOM.nextInt(CobbleWonderTrade.config.getShinyrate());
    int legendaryR = RANDOM.nextInt(CobbleWonderTrade.config.getLegendaryrate());

    Set<String> generatedPokemonNames = new HashSet<>();
    listpokemons.forEach(pokemon1 -> generatedPokemonNames.add(pokemon1.getSpecies().getName()));

    do {
      if (shinyR == 0) {
        pokemon.setSpecies(pokemons.get(RANDOM.nextInt(pokemons.size())));
        pokemon.setShiny(true);
      } else {
        pokemon.setSpecies(pokemons.get(RANDOM.nextInt(pokemons.size())));
        pokemon.setShiny(false);
      }

      if (legendaryR == 0) {
        pokemon.setSpecies(legendarys.get(RANDOM.nextInt(legendarys.size())));
        if (shinyR == 0) {
          pokemon.setShiny(true);
        }
      }
    } while (generatedPokemonNames.contains(pokemon.getSpecies().getName()));

    generatedPokemonNames.add(pokemon.getSpecies().getName());
    pokemon.setLevel(RANDOM.nextInt(CobbleWonderTrade.config.getMaxlv() - CobbleWonderTrade.config.getMinlv()) + CobbleWonderTrade.config.getMinlv());
    return pokemon;
  }


  public static String getUserCooldown(UUID userId) {
    Date userDate = CobbleWonderTrade.manager.getUserInfo().get(userId).getDate();
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
    String moveOne = pokemon.getMoveSet().getMoves().size() >= 1 ? pokemon.getMoveSet().get(0).getDisplayName().getString() : "Empty";
    String moveTwo = pokemon.getMoveSet().getMoves().size() >= 2 ? pokemon.getMoveSet().get(1).getDisplayName().getString() : "Empty";
    String moveThree = pokemon.getMoveSet().getMoves().size() >= 3 ? pokemon.getMoveSet().get(2).getDisplayName().getString() : "Empty";
    String moveFour = pokemon.getMoveSet().getMoves().size() >= 4 ? pokemon.getMoveSet().get(3).getDisplayName().getString() : "Empty";
    List<String> l = new ArrayList<>(CobbleWonderTrade.language.getLorepokemon());
    List<String> lore = new ArrayList<>();

    if (pokemon.getLevel() < CobbleWonderTrade.config.getMinlvreq()) {
      lore.add(CobbleWonderTrade.language.getDonthavelevel());
    }
    lore.addAll(l);
    lore.replaceAll(s -> s.replace("%level%", String.valueOf(pokemon.getLevel()))
      .replace("%shiny%", pokemon.getShiny() ? "Si" : "No")
      .replace("%legends%", pokemon.isLegendary() ? "Si" : "No")
      .replace("%nature%",
        LocalizationUtilsKt.lang(pokemon.getNature().getDisplayName().replace("cobblemon.", "")).getString())
      .replace("%ability%",
        LocalizationUtilsKt.lang(pokemon.getAbility().getDisplayName().replace("cobblemon.", "")).getString())
      .replace("%hp%", String.valueOf(pokemon.getIvs().get(Stats.HP)))
      .replace("%atk%", String.valueOf(pokemon.getIvs().get(Stats.ATTACK)))
      .replace("%def%", String.valueOf(pokemon.getIvs().get(Stats.DEFENCE)))
      .replace("%spa%", String.valueOf(pokemon.getIvs().get(Stats.SPECIAL_ATTACK)))
      .replace("%spd%", String.valueOf(pokemon.getIvs().get(Stats.SPECIAL_DEFENCE)))
      .replace("%spe%", String.valueOf(pokemon.getIvs().get(Stats.SPEED)))
      .replace("%evhp%", String.valueOf(pokemon.getEvs().get(Stats.HP)))
      .replace("%evatk%", String.valueOf(pokemon.getEvs().get(Stats.ATTACK)))
      .replace("%evdef%", String.valueOf(pokemon.getEvs().get(Stats.DEFENCE)))
      .replace("%evspa%", String.valueOf(pokemon.getEvs().get(Stats.SPECIAL_ATTACK)))
      .replace("%evspd%", String.valueOf(pokemon.getEvs().get(Stats.SPECIAL_DEFENCE)))
      .replace("%evspe%", String.valueOf(pokemon.getEvs().get(Stats.SPEED)))
      .replace("%move1%", moveOne)
      .replace("%move2%", moveTwo)
      .replace("%move3%", moveThree)
      .replace("%move4%", moveFour)
      .replace("%form%", pokemon.getForm().getName())
      .replace("%minlevel%", String.valueOf(CobbleWonderTrade.config.getMinlvreq())));
    return lore;
  }

}
