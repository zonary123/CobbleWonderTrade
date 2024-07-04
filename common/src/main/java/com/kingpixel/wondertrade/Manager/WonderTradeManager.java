package com.kingpixel.wondertrade.Manager;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.utils.Utils;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 29/04/2024 1:31
 */
@Getter
@ToString
public class WonderTradeManager {
  private HashMap<UUID, UserInfo> userInfo;
  private List<Pokemon> pokemonList;

  public CharSequence getCooldown(UUID uuid) {
    UserInfo userInfo = this.userInfo.get(uuid);

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


  public void addPlayer(Entity player) {
    userInfo.putIfAbsent(player.getUUID(), new UserInfo(new Date()));
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json",
      el -> {
        Gson gson = Utils.newGson();
        WonderTradeManager manager = gson.fromJson(el, WonderTradeManager.class);
        this.pokemonList = manager.getPokemonList();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json",
          data);
        if (!futureWrite.join()) {
          CobbleWonderTrade.LOGGER.fatal("Could not write lang.json file for CobbleHunt.");
        }
      });

    if (!futureRead.join()) {
      CobbleWonderTrade.LOGGER.info("No config.json file found for" + CobbleWonderTrade.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json",
        data);

      if (!futureWrite.join()) {
        CobbleWonderTrade.LOGGER.fatal("Could not write config.json file for CobbleHunt.");
      }
    }

    if (pokemonList.isEmpty()) generatePokemonList();

    for (ServerPlayer player : CobbleWonderTrade.server.getPlayerList().getPlayers()) {
      addPlayer(player);
    }
  }

  public static void writeInfo() {
    Gson gson = Utils.newGson();
    String data = gson.toJson(CobbleWonderTrade.manager);
    CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json",
      data);

    if (!futureWrite.join()) {
      CobbleWonderTrade.LOGGER.fatal("Could not write config.json file for CobbleHunt.");
    }
  }

  public void generatePokemonList() {
    pokemonList.clear();
    for (int i = 0; i < CobbleWonderTrade.config.getSizePool(); i++) {
      pokemonList.add(WonderTradeUtil.getRandomPokemon());
    }
  }

  public void addPlayerWithDate(Entity player, int minutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, minutes);
    Date futureDate = calendar.getTime();
    userInfo.put(player.getUUID(), new UserInfo(futureDate));
  }

  public Pokemon putPokemon(Pokemon pokemon) {
    int randomIndex = new Random().nextInt(pokemonList.size());
    Pokemon oldPokemon = pokemonList.get(randomIndex);
    pokemonList.set(randomIndex, pokemon);
    return oldPokemon;
  }


  public boolean hasCooldownEnded(Entity player) {
    UserInfo userDate = userInfo.get(player.getUUID());
    if (userDate == null) {
      return true; // No cooldown was set for this player
    }

    Date now = new Date();
    return now.after(userDate.getDate()); // Returns true if the current date is after the user's date
  }

  public Pokemon getRandomPokemon() {
    int randomIndex = new Random().nextInt(pokemonList.size());
    Pokemon randomPokemon = pokemonList.get(randomIndex);
    pokemonList.set(randomIndex, WonderTradeUtil.getRandomPokemon());
    return randomPokemon;
  }

  public static class UserInfo {
    private boolean messagesend;
    private Date date;

    public UserInfo(Date date) {
      this.date = date;
      this.messagesend = false;
    }

    public boolean isMessagesend() {
      return messagesend;
    }

    public Date getDate() {
      return date;
    }

    public void setMessagesend(boolean messagesend) {
      this.messagesend = messagesend;
    }

    public void setDate(Date date) {
      this.date = date;
    }
  }
}