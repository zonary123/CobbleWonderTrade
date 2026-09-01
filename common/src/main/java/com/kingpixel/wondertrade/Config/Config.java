package com.kingpixel.wondertrade.Config;

import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.google.gson.Gson;
import com.kingpixel.cobbleutils.Model.Animations.core.Animations;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.Model.FilterPokemons;
import com.kingpixel.cobbleutils.Model.PokemonBlackList;
import com.kingpixel.cobbleutils.Model.WebHookData;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import lombok.Getter;

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
  private Animations animation;
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
  private int mythicalrate;
  private int mythicalperfectivs;
  private int legendaryrate;
  private int legendaryperfectivs;
  private int ultrabeastrate;
  private int ultrabeastperfectivs;
  private int paradoxrate;
  private int paradoxperfectivs;
  private boolean israndom;
  private int cooldown;
  private Map<String, Integer> cooldownPermission;
  private List<String> poketradeblacklist;
  private List<String> legends;
  private PokemonBlackList specialPokemons;
  private PokemonBlackList blackList;
  private FilterPokemons filterGenerationPokemon;

  public Config() {
    debug = false;
    lang = "en";
    animation = Animations.CSGO;
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
    mythicalrate = 16512;
    mythicalperfectivs = 3;
    legendaryrate = 16512;
    legendaryperfectivs = 3;
    ultrabeastrate = 512;
    ultrabeastperfectivs = 2;
    paradoxrate = 256;
    paradoxperfectivs = 2;
    poolview = true;
    israndom = false;
    cooldownPermission = Map.of(
      "wondertrade.vip", 15,
      "wondertrade.master", 10,
      "wondertrade.legendary", 5
    );
    commands = List.of("wt", "wondertrade");
    filterGenerationPokemon = new FilterPokemons();
    blackList = new PokemonBlackList();
    specialPokemons = new PokemonBlackList();
    specialPokemons.getPokemons().clear();
    specialPokemons.getEggGroups().clear();
    specialPokemons.getTypes().clear();
    specialPokemons.getLabels().clear();
    specialPokemons.getForms().clear();
    specialPokemons.getLabels().addAll(
      List.of(
        CobblemonPokemonLabels.LEGENDARY,
        CobblemonPokemonLabels.MYTHICAL,
        CobblemonPokemonLabels.PARADOX,
        CobblemonPokemonLabels.ULTRA_BEAST
      )
    );
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
    if (sizePool < DatabaseClientFactory.MIN_POOL_SIZE) sizePool = DatabaseClientFactory.MIN_POOL_SIZE;
    if (poketradeblacklist != null) {
      for (String s : poketradeblacklist) {
        blackList.getPokemons().add(s);
      }
      poketradeblacklist = null;
    }
    if (legends != null) {
      for (String s : legends) {
        blackList.getPokemons().add(s);
      }
      legends = null;
    }
  }
}
