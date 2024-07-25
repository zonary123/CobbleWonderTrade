package com.kingpixel.wondertrade.Manager;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DataBaseType;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.model.UserInfo;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 29/04/2024 1:31
 */
@Getter
@Setter
@ToString
public class WonderTradeManager {
  private HashMap<UUID, UserInfo> userInfo;
  private List<JsonObject> pokemonList;

  public CharSequence getCooldown(UUID uuid) throws ExecutionException, InterruptedException {
    UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserinfo(uuid).get();

    Date now = new Date();

    long diff = userInfo.getDate().getTime() - now.getTime();
    long diffSeconds = diff / 1000 % 60;
    long diffMinutes = diff / (60 * 1000) % 60;
    long diffHours = diff / (60 * 60 * 1000) % 24;
    long diffDays = diff / (24 * 60 * 60 * 1000);

    return String.format("%d days, %d hours, %d minutes, %d seconds", diffDays, diffHours, diffMinutes, diffSeconds);
  }

  public WonderTradeManager() {
    userInfo = new HashMap<>();
    pokemonList = new ArrayList<>();
  }


  public void addPlayer(ServerPlayer player) {
    userInfo.putIfAbsent(player.getUUID(), new UserInfo(player.getUUID()));
  }

  public void init() {
    DatabaseClientFactory.createDatabaseClient(CobbleWonderTrade.config.getDatabaseType(),
      CobbleWonderTrade.config.getDatabaseConfig().getUrl(),
      CobbleWonderTrade.config.getDatabaseConfig().getDatabase(),
      CobbleWonderTrade.config.getDatabaseConfig().getUser(),
      CobbleWonderTrade.config.getDatabaseConfig().getPassword());

    if (DatabaseClientFactory.type == DataBaseType.JSON) {
      CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json",
        el -> {
          Gson gson = Utils.newWithoutSpacingGson();
          WonderTradeManager manager = gson.fromJson(el, WonderTradeManager.class);
          this.pokemonList = manager.getPokemonList();
        });

      futureRead.thenRun(() -> {
        if (pokemonList.isEmpty()) {
          try {
            generatePokemonList();
          } catch (ExecutionException e) {
            throw new RuntimeException(e);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          Gson gson = Utils.newWithoutSpacingGson();
          String data = gson.toJson(this);
          CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json", data);
          if (!futureWrite.join()) {
            CobbleWonderTrade.LOGGER.fatal("Could not write pool.json file for CobbleWonderTrade.");
          }
        }

        for (ServerPlayer player : CobbleWonderTrade.server.getPlayerList().getPlayers()) {
          addPlayer(player);
        }
      }).exceptionally(ex -> {
        CobbleWonderTrade.LOGGER.info("No pool.json file found for " + CobbleWonderTrade.MOD_NAME + ". Attempting to generate one.");
        try {
          generatePokemonList();
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        Gson gson = Utils.newWithoutSpacingGson();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json", data);
        if (!futureWrite.join()) {
          CobbleWonderTrade.LOGGER.fatal("Could not write pool.json file for CobbleWonderTrade.");
        }
        return null;
      });
    }
    DatabaseClientFactory.databaseClient.resetPool();
  }


  public void writeInfo() {
    if (CobbleWonderTrade.config.isSavepool()) {
      Gson gson = Utils.newWithoutSpacingGson();
      String data = gson.toJson(CobbleWonderTrade.manager);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json",
        data);

      if (!futureWrite.join()) {
        CobbleWonderTrade.LOGGER.fatal("Could not write config.json file for CobbleHunt.");
      }
    }
  }


  public void generatePokemonList() throws ExecutionException, InterruptedException {
    DatabaseClientFactory.databaseClient.getPokemonList(false).get().clear();
    for (int i = 0; i < CobbleWonderTrade.config.getSizePool(); i++) {
      DatabaseClientFactory.databaseClient.getPokemonList(false).get().add(WonderTradeUtil.getRandomPokemon().saveToJSON(new JsonObject()));
    }
    if (CobbleWonderTrade.config.getDatabaseType() == DataBaseType.JSON) {
      writeInfo();
    } else {
      DatabaseClientFactory.databaseClient.save();
    }
  }

  public void addPlayerWithDate(Entity player, int minutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, minutes);
    Date futureDate = calendar.getTime();
    userInfo.put(player.getUUID(), new UserInfo(player.getUUID(), futureDate));
  }

  public Pokemon putPokemon(Pokemon pokemon) {
    int randomIndex = new Random().nextInt(pokemonList.size());
    Pokemon oldPokemon = Pokemon.Companion.loadFromJSON(pokemonList.get(randomIndex));
    pokemonList.set(randomIndex, pokemon.saveToJSON(new JsonObject()));
    return oldPokemon;
  }


  public boolean hasCooldownEnded(ServerPlayer player) throws ExecutionException, InterruptedException {
    UserInfo userDate = DatabaseClientFactory.databaseClient.getUserinfo(player.getUUID()).get();
    if (userDate == null) {
      CobbleWonderTrade.LOGGER.error("User " + player.getUUID() + " not found in the database.");
      return true; // No cooldown was set for this player
    }

    Date now = new Date();
    return now.after(userDate.getDate()); // Returns true if the current date is after the user's date
  }

  public Pokemon getRandomPokemon() throws ExecutionException, InterruptedException {
    int randomIndex = new Random().nextInt(pokemonList.size());
    Pokemon randomPokemon = Pokemon.Companion.loadFromJSON(pokemonList.get(randomIndex));
    pokemonList.set(randomIndex, WonderTradeUtil.getRandomPokemon().saveToJSON(new JsonObject()));
    return randomPokemon;
  }


}