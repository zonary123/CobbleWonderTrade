package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.Model.FilterPokemons;
import com.kingpixel.cobbleutils.Model.PokemonBlackList;
import com.kingpixel.cobbleutils.Model.WebHookData;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Getter
public class Config {
  private boolean debug;
  private String lang;
  private boolean autoReset;
  private int cooldownReset;
  private List<String> commands;
  private WebHookData discord_webhook;
  private DataBaseConfig databaseConfig;
  private int cooldownmessage;
  private int cooldownBroadcast;
  private int sizePool;
  private int minlvreq;
  private int minlv;
  private int maxlv;
  private boolean poolview;
  private int shinyrate;
  private int legendaryrate;
  private boolean israndom;
  private int cooldown;
  private Map<String, Integer> cooldownPermission;
  private List<String> poketradeblacklist;
  private List<String> legends;
  private PokemonBlackList blackList;
  private FilterPokemons filterGenerationPokemon;

  public Config() {
    debug = false;
    lang = "en";
    autoReset = false;
    cooldownReset = 30;
    discord_webhook = new WebHookData("", "", "");
    databaseConfig = new DataBaseConfig("wondertrade");
    cooldown = 30;
    cooldownmessage = 15;
    cooldownBroadcast = 30;
    sizePool = 72;
    minlvreq = 5;
    minlv = 5;
    maxlv = 36;
    shinyrate = 8192;
    legendaryrate = 16512;
    poolview = true;
    israndom = false;
    cooldownPermission = Map.of(
      "wondertrade.bypasscooldown", 0,
      "wondertrade.vip", 15,
      "wondertrade.vip+", 10,
      "wondertrade.vip++", 5
    );
    poketradeblacklist = List.of("Magikarp", "egg", "pokestop");
    legends = List.of("Magikarp");
    commands = List.of("wt", "wondertrade");
    filterGenerationPokemon = new FilterPokemons();
    blackList = new PokemonBlackList();
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.PATH, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        CobbleWonderTrade.config = gson.fromJson(el, Config.class);
        CobbleWonderTrade.config.fix();
        String data = gson.toJson(CobbleWonderTrade.config);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH, "config.json",
          data);
        if (!futureWrite.join()) {

        }
      });

    if (!futureRead.join()) {
      Gson gson = Utils.newGson();
      CobbleWonderTrade.config = this;
      String data = gson.toJson(CobbleWonderTrade.config);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH, "config.json",
        data);

      if (!futureWrite.join()) {

      }
    }
  }

  private void fix() {
    if (!poketradeblacklist.isEmpty()) {
      List<String> remove = new ArrayList<>();
      for (String s : poketradeblacklist) {
        blackList.getPokemons().add(s);
        remove.add(s);
      }
      poketradeblacklist.removeAll(remove);
    }
    if (!legends.isEmpty()) {
      List<String> remove = new ArrayList<>();
      for (String s : legends) {
        blackList.getPokemons().add(s);
        remove.add(s);
      }
      legends.removeAll(remove);
    }
  }
}
