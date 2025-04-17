package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.ui.ConfirmMenu;
import com.kingpixel.cobbleutils.ui.PartyPcMenu;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.ui.WonderTradePoolUI;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 28/04/2024 23:58
 */
@Data
public class Lang {
  private String prefix;
  private String reload;
  private String titlepool;
  private String messagepoolwondertrade;
  private String messagewondertradeready;
  private String messagewondertraderecieved;
  private String messagewondertradecooldown;
  private String messagePokemonToWondertrade;
  private String messageThePokemonNotHaveMinLevel;
  private ItemModel info;
  private String title_webhook;
  private List<String> message_WebHook;
  private PartyPcMenu partyPcMenu;
  private ConfirmMenu confirmMenu;
  private WonderTradePoolUI pool;

  public Lang() {
    prefix = "&8[<gradient:#ff7900:#ffdbba>WonderTrade&8] ";
    reload = "%prefix% <#64de7c>Reloaded!";
    titlepool = "&6WonderTrade Pool";
    messagepoolwondertrade = "%prefix% <#64de7c>There are currently &e%total% <#d65549>pokemons <#64de7c>in the WonderTrade pool! \n" +
      "%prefix% " +
      "<#64de7c>Use " +
      "&6/wt <#64de7c>to trade a pokemon! \nThere are &6%shinys% &eshinys <#64de7c>and &6%legends% &dlegendaries!";
    messagewondertradeready = "%prefix% <#64de7c>WonderTrade is ready!";
    messagewondertraderecieved = "%prefix% <#64de7c>You have received a &6%pokemon% %gender% &f(&b%form%&f) %shiny%<#64de7c>!";
    messagePokemonToWondertrade = "%prefix% <#64de7c>The player &6%player% <#64de7c>has introduced &6%pokemon% %gender% &f(&b%form%&f) %shiny%";
    messageThePokemonNotHaveMinLevel = "%prefix% <#d65549>The pokemon &6%pokemon% %gender% &f(&b%form%&f) %shiny% <#d65549>doesn't have the minimum level <#ebab34>%minlevel%<#d65549>!";
    info = new ItemModel("minecraft:book", "<gradient:#ff7900:#ffdbba>Info WonderTrade", List.of(
      "",
      "<#ecca18>Shinys: &f%shinys%",
      "<#ab8fdb>Legendaries: &f%legends%",
      "<#d65549>UltraBeast: &f%ultrabeast%",
      "<#d65549>Paradox: &f%paradox%",
      "<#d65549>IVs 31: &f%ivs%",
      "<#3492eb>Cooldown: %time%",
      "",
      "<#ebab34>⏺ &7Left click to open the WonderTrade pool view.",
      "<#ebab34>⏺ &7Right click to open the WonderTrade pool especial view."
    ));
    info.setSlot(4);
    messagewondertradecooldown = "%prefix% <#d65549>You must wait before trading again %time%!";
    title_webhook = "WonderTrade";
    message_WebHook = List.of(
      "%pokemon% %gender% %form% %shiny% %player%",
      "Shinys: %shiny%",
      " - Ivs: %ivshp% / %ivsatk% / %ivsdef% / %ivsspa% / %ivsspdef% / %ivsspeed%",
      " - Evs: %evshp% / %evsatk% / %evsdef% / %evsspa% / %evsspdef% / %evsspeed%",
      "Ability: %ability%",
      "Nature: %nature%",
      "Owner: %owner%",
      "Country: %country%",
      "Ball: %ball%",
      "Size: %size%",
      "Moves: %move1% - %move2% - %move3% - %move4%",
      "Tradeable: %tradeable%",
      "Breedable: %breedable%"
    );
    partyPcMenu = new PartyPcMenu();
    confirmMenu = new ConfirmMenu();
    pool = new WonderTradePoolUI();
  }


  public void init() {
    CompletableFuture<Boolean> futureRead = Utils.readFileAsync(CobbleWonderTrade.PATH + "lang/",
      CobbleWonderTrade.config.getLang() + ".json",
      el -> {
        Gson gson = Utils.newGson();
        CobbleWonderTrade.language = gson.fromJson(el, Lang.class);
        String data = gson.toJson(CobbleWonderTrade.language);
        CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH + "lang/", CobbleWonderTrade.config.getLang() + ".json",
          data);
        if (!futureWrite.join()) {
          CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Error writing lang file");
        }
      });

    if (!futureRead.join()) {
      Gson gson = Utils.newGson();
      CobbleWonderTrade.language = this;
      String data = gson.toJson(CobbleWonderTrade.language);
      CompletableFuture<Boolean> futureWrite = Utils.writeFileAsync(CobbleWonderTrade.PATH + "lang/", CobbleWonderTrade.config.getLang() + ".json",
        data);

      if (!futureWrite.join()) {
        CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Error writing lang file");
      }
    }
  }

}
