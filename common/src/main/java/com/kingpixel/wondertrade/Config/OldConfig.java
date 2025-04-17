package com.kingpixel.wondertrade.Config;

import com.kingpixel.cobbleutils.Model.FilterPokemons;
import com.kingpixel.cobbleutils.Model.PokemonBlackList;
import com.kingpixel.cobbleutils.Model.WebHookData;
import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * @author Carlos Varas Alonso - 29/04/2024 0:14
 */
@Data
public class OldConfig {
  private boolean debug;
  private String lang;
  private List<String> aliases;
  private WebHookData discord_webhook;
  private boolean autoReset;
  private int cooldownReset;
  private int cooldown;
  private int cooldownmessage;
  private int sizePool;
  private int minlvreq;
  private int minlv;
  private int maxlv;
  private boolean allowshiny;
  private boolean allowlegendary;
  private boolean poolview;
  private boolean savepool;
  private int shinyrate;
  private int legendaryrate;
  private int shinys;
  private int legendaries;
  private boolean israndom;
  private Map<String, Integer> cooldownPermission;
  private FilterPokemons filterGenerationPokemon;
  private List<String> poketradeblacklist;
  private List<String> legends;
  private PokemonBlackList blackList;

  public OldConfig() {
    debug = false;
    lang = "en";
    autoReset = false;
    cooldownReset = 30;
    discord_webhook = new WebHookData("", "", "");
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
    israndom = false;
    allowshiny = true;
    allowlegendary = true;
    cooldownPermission = Map.of(
      "wondertrade.bypasscooldown", 0,
      "wondertrade.vip", 15,
      "wondertrade.vip+", 10,
      "wondertrade.vip++", 5
    );
    filterGenerationPokemon = new FilterPokemons();
    poketradeblacklist = List.of("Magikarp", "egg", "pokestop");
    legends = List.of("Magikarp");
    aliases = List.of("wt", "wondertrade");
    blackList = new PokemonBlackList();
  }
}
