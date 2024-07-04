package com.kingpixel.wondertrade.utils;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Carlos Varas Alonso - 26/05/2024 4:23
 */
public class WonderTradeUtil {
  public static ArrayList<Species> pokemons = new ArrayList<>();
  public static ArrayList<Species> legendarys = new ArrayList<>();

  public static void init() {
    Collection<Species> species = PokemonSpecies.INSTANCE.getSpecies();
    Set<String> pokeBlacklist = new HashSet<>(CobbleWonderTrade.config.getPokeblacklist());

    // Primero filtramos los Pokémon que no deben estar en la lista principal
    List<Species> filteredSpecies = species.stream()
      .filter(species1 -> species1.getNationalPokedexNumber() != 9999)
      .filter(species1 -> !pokeBlacklist.contains(species1.showdownId()))
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList();

    // Luego particionamos en legendarios y no legendarios
    Map<Boolean, List<Species>> sortedSpecies = filteredSpecies.stream()
      .collect(Collectors.partitioningBy(species1 -> {
        Pokemon p = new Pokemon();
        p.setSpecies(species1);
        boolean isLegendary = p.isLegendary() || CobbleWonderTrade.config.getLegends().contains(species1.showdownId());
        return isLegendary;
      }));

    // Filtramos los no legendarios con rareza -1
    pokemons = new ArrayList<>(sortedSpecies.get(false).stream()
      .filter(species1 -> CobbleWonderTrade.spawnRates.getRarity(species1) != -1)
      .toList());

    legendarys = new ArrayList<>(sortedSpecies.get(true));
  }

  public static void emiteventcaptured(Pokemon pokemon, Player player) {
    PokemonCapturedEvent event = new PokemonCapturedEvent(pokemon,
      Objects.requireNonNull(CobbleWonderTrade.server.getPlayerList().getPlayer(player.getUUID())),
      new EmptyPokeBallEntity(CobbleWonderTrade.server.overworld())
    );
    CobblemonEvents.POKEMON_CAPTURED.emit(event);
  }

  public static void messagePool(List<Pokemon> pokemons) {
    int shinys = (int) pokemons.stream().filter(Pokemon::getShiny).count();
    int legendaries =
      (int) pokemons.stream()
        .filter(pokemon -> pokemon.isLegendary() || CobbleWonderTrade.config.getLegends().contains(pokemon.showdownId()))
        .count();
    int total = pokemons.size();

    Utils.broadcastMessage(CobbleWonderTrade.language.getMessagepoolwondertrade()
      .replace("%shinys%", String.valueOf(shinys))
      .replace("%legends%", String.valueOf(legendaries))
      .replace("%total%", String.valueOf(total))
      .replace("%prefix%", CobbleWonderTrade.language.getPrefix()));

  }

  private static final Random RANDOM = new Random();

  public static Pokemon getRandomPokemon() {
    Pokemon pokemon = new Pokemon();
    pokemon.createPokemonProperties(PokemonPropertyExtractor.ALL);

    int shinyR = RANDOM.nextInt(CobbleWonderTrade.config.getShinyrate());
    int legendaryR = RANDOM.nextInt(CobbleWonderTrade.config.getLegendaryrate());

    Set<String> generatedPokemonNames = new HashSet<>();
    List<Pokemon> arraypokemons = new ArrayList<>();
    CobbleWonderTrade.manager.getPokemonList().forEach(pokemon1 -> {
      arraypokemons.add(Pokemon.Companion.loadFromJSON(pokemon1));
    });
    arraypokemons.forEach(pokemon1 -> generatedPokemonNames.add(pokemon1.getSpecies().showdownId()));

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
    } while (generatedPokemonNames.contains(pokemon.getSpecies().showdownId()));

    generatedPokemonNames.add(pokemon.getSpecies().showdownId());
    pokemon.setLevel(RANDOM.nextInt(CobbleWonderTrade.config.getMaxlv() - CobbleWonderTrade.config.getMinlv()) + CobbleWonderTrade.config.getMinlv());
    return pokemon;
  }


  public static String getUserCooldown(UUID userId) {
    Date userDate = CobbleWonderTrade.manager.getUserInfo().get(userId).getDate();
    if (userDate == null) {
      return "No cooldown";
    }

    long diffInMillies = userDate.getTime() - new Date().getTime();
    if (diffInMillies < 0) {
      return "00:00";
    }
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
      .replace("%shiny%", pokemon.getShiny() ? CobbleWonderTrade.language.getYes() : CobbleWonderTrade.language.getNo())
      .replace("%legends%", pokemon.isLegendary() ? CobbleWonderTrade.language.getYes() : CobbleWonderTrade.language.getNo())
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
