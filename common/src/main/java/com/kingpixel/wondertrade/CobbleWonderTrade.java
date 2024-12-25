package com.kingpixel.wondertrade;

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
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private static final List<ScheduledFuture<?>> tasks = new ArrayList<>();

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
      tasks.forEach(task -> task.cancel(true));
      tasks.clear();
      DatabaseClientFactory.databaseClient.disconnect();
      scheduler.shutdown();
      try {
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
          scheduler.shutdownNow();
        }
      } catch (InterruptedException ex) {
        scheduler.shutdownNow();
      }
      LOGGER.info("Stopping " + MOD_NAME);
    });

  }

  private static void files() {
    config.init();
    language.init();
  }

  private static void tasks() {
    for (ScheduledFuture<?> task : tasks) {
      if (task != null && !task.isCancelled()) {
        task.cancel(false);
      }
    }
    tasks.clear();

    ScheduledFuture<?> broadcastTask = scheduler.scheduleAtFixedRate(() -> {
      if (server != null) {
        List<Pokemon> pokemons = new ArrayList<>();
        DatabaseClientFactory.databaseClient.getPokemonList(false).forEach(pokemon -> pokemons.add(Pokemon.Companion.loadFromJSON(pokemon)));
        WonderTradeUtil.messagePool(pokemons);
      }
    }, CobbleWonderTrade.config.getCooldownmessage(), CobbleWonderTrade.config.getCooldownmessage(), TimeUnit.MINUTES);
    tasks.add(broadcastTask);

    ScheduledFuture<?> autoResetPool = scheduler.scheduleAtFixedRate(() -> {
      if (CobbleWonderTrade.config.isAutoReset()) {
        if (!PlayerUtils.isCooldown(DatabaseClientFactory.cooldown))
          DatabaseClientFactory.databaseClient.resetPool(true);
      }
    }, 0, 1, TimeUnit.MINUTES);
    tasks.add(autoResetPool);

    ScheduledFuture<?> playerCheckTask = scheduler.scheduleAtFixedRate(() -> {
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
    }, 0, 30, TimeUnit.SECONDS);
    tasks.add(playerCheckTask);
  }
}