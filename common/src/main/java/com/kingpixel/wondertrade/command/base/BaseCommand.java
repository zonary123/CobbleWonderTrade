package com.kingpixel.wondertrade.command.base;

import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.api.WonderTradeApi;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Base command registration for WonderTrade (/wt, /wondertrade).
 *
 * @author Carlos Varas Alonso
 */
public class BaseCommand {

  private BaseCommand() {}

  public static LiteralArgumentBuilder<ServerCommandSource> register(String command) {
    return CommandManager.literal(command)
      .requires(source -> PermissionApi.hasPermission(source, List.of(
        CobbleWonderTrade.MOD_ID + ".user",
        CobbleWonderTrade.MOD_ID + ".admin"
      ), 2))
      .executes(context -> {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
          context.getSource().sendFeedback(() -> Text.literal("This command can only be executed by players in-game."), false);
          return 0;
        }
        WonderTradeApi.openTradeMenu(player);
        return 1;
      });
  }
}
