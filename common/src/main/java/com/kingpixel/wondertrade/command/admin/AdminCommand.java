package com.kingpixel.wondertrade.command.admin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Aggregator for all WonderTrade administrative subcommands.
 *
 * @author Carlos Varas Alonso
 */
public class AdminCommand {

  private AdminCommand() {}

  public static void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
    builder
      .then(ReloadSubCommand.register())
      .then(OtherSubCommand.register())
      .then(RestartPoolSubCommand.register())
      .then(RestartUserSubCommand.register());
  }
}
