package com.kingpixel.wondertrade;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.DataBaseType;
import com.kingpixel.cobbleutils.Model.FilterPokemons;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.Config.Config;
import com.kingpixel.wondertrade.Config.Lang;
import com.kingpixel.wondertrade.command.CommandTree;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * Optimized version using ScheduledExecutorService to avoid duplicate tasks.
 */
public class CobbleWonderTrade {
  public static final String MOD_ID = "wondertrade";
  public static final String PATH = "/config/wondertrade/";
  public static final String PATH_DATA = PATH + "data/";
  public static final String PATH_DATA_USER = PATH_DATA + "users/";
  public static Lang language = new Lang();
  public static MinecraftServer server;
  public static Config config = new Config();

  public static final Executor EXECUTOR_WONDERTRADE = Executors.newFixedThreadPool(
    2,
    new ThreadFactoryBuilder().setNameFormat("Executor-WonderTrade-%d").setDaemon(true).build()
  );
  private static final ScheduledExecutorService SCHEDULER_WONDERTRADE = Executors.newScheduledThreadPool(
    2,
    new ThreadFactoryBuilder().setNameFormat("Scheduler-WonderTrade-%d").setDaemon(true).build()
  );

  private static ScheduledFuture<?> broadcastFuture;
  private static ScheduledFuture<?> autoResetFuture;
  private static ScheduledFuture<?> playerCheckFuture;

  public static void init() {
    events();
  }

  public static void load() {
    files();
    scheduleTasks();
    DatabaseClientFactory.createDatabaseClient(config.getDatabaseConfig());
  }

  private static void events() {
    files();

    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> CommandTree.register(dispatcher));

    LifecycleEvent.SERVER_STARTED.register(server -> load());

    PlayerEvent.PLAYER_JOIN.register(player -> {
      CompletableFuture.runAsync(() -> DatabaseClientFactory.databaseClient.getUserInfo(player), EXECUTOR_WONDERTRADE)
        .exceptionally(e -> {
          e.printStackTrace();
          return null;
        });
    });

    PlayerEvent.PLAYER_QUIT.register(player -> DatabaseClientFactory.databaseClient.removeIfNecessary(player));

    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());

    LifecycleEvent.SERVER_STOPPING.register((server) -> {
      shutdownScheduler();
      DatabaseClientFactory.databaseClient.disconnect();
    });
  }

  private static void files() {
    FilterPokemons.removeCache(MOD_ID);
    config.init();
    language.init();
    if (config.getDatabaseConfig().getType() == DataBaseType.JSON) {
      Utils.getAbsolutePath(PATH_DATA_USER).mkdirs();
    }
  }

  private static void scheduleTasks() {
    // Cancel previous tasks if they exist
    if (playerCheckFuture != null) playerCheckFuture.cancel(true);
    if (autoResetFuture != null) autoResetFuture.cancel(true);
    if (broadcastFuture != null) broadcastFuture.cancel(true);

    long intervalBroadcast = 60L * config.getCooldownBroadcast();
    long intervalAutoReset = 60L * config.getCooldownReset();
    long intervalPlayerCheck = 60L * config.getCooldownmessage();

    // Player check task
    if (config.getCooldownmessage() > 0) {
      playerCheckFuture = SCHEDULER_WONDERTRADE.scheduleWithFixedDelay(() -> {
        if (config.isDebug()) CobbleUtils.LOGGER.info(MOD_ID, "Checking players");
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
          if (player == null) continue;
          var userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
          if (userInfo != null && !userInfo.hasCooldown()) {
            PlayerUtils.sendMessage(player, language.getMessagewondertradeready(), language.getPrefix(), TypeMessage.CHAT);
          }
        }
      }, 10, intervalPlayerCheck, TimeUnit.SECONDS);
    }

    // Auto reset pool task
    if (config.getCooldownReset() > 0 && config.isAutoReset() && !config.isIsrandom()) {
      autoResetFuture = SCHEDULER_WONDERTRADE.scheduleWithFixedDelay(() -> {
        if (config.isDebug()) CobbleUtils.LOGGER.info(MOD_ID, "Auto Reset Pool");
        if (DatabaseClientFactory.databaseClient.shouldRestartPool()) {
          if (config.isDebug()) CobbleUtils.LOGGER.info(MOD_ID, "Resetting Pool");
          DatabaseClientFactory.databaseClient.restartPool();
        }
      }, 10, intervalAutoReset, TimeUnit.SECONDS);
    }

    // Broadcast task
    if (config.getCooldownBroadcast() > 0) {
      broadcastFuture = SCHEDULER_WONDERTRADE.scheduleWithFixedDelay(() -> {
        if (config.isDebug()) CobbleUtils.LOGGER.info(MOD_ID, "Broadcasting Pokemon stats");
        var stats = CommandTree.calculatePokemonStats(DatabaseClientFactory.databaseClient.getAllPokemons());
        String message = CommandTree.prepareLore(language.getMessagepoolwondertrade(), stats)
          .replace("%total%", String.valueOf(stats.getPokemons().size()));
        PlayerUtils.sendMessage((UUID) null, message, language.getPrefix(), TypeMessage.BROADCAST);
      }, 10, intervalBroadcast, TimeUnit.SECONDS);
    }
  }

  private static void shutdownScheduler() {
    if (!SCHEDULER_WONDERTRADE.isShutdown()) {
      SCHEDULER_WONDERTRADE.shutdownNow();
    }
  }
}
