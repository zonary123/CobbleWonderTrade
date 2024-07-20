package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
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
  private String messageisLegendaryOrShinyMessage;
  private String messageThePokemonNotHaveMinLevel;
  private String fill;
  private String colorhexnamepoke;
  private String donthavelevel;
  private String notallowshiny;
  private String notallowlegendary;
  private ItemModel info;
  private ItemModel itemnotallowpokemon;
  private ItemModel itemnotallowshiny;
  private ItemModel itemnotallowlegendary;
  private ItemModel Pc;
  private List<String> lorepokemon;

  public Lang() {
    prefix = "&8[<gradient:#ff7900:#ffdbba>WonderTrade&8] ";
    reload = "%prefix% <#64de7c>Reloaded!";
    title = "<gradient:#ff7900:#ffdbba>WonderTrade";
    titlepool = "&6WonderTrade Pool";
    titlePc = "§bPC";
    titleconfirm = "<gradient:#6ed480:#96e0a3>Confirm";
    message = "%prefix% <#64de7c>You have received a &6%pokemon% %gender% &f(&b%form%&f) %shiny%<#64de7c>!";
    messagepoolwondertrade = "%prefix% <#64de7c>There are currently &e%total% <#d65549>pokemons <#64de7c>in the WonderTrade pool! \n" +
      "%prefix% " +
      "<#64de7c>Use " +
      "&6/wt <#64de7c>to trade a pokemon! \nThere are &6%shinys% &eshinys <#64de7c>and &6%legends% &dlegendaries!";
    messageNoPoolView = "%prefix% <#d65549>The pool view is disabled!";
    messagewondertradeready = "%prefix% <#64de7c>WonderTrade is ready!";
    messagewondertraderecieved = "%prefix% <#64de7c>You have received a &6%pokemon% %gender% &f(&b%form%&f) %shiny%<#64de7c>!";
    messageNoPokemonSlot = "%prefix% <#d65549>You don't have any pokemon in this slot!";
    messagePokemonTradeBlackList = "%prefix% <#d65549>You can't trade this pokemon %pokemon%!";
    messageisLegendaryOrShinyMessage = "%prefix% <#64de7c>The special pokemon &6%pokemon% %gender% &f(&b%form%&f) %shiny%<#64de7c>! It has been withdrawn from WonderTrade by the player &6%player%<#64de7c>.";
    fill = "minecraft:gray_stained_glass_pane";
    colorhexnamepoke = "<gradient:#ff7900:#ffdbba>%pokemon%</gradient>";
    notallowshiny = "%prefix% <gradient:#db2e2e:#e68c8c>You can't trade shiny pokemon!";
    notallowlegendary = "%prefix% <gradient:#db2e2e:#e68c8c>You can't trade legendary pokemon!";
    messagePokemonToWondertrade = "%prefix% <#64de7c>The player &6%player% <#64de7c>has introduced &6%pokemon% %gender% &f(&b%form%&f) %shiny%";
    messageThePokemonNotHaveMinLevel = "%prefix% <#d65549>The pokemon &6%pokemon% %gender% &f(&b%form%&f) %shiny% <#d65549>doesn't have the minimum level <#ebab34>%minlevel%<#d65549>!";
    info = new ItemModel("minecraft:book", "<gradient:#ff7900:#ffdbba>Info WonderTrade", List.of(
      "",
      "<#ecca18>Shinys: &f%shinys%",
      "<#ab8fdb>Legendaries: &f%legends%",
      "<#3492eb>Cooldown: %time%",
      "",
      "<#ebab34>⏺ &7Left click to open the WonderTrade pool view.",
      "<#ebab34>⏺ &7Right click to open the WonderTrade pool especial view."
    ));
    messagewondertradecooldown = "%prefix% <#d65549>You must wait before trading again %time%!";
    Pc = new ItemModel("cobblemon:pc", "<#49a0d6>PC", List.of("<#ebab34>⏺ &7Click to open the PC"));
    lorepokemon = List.of("<#ebab34>⏺ &7Click to select this pokemon");
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
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.PATH + "lang/",
      CobbleWonderTrade.config.getLang() + ".json",
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
        titlePc = lang.getTitlePc();
        titleconfirm = lang.getTitleconfirm();
        titlepool = lang.getTitlepool();
        messagePokemonToWondertrade = lang.getMessagePokemonToWondertrade();
        lorepokemon = lang.getLorepokemon();
        donthavelevel = lang.getDonthavelevel();
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
        messageisLegendaryOrShinyMessage = lang.getMessageisLegendaryOrShinyMessage();
        messageThePokemonNotHaveMinLevel = lang.getMessageThePokemonNotHaveMinLevel();
        Pc = lang.getPc();
        String data = gson.toJson(this);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH + "lang/", CobbleWonderTrade.config.getLang() + ".json",
          data);
        if (!futureWrite.join()) {
          CobbleWonderTrade.LOGGER.fatal("Could not write lang.json file for CobbleHunt.");
        }
      });

    if (!futureRead.join()) {
      CobbleWonderTrade.LOGGER.info("No lang.json file found for" + CobbleWonderTrade.MOD_NAME + ". Attempting to generate one.");
      Gson gson = Utils.newGson();
      String data = gson.toJson(this);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH + "lang/", CobbleWonderTrade.config.getLang() + ".json",
        data);

      if (!futureWrite.join()) {
        CobbleWonderTrade.LOGGER.fatal("Could not write lang.json file for CobbleHunt.");
      }
    }
  }

}
