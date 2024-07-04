package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Model.ItemModel;
import com.kingpixel.wondertrade.utils.Utils;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Getter
public class Config {
  private String lang;
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
  private ItemModel itempreviouspage;
  private ItemModel itemclose;
  private ItemModel itemnextpage;

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
    savepool = false;
    poolview = false;
    emitcapture = false;
    israndom = true;
    allowshiny = true;
    allowlegendary = true;
    pokeblacklist = List.of("Magikarp");
    poketradeblacklist = List.of("Magikarp");
    legends = List.of("Magikarp");
    aliases = List.of("wt", "wondertrade");
    //forms = Map.of("hisui", "&f(&eHisuian&f)");
    itempreviouspage = new ItemModel("minecraft:arrow", "&7Previous Page", List.of("&7Click to go to the previous " +
      "page"));
    itemnextpage = new ItemModel("minecraft:arrow", "&7Next Page", List.of("&7Click to go to the next page"));
    itemclose = new ItemModel("minecraft:barrier", "&cClose", List.of("&7Click to close the menu"));
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.PATH, "config.json",
      el -> {
        Gson gson = Utils.newGson();
        Config config = gson.fromJson(el, Config.class);
        lang = config.getLang();
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
        itempreviouspage = config.getItempreviouspage();
        itemclose = config.getItemclose();
        itemnextpage = config.getItemnextpage();
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
