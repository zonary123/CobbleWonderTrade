package com.kingpixel.wondertrade;

import com.kingpixel.cobbleutils.Model.DataBaseType;
import com.kingpixel.cobbleutils.Model.FilterPokemons;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.cobbleutils.util.UtilsLogger;
import com.kingpixel.cobbleutils.util.async.AsyncContext;
import com.kingpixel.cobbleutils.util.async.UtilsAsync;
import com.kingpixel.wondertrade.Config.Config;
import com.kingpixel.wondertrade.Config.Lang;
import com.kingpixel.wondertrade.command.CommandTree;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.ui.WonderTradePoolUI;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for WonderTrade mod.
 * Features optimized async processing via CobbleUtils AsyncContext and centralized UtilsLogger.
 *
 * @author Carlos Varas Alonso
 */
public class CobbleWonderTrade {
  public static final String MOD_ID = "wondertrade";
  public static final String MOD_NAME = "UltraWondertrade";
  public static final String PATH = "config/wondertrade/";
  public static final String PATH_DATA = PATH + "data/";
  public static final String PATH_DATA_USER = PATH_DATA + "users/";

  public static final Logger LOGGER = UtilsLogger.getLogger(MOD_ID);
  public static final AsyncContext ASYNC = UtilsAsync.createContext(MOD_ID, "UltraWondertrade-Async", 2, 8);

  public static Lang language = new Lang();
  public static MinecraftServer server;
  public static Config config = new Config();

  private static ScheduledFuture<?> broadcastFuture;
  private static ScheduledFuture<?> autoResetFuture;
  private static ScheduledFuture<?> playerCheckFuture;

  public static void init() {
    events();
  }

  public static void load() {
    files();
    DatabaseClientFactory.createDatabaseClient(config.getDatabaseConfig());
    scheduleTasks();
  }

  private static void events() {
    files();

    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> CommandTree.register(dispatcher));

    LifecycleEvent.SERVER_STARTED.register(s -> load());

    PlayerEvent.PLAYER_JOIN.register(player -> {
      ASYNC.runAsync(() -> DatabaseClientFactory.databaseClient.getUserInfo(player));
    });

    PlayerEvent.PLAYER_QUIT.register(player -> {
      DatabaseClientFactory.databaseClient.removeIfNecessary(player);
      WonderTradePoolUI.removePlayer(player.getUuid());
    });

    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());

    LifecycleEvent.SERVER_STOPPING.register(s -> {
      cancelTasks();
      if (DatabaseClientFactory.databaseClient != null) {
        DatabaseClientFactory.databaseClient.disconnect();
      }
      ASYNC.shutdown();
    });
  }

  private static void files() {
    FilterPokemons.removeCache(MOD_ID);
    config.init();
    language.init();
    if (config.getDatabaseConfig().getType() == DataBaseType.JSON) {
      try {
        Files.createDirectories(Path.of(PATH_DATA_USER));
      } catch (Exception e) {
        LOGGER.error("Failed to create user directory: " + PATH_DATA_USER, e);
      }
    }
  }

  private static void scheduleTasks() {
    cancelTasks();

    long intervalBroadcast = 60L * config.getCooldowns().getCooldownBroadcast();
    long intervalAutoReset = 60L * config.getPool().getCooldownReset();
    long intervalPlayerCheck = 60L * config.getCooldowns().getCooldownmessage();

    // Player check task
    if (config.getCooldowns().getCooldownmessage() > 0) {
      playerCheckFuture = ASYNC.scheduleAtFixedRateWithFuture(() -> {
        if (config.isDebug()) LOGGER.info("Checking players for WonderTrade cooldown");
        if (server == null || server.getPlayerManager() == null) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
          if (player == null) continue;
          var userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
          if (userInfo != null && !userInfo.hasCooldown() && !userInfo.isMessagesend()) {
            PlayerUtils.sendMessage(player, language.getMessagewondertradeready(), language.getPrefix(), TypeMessage.CHAT);
            userInfo.setMessagesend(true);
            DatabaseClientFactory.databaseClient.updateUserInfo(player, userInfo);
          }
        }
      }, 10, intervalPlayerCheck, TimeUnit.SECONDS);
    }

    // Auto reset pool task
    if (config.getPool().getCooldownReset() > 0 && config.getPool().isAutoReset() && !config.getPool().isIsrandom()) {
      autoResetFuture = ASYNC.scheduleAtFixedRateWithFuture(() -> {
        if (config.isDebug()) LOGGER.info("Checking Auto Reset Pool");
        if (DatabaseClientFactory.databaseClient.shouldRestartPool()) {
          if (config.isDebug()) LOGGER.info("Resetting WonderTrade Pool");
          DatabaseClientFactory.databaseClient.restartPool();
        }
      }, 10, intervalAutoReset, TimeUnit.SECONDS);
    }

    // Broadcast task
    if (config.getCooldowns().getCooldownBroadcast() > 0) {
      broadcastFuture = ASYNC.scheduleAtFixedRateWithFuture(() -> {
        if (server == null) return;
        if (config.isDebug()) LOGGER.info("Broadcasting WonderTrade stats");
        var stats = CommandTree.getPokemonStats();
        String message = CommandTree.prepareLore(language.getMessagepoolwondertrade(), stats)
          .replace("%total%", String.valueOf(stats.getPokemons().size()));
        PlayerUtils.broadcast(message, language.getPrefix());
      }, 10, intervalBroadcast, TimeUnit.SECONDS);
    }
  }

  private static void cancelTasks() {
    if (playerCheckFuture != null && !playerCheckFuture.isCancelled()) {
      playerCheckFuture.cancel(false);
      playerCheckFuture = null;
    }
    if (autoResetFuture != null && !autoResetFuture.isCancelled()) {
      autoResetFuture.cancel(false);
      autoResetFuture = null;
    }
    if (broadcastFuture != null && !broadcastFuture.isCancelled()) {
      broadcastFuture.cancel(false);
      broadcastFuture = null;
    }
  }
}
