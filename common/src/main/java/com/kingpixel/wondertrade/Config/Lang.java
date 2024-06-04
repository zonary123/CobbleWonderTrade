package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Model.ItemModel;
import com.kingpixel.wondertrade.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 28/04/2024 23:58
 */

public class Lang {
  private String prefix;
  private String reload;
  private String title;
  private String titleconfirm;
  private String message;
  private String messagepoolwondertrade;
  private String fill;
  private String colorhexnamepoke;
  private String donthavelevel;
  private String notallowshiny;
  private String notallowlegendary;
  private ItemModel info;
  private ItemModel nopokemon;
  private ItemModel confirm;
  private ItemModel cancel;
  private ItemModel itempreviouspage;
  private ItemModel itemnextpage;
  private ItemModel itemclose;
  private ItemModel itemnotallowshiny;
  private ItemModel itemnotallowlegendary;
  private List<String> lorepokemon;

  public Lang() {
    prefix = "&8[&6WonderTrade&8] ";
    reload = "%prefix% &aReloaded!";
    title = "{#ff7900>#ffdbba}WonderTrade";
    titleconfirm = "{#ff7900>#ffdbba}Confirm";
    message = "%prefix% &aYou have received a &6%pokemon%&a!";
    messagepoolwondertrade = "%prefix% &aHay actualmente &e%total% &cpokemons &aen la pool de WonderTrade! \n " +
      "%prefix% " +
      "&aUsa " +
      "&6/wt &apara intercambiar un pokemon! \n Hay %shinys% shinys y %legends% legendarios!";
    fill = "minecraft:gray_stained_glass_pane";
    colorhexnamepoke = "{#ff7900>#ffdbba}";
    notallowshiny = "{#db2e2e>#e68c8c}You can't trade shiny pokemon!";
    notallowlegendary = "{#db2e2e>#e68c8c}You can't trade legendary pokemon!";
    info = new ItemModel("minecraft:book", "{#ff7900>#ffdbba}Info WonderTrade", List.of("Shinys: %shiny%",
      "Legendaries: %legends%", "Time:" +
        " " +
        "%time%"));
    nopokemon = new ItemModel("cobblemon:poke_ball", "{#db2e2e>#e68c8c}Empty slot", List.of(""));
    confirm = new ItemModel("minecraft:lime_stained_glass_pane", "{#3ec758>#a2f2b2}Confirm", List.of(""));
    cancel = new ItemModel("minecraft:red_stained_glass_pane", "{#db2e2e>#e68c8c}Cancel", List.of(""));
    itempreviouspage = new ItemModel("minecraft:arrow", "§7Previous Page", List.of("§7Click to go to the previous page"));
    itemnextpage = new ItemModel("minecraft:arrow", "§7Next Page", List.of("§7Click to go to the next page"));
    itemclose = new ItemModel("minecraft:barrier", "§cClose", List.of("§7Click to close the menu"));
    lorepokemon = new ArrayList<>();
    lorepokemon.add("{#808080}§7Click to select this pokemon");
    lorepokemon.add("{#FFFFFF}Level: {#FFD700}%level%");
    lorepokemon.add("{#FFFFFF}%pokeball%");
    lorepokemon.add("{#FFFFFF}Shiny: {#FFD700}%shiny%");
    lorepokemon.add("{#FFFFFF}Legendario: {#FFD700}%legends%");
    lorepokemon.add("{#FFFFFF}Nature: {#FFD700}%nature%");
    lorepokemon.add("{#FFFFFF}Ability: {#FFD700}%ability%");
    lorepokemon.add("{#FFFFFF}IVs:");
    lorepokemon.add("{#FFFFFF}  HP: {#FFD700}%hp% {#FFFFFF}Atk: {#FFD700}%atk%  {#FFFFFF}Def: {#FFD700}%def%");
    lorepokemon.add("{#FFFFFF}  SpA: {#FFD700}%spa% {#FFFFFF}SpD: {#FFD700}%spd% {#FFFFFF}Spe: {#FFD700}%spe%");
    lorepokemon.add("{#FFFFFF}EVs:");
    lorepokemon.add("{#FFFFFF}  HP: {#FFD700}%evhp% {#FFFFFF}Atk: {#FFD700}%evatk%  {#FFFFFF}Def: {#FFD700}%evdef%");
    lorepokemon.add("{#FFFFFF}  SpA: {#FFD700}%evspa% {#FFFFFF}SpD: {#FFD700}%evspd% {#FFFFFF}Spe: {#FFD700}%evspe%");
    lorepokemon.add("{#FFFFFF}Moves:");
    lorepokemon.add("{#FFFFFF}  %move1%");
    lorepokemon.add("{#FFFFFF}  %move2%");
    lorepokemon.add("{#FFFFFF}  %move3%");
    lorepokemon.add("{#FFFFFF}  %move4%");
    lorepokemon.add("{#FFFFFF}Form: {#FFD700}%form%");
    donthavelevel = "{#db2e2e>#e68c8c}You don't have a pokemon with level %minlevel%!";
    itemnotallowshiny = new ItemModel("cobblemon:luxury_ball", "{#db2e2e>#e68c8c}Not allow shiny", List.of(""));
    itemnotallowlegendary = new ItemModel("cobblemon:master_ball", "{#db2e2e>#e68c8c}Not allow legendary", List.of(""));
  }

  public ItemModel getItemnotallowshiny() {
    return itemnotallowshiny;
  }

  public ItemModel getItemnotallowlegendary() {
    return itemnotallowlegendary;
  }

  public ItemModel getItempreviouspage() {
    return itempreviouspage;
  }

  public List<String> getLorepokemon() {
    return lorepokemon;
  }

  public ItemModel getItemnextpage() {
    return itemnextpage;
  }

  public ItemModel getItemclose() {
    return itemclose;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getReload() {
    return reload;
  }

  public String getMessage() {
    return message;
  }

  public String getMessagepoolwondertrade() {
    return messagepoolwondertrade;
  }

  public String getFill() {
    return fill;
  }

  public String getColorhexnamepoke() {
    return colorhexnamepoke;
  }

  public String getDonthavelevel() {
    return donthavelevel;
  }

  public ItemModel getInfo() {
    return info;
  }

  public String getTitle() {
    return title;
  }

  public String getTitleconfirm() {
    return titleconfirm;
  }

  public ItemModel getNopokemon() {
    return nopokemon;
  }

  public ItemModel getConfirm() {
    return confirm;
  }

  public ItemModel getCancel() {
    return cancel;
  }

  public String getNotallowshiny() {
    return notallowshiny;
  }

  public String getNotallowlegendary() {
    return notallowlegendary;
  }

  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.path + "lang/", CobbleWonderTrade.config.getLang() + ".json",
      el -> {
        Gson gson = Utils.newGson();
        Lang lang = gson.fromJson(el, Lang.class);
        prefix = lang.getPrefix();
        reload = lang.getReload();
        message = lang.getMessage();
        messagepoolwondertrade = lang.getMessagepoolwondertrade();
        fill = lang.getFill();
        colorhexnamepoke = lang.getColorhexnamepoke();
        info = lang.getInfo();
        title = lang.getTitle();
        titleconfirm = lang.getTitleconfirm();
        nopokemon = lang.getNopokemon();
        confirm = lang.getConfirm();
        cancel = lang.getCancel();
        itempreviouspage = lang.getItempreviouspage();
        itemnextpage = lang.getItemnextpage();
        itemclose = lang.getItemclose();
        lorepokemon = lang.getLorepokemon();
        donthavelevel = lang.getDonthavelevel();
        notallowshiny = lang.getNotallowshiny();
        notallowlegendary = lang.getNotallowlegendary();
        itemnotallowshiny = lang.getItemnotallowshiny();
        itemnotallowlegendary = lang.getItemnotallowlegendary();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.path + "lang/", CobbleWonderTrade.config.getLang() + ".json",
          data);
        if (!futureWrite.join()) {
          CobbleWonderTrade.LOGGER.fatal("Could not write lang.json file for CobbleHunt.");
        }
      });

    if (!futureRead.join()) {
      CobbleWonderTrade.LOGGER.info("No lang.json file found for" + CobbleWonderTrade.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.path + "lang/", CobbleWonderTrade.config.getLang() + ".json",
        data);

      if (!futureWrite.join()) {
        CobbleWonderTrade.LOGGER.fatal("Could not write lang.json file for CobbleHunt.");
      }
    }
  }

}
