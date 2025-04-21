package com.kingpixel.wondertrade;

import ca.landonjw.gooeylibs2.api.tasks.Task;
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

import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 28/04/2024 23:50
 */
public class CobbleWonderTrade {
  public static final String MOD_ID = "wondertrade";
  public static final String PATH = "/config/wondertrade/";
  public static final String PATH_DATA = PATH + "data/";
  public static final String PATH_DATA_USER = PATH_DATA + "users/";
  public static Lang language = new Lang();
  public static MinecraftServer server;
  public static Config config = new Config();
  private static Task broadcastTask;
  private static Task autoResetPool;
  private static Task playerCheckTask;

  public static void init() {
    events();
  }

  public static void load() {
    files();
    tasks();
    DatabaseClientFactory.createDatabaseClient(config.getDatabaseConfig());
  }

  private static void events() {
    files();

    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> CommandTree.register(dispatcher));

    LifecycleEvent.SERVER_STARTED.register(server -> load());

    PlayerEvent.PLAYER_JOIN.register(player -> DatabaseClientFactory.databaseClient.getUserInfo(player));

    PlayerEvent.PLAYER_QUIT.register(player -> DatabaseClientFactory.databaseClient.removeIfNecessary(player));

    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());

    LifecycleEvent.SERVER_STOPPING.register((server) -> {
      DatabaseClientFactory.databaseClient.disconnect();
    });

  }

  private static void files() {
    FilterPokemons.removeCache(MOD_ID);
    config.init();
    language.init();
    if (config.getDatabaseConfig().getType() == DataBaseType.JSON) {
      Utils.getAbsolutePath(CobbleWonderTrade.PATH_DATA_USER).mkdirs();
    }
  }

  private static void tasks() {
    if (playerCheckTask != null) playerCheckTask.setExpired();
    if (broadcastTask != null) broadcastTask.setExpired();
    if (autoResetPool != null) autoResetPool.setExpired();

    long intervalBroadcast = 20L * 60 * config.getCooldownBroadcast();
    long intervalAutoReset = 20L * 60 * config.getCooldownReset();
    long intervalPlayerCheck = 20L * 60 * config.getCooldownmessage();

    if (config.getCooldownmessage() > 0) {
      playerCheckTask = Task.builder()
        .execute(() -> {
          CompletableFuture.runAsync(() -> {
            if (config.isDebug()) {
              CobbleUtils.LOGGER.info(MOD_ID, "Checking players");
            }
            var players = server.getPlayerManager().getPlayerList();
            for (ServerPlayerEntity player : players) {
              if (player == null) continue;
              var userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
              if (userInfo == null) continue;
              if (!userInfo.hasCooldown()) {
                PlayerUtils.sendMessage(
                  player,
                  language.getMessagewondertradeready(),
                  language.getPrefix(),
                  TypeMessage.CHAT
                );
              }
            }
          });
        })
        .interval(intervalPlayerCheck)
        .infinite()
        .build();
    }

    if (config.getCooldownReset() > 0 && config.isAutoReset() && !config.isIsrandom()) {
      autoResetPool = Task.builder()
        .execute(() -> {
          if (config.isDebug()) {
            CobbleUtils.LOGGER.info(MOD_ID, "Auto Reset Pool");
          }
          CompletableFuture.runAsync(() -> {
            DatabaseClientFactory.databaseClient.restartPool();
          });
        })
        .delay(intervalAutoReset)
        .interval(intervalAutoReset)
        .infinite()
        .build();
    }

    if (config.getCooldownBroadcast() > 0) {
      broadcastTask = Task.builder()
        .execute(() -> {
          CompletableFuture.runAsync(() -> {
            if (config.isDebug()) {
              CobbleUtils.LOGGER.info(MOD_ID, "Broadcasting Pokemon stats");
            }
            CommandTree.PokemonStats stats =
              CommandTree.calculatePokemonStats(DatabaseClientFactory.databaseClient.getAllPokemons());
            String message = CommandTree.prepareLore(CobbleWonderTrade.language.getMessagepoolwondertrade(), stats)
              .replace("%total%", stats.getPokemons().size() + "");
            PlayerUtils.sendMessage(
              null,
              message,
              language.getPrefix(),
              TypeMessage.BROADCAST
            );
          });
        })
        .interval(intervalBroadcast)
        .infinite()
        .build();
    }
  }
}