package com.kingpixel.wondertrade.utils;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ExecutionException;
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

    List<Species> filteredSpecies = species.stream()
      .filter(species1 -> species1.getNationalPokedexNumber() != 9999)
      .filter(species1 -> !pokeBlacklist.contains(species1.showdownId()))
      .sorted(Comparator.comparingInt(Species::getNationalPokedexNumber))
      .toList();

    Map<Boolean, List<Species>> sortedSpecies = filteredSpecies.stream()
      .collect(Collectors.partitioningBy(species1 -> {
        Pokemon p = new Pokemon();
        p.setSpecies(species1);
        boolean isLegendary = p.isLegendary() || CobbleWonderTrade.config.getLegends().contains(species1.showdownId());
        return isLegendary;
      }));

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

  public static Component toNative(String message) {
    return AdventureTranslator.toNativeWithOutPrefix(message
      .replace("%prefix%", CobbleWonderTrade.language.getPrefix()));
  }

  public static void broadcast(String message) {
    if (message.isEmpty()) return;
    CobbleWonderTrade.server.getPlayerList().getPlayers().forEach(player -> {
      player.sendSystemMessage(toNative(message));
    });
  }


  public static Pokemon getRandomPokemon() throws ExecutionException, InterruptedException {
    Pokemon pokemon = new Pokemon();
    pokemon.createPokemonProperties(PokemonPropertyExtractor.ALL);

    int shinyR = RANDOM.nextInt(CobbleWonderTrade.config.getShinyrate());
    int legendaryR = RANDOM.nextInt(CobbleWonderTrade.config.getLegendaryrate());

    Set<String> generatedPokemonNames = new HashSet<>();
    List<Pokemon> arraypokemons = new ArrayList<>();

    DatabaseClientFactory.databaseClient.getPokemonList(false).get().forEach(pokemon1 -> {
      arraypokemons.add(Pokemon.Companion.loadFromJSON(pokemon1));
    });

    arraypokemons.forEach(pokemon1 -> generatedPokemonNames.add(pokemon1.getSpecies().showdownId()));


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

    generatedPokemonNames.add(pokemon.getSpecies().showdownId());
    pokemon.setLevel(RANDOM.nextInt(CobbleWonderTrade.config.getMaxlv() - CobbleWonderTrade.config.getMinlv()) + CobbleWonderTrade.config.getMinlv());
    return pokemon;
  }

}
