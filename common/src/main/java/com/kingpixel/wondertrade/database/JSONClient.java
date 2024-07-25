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
import java.util.stream.Collectors;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:26
 */
public class JSONClient implements DatabaseClient {

  public JSONClient(String uri, String user, String password) {
    // Constructor vacío para este caso
  }

  @Override
  public void connect() {
    CobbleWonderTrade.LOGGER.info("Connecting to JSON");
    try {
      if (getSpecialPool(false).get().isEmpty() || getSpecialPool(true).get().isEmpty()) {
        resetPool();
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CompletableFuture<List<Pokemon>> getSpecialPool(boolean special) {
    return CompletableFuture.supplyAsync(() -> {
      List<Pokemon> pokemons = new ArrayList<>();
      List<JsonObject> jsonObjects = CobbleWonderTrade.manager.getPokemonList(); // Operación sincrónica

      for (JsonObject jsonObject : jsonObjects) {
        Pokemon pokemon = Pokemon.Companion.loadFromJSON(jsonObject);
        if (!special) {
          pokemons.add(pokemon);
        } else {
          if (pokemon.getShiny() || pokemon.isLegendary() || pokemon.isUltraBeast() || PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31) {
            pokemons.add(pokemon);
          }
        }
      }
      return pokemons;
    });
  }

  @Override
  public CompletableFuture<List<JsonObject>> getPokemonList(boolean special) {
    return CompletableFuture.supplyAsync(() -> CobbleWonderTrade.manager.getPokemonList()); // Operación sincrónica
  }

  @Override
  public Pokemon getRandomPokemon() {
    try {
      return WonderTradeUtil.getRandomPokemon();
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CompletableFuture<Pokemon> putPokemon(Pokemon pokemonPlayer) {
    return CompletableFuture.supplyAsync(() -> {
      List<JsonObject> jsonObjects = CobbleWonderTrade.manager.getPokemonList(); // Operación sincrónica
      if (CobbleWonderTrade.config.isIsrandom()) {
        // Devuelve un Pokémon aleatorio
        CobbleWonderTrade.manager.writeInfo();
        try {
          return WonderTradeUtil.getRandomPokemon(); // Espera el resultado de getRandomPokemon()
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

      } else {
        // Añadir el Pokémon recibido a la lista
        List<JsonObject> list = new ArrayList<>(jsonObjects);
        list.add(pokemonPlayer.saveToJSON(new JsonObject()));

        // Seleccionar un Pokémon de la lista
        if (!list.isEmpty()) {
          JsonObject selectedPokemonJson = list.get(Utils.RANDOM.nextInt(list.size()));
          // Eliminar el Pokémon de la lista
          list.remove(selectedPokemonJson);
          // Guardar la lista actualizada
          CobbleWonderTrade.manager.setPokemonList(list);
          CobbleWonderTrade.manager.writeInfo();
          return Pokemon.Companion.loadFromJSON(selectedPokemonJson);
        } else {
          CobbleWonderTrade.manager.writeInfo();
          return getRandomPokemon(); // Espera el resultado de getRandomPokemon()
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
      return player != null ? getUserInfo(player).join() : null; // Espera el resultado de getUserInfo()
    });
  }

  @Override
  public CompletableFuture<UserInfo> putUserInfo(UserInfo userInfo) {
    return CompletableFuture.supplyAsync(() -> {
      userInfo.setMessagesend(false);
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.MINUTE, CobbleWonderTrade.config.getCooldown());
      Date futureDate = calendar.getTime();
      userInfo.setDate(futureDate);
      CobbleWonderTrade.manager.writeInfo();
      return userInfo;
    });
  }

  @Override public CompletableFuture<UserInfo> putUserInfo(UserInfo userInfo, boolean update) {
    return CompletableFuture.supplyAsync(() -> {
      userInfo.setMessagesend(false);
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.MINUTE, CobbleWonderTrade.config.getCooldown());
      Date futureDate = calendar.getTime();
      userInfo.setDate(futureDate);
      CobbleWonderTrade.manager.writeInfo();
      return userInfo;
    });
  }

  @Override
  public void resetPool() {
    List<JsonObject> pokemonList = CobbleWonderTrade.manager.getPokemonList(); // Operación sincrónica

    if (pokemonList.size() == CobbleWonderTrade.config.getSizePool()) return;
    if (pokemonList.size() > CobbleWonderTrade.config.getSizePool()) {
      // Mantener solo la cantidad adecuada de Pokémon
      List<JsonObject> sublist = pokemonList.stream()
        .limit(CobbleWonderTrade.config.getSizePool())
        .collect(Collectors.toList());
      CobbleWonderTrade.manager.setPokemonList(sublist);
    } else {
      // Añadir Pokémon aleatorios hasta alcanzar el tamaño del pool
      List<JsonObject> updatedList = new ArrayList<>(pokemonList);
      while (updatedList.size() < CobbleWonderTrade.config.getSizePool()) {
        try {
          updatedList.add(WonderTradeUtil.getRandomPokemon().saveToJSON(new JsonObject()));
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      CobbleWonderTrade.manager.setPokemonList(updatedList);
    }
    CobbleWonderTrade.manager.writeInfo(); // Operación sincrónica
  }

  @Override
  public void disconnect() {
    // No se requiere implementación para JSONClient
  }

  @Override
  public void save() {
    // No se requiere implementación para JSONClient
  }
}
