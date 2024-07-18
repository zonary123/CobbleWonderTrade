package com.kingpixel.wondertrade.command;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Manager.WonderTradeManager;
import com.kingpixel.wondertrade.Manager.WonderTradePermission;
import com.kingpixel.wondertrade.command.base.CommandWonderTrade;
import com.kingpixel.wondertrade.command.base.CommandWonderTradeOther;
import com.kingpixel.wondertrade.command.base.CommandWonderTradePool;
import com.kingpixel.wondertrade.gui.WonderTradeConfirm;
import com.kingpixel.wondertrade.gui.WonderTradePC;
import com.kingpixel.wondertrade.utils.AdventureTranslator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 25/05/2024 19:35
 */
public class CommandTree {

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher
  ) {
    for (String s : CobbleWonderTrade.config.getAliases()) {
      LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal(s)
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION));
      // /wt
      dispatcher.register(
        base.executes(new CommandWonderTrade())
      );

      // /wt other <player>
      dispatcher.register(
        base
          .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_OTHER_PERMISSION))
          .then(Commands.literal("other")
            .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_OTHER_PERMISSION))
            .then(
              Commands.argument("player", EntityArgument.player())
                .executes(new CommandWonderTradeOther())
            ))
      );

      // /wt reload
      dispatcher.register(base
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
        .then(Commands.literal("reload")
          .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
          .executes(context -> {
            CobbleWonderTrade.load();
            if (context.getSource().isPlayer()) {
              Objects.requireNonNull(context.getSource().getPlayer()).sendSystemMessage(AdventureTranslator.toNative(CobbleWonderTrade.language.getReload().replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
            } else {
              CobbleWonderTrade.LOGGER.info(CobbleWonderTrade.language.getReload().replace("%prefix%",
                CobbleWonderTrade.language.getPrefix()));
            }
            return 1;
          })));

      // /wt pool
      if (CobbleWonderTrade.config.isPoolview()) {
        dispatcher.register(base
          .then(Commands.literal("pool")
            .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION))
            .executes(new CommandWonderTradePool())));
      }

      // /wt resetcooldown
      dispatcher.register(base
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
        .then(Commands.literal("resetcooldown")
          .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
          .executes(context -> {
            if (!context.getSource().isPlayer()) {
              context.getSource().getServer().sendSystemMessage(AdventureTranslator.toNative("You must be a player to use this command"));
              return 0;
            }

            ServerPlayer player = context.getSource().getPlayer();
            if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
              player.sendSystemMessage(AdventureTranslator.toNative("&cYou can't use this command while in battle!"));
              return 0;
            }
            CobbleWonderTrade.manager.getUserInfo().put(player.getUUID(), new WonderTradeManager.UserInfo(new Date()));
            return 1;
          })
          .then(Commands.argument("player", EntityArgument.player())
            .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
            .executes(context -> {
              ServerPlayer player = EntityArgument.getPlayer(context, "player");
              if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
                player.sendSystemMessage(AdventureTranslator.toNative("&cYou can't use this command while in battle!"));
                return 0;
              }
              CobbleWonderTrade.manager.getUserInfo().put(player.getUUID(), new WonderTradeManager.UserInfo(new Date()));
              return 1;
            })
          )
        )
      );

      // /wt pc
      dispatcher.register(base.then(Commands.literal("pc")
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION))
        .executes(context -> {
          ServerPlayer player = context.getSource().getPlayerOrException();
          if (player == null) return 0;
          if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
            player.sendSystemMessage(AdventureTranslator.toNative("&cYou can't use this command while in battle!"));
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
          Commands.literal("slot")
            .requires(source -> WonderTradePermission.checkPermission(source,
              CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION))
            .then(
              Commands.argument("slot", IntegerArgumentType.integer(1, 6))
                .suggests(
                  (context, builder) -> {
                    for (int i = 1; i <= 6; i++) {
                      builder.suggest(String.valueOf(i));
                    }
                    return builder.buildFuture();
                  }
                )
                .then(
                  Commands.literal("confirm")
                    .executes(
                      context -> {
                        if (!context.getSource().isPlayer()) {
                          context.getSource().getServer().sendSystemMessage(AdventureTranslator.toNative("You must be a player to use this command"));
                          return 0;
                        }
                        ServerPlayer player = context.getSource().getPlayer();
                        if (player == null) return 0;
                        if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
                          player.sendSystemMessage(AdventureTranslator.toNative("&cYou can't use this command while in battle!"));
                          return 0;
                        }
                        Integer slot = IntegerArgumentType.getInteger(context, "slot");
                        slot--;
                        if (slot < 0 || slot > 5) return 0;
                        try {
                          Pokemon pokemon = Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).get(slot);
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
          .requires(source -> source.hasPermission(2))
          .then(
            Commands.literal("reset")
              .requires(source -> source.hasPermission(2))
              .executes(context -> {
                CobbleWonderTrade.manager.generatePokemonList();
                return 1;
              })
          )
      );
    }
  }
}
