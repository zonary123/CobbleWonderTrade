package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DataBaseType;
import com.kingpixel.wondertrade.model.DataBaseData;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Getter
public class Config {
  private boolean debug;
  private String lang;
  private DataBaseType databaseType;
  private DataBaseData databaseConfig;
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
  private List<String> pokeblacklist;
  private List<String> poketradeblacklist;
  private List<String> legends;
  private List<String> aliases;

  public Config() {
    debug = false;
    lang = "en";
    databaseType = DataBaseType.JSON;
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
    pokeblacklist = List.of("Magikarp");
    poketradeblacklist = List.of("Magikarp");
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
        pokeblacklist = config.getPokeblacklist();
        aliases = config.getAliases();
        allowshiny = config.isAllowshiny();
        allowlegendary = config.isAllowlegendary();
        legends = config.getLegends();
        poketradeblacklist = config.getPoketradeblacklist();
        // forms = config.getForms();
        poolview = config.isPoolview();
        savepool = config.isSavepool();

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
}
