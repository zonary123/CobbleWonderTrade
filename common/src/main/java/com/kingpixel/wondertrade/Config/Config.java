package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.utils.Utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
public class Config {
  private String lang;
  private int cooldown;
  private int sizePool;
  private int shinyrate;
  private int legendaryrate;
  private int shinys;
  private int legendaries;
  private boolean israndom;
  private List<String> pokeblacklist;
  private List<String> aliases;

  public Config() {
    lang = "en";
    cooldown = 30;
    sizePool = 50;
    shinyrate = 8192;
    legendaryrate = 16512;
    shinys = 0;
    legendaries = 0;
    israndom = true;
    pokeblacklist = List.of("magikarp");
    aliases = List.of("wt", "wondertrade");
  }

  public String getLang() {
    return lang;
  }

  public int getCooldown() {
    return cooldown;
  }

  public int getSizePool() {
    return sizePool;
  }

  public int getShinys() {
    return shinys;
  }

  public int getLegendaries() {
    return legendaries;
  }

  public boolean isIsrandom() {
    return israndom;
  }

  public int getShinyrate() {
    return shinyrate;
  }

  public int getLegendaryrate() {
    return legendaryrate;
  }

  public List<String> getPokeblacklist() {
    return pokeblacklist;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.path, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        Config config = gson.fromJson(el, Config.class);
        lang = config.getLang();
        cooldown = config.getCooldown();
        sizePool = config.getSizePool();
        shinyrate = config.getShinyrate();
        legendaryrate = config.getLegendaryrate();
        shinys = config.getShinys();
        legendaries = config.getLegendaries();
        israndom = config.isIsrandom();
        pokeblacklist = config.getPokeblacklist();
        aliases = config.getAliases();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.path, "config.json",
          data);
        if (!futureWrite.join()) {
          CobbleWonderTrade.LOGGER.fatal("Could not write lang.json file for CobbleHunt.");
        }
      });

    if (!futureRead.join()) {
      CobbleWonderTrade.LOGGER.info("No config.json file found for" + CobbleWonderTrade.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.path, "config.json",
        data);

      if (!futureWrite.join()) {
        CobbleWonderTrade.LOGGER.fatal("Could not write config.json file for CobbleHunt.");
      }
    }

  }
}
