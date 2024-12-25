package com.kingpixel.wondertrade.utils;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 26/05/2024 4:23
 */
public class WonderTradeUtil {

  public static void emiteventcaptured(Pokemon pokemon, ServerPlayerEntity player) {
    PokemonCapturedEvent event = new PokemonCapturedEvent(pokemon,
      Objects.requireNonNull(CobbleWonderTrade.server.getPlayerManager().getPlayer(player.getUuid())),
      new EmptyPokeBallEntity(CobbleWonderTrade.server.getOverworld())
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

  public static Text toNative(String message) {
    return AdventureTranslator.toNativeWithOutPrefix(message
      .replace("%prefix%", CobbleWonderTrade.language.getPrefix()));
  }

  public static void broadcast(String message) {
    if (message.isEmpty()) return;
    CobbleWonderTrade.server.getPlayerManager().getPlayerList().forEach(player -> {
      player.sendMessage(toNative(message));
    });
  }


  public static Pokemon getRandomPokemon() throws ExecutionException, InterruptedException {
    Pokemon pokemon = new Pokemon();
    pokemon.createPokemonProperties(PokemonPropertyExtractor.ALL);


    Set<String> generatedPokemonNames = new HashSet<>();
    List<Pokemon> arraypokemons = new ArrayList<>();

    DatabaseClientFactory.databaseClient.getPokemonList(false).forEach(pokemon1 -> {
      arraypokemons.add(Pokemon.Companion.loadFromJSON(DynamicRegistryManager.EMPTY,pokemon1));
    });

    arraypokemons.forEach(pokemon1 -> generatedPokemonNames.add(pokemon1.getSpecies().showdownId()));


    generatedPokemonNames.add(pokemon.getSpecies().showdownId());
    pokemon.setLevel(RANDOM.nextInt(CobbleWonderTrade.config.getMaxlv() - CobbleWonderTrade.config.getMinlv()) + CobbleWonderTrade.config.getMinlv());
    return pokemon;
  }

  public static boolean isSpecial(Pokemon pokemon) {
    return pokemon.getShiny() || pokemon.isLegendary() || pokemon.isUltraBeast() || pokemon.getForm().getLabels().contains("paradox") || PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31;
  }
}
