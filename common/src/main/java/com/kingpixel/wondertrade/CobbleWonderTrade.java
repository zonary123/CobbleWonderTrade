package com.kingpixel.wondertrade;

import com.kingpixel.wondertrade.Config.Config;
import com.kingpixel.wondertrade.Config.Lang;
import com.kingpixel.wondertrade.Config.WonderTradeConfig;
import com.kingpixel.wondertrade.Manager.WonderTradeManager;
import com.kingpixel.wondertrade.permissions.WonderTradePermission;
import com.kingpixel.wondertrade.utils.SpawnRates;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Carlos Varas Alonso - 28/04/2024 23:50
 */
public class CobbleWonderTrade {
  public static final String MOD_ID = "wondertrade";
  public static final Logger LOGGER = LogManager.getLogger();
  public static final String MOD_NAME = "CobbleWonderTrade";
  public static final String path = "/config/wondertrade/";
  public static Lang language = new Lang();
  public static MinecraftServer server;
  public static Config config = new Config();
  public static WonderTradeManager manager = new WonderTradeManager();
  public static WonderTradeConfig dexpermission = new WonderTradeConfig();
  public static WonderTradePermission permissions = new WonderTradePermission();
  public static SpawnRates spawnRates = new SpawnRates();

  public static void init() {
    LOGGER.info("Initializing " + MOD_NAME);
  }

  public static void load() {
    language.init();
    config.init();
    spawnRates.init();
    manager.init();
    WonderTradeUtil.init();
    manager.generatePokemonList();
  }

}