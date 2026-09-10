package com.kingpixel.wondertrade.command.base;

import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.utils.WonderTradeUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

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
        WonderTradeUtils.open(player);
        return 1;
      });
  }
}
