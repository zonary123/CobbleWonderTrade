package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.Model.FilterPokemons;
import com.kingpixel.cobbleutils.Model.WebHookData;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DataBaseType;
import com.kingpixel.wondertrade.model.DataBaseData;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;

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
  private List<String> aliases;
  private WebHookData discord_webhook;
  private DataBaseType databaseType;
  private DataBaseData databaseConfig;
  private boolean autoReset;
  private int cooldownReset;
  private int cooldown;
  private int cooldownmessage;
  private int sizePool;
  private int minlvreq;
  private int minlv;
  private int maxlv;
  private boolean emitcapture;
  private boolean allowshiny;
  private boolean allowlegendary;
  private boolean poolview;
  private boolean savepool;
  private int shinyrate;
  private int legendaryrate;
  private int shinys;
  private int legendaries;
  private boolean israndom;
  private Map<String, Integer> cooldownPermission;
  private FilterPokemons filterGenerationPokemon;
  private List<String> poketradeblacklist;
  private List<String> legends;

  public Config() {
    debug = false;
    lang = "en";
    autoReset = false;
    cooldownReset = 30;
    databaseType = DataBaseType.JSON;
    discord_webhook = new WebHookData("", "", "");
    databaseConfig = new DataBaseData();
    cooldown = 30;
    cooldownmessage = 15;
    sizePool = 72;
    minlvreq = 5;
    minlv = 5;
    maxlv = 36;
    shinyrate = 8192;
    legendaryrate = 16512;
    shinys = 0;
    legendaries = 0;
    savepool = true;
    poolview = true;
    emitcapture = false;
    israndom = false;
    allowshiny = true;
    allowlegendary = true;
    cooldownPermission = Map.of(
      "wondertrade.bypasscooldown", 0,
      "wondertrade.vip", 15,
      "wondertrade.vip+", 10,
      "wondertrade.vip++", 5
    );
    filterGenerationPokemon = new FilterPokemons();
    poketradeblacklist = List.of("Magikarp", "egg", "pokestop");
    legends = List.of("Magikarp");
    aliases = List.of("wt", "wondertrade");

  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.PATH, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        Config config = gson.fromJson(el, Config.class);
        debug = config.isDebug();
        lang = config.getLang();
        databaseType = config.getDatabaseType();
        databaseConfig = config.getDatabaseConfig();
        cooldown = config.getCooldown();
        cooldownmessage = config.getCooldownmessage();
        sizePool = config.getSizePool();
        emitcapture = config.isEmitcapture();
        minlvreq = config.getMinlvreq();
        minlv = config.getMinlv();
        maxlv = config.getMaxlv();
        shinyrate = config.getShinyrate();
        legendaryrate = config.getLegendaryrate();
        shinys = config.getShinys();
        legendaries = config.getLegendaries();
        israndom = config.isIsrandom();
        filterGenerationPokemon = config.getFilterGenerationPokemon();
        aliases = config.getAliases();
        allowshiny = config.isAllowshiny();
        allowlegendary = config.isAllowlegendary();
        legends = config.getLegends();
        poketradeblacklist = config.getPoketradeblacklist();
        poolview = config.isPoolview();
        savepool = config.isSavepool();
        cooldownPermission = config.getCooldownPermission();
        autoReset = config.isAutoReset();
        cooldownReset = config.getCooldownReset();
        discord_webhook = config.getDiscord_webhook();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH, "config.json",
          data);
        if (!futureWrite.join()) {
          CobbleWonderTrade.LOGGER.fatal("Could not write lang.json file for CobbleHunt.");
        }
      });

    if (!futureRead.join()) {
      CobbleWonderTrade.LOGGER.info("No config.json file found for" + CobbleWonderTrade.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH, "config.json",
        data);

      if (!futureWrite.join()) {
        CobbleWonderTrade.LOGGER.fatal("Could not write config.json file for CobbleHunt.");
      }
    }
  }

  public Integer getCooldown(ServerPlayerEntity player) {
    Integer cooldown = this.cooldown;
    for (Map.Entry<String, Integer> entry : cooldownPermission.entrySet()) {
      if (PermissionApi.hasPermission(player, entry.getKey(), 4)) {
        if (entry.getValue() < cooldown) cooldown = entry.getValue();
      }
    }
    return cooldown;
  }
}
