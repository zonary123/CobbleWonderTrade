package com.kingpixel.wondertrade.command;

import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Manager.WonderTradePermission;
import com.kingpixel.wondertrade.command.base.CommandWonderTrade;
import com.kingpixel.wondertrade.command.base.CommandWonderTradeOther;
import com.kingpixel.wondertrade.command.base.CommandWonderTradePool;
import com.kingpixel.wondertrade.gui.WonderTradeConfirm;
import com.kingpixel.wondertrade.utils.AdventureTranslator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

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
        base.then(Commands.literal("other")
          .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_OTHER_PERMISSION))
          .then(
            Commands.argument("player", EntityArgument.player())
              .executes(new CommandWonderTradeOther())
          ))
      );

      // /wt reload
      dispatcher.register(base.then(Commands.literal("reload")
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_RELOAD_PERMISSION))
        .executes(context -> {
          CobbleWonderTrade.load();
          Objects.requireNonNull(context.getSource().getPlayer()).sendSystemMessage(AdventureTranslator.toNative(CobbleWonderTrade.language.getReload().replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
          return 1;
        })));

      // /wt pool
      dispatcher.register(base.then(Commands.literal("pool")
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION))
        .executes(new CommandWonderTradePool())));

      // /wt <slot>
      dispatcher.register(
        base.then(
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
                    Player player = context.getSource().getPlayer();
                    if (player == null) return 0;
                    Integer slot = IntegerArgumentType.getInteger(context, "slot");
                    slot--;
                    if (slot < 0 || slot > 5) return 0;
                    try {
                      WonderTradeConfirm.trade(player, slot);
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                    return 1;
                  }
                )
            )
        )
      );
    }
  }
}
