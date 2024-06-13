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
  private int cooldownmessage;
  private int sizePool;
  private int minlvreq;
  private int minlv;
  private int maxlv;
  private boolean allowshiny;
  private boolean allowlegendary;
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
    lang = "en";
    cooldown = 30;
    cooldownmessage = 15;
    sizePool = 50;
    minlvreq = 5;
    minlv = 5;
    maxlv = 36;
    shinyrate = 8192;
    legendaryrate = 16512;
    shinys = 0;
    legendaries = 0;
    israndom = true;
    allowshiny = true;
    allowlegendary = true;
    pokeblacklist = List.of("Magikarp");
    poketradeblacklist = List.of("Magikarp");
    legends = List.of("Magikarp");
    aliases = List.of("wt", "wondertrade");
  }

  public String getLang() {
    return lang;
  }

  public List<String> getLegends() {
    return legends;
  }

  public int getMinlvreq() {
    return minlvreq;
  }

  public List<String> getPoketradeblacklist() {
    return poketradeblacklist;
  }

  public int getCooldownmessage() {
    return cooldownmessage;
  }

  public int getMinlv() {
    return minlv;
  }

  public int getMaxlv() {
    return maxlv;
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

  public boolean isAllowshiny() {
    return allowshiny;
  }

  public boolean isAllowlegendary() {
    return allowlegendary;
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.path, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        Config config = gson.fromJson(el, Config.class);
        lang = config.getLang();
        cooldown = config.getCooldown();
        cooldownmessage = config.getCooldownmessage();
        sizePool = config.getSizePool();
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
