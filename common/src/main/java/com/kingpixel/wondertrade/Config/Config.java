package com.kingpixel.wondertrade.Config;

import com.kingpixel.cobbleutils.Model.Animations.core.Animations;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.Model.DurationValue;
import com.kingpixel.cobbleutils.Model.FilterPokemons;
import com.kingpixel.cobbleutils.Model.PokemonBlackList;
import com.kingpixel.cobbleutils.Model.WebHookData;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Config.models.CooldownConfig;
import com.kingpixel.wondertrade.Config.models.PoolConfig;
import com.kingpixel.wondertrade.Config.models.RatesConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Root configuration class for WonderTrade.
 * Organizes sub-settings cleanly into dedicated modular domains.
 *
 * @author Carlos Varas Alonso
 */
@Getter
@Setter
public class Config {
  private boolean debug;
  private String lang;
  private Animations animation;
  private List<String> commands;
  private WebHookData discord_webhook;
  private DataBaseConfig databaseConfig;

  private PoolConfig pool;
  private RatesConfig rates;
  private CooldownConfig cooldowns;

  // Legacy flat fields for automatic backward-compatibility migration
  private Boolean autoReset;
  private Integer cooldownReset;
  private Integer cooldownmessage;
  private Integer cooldownBroadcast;
  private Integer sizePool;
  private Integer minlvreq;
  private Integer minlv;
  private Integer maxlv;
  private Boolean poolview;
  private Integer shinyrate;
  private Integer mythicalrate;
  private Integer mythicalperfectivs;
  private Integer legendaryrate;
  private Integer legendaryperfectivs;
  private Integer ultrabeastrate;
  private Integer ultrabeastperfectivs;
  private Integer paradoxrate;
  private Integer paradoxperfectivs;
  private Boolean israndom;
  private DurationValue cooldown;
  private Map<String, DurationValue> cooldownPermission;
  private List<String> poketradeblacklist;
  private List<String> legends;
  private PokemonBlackList specialPokemons;
  private PokemonBlackList blackList;
  private FilterPokemons filterGenerationPokemon;

  public Config() {
    this.debug = false;
    this.lang = "en";
    this.animation = Animations.CSGO;
    this.commands = List.of("wt", "wondertrade");
    this.discord_webhook = new WebHookData("", "", "");
    this.databaseConfig = new DataBaseConfig("wondertrade");
    this.pool = new PoolConfig();
    this.rates = new RatesConfig();
    this.cooldowns = new CooldownConfig();
  }

  public void init() {
    Path configPath = Path.of(CobbleWonderTrade.PATH, "config.json");
    try {
      Config loaded = UtilsFile.readOrCreate(configPath, Config.class, Config::new);
      if (loaded != null) {
        CobbleWonderTrade.config = loaded;
      } else {
        CobbleWonderTrade.config = this;
      }
      CobbleWonderTrade.config.fix();
      UtilsFile.write(configPath, CobbleWonderTrade.config);
    } catch (IOException e) {
      CobbleWonderTrade.LOGGER.error("Failed to load or save config.json", e);
      CobbleWonderTrade.config = this;
      CobbleWonderTrade.config.fix();
    }
  }

  public void fix() {
    if (this.pool == null) this.pool = new PoolConfig();
    if (this.rates == null) this.rates = new RatesConfig();
    if (this.cooldowns == null) this.cooldowns = new CooldownConfig();
    if (this.commands == null || this.commands.isEmpty()) this.commands = List.of("wt", "wondertrade");
    if (this.discord_webhook == null) this.discord_webhook = new WebHookData("", "", "");
    if (this.databaseConfig == null) this.databaseConfig = new DataBaseConfig("wondertrade");

    // Migrate legacy fields into structured modules if present
    if (this.sizePool != null) {
      this.pool.setSizePool(this.sizePool);
      this.sizePool = null;
    }
    if (this.minlvreq != null) {
      this.pool.setMinlvreq(this.minlvreq);
      this.minlvreq = null;
    }
    if (this.minlv != null) {
      this.pool.setMinlv(this.minlv);
      this.minlv = null;
    }
    if (this.maxlv != null) {
      this.pool.setMaxlv(this.maxlv);
      this.maxlv = null;
    }
    if (this.poolview != null) {
      this.pool.setPoolview(this.poolview);
      this.poolview = null;
    }
    if (this.israndom != null) {
      this.pool.setIsrandom(this.israndom);
      this.israndom = null;
    }
    if (this.autoReset != null) {
      this.pool.setAutoReset(this.autoReset);
      this.autoReset = null;
    }
    if (this.cooldownReset != null) {
      this.pool.setCooldownReset(this.cooldownReset);
      this.cooldownReset = null;
    }
    if (this.filterGenerationPokemon != null) {
      this.pool.setFilterGenerationPokemon(this.filterGenerationPokemon);
      this.filterGenerationPokemon = null;
    }
    if (this.specialPokemons != null) {
      this.pool.setSpecialPokemons(this.specialPokemons);
      this.specialPokemons = null;
    }
    if (this.blackList != null) {
      this.pool.setBlackList(this.blackList);
      this.blackList = null;
    }
    if (this.poketradeblacklist != null) {
      for (String s : this.poketradeblacklist) {
        this.pool.getBlackList().getPokemons().add(s);
      }
      this.poketradeblacklist = null;
    }
    if (this.legends != null) {
      for (String s : this.legends) {
        this.pool.getBlackList().getPokemons().add(s);
      }
      this.legends = null;
    }

    if (this.shinyrate != null) {
      this.rates.setShinyrate(this.shinyrate);
      this.shinyrate = null;
    }
    if (this.mythicalrate != null) {
      this.rates.setMythicalrate(this.mythicalrate);
      this.mythicalrate = null;
    }
    if (this.mythicalperfectivs != null) {
      this.rates.setMythicalperfectivs(this.mythicalperfectivs);
      this.mythicalperfectivs = null;
    }
    if (this.legendaryrate != null) {
      this.rates.setLegendaryrate(this.legendaryrate);
      this.legendaryrate = null;
    }
    if (this.legendaryperfectivs != null) {
      this.rates.setLegendaryperfectivs(this.legendaryperfectivs);
      this.legendaryperfectivs = null;
    }
    if (this.ultrabeastrate != null) {
      this.rates.setUltrabeastrate(this.ultrabeastrate);
      this.ultrabeastrate = null;
    }
    if (this.ultrabeastperfectivs != null) {
      this.rates.setUltrabeastperfectivs(this.ultrabeastperfectivs);
      this.ultrabeastperfectivs = null;
    }
    if (this.paradoxrate != null) {
      this.rates.setParadoxrate(this.paradoxrate);
      this.paradoxrate = null;
    }
    if (this.paradoxperfectivs != null) {
      this.rates.setParadoxperfectivs(this.paradoxperfectivs);
      this.paradoxperfectivs = null;
    }

    if (this.cooldown != null) {
      this.cooldowns.setCooldown(this.cooldown);
      this.cooldown = null;
    }
    if (this.cooldownPermission != null) {
      this.cooldowns.setCooldownPermission(this.cooldownPermission);
      this.cooldownPermission = null;
    }
    if (this.cooldownmessage != null) {
      this.cooldowns.setCooldownmessage(this.cooldownmessage);
      this.cooldownmessage = null;
    }
    if (this.cooldownBroadcast != null) {
      this.cooldowns.setCooldownBroadcast(this.cooldownBroadcast);
      this.cooldownBroadcast = null;
    }

    this.pool.fix();
  }
}
