package com.kingpixel.wondertrade.command;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.ui.PartyPcMenu;
import com.kingpixel.cobbleutils.util.*;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.model.UserInfo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Data;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 25/05/2024 19:35
 */
public class CommandTree {

  public static void register(
    CommandDispatcher<ServerCommandSource> dispatcher
  ) {
    for (String command : CobbleWonderTrade.config.getCommands()) {
      var base = CommandManager.literal(command);
      dispatcher.register(
        base
          .requires(source -> PermissionApi.hasPermission(source, List.of(CobbleWonderTrade.MOD_ID + ".user",
            CobbleWonderTrade.MOD_ID + ".admin"), 2))
          .executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayer();
            open(player);
            return 1;
          }).then(
            CommandManager.literal("reload")
              .requires(source -> PermissionApi.hasPermission(source, List.of(CobbleWonderTrade.MOD_ID + ".admin",
                  CobbleWonderTrade.MOD_ID + ".reload"),
                2))
              .executes(context -> {
                CobbleWonderTrade.load();
                return 1;
              })
          ).then(
            CommandManager.literal("other")
              .requires(source -> PermissionApi.hasPermission(source, List.of(CobbleWonderTrade.MOD_ID + ".admin",
                  CobbleWonderTrade.MOD_ID + ".other"),
                2))
              .then(
                CommandManager.argument("player", EntityArgumentType.players())
                  .executes(context -> {
                    var players = EntityArgumentType.getPlayers(context, "player");
                    for (ServerPlayerEntity player : players) open(player);
                    return 1;
                  })
              )
          ).then(
            CommandManager.literal("restartPool")
              .requires(source -> PermissionApi.hasPermission(source, List.of(CobbleWonderTrade.MOD_ID + ".admin",
                  CobbleWonderTrade.MOD_ID + ".restart.pool"),
                2))
              .executes(context -> {
                CompletableFuture.runAsync(() -> {
                    DatabaseClientFactory.databaseClient.restartPool();
                  })
                  .orTimeout(5, TimeUnit.SECONDS)
                  .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                  });
                return 1;
              })
          ).then(
            CommandManager.literal("restartUser")
              .requires(source -> PermissionApi.hasPermission(source, List.of(CobbleWonderTrade.MOD_ID + ".admin",
                  CobbleWonderTrade.MOD_ID + ".restart.user"),
                2))
              .then(
                CommandManager.argument("players", EntityArgumentType.players())
                  .executes(context -> {
                    CompletableFuture.runAsync(() -> {
                        Collection<ServerPlayerEntity> players = null;
                        try {
                          players = EntityArgumentType.getPlayers(context, "players");
                        } catch (CommandSyntaxException e) {
                          e.printStackTrace();
                        }
                        for (ServerPlayerEntity player : players) {
                          var userinfo = new UserInfo(player);
                          DatabaseClientFactory.userInfoMap.put(player.getUuid(), userinfo);
                          DatabaseClientFactory.databaseClient.updateUserInfo(player, userinfo);
                        }
                      })
                      .orTimeout(5, TimeUnit.SECONDS)
                      .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                      });
                    return 1;
                  })
              )
          )
      );
    }
  }

  public static void open(ServerPlayerEntity player) {
    if (player == null) return;
    var battle = Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player);
    if (battle != null) return;

    var build = PartyPcMenu.builder()
      .setPlayer(player)
      .setTemplateConsumer(template -> {
        if (CobbleWonderTrade.config.isIsrandom()) return;
        long currentTime = System.currentTimeMillis();
        ItemModel itemModelInfo = CobbleWonderTrade.language.getInfo();
        if (UIUtils.isInside(itemModelInfo, CobbleWonderTrade.language.getPartyPcMenu().getRowsParty()) && CobbleWonderTrade.config.isPoolview()) {
          var pokemons = DatabaseClientFactory.databaseClient.getAllPokemons();
          var userinfo = DatabaseClientFactory.databaseClient.getUserInfo(player);

          // Contar estadísticas de Pokémon
          var stats = calculatePokemonStats(pokemons);

          // Preparar lore con reemplazos
          List<String> lore = prepareLore(CobbleWonderTrade.language.getInfo().getLore(), stats, userinfo);

          // Crear botón
          GooeyButton button = itemModelInfo.getButton(1, itemModelInfo.getDisplayname(), lore,
            action -> {
              List<Pokemon> list;
              switch (action.getClickType()) {
                case RIGHT_CLICK, SHIFT_RIGHT_CLICK -> list = stats.getSpecial();
                default -> list = stats.getPokemons();
              }
              CobbleWonderTrade.language.getPool().open(player, list);
            });
          itemModelInfo.applyTemplate((ChestTemplate) template, button);
        }
        long time = System.currentTimeMillis() - currentTime;
        if (CobbleWonderTrade.config.isDebug())
          CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, " Info Time: " + time + "ms");
      })
      .setPokemonAction(action -> handlePokemonAction(player, action.getPokemon()))
      .setPartyPcMenu(CobbleWonderTrade.language.getPartyPcMenu())
      .setConfirmMenu(CobbleWonderTrade.language.getConfirmMenu())
      .setCloseAction(close -> {
        open(player);
      })
      .setBlackList(CobbleWonderTrade.config.getBlackList())
      .build();

    build.getPartyPcMenu().openParty(build);
  }

  public static PokemonStats calculatePokemonStats(List<Pokemon> pokemons) {
    int shinys = 0, legendaries = 0, ultraBeasts = 0, paradoxes = 0, ivs31 = 0;
    List<Pokemon> special = new ArrayList<>();

    for (Pokemon pokemon : pokemons) {
      boolean isSpecial = false;
      if (pokemon.getShiny()) {
        shinys++;
        isSpecial = true;
      }
      if (pokemon.isLegendary()) {
        legendaries++;
        isSpecial = true;
      }
      if (pokemon.isUltraBeast()) {
        ultraBeasts++;
        isSpecial = true;
      }
      if (pokemon.getForm().getLabels().contains(CobblemonPokemonLabels.PARADOX)) {
        paradoxes++;
        isSpecial = true;
      }
      if (PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31) {
        ivs31++;
        isSpecial = true;
      }
      if (isSpecial) {
        special.add(pokemon);
      }
    }

    return new PokemonStats(shinys, legendaries, ultraBeasts, paradoxes, ivs31, special, pokemons);
  }

  public static List<String> prepareLore(List<String> loreTemplate, PokemonStats stats, UserInfo userinfo) {
    String cooldown;
    if (userinfo != null) {
      cooldown = PlayerUtils.getCooldown(userinfo.getDate());
    } else {
      cooldown = "";
    }
    return loreTemplate.stream()
      .map(s ->
        prepareLore(s, stats)
          .replace("%cooldown%", cooldown)
          .replace("%time%", cooldown))
      .toList();
  }

  public static String prepareLore(String s, PokemonStats stats) {
    return s
      .replace("%shinys%", String.valueOf(stats.shinys))
      .replace("%legends%", String.valueOf(stats.legendaries))
      .replace("%ultrabeast%", String.valueOf(stats.ultraBeasts))
      .replace("%paradox%", String.valueOf(stats.paradoxes))
      .replace("%ivs%", String.valueOf(stats.ivs31));
  }

  private static void handlePokemonAction(ServerPlayerEntity player, Pokemon pokemon) {
    CompletableFuture.runAsync(() -> {
        long currentTime = System.currentTimeMillis();
        UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);

        if (userInfo.hasCooldown()) {
          sendCooldownMessage(player, userInfo);
          UIManager.closeUI(player);
          return;
        }

        if (pokemon.getLevel() < CobbleWonderTrade.config.getMinlvreq()) {
          sendMinLevelMessage(player, pokemon);
          return;
        }

        UIManager.closeUI(player);
        userInfo.setCooldown(player);
        DatabaseClientFactory.databaseClient.updateUserInfo(player, userInfo);
        Pokemon pokemonObtained;
        if (!CobbleWonderTrade.config.isIsrandom()) {
          pokemonObtained = DatabaseClientFactory.databaseClient.tradePokemon(player, pokemon);
          if (DatabaseClientFactory.databaseClient.shouldRestartPool()) {
            if (CobbleWonderTrade.config.isDebug()) {
              CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Resetting Pool");
            }
            DatabaseClientFactory.databaseClient.restartPool();
          }
        } else {
          pokemonObtained = CobbleWonderTrade.config.getFilterGenerationPokemon().generateRandomPokemon(
            CobbleWonderTrade.MOD_ID,
            "pool");

          int legendary = Utils.RANDOM.nextInt(CobbleWonderTrade.config.getLegendaryrate());
          int shiny = Utils.RANDOM.nextInt(CobbleWonderTrade.config.getShinyrate());
          if (legendary == 0 && !pokemonObtained.getForm().getLabels().contains(CobblemonPokemonLabels.LEGENDARY)) {
            pokemonObtained = DatabaseClientFactory.getLegendary();
          }
          if (shiny == 0) {
            pokemonObtained.setShiny(true);
          }
          if (CobbleWonderTrade.config.isDebug()) {
            CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Legendary: " + legendary);
            CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Shiny: " + shiny);
          }
          DatabaseClientFactory.setLevel(pokemonObtained);
        }
        updatePlayerStorage(player, pokemon, pokemonObtained);
        long time = System.currentTimeMillis() - currentTime;
        if (CobbleWonderTrade.config.isDebug()) CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Time: " + time + "ms");
      })
      .orTimeout(5, TimeUnit.SECONDS)
      .exceptionally(e -> {
        e.printStackTrace();
        return null;
      });
  }

  private static void sendCooldownMessage(ServerPlayerEntity player, UserInfo userInfo) {
    String cooldown = PlayerUtils.getCooldown(userInfo.getDate());
    PlayerUtils.sendMessage(
      player,
      CobbleWonderTrade.language.getMessagewondertradecooldown()
        .replace("%cooldown%", cooldown)
        .replace("%time%", cooldown),
      CobbleWonderTrade.language.getPrefix(),
      TypeMessage.CHAT
    );
  }

  private static void sendMinLevelMessage(ServerPlayerEntity player, Pokemon pokemon) {
    PlayerUtils.sendMessage(
      player,
      PokemonUtils.replace(CobbleWonderTrade.language.getMessageThePokemonNotHaveMinLevel(), pokemon)
        .replace("%minlevel%", String.valueOf(CobbleWonderTrade.config.getMinlvreq())),
      CobbleWonderTrade.language.getPrefix(),
      TypeMessage.CHAT
    );
  }

  private static void updatePlayerStorage(ServerPlayerEntity player, Pokemon oldPokemon, Pokemon newPokemon) {
    PlayerUtils.sendMessage(
      player,
      PokemonUtils.replace(CobbleWonderTrade.language.getMessagePokemonToWondertrade(), oldPokemon)
        .replace("%player%", player.getGameProfile().getName()),
      CobbleWonderTrade.language.getPrefix(),
      TypeMessage.BROADCAST
    );

    PlayerUtils.sendMessage(
      player,
      PokemonUtils.replace(CobbleWonderTrade.language.getMessagewondertraderecieved(), newPokemon)
        .replace("%player%", player.getGameProfile().getName()),
      CobbleWonderTrade.language.getPrefix(),
      TypeMessage.CHAT
    );

    sendWebHook(oldPokemon, newPokemon, player);
    List<Pokemon> animations;
    if (CobbleWonderTrade.config.isIsrandom()) {
      animations = DatabaseClientFactory.databaseClient.getPokemonsAnimation();
    } else {
      animations =
        CobbleWonderTrade.config.getFilterGenerationPokemon().generateRandomPokemons(CobbleWonderTrade.MOD_ID, "pool"
          , DatabaseClientFactory.POKEMON_ANIMATION_SIZE);
    }

    var tintNotObtained = new Vector4f(0.5f, 0.5f, 0.5f, 1);
    var animationsItems = animations.stream()
      .map(pokemon -> PokemonItem.from(pokemon, 1, tintNotObtained))
      .toList();

    var tintObtained = new Vector4f(1, 1, 1, 1);
    List<ItemStack> pokemonItem = new ArrayList<>();
    pokemonItem.add(PokemonItem.from(newPokemon, 1, tintObtained));
    AdvancedItemChance.initAnimation(
      CobbleWonderTrade.config.getAnimation(),
      player,
      animationsItems,
      pokemonItem
    );
    var party = Cobblemon.INSTANCE.getStorage().getParty(player);
    var pc = Cobblemon.INSTANCE.getStorage().getPC(player);

    if (!party.remove(oldPokemon)) pc.remove(oldPokemon);
    party.add(newPokemon);
  }

  private static void sendWebHook(Pokemon oldPokemon, Pokemon newPokemon, ServerPlayerEntity player) {
    if (!CobbleWonderTrade.config.getDiscord_webhook().isENABLED()) return;
    var webHook = CobbleWonderTrade.config.getDiscord_webhook();
    boolean specialPut = oldPokemon.getShiny() | CobbleWonderTrade.config.getSpecialPokemons().isBlackListed(oldPokemon);
    if (!specialPut) {
      webHook.sendWebHook(CobbleWonderTrade.MOD_ID, CobbleWonderTrade.language.getWebHookPutPool(), List.of(player), List.of(oldPokemon));
    } else {
      webHook.sendWebHook(CobbleWonderTrade.MOD_ID, CobbleWonderTrade.language.getWebHookSpecialPutPool(), List.of(player), List.of(oldPokemon));
    }
    boolean specialObtained = newPokemon.getShiny() | CobbleWonderTrade.config.getSpecialPokemons().isBlackListed(newPokemon);
    if (!specialObtained) {
      webHook.sendWebHook(CobbleWonderTrade.MOD_ID, CobbleWonderTrade.language.getWebHookObtainedPool(), List.of(player), List.of(newPokemon));
    } else {
      webHook.sendWebHook(CobbleWonderTrade.MOD_ID, CobbleWonderTrade.language.getWebHookSpecialObtainedPool(), List.of(player), List.of(newPokemon));
    }
  }

  // Clase auxiliar para estadísticas de Pokémon
  @Data
  public static class PokemonStats {
    int shinys, legendaries, ultraBeasts, paradoxes, ivs31;
    private List<Pokemon> special;
    private List<Pokemon> pokemons;

    public PokemonStats(int shinys, int legendaries, int ultraBeasts, int paradoxes, int ivs31, List<Pokemon> special
      , List<Pokemon> pokemons) {
      this.shinys = shinys;
      this.legendaries = legendaries;
      this.ultraBeasts = ultraBeasts;
      this.paradoxes = paradoxes;
      this.ivs31 = ivs31;
      this.special = special;
      this.pokemons = pokemons;
    }
  }
}
