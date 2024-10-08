package com.kingpixel.wondertrade;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.wondertrade.Config.Config;
import com.kingpixel.wondertrade.Config.Lang;
import com.kingpixel.wondertrade.Manager.WonderTradeConfig;
import com.kingpixel.wondertrade.Manager.WonderTradeManager;
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
import java.util.concurrent.*;

/**
 * @author Carlos Varas Alonso - 28/04/2024 23:50
 */
public class CobbleWonderTrade {
  public static final String MOD_ID = "wondertrade";
  public static final Logger LOGGER = LogManager.getLogger();
  public static final String MOD_NAME = "CobbleWonderTrade";
  public static final String PATH = "/config/wondertrade/";
  public static final String PATH_DATA = PATH + "data/";
  public static Lang language = new Lang();
  public static MinecraftServer server;
  public static Config config = new Config();
  public static WonderTradeManager manager = new WonderTradeManager();
  public static WonderTradeConfig dexpermission = new WonderTradeConfig();
  public static WonderTradePermission permissions = new WonderTradePermission();
  public static SpawnRates spawnRates = new SpawnRates();
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private static final List<ScheduledFuture<?>> tasks = new ArrayList<>();


  public static void init() {
    LOGGER.info("Initializing " + MOD_NAME);
    events();
  }

  public static void load() {
    files();
    manager.init();
    spawnRates.init();
    WonderTradeUtil.init();
    tasks();
    DatabaseClientFactory.databaseClient.resetPool(false);
  }

  private static void events() {
    files();

    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> CommandTree.register(dispatcher));

    LifecycleEvent.SERVER_STARTED.register(server -> load());

    PlayerEvent.PLAYER_JOIN.register(player -> manager.addPlayer(player));

    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getLevel().getServer());

    LifecycleEvent.SERVER_STOPPING.register((server) -> {
      tasks.forEach(task -> task.cancel(true));
      tasks.clear();
      DatabaseClientFactory.databaseClient.disconnect();
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
        try {
          DatabaseClientFactory.databaseClient.getPokemonList(false).get().forEach(pokemon -> {
            pokemons.add(Pokemon.Companion.loadFromJSON(pokemon));
          });
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
        WonderTradeUtil.messagePool(pokemons);
      }
    }, CobbleWonderTrade.config.getCooldownmessage(), CobbleWonderTrade.config.getCooldownmessage(), TimeUnit.MINUTES);
    tasks.add(broadcastTask);

    ScheduledFuture<?> playerCheckTask = scheduler.scheduleAtFixedRate(() -> {
      if (server != null) {
        server.getPlayerList().getPlayers().forEach(player -> {
          UserInfo userInfo;
          try {
            userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player).get();
            if (manager.hasCooldownEnded(player) && !userInfo.isMessagesend()) {
              userInfo.setMessagesend(true);
              DatabaseClientFactory.databaseClient.putUserInfo(userInfo, true);
              player.sendSystemMessage(AdventureTranslator.toNative(language.getMessagewondertradeready()
                .replace("%prefix%",
                  language.getPrefix()
                )));
            }
          } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
      }
    }, 0, 30, TimeUnit.SECONDS);
    tasks.add(playerCheckTask);
  }
}