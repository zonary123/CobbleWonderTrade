package com.kingpixel.wondertrade.Manager;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.*;

/**
 * @author Carlos Varas Alonso - 29/04/2024 1:31
 */
public class WonderTradeManager {
  private HashMap<UUID, UserInfo> userInfo;
  private List<Pokemon> pokemonList;

  public class UserInfo {
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

  public HashMap<UUID, UserInfo> getUserInfo() {
    return userInfo;
  }

  public List<Pokemon> getPokemonList() {
    return pokemonList;
  }

  public WonderTradeManager() {
    userInfo = new HashMap<>();
    pokemonList = new ArrayList<>();
  }


  public void addPlayer(Entity player) {
    userInfo.put(player.getUUID(), new UserInfo(new Date()));
  }

  public void init() {
    for (ServerPlayer player : CobbleWonderTrade.server.getPlayerList().getPlayers()) {
      addPlayer(player);
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

  @Override public String toString() {
    return "WonderTradeManager{" +
      "userInfo=" + userInfo +
      '}';
  }
}