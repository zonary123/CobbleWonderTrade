package com.kingpixel.wondertrade.command.admin;

import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.utils.WonderTradeUtils;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

/**
 * Admin subcommand to force reset the WonderTrade Pokémon pool.
 *
 * @author Carlos Varas Alonso
 */
public class RestartPoolSubCommand {

  private RestartPoolSubCommand() {}

  public static LiteralArgumentBuilder<ServerCommandSource> register() {
    return CommandManager.literal("restartPool")
      .requires(source -> PermissionApi.hasPermission(source, List.of(
        CobbleWonderTrade.MOD_ID + ".admin",
        CobbleWonderTrade.MOD_ID + ".restart.pool"
      ), 2))
      .executes(context -> {
        CobbleWonderTrade.ASYNC.runAsync(() -> {
          DatabaseClientFactory.databaseClient.restartPool();
          WonderTradeUtils.invalidateStats();
        });
        return 1;
      });
  }
}
