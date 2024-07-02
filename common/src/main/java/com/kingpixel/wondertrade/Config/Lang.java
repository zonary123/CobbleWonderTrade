package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Model.ItemModel;
import com.kingpixel.wondertrade.utils.Utils;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 28/04/2024 23:58
 */
@Getter
public class Lang {
  private String prefix;
  private String reload;
  private String title;
  private String titleconfirm;
  private String titlepool;
  private String titlePc;
  private String message;
  private String messagepoolwondertrade;
  private String messagewondertradeready;
  private String messagewondertraderecieved;
  private String messagewondertradecooldown;
  private String messagePokemonTradeBlackList;
  private String messageNoPokemonSlot;
  private String messageNoPoolView;
  private String messagePokemonToWondertrade;
  private String fill;
  private String yes;
  private String no;
  private String symbolShiny;
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
  private ItemModel Pc;
  private List<String> lorepokemon;

  public Lang() {
    prefix = "&8[&6WonderTrade&8] ";
    reload = "%prefix% &aReloaded!";
    title = "<gradient:#ff7900:#ffdbba>WonderTrade";
    titlepool = "&6WonderTrade Pool";
    titlePc = "§bPC";
    titleconfirm = "<gradient:#ff7900:#ffdbba>Confirm";
    yes = "&aYes";
    no = "&cNo";
    message = "%prefix% &aYou have received a %name% %gender% %form% %shiny%&a!";
    symbolShiny = "&e⭐";
    messagepoolwondertrade = "%prefix% &aThere are currently &e%total% &cpokemons &ain the WonderTrade pool! \n" +
      "%prefix% " +
      "&aUse " +
      "&6/wt &ato trade a pokemon! \nThere are &6%shinys% &eshinys &aand &6%legends% &dlegendaries!";
    messageNoPoolView = "%prefix% &cThe pool view is disabled!";
    messagewondertradeready = "%prefix% &aWonderTrade is ready!";
    messagewondertraderecieved = "%prefix% &aYou have received a &6%name% %gender% &f(&b%form%&f) %shiny%&a!";
    messageNoPokemonSlot = "%prefix% &cYou don't have any pokemon in this slot!";
    messagePokemonTradeBlackList = "%prefix% &cYou can't trade this pokemon %pokemon%!";
    fill = "minecraft:gray_stained_glass_pane";
    colorhexnamepoke = "<gradient:#ff7900:#ffdbba>%pokemon%</gradient>";
    notallowshiny = "<gradient:#db2e2e:#e68c8c>You can't trade shiny pokemon!";
    notallowlegendary = "<gradient:#db2e2e:#e68c8c>You can't trade legendary pokemon!";
    messagePokemonToWondertrade = "%prefix% &aThe player &6%player% &ahas introduced &6%pokemon% &f(&b%form%&f) " +
      "%shiny%";
    info = new ItemModel("minecraft:book", "<gradient:#ff7900:#ffdbba>Info WonderTrade", List.of("&7Shinys: &e%shinys%",
      "&7Legendaries: &e%legends%", "&7Time: &e%time%"));
    nopokemon = new ItemModel("cobblemon:poke_ball", "<gradient:#db2e2e:#e68c8c>Empty slot", List.of(""));
    confirm = new ItemModel("minecraft:lime_stained_glass_pane", "<gradient:#3ec758:#a2f2b2>Confirm</gradient>",
      List.of(
        ""));
    cancel = new ItemModel("minecraft:red_stained_glass_pane", "<gradient:#db2e2e:#e68c8c>Cancel", List.of(""));
    itempreviouspage = new ItemModel("minecraft:arrow", "&7Previous Page", List.of("&7Click to go to the previous " +
      "page"));
    messagewondertradecooldown = "&cYou must wait before trading again %time%!";
    itemnextpage = new ItemModel("minecraft:arrow", "&7Next Page", List.of("&7Click to go to the next page"));
    itemclose = new ItemModel("minecraft:barrier", "&cClose", List.of("&7Click to close the menu"));
    Pc = new ItemModel("cobblemon:pc", "<gradient:#ff7900:#ffdbba>PC", List.of("&7Click to open the PC"));
    lorepokemon = List.of("&7Click to select this pokemon");
    donthavelevel = "<gradient:#db2e2e:#e68c8c>You don't have a pokemon with level %minlevel%!</gradient>";
    itemnotallowpokemon = new ItemModel("cobblemon:net_ball", "<gradient:#db2e2e:#e68c8c>Not allow pokemon", List.of(
      ""));
    itemnotallowshiny = new ItemModel("cobblemon:luxury_ball", "<gradient:#db2e2e:#e68c8c>Not allow shiny", List.of(
      ""));
    itemnotallowlegendary = new ItemModel("cobblemon:master_ball", "<gradient:#db2e2e:#e68c8c>Not allow legendary",
      List.of(
        ""));
  }


  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.path + "lang/", CobbleWonderTrade.config.getLang() + ".json",
      el -> {
        Gson gson = Utils.newGson();
        Lang lang = gson.fromJson(el, Lang.class);
        prefix = lang.getPrefix();
        reload = lang.getReload();
        yes = lang.getYes();
        no = lang.getNo();
        message = lang.getMessage();
        messagepoolwondertrade = lang.getMessagepoolwondertrade();
        fill = lang.getFill();
        colorhexnamepoke = lang.getColorhexnamepoke();
        info = lang.getInfo();
        title = lang.getTitle();
        titlePc = lang.getTitlePc();
        titleconfirm = lang.getTitleconfirm();
        titlepool = lang.getTitlepool();
        nopokemon = lang.getNopokemon();
        confirm = lang.getConfirm();
        cancel = lang.getCancel();
        messagePokemonToWondertrade = lang.getMessagePokemonToWondertrade();
        itempreviouspage = lang.getItempreviouspage();
        itemnextpage = lang.getItemnextpage();
        itemclose = lang.getItemclose();
        lorepokemon = lang.getLorepokemon();
        donthavelevel = lang.getDonthavelevel();
        symbolShiny = lang.getSymbolShiny();
        notallowshiny = lang.getNotallowshiny();
        notallowlegendary = lang.getNotallowlegendary();
        itemnotallowpokemon = lang.getItemnotallowpokemon();
        itemnotallowshiny = lang.getItemnotallowshiny();
        itemnotallowlegendary = lang.getItemnotallowlegendary();
        messagewondertradeready = lang.getMessagewondertradeready();
        messagewondertraderecieved = lang.getMessagewondertraderecieved();
        messagewondertradecooldown = lang.getMessagewondertradecooldown();
        messageNoPokemonSlot = lang.getMessageNoPokemonSlot();
        messagePokemonTradeBlackList = lang.getMessagePokemonTradeBlackList();
        messageNoPoolView = lang.getMessageNoPoolView();
        Pc = lang.getPc();
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
