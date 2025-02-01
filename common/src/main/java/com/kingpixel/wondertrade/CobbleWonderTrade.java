package com.kingpixel.wondertrade;

import ca.landonjw.gooeylibs2.api.tasks.Task;
import club.minnced.discord.webhook.WebhookClient;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.wondertrade.Config.Config;
import com.kingpixel.wondertrade.Config.Lang;
import com.kingpixel.wondertrade.Manager.WonderTradeConfig;
import com.kingpixel.wondertrade.Manager.WonderTradePermission;
import com.kingpixel.wondertrade.command.CommandTree;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.model.UserInfo;
import com.kingpixel.wondertrade.utils.SpawnRates;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 28/04/2024 23:50
 */
public class CobbleWonderTrade {
  public static final String MOD_ID = "wondertrade";
  public static final Logger LOGGER = LogManager.getLogger();
  public static final String MOD_NAME = "CobbleWonderTrade";
  public static final String PATH = "/config/wondertrade/";
  public static final String PATH_DATA = PATH + "data/";
  public static final String PATH_DATA_USER = PATH_DATA + "users/";
  public static Lang language = new Lang();
  public static MinecraftServer server;
  public static Config config = new Config();
  public static WonderTradeConfig dexpermission = new WonderTradeConfig();
  public static WonderTradePermission permissions = new WonderTradePermission();
  public static SpawnRates spawnRates = new SpawnRates();
  public static WebhookClient webhookClient;
  private static Task broadcastTask;
  private static Task autoResetPool;
  private static Task playerCheckTask;

  public static void init() {
    LOGGER.info("Initializing " + MOD_NAME);
    events();
  }

  public static void load() {
    files();
    spawnRates.init();
    tasks();
    DatabaseClientFactory.createDatabaseClient(config.getDatabaseType(), config.getDatabaseConfig().getUrl(),
      config.getDatabaseConfig().getDatabase(), config.getDatabaseConfig().getUser()
      , config.getDatabaseConfig().getPassword());
    DatabaseClientFactory.databaseClient.resetPool(false);
    if (config.getDiscord_webhook().isENABLED()){
      try {
        webhookClient = WebhookClient.withUrl(config.getDiscord_webhook().getURL_WEBHOOK());
      } catch (Exception e) {
        LOGGER.error("Error loading webhook: {}", e.getMessage());
      }
    }
  }

  private static void events() {
    files();


    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> CommandTree.register(dispatcher));

    LifecycleEvent.SERVER_STARTED.register(server -> load());

    PlayerEvent.PLAYER_JOIN.register(player -> DatabaseClientFactory.databaseClient.getUserInfo(player));

    PlayerEvent.PLAYER_QUIT.register(player -> DatabaseClientFactory.databaseClient.getUserInfo(player));

    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());

    LifecycleEvent.SERVER_STOPPING.register((server) -> {
      DatabaseClientFactory.databaseClient.disconnect();
      LOGGER.info("Stopping " + MOD_NAME);
    });

  }

  private static void files() {
    config.init();
    language.init();
  }

  private static void tasks() {

    if (broadcastTask != null) broadcastTask.setExpired();

    broadcastTask = Task.builder().execute(() -> {
      if (server != null) {
        List<Pokemon> pokemons = new ArrayList<>();
        DatabaseClientFactory.databaseClient.getPokemonList(false).forEach(pokemon -> pokemons.add(Pokemon.Companion.loadFromJSON(DynamicRegistryManager.EMPTY,pokemon)));
        WonderTradeUtil.messagePool(pokemons);
      }
    })
      .infinite()
      .interval(20L * 60 * config.getCooldownmessage())
      .build();


    if (autoResetPool != null) autoResetPool.setExpired();
    autoResetPool = Task.builder().execute(() -> {
      if (config.isAutoReset()) {
        if (!PlayerUtils.isCooldown(DatabaseClientFactory.cooldown))
          DatabaseClientFactory.databaseClient.resetPool(true);
      }
    })
      .infinite()
      .interval(20L * 60 * config.getCooldownReset())
      .build();

    if (playerCheckTask != null) playerCheckTask.setExpired();
    playerCheckTask = Task.builder().execute(() -> {
      if (server != null) {
        server.getPlayerManager().getPlayerList().forEach(player -> {
          UserInfo userInfo;

          userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
          if (!PlayerUtils.isCooldown(userInfo.getDate()) && !userInfo.isMessagesend()) {
            userInfo.setMessagesend(true);
            DatabaseClientFactory.databaseClient.putUserInfo(userInfo, true);
            player.sendMessage(AdventureTranslator.toNative(language.getMessagewondertradeready()
              .replace("%prefix%",
                language.getPrefix()
              )));
          }

        });
      }
    })
      .infinite()
      .interval(20L * 30)
      .build();
  }
}