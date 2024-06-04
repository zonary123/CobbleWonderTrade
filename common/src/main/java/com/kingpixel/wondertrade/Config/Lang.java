package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Model.ItemModel;
import com.kingpixel.wondertrade.utils.Utils;

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
  private String titlepool;
  private String message;
  private String messagepoolwondertrade;
  private String messagewondertradeready;
  private String messagewondertraderecieved;
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
  private ItemModel itemnotallowpokemon;
  private ItemModel itemnotallowshiny;
  private ItemModel itemnotallowlegendary;
  private List<String> lorepokemon;

  public Lang() {
    prefix = "&8[&6WonderTrade&8] ";
    reload = "%prefix% &aReloaded!";
    title = "{#ff7900>#ffdbba}WonderTrade";
    titlepool = "{#ff7900>#ffdbba}WonderTrade Pool";
    titleconfirm = "{#ff7900>#ffdbba}Confirm";
    message = "%prefix% &aYou have received a &6%pokemon%&a!";
    messagepoolwondertrade = "%prefix% &aThere are currently &e%total% &cpokemons &ain the WonderTrade pool! \n " +
      "%prefix% " +
      "&aUse " +
      "&6/wt &ato trade a pokemon! \n There are %shinys% shinys and %legends% legendaries!";
    messagewondertradeready = "%prefix% &aWonderTrade is ready!";
    messagewondertraderecieved = "%prefix% &aYou have received a %pokemon% pokemon!";
    fill = "minecraft:gray_stained_glass_pane";
    colorhexnamepoke = "{#ff7900>#ffdbba}";
    notallowshiny = "{#db2e2e>#e68c8c}You can't trade shiny pokemon!";
    notallowlegendary = "{#db2e2e>#e68c8c}You can't trade legendary pokemon!";
    info = new ItemModel("minecraft:book", "{#ff7900>#ffdbba}Info WonderTrade", List.of("&7Shinys: &e%shinys%",
      "&7Legendaries: &e%legends%", "&7Time: &e%time%"));
    nopokemon = new ItemModel("cobblemon:poke_ball", "{#db2e2e>#e68c8c}Empty slot", List.of(""));
    confirm = new ItemModel("minecraft:lime_stained_glass_pane", "{#3ec758>#a2f2b2}Confirm", List.of(""));
    cancel = new ItemModel("minecraft:red_stained_glass_pane", "{#db2e2e>#e68c8c}Cancel", List.of(""));
    itempreviouspage = new ItemModel("minecraft:arrow", "§7Previous Page", List.of("§7Click to go to the previous page"));
    itemnextpage = new ItemModel("minecraft:arrow", "§7Next Page", List.of("§7Click to go to the next page"));
    itemclose = new ItemModel("minecraft:barrier", "§cClose", List.of("§7Click to close the menu"));
    lorepokemon = List.of("{#D3D3D3}§7Click to select this pokemon",
      "&8Level: &f%level%",
      "&eShiny: &f%shiny%",
      "&5Legendario: &f%legends%",
      "&eNature: &f%nature%",
      "&6Ability: &f%ability%",
      "&dIVs:",
      "  &cHP: &f%hp% &9Atk: &f%atk%  &7Def: &f%def%",
      "  &bSpAtk: &f%spa% &eSpDef: &f%spd% &aSpd: &f%spe%",
      "&3EVs:",
      "  &cHP: &f%evhp% &9Atk: &f%evatk%  &7Def: &f%evdef%",
      "  &bSpAtk: &f%evspa% &eSpDef: &f%evspd% &aSpd: &f%evspe%",
      "&2Moves:",
      "  &f%move1%",
      "  &f%move2%",
      "  &f%move3%",
      "  &f%move4%",
      "&6Form: %form%");
    donthavelevel = "{#db2e2e>#e68c8c}You don't have a pokemon with level %minlevel%!";
    itemnotallowpokemon = new ItemModel("cobblemon:net_ball", "{#db2e2e>#e68c8c}Not allow pokemon", List.of(""));
    itemnotallowshiny = new ItemModel("cobblemon:luxury_ball", "{#db2e2e>#e68c8c}Not allow shiny", List.of(""));
    itemnotallowlegendary = new ItemModel("cobblemon:master_ball", "{#db2e2e>#e68c8c}Not allow legendary", List.of(""));
  }

  public ItemModel getItemnotallowpokemon() {
    return itemnotallowpokemon;
  }

  public String getMessagewondertraderecieved() {
    return messagewondertraderecieved;
  }

  public String getMessagewondertradeready() {
    return messagewondertradeready;
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

  public String getTitlepool() {
    return titlepool;
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
        titlepool = lang.getTitlepool();
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
        itemnotallowpokemon = lang.getItemnotallowpokemon();
        itemnotallowshiny = lang.getItemnotallowshiny();
        itemnotallowlegendary = lang.getItemnotallowlegendary();
        messagewondertradeready = lang.getMessagewondertradeready();
        messagewondertraderecieved = lang.getMessagewondertraderecieved();
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
