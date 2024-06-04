package com.kingpixel.wondertrade;

import com.kingpixel.wondertrade.Config.Config;
import com.kingpixel.wondertrade.Config.Lang;
import com.kingpixel.wondertrade.Config.WonderTradeConfig;
import com.kingpixel.wondertrade.Manager.WonderTradeManager;
import com.kingpixel.wondertrade.command.CommandTree;
import com.kingpixel.wondertrade.permissions.WonderTradePermission;
import com.kingpixel.wondertrade.utils.SpawnRates;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
  private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public static void init() {
    LOGGER.info("Initializing " + MOD_NAME);
    events();
  }

  public static void load() {
    files();
    spawnRates.init();
    manager.init();
    WonderTradeUtil.init();
    manager.generatePokemonList();
    tasks();
  }

  private static void events() {
    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> CommandTree.register(dispatcher));
    LifecycleEvent.SERVER_STARTED.register(server -> load());
    PlayerEvent.PLAYER_JOIN.register(player -> manager.addPlayer(player));
    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getLevel().getServer());
  }

  private static void files() {
    language.init();
    config.init();
  }

  private static void tasks() {

  }
}