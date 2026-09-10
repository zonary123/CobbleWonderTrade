package com.kingpixel.wondertrade.command;

import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.admin.AdminCommand;
import com.kingpixel.wondertrade.command.base.BaseCommand;
import com.kingpixel.wondertrade.command.utils.PokemonStats;
import com.kingpixel.wondertrade.command.utils.WonderTradeUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Root command coordinator for WonderTrade.
 * Dispatches root base commands and attaches administrative subcommand trees.
 *
 * @author Carlos Varas Alonso
 */
public class CommandTree {

  private CommandTree() {}

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    for (String command : CobbleWonderTrade.config.getCommands()) {
      LiteralArgumentBuilder<ServerCommandSource> baseBuilder = BaseCommand.register(command);
      AdminCommand.register(baseBuilder);
      dispatcher.register(baseBuilder);
    }
  }

  public static PokemonStats getPokemonStats() {
    return WonderTradeUtils.getPokemonStats();
  }

  public static void invalidateStats() {
    WonderTradeUtils.invalidateStats();
  }

  public static void open(ServerPlayerEntity player) {
    WonderTradeUtils.open(player);
  }

  public static String prepareLore(String s, PokemonStats stats) {
    return WonderTradeUtils.prepareLore(s, stats);
  }
}
