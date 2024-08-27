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
    resetPool(false);
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
          throw new RuntimeException("Error al obtener un Pokémon aleatorio.", e);
        }
      } else {
        List<JsonObject> list = new ArrayList<>(jsonObjects);

        if (!list.isEmpty()) {
          // Seleccionar un Pokémon aleatorio de la lista
          JsonObject selectedPokemonJson = list.get(Utils.RANDOM.nextInt(list.size()));

          // Registro del Pokémon seleccionado
          if (CobbleWonderTrade.config.isDebug()) {
            CobbleWonderTrade.LOGGER.info("Pokémon seleccionado de la lista: " + selectedPokemonJson);
          }

          // Eliminar el Pokémon seleccionado de la lista
          list.remove(selectedPokemonJson);

          // Agregar el Pokémon del jugador a la lista
          list.add(pokemonPlayer.saveToJSON(new JsonObject()));

          // Actualizar la lista de Pokémon
          CobbleWonderTrade.manager.setPokemonList(list);
          CobbleWonderTrade.manager.writeInfo();

          // Registro del Pokémon del jugador añadido a la lista
          if (CobbleWonderTrade.config.isDebug()) {
            CobbleWonderTrade.LOGGER.info("Pokémon del jugador añadido a la lista: " + pokemonPlayer.saveToJSON(new JsonObject()));
          }

          // Cargar y devolver el Pokémon seleccionado
          return Pokemon.Companion.loadFromJSON(selectedPokemonJson);
        } else {
          // La lista está vacía, reiniciar el pool
          resetPool(true);

          // Volver a obtener la lista actualizada
          List<JsonObject> updatedList = CobbleWonderTrade.manager.getPokemonList();

          if (!updatedList.isEmpty()) {
            // Seleccionar un Pokémon aleatorio de la lista actualizada
            JsonObject selectedPokemonJsonAfterReset = updatedList.get(Utils.RANDOM.nextInt(updatedList.size()));

            // Registro del Pokémon seleccionado después del reinicio
            if (CobbleWonderTrade.config.isDebug()) {
              CobbleWonderTrade.LOGGER.info("Pokémon seleccionado después del reinicio: " + selectedPokemonJsonAfterReset);
            }

            // Agregar el Pokémon del jugador a la lista
            updatedList.add(pokemonPlayer.saveToJSON(new JsonObject()));

            // Actualizar la lista de Pokémon
            CobbleWonderTrade.manager.setPokemonList(updatedList);
            CobbleWonderTrade.manager.writeInfo();

            // Registro del Pokémon del jugador añadido a la lista
            if (CobbleWonderTrade.config.isDebug()) {
              CobbleWonderTrade.LOGGER.info("Pokémon del jugador añadido a la lista después del reinicio: " + pokemonPlayer.saveToJSON(new JsonObject()));
            }

            // Cargar y devolver el Pokémon seleccionado después del reinicio
            return Pokemon.Companion.loadFromJSON(selectedPokemonJsonAfterReset);
          } else {
            // Si después del reinicio la lista sigue vacía, manejar el caso de error
            throw new RuntimeException("La lista de Pokémon sigue vacía después del reinicio.");
          }
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
  public void resetPool(boolean force) {
    if (CobbleWonderTrade.manager.getPokemonList() == null) return;
    List<JsonObject> pokemonList = CobbleWonderTrade.manager.getPokemonList();


    if (CobbleWonderTrade.config.isSavepool()) {
      if (!pokemonList.isEmpty() && pokemonList.size() == CobbleWonderTrade.config.getSizePool()) {
        return;
      } else {
        CobbleWonderTrade.manager.setPokemonList(new ArrayList<>());
      }
    }

    // Variable para determinar si se debe forzar el reinicio
    boolean shouldForceReset = force;

    // Si la lista está vacía o cumple alguna condición específica, establecer shouldForceReset en true
    if (CobbleWonderTrade.config.isDebug()) {
      CobbleWonderTrade.LOGGER.info(pokemonList.size() + " - " + CobbleWonderTrade.config.getSizePool());
    }
    
    if (pokemonList.isEmpty() || pokemonList.size() != CobbleWonderTrade.config.getSizePool()) {
      shouldForceReset = true;
    }

    int requiredSize = CobbleWonderTrade.config.getSizePool();

    if (CobbleWonderTrade.config.isDebug()) {
      CobbleWonderTrade.LOGGER.info("Forzar reinicio: " + shouldForceReset);
    }

    if (!shouldForceReset) {
      return;
    }

    List<JsonObject> updatedList = new ArrayList<>(pokemonList);

    // Añadir Pokémon si la lista tiene menos elementos de los requeridos
    while (updatedList.size() < requiredSize) {
      try {
        updatedList.add(WonderTradeUtil.getRandomPokemon().saveToJSON(new JsonObject()));
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    // Quitar Pokémon si la lista tiene más elementos de los requeridos
    while (updatedList.size() > requiredSize) {
      updatedList.remove(updatedList.size() - 1);  // Quitar el último Pokémon de la lista
    }

    // Actualizar la lista de Pokémon en el manager
    CobbleWonderTrade.manager.setPokemonList(updatedList);

    // Guardar la información
    CobbleWonderTrade.manager.writeInfo();
  }


  @Override
  public void disconnect() {
    CobbleWonderTrade.manager.writeInfo();
  }

  @Override
  public void save() {
    CobbleWonderTrade.manager.writeInfo();
  }
}
