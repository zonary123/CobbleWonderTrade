package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.model.UserInfo;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:26
 */
public class JSONClient implements DatabaseClient {

  public JSONClient(String uri, String user, String password) {

  }

  @Override
  public void connect() {
    CobbleWonderTrade.LOGGER.info("Connecting to JSON");
    if (!CobbleWonderTrade.config.isSavepool()) {
      resetPool();
    }
  }

  @Override
  public CompletableFuture<List<Pokemon>> getSpecialPool(boolean special) {
    return CompletableFuture.supplyAsync(() -> {
      List<Pokemon> pokemons = new ArrayList<>();
      List<JsonObject> jsonObjects = CobbleWonderTrade.manager.getPokemonList();

      for (JsonObject jsonObject : jsonObjects) {
        Pokemon pokemon = Pokemon.Companion.loadFromJSON(jsonObject);
        if (!special) {
          pokemons.add(pokemon);
        } else {
          if (pokemon.getShiny() || pokemon.isLegendary() || pokemon.isUltraBeast() || pokemon.getForm().getLabels().contains("paradox") || PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31) {
            pokemons.add(pokemon);
          }
        }
      }
      return pokemons;
    });
  }

  @Override
  public CompletableFuture<List<JsonObject>> getPokemonList(boolean special) {
    return CompletableFuture.supplyAsync(() -> CobbleWonderTrade.manager.getPokemonList());
  }

  @Override
  public Pokemon getRandomPokemon() {
    try {
      return WonderTradeUtil.getRandomPokemon();
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CompletableFuture<Pokemon> putPokemon(Pokemon pokemonPlayer) {
    return CompletableFuture.supplyAsync(() -> {
      List<JsonObject> jsonObjects = CobbleWonderTrade.manager.getPokemonList();

      if (CobbleWonderTrade.config.isIsrandom()) {
        CobbleWonderTrade.manager.writeInfo();
        try {
          return WonderTradeUtil.getRandomPokemon();
        } catch (ExecutionException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      } else {
        List<JsonObject> list = new ArrayList<>(jsonObjects);

        if (!list.isEmpty()) {
          // Seleccionar un Pokémon aleatorio de la lista
          JsonObject selectedPokemonJson = list.get(Utils.RANDOM.nextInt(list.size()));

          // Registro del Pokémon seleccionado
          if (CobbleWonderTrade.config.isDebug())
            CobbleWonderTrade.LOGGER.info("Pokémon seleccionado de la lista: " + selectedPokemonJson);


          // Eliminar el Pokémon seleccionado de la lista
          list.remove(selectedPokemonJson);

          // Agregar el Pokémon del jugador a la lista
          list.add(pokemonPlayer.saveToJSON(new JsonObject()));

          // Actualizar la lista de Pokémon
          CobbleWonderTrade.manager.setPokemonList(list);
          CobbleWonderTrade.manager.writeInfo();

          // Registro del Pokémon del jugador añadido a la lista
          if (CobbleWonderTrade.config.isDebug())
            CobbleWonderTrade.LOGGER.info("Pokémon del jugador añadido a la lista: " + pokemonPlayer.saveToJSON(new JsonObject()));

          // Cargar y devolver el Pokémon seleccionado
          return Pokemon.Companion.loadFromJSON(selectedPokemonJson);
        } else {
          CobbleWonderTrade.manager.writeInfo();
          return getRandomPokemon();
        }
      }
    });
  }


  @Override
  public CompletableFuture<UserInfo> getUserInfo(ServerPlayer player) {
    return CompletableFuture.supplyAsync(() -> CobbleWonderTrade.manager.getUserInfo().get(player.getUUID()));
  }

  @Override
  public CompletableFuture<UserInfo> getUserinfo(UUID uuid) {
    return CompletableFuture.supplyAsync(() -> {
      ServerPlayer player = CobbleWonderTrade.server.getPlayerList().getPlayer(uuid);
      return player != null ? getUserInfo(player).join() : null;
    });
  }

  @Override
  public CompletableFuture<UserInfo> putUserInfo(UserInfo userInfo) {
    return CompletableFuture.supplyAsync(() -> {
      userInfo.setMessagesend(false);
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.MINUTE, CobbleWonderTrade.config.getCooldown());
      Date futureDate = calendar.getTime();
      userInfo.setDate(futureDate.getTime());
      CobbleWonderTrade.manager.writeInfo();
      return userInfo;
    });
  }

  @Override public CompletableFuture<UserInfo> putUserInfo(UserInfo userInfo, boolean update) {
    return CompletableFuture.supplyAsync(() -> {
      CobbleWonderTrade.manager.writeInfo();
      return userInfo;
    });
  }

  @Override
  public void resetPool() {
    List<JsonObject> pokemonList = CobbleWonderTrade.manager.getPokemonList();
    pokemonList.clear();

    List<JsonObject> updatedList = new ArrayList<>();
    while (updatedList.size() < CobbleWonderTrade.config.getSizePool()) {
      try {
        updatedList.add(WonderTradeUtil.getRandomPokemon().saveToJSON(new JsonObject()));
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    CobbleWonderTrade.manager.setPokemonList(updatedList);

    CobbleWonderTrade.manager.writeInfo();
  }

  @Override
  public void disconnect() {

  }

  @Override
  public void save() {

  }
}
