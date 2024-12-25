package com.kingpixel.wondertrade.command;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Manager.WonderTradePermission;
import com.kingpixel.wondertrade.command.base.CommandWonderTrade;
import com.kingpixel.wondertrade.command.base.CommandWonderTradeOther;
import com.kingpixel.wondertrade.command.base.CommandWonderTradePool;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.gui.WonderTradeConfirm;
import com.kingpixel.wondertrade.gui.WonderTradePC;
import com.kingpixel.wondertrade.model.UserInfo;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 25/05/2024 19:35
 */
public class CommandTree {

  public static void register(
    CommandDispatcher<ServerCommandSource> dispatcher
  ) {
    for (String s : CobbleWonderTrade.config.getAliases()) {
      LiteralArgumentBuilder<ServerCommandSource> base = CommandManager.literal(s)
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION));
      // /wt
      dispatcher.register(
        base.executes(new CommandWonderTrade())
      );

      // /wt other <player>
      dispatcher.register(
        base
          .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_OTHER_PERMISSION))
          .then(CommandManager.literal("other")
            .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_OTHER_PERMISSION))
            .then(
              CommandManager.argument("player", EntityArgumentType.player())
                .executes(new CommandWonderTradeOther())
            ))
      );

      // /wt reload
      dispatcher.register(base
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
        .then(CommandManager.literal("reload")
          .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
          .executes(context -> {
            CobbleWonderTrade.load();
            if (context.getSource().isExecutedByPlayer()) {
              Objects.requireNonNull(context.getSource().getPlayer()).sendMessage(AdventureTranslator.toNative(CobbleWonderTrade.language.getReload().replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
            } else {
              CobbleWonderTrade.LOGGER.info(CobbleWonderTrade.language.getReload().replace("%prefix%",
                CobbleWonderTrade.language.getPrefix()));
            }
            return 1;
          })));

      // /wt pool
      if (CobbleWonderTrade.config.isPoolview()) {
        dispatcher.register(base
          .then(CommandManager.literal("pool")
            .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION))
            .executes(new CommandWonderTradePool())));
      }

      // /wt resetcooldown
      dispatcher.register(base
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
        .then(CommandManager.literal("resetcooldown")
          .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
          .executes(context -> {
            if (!context.getSource().isExecutedByPlayer()) {
              context.getSource().getServer().sendMessage(WonderTradeUtil.toNative("You must be a player to use " +
                "this command"));
              return 0;
            }

            ServerPlayerEntity player = context.getSource().getPlayer();
            if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
              player.sendMessage(WonderTradeUtil.toNative("&cYou can't use this command while in battle!"));
              return 0;
            }
            DatabaseClientFactory.databaseClient.putUserInfo(new UserInfo(player.getUuid()), true);
            return 1;
          })
          .then(CommandManager.argument("player", EntityArgumentType.player())
            .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
            .executes(context -> {
              ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
              if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
                player.sendMessage(WonderTradeUtil.toNative("&cYou can't use this command while in battle!"));
                return 0;
              }
              DatabaseClientFactory.databaseClient.putUserInfo(new UserInfo(player.getUuid()), true);
              return 1;
            })
          )
        )
      );

      // /wt pc
      dispatcher.register(base.then(CommandManager.literal("pc")
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION))
        .executes(context -> {
          ServerPlayerEntity player = context.getSource().getPlayer();
          if (player == null) return 0;
          if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
            player.sendMessage(AdventureTranslator.toNative("&cYou can't use this command while in battle!"));
            return 0;
          }
          try {
            UIManager.openUIForcefully(player, Objects.requireNonNull(WonderTradePC.open(player)));
          } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
          }
          return 1;
        })));

      // /wt <slot>
      dispatcher.register(
        base.then(
          CommandManager.literal("slot")
            .requires(source -> WonderTradePermission.checkPermission(source,
              CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION))
            .then(
              CommandManager.argument("slot", IntegerArgumentType.integer(1, 6))
                .suggests(
                  (context, builder) -> {
                    for (int i = 1; i <= 6; i++) {
                      builder.suggest(String.valueOf(i));
                    }
                    return builder.buildFuture();
                  }
                )
                .then(
                  CommandManager.literal("confirm")
                    .executes(
                      context -> {
                        if (!context.getSource().isExecutedByPlayer()) {
                          context.getSource().getServer().sendMessage(AdventureTranslator.toNative("You must be a player to " +
                            "use this command"));
                          return 0;
                        }
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player == null) return 0;
                        if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
                          player.sendMessage(AdventureTranslator.toNative("&cYou can't use this command while in battle!"));
                          return 0;
                        }
                        Integer slot = IntegerArgumentType.getInteger(context, "slot");
                        slot--;
                        if (slot < 0 || slot > 5) return 0;
                        try {
                          Pokemon pokemon = Cobblemon.INSTANCE.getStorage().getParty(player).get(slot);
                          if (pokemon != null)
                            WonderTradeConfirm.trade(player, pokemon);
                        } catch (Exception e) {
                          e.printStackTrace();
                        }
                        return 1;
                      }
                    )
                )
            )
        )
      );

      // /wt reset
      dispatcher.register(
        base
          .requires(source -> source.hasPermissionLevel(2))
          .then(
            CommandManager.literal("reset")
              .requires(source -> source.hasPermissionLevel(2))
              .executes(context -> {
                DatabaseClientFactory.databaseClient.resetPool(true);
                return 1;
              })
          )
      );

    }
  }
}
