package com.kingpixel.wondertrade.command;

import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.Manager.WonderTradePermission;
import com.kingpixel.wondertrade.command.base.CommandWonderTrade;
import com.kingpixel.wondertrade.command.base.CommandWonderTradeOther;
import com.kingpixel.wondertrade.command.base.CommandWonderTradePool;
import com.kingpixel.wondertrade.utils.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

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
          Objects.requireNonNull(context.getSource().getPlayer()).sendSystemMessage(TextUtil.parseHexCodes(CobbleWonderTrade.language.getReload().replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
          return 1;
        })));

      // /wt pool
      dispatcher.register(base.then(Commands.literal("pool")
        .requires(source -> WonderTradePermission.checkPermission(source, CobbleWonderTrade.permissions.WONDERTRADE_BASE_PERMISSION))
        .executes(new CommandWonderTradePool())));
    }
  }
}
