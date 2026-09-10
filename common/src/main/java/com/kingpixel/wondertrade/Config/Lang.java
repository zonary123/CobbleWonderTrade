package com.kingpixel.wondertrade.Config;

import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.discord.WebHookStruct;
import com.kingpixel.cobbleutils.ui.ConfirmMenu;
import com.kingpixel.cobbleutils.ui.PartyPcMenu;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.ui.WonderTradePoolUI;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Localization configuration for UltraWonderTrade.
 *
 * @author Carlos Varas Alonso
 */
@Getter
@Setter
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
  private String noPermission;
  private String clickingTooFast;
  private ItemModel info;
  private WebHookStruct webHookPutPool;
  private WebHookStruct webHookObtainedPool;
  private WebHookStruct webHookSpecialPutPool;
  private WebHookStruct webHookSpecialObtainedPool;
  private PartyPcMenu partyPcMenu;
  private ConfirmMenu confirmMenu;
  private WonderTradePoolUI pool;

  public Lang() {
    this.prefix = "&8[<gradient:#ff7900:#ffdbba>UltraWonderTrade&8] ";
    this.reload = "%prefix% <#64de7c>Reloaded!";
    this.titlepool = "&6UltraWonderTrade Pool";
    this.messagepoolwondertrade = "%prefix% <#64de7c>There are currently &e%total% <#d65549>pokemons <#64de7c>in the UltraWonderTrade pool! \n" +
      "%prefix% <#64de7c>Use &6/wt <#64de7c>to trade a pokemon! \nThere are &6%shinys% &eshinys <#64de7c>and &6%legends% &dlegendaries!";
    this.messagewondertradeready = "%prefix% <#64de7c>UltraWonderTrade is ready!";
    this.messagewondertraderecieved = "%prefix% <#64de7c>You have received a &6%pokemon% %gender% &f(&b%form%&f) %shiny%<#64de7c>!";
    this.messagePokemonToWondertrade = "%prefix% <#64de7c>The player &6%player% <#64de7c>has introduced &6%pokemon% %gender% &f(&b%form%&f) %shiny%";
    this.messageThePokemonNotHaveMinLevel = "%prefix% <#d65549>The pokemon &6%pokemon% %gender% &f(&b%form%&f) %shiny% <#d65549>doesn't have the minimum level <#ebab34>%minlevel%<#d65549>!";
    this.noPermission = "%prefix% <#d65549>You don't have permission to perform this action!";
    this.clickingTooFast = "%prefix% <#d65549>You are clicking too fast!";
    this.messagewondertradecooldown = "%prefix% <#d65549>You must wait before trading again %time%!";

    this.info = new ItemModel("minecraft:book", "<gradient:#ff7900:#ffdbba>Info UltraWonderTrade", List.of(
      "",
      "<#ecca18>Shinys: &f%shinys%",
      "<#ab8fdb>Legendaries: &f%legends%",
      "<#d65549>UltraBeast: &f%ultrabeast%",
      "<#d65549>Paradox: &f%paradox%",
      "<#d65549>IVs 31: &f%ivs%",
      "<#3492eb>Cooldown: %time%",
      "",
      "<#ebab34>⏺ &7Left click to open the UltraWonderTrade pool view.",
      "<#ebab34>⏺ &7Right click to open the UltraWonderTrade pool especial view."
    ));
    this.info.setSlot(4);

    StringBuilder defaultStruct = new StringBuilder();
    defaultStruct.append(" - Level: %level%\n");
    defaultStruct.append(" - Gender: %gender%\n");
    defaultStruct.append(" - Form: %form%\n");
    defaultStruct.append(" - Shiny: %shiny%\n");
    defaultStruct.append(" - Nature: %nature%\n");
    defaultStruct.append(" - Ability: %ability% %ah%\n");
    defaultStruct.append(" - Friendship: %friendship%\n");
    defaultStruct.append(" - Breedable: %breedable%\n");
    defaultStruct.append(" - IVs:\n");
    defaultStruct.append("   HP: %ivshp% - Atk: %ivsatk% - Def: %ivsdef%\n");
    defaultStruct.append("   SpA: %ivsspa% - SpD: %ivsspdef% - Spe: %ivsspeed%\n");
    defaultStruct.append(" - Item: %item%\n");
    defaultStruct.append(" - Ball: %ball%\n");
    defaultStruct.append(" - Moves: %move1% | %move2% | %move3% | %move4%\n");
    defaultStruct.append(" - Owner: %owner%\n");

    this.webHookPutPool = new WebHookStruct();
    var embed = this.webHookPutPool.getEmbeds().getFirst();
    embed.setColor("f3ac67");
    embed.setTitle("UltraWonderTrade Pool | Put");
    StringBuilder descriptionPut = new StringBuilder();
    descriptionPut.append("Pokemon put by %player%\n");
    embed.setDescription(descriptionPut.append(defaultStruct).toString());

    this.webHookObtainedPool = new WebHookStruct();
    embed = this.webHookObtainedPool.getEmbeds().getFirst();
    embed.setColor("5abad9");
    embed.setTitle("UltraWonderTrade Pool | Obtained");
    StringBuilder descriptionObtained = new StringBuilder();
    descriptionObtained.append("Pokemon obtained by %player%\n");
    embed.setDescription(descriptionObtained.append(defaultStruct).toString());

    this.webHookSpecialPutPool = new WebHookStruct();
    embed = this.webHookSpecialPutPool.getEmbeds().getFirst();
    embed.setColor("c3b0f6");
    embed.setTitle("UltraWonderTrade Pool | Special Put");
    StringBuilder descriptionSpecialPut = new StringBuilder();
    descriptionSpecialPut.append("Special Pokemon put by %player%\n");
    embed.setDescription(descriptionSpecialPut.append(defaultStruct).toString());

    this.webHookSpecialObtainedPool = new WebHookStruct();
    embed = this.webHookSpecialObtainedPool.getEmbeds().getFirst();
    embed.setColor("c3b0f6");
    embed.setTitle("UltraWonderTrade Pool | Special Obtained");
    StringBuilder descriptionSpecialObtained = new StringBuilder();
    descriptionSpecialObtained.append("Special Pokemon obtained by %player%\n");
    embed.setDescription(descriptionSpecialObtained.append(defaultStruct).toString());

    this.partyPcMenu = new PartyPcMenu();
    this.confirmMenu = new ConfirmMenu();
    this.pool = new WonderTradePoolUI();
  }

  public void init() {
    String langName = CobbleWonderTrade.config != null ? CobbleWonderTrade.config.getLang() : "en";
    Path langPath = Path.of(CobbleWonderTrade.PATH, "lang", langName + ".json");
    try {
      Lang loaded = UtilsFile.readOrCreate(langPath, Lang.class, Lang::new);
      if (loaded != null) {
        CobbleWonderTrade.language = loaded;
      } else {
        CobbleWonderTrade.language = this;
      }
      UtilsFile.write(langPath, CobbleWonderTrade.language);
    } catch (IOException e) {
      CobbleWonderTrade.LOGGER.error("Failed to load or save language file: " + langPath, e);
      CobbleWonderTrade.language = this;
    }
  }
}
