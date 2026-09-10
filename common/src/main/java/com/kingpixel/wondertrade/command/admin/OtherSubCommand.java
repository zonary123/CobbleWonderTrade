package com.kingpixel.wondertrade.command.admin;

import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.utils.WonderTradeUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.List;

/**
 * Admin subcommand to open the WonderTrade interface for another player.
 *
 * @author Carlos Varas Alonso
 */
public class OtherSubCommand {

  private OtherSubCommand() {}

  public static LiteralArgumentBuilder<ServerCommandSource> register() {
    return CommandManager.literal("other")
      .requires(source -> PermissionApi.hasPermission(source, List.of(
        CobbleWonderTrade.MOD_ID + ".admin",
        CobbleWonderTrade.MOD_ID + ".other"
      ), 2))
      .then(
        CommandManager.argument("player", EntityArgumentType.players())
          .executes(context -> {
            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
            for (ServerPlayerEntity player : players) {
              WonderTradeUtils.open(player);
            }
            return 1;
          })
      );
  }
}
