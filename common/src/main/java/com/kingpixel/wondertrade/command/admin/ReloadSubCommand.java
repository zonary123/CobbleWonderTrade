package com.kingpixel.wondertrade.command.admin;

import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.utils.WonderTradeUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

/**
 * Admin subcommand to reload WonderTrade configurations and language files.
 *
 * @author Carlos Varas Alonso
 */
public class ReloadSubCommand {

  private ReloadSubCommand() {}

  public static LiteralArgumentBuilder<ServerCommandSource> register() {
    return CommandManager.literal("reload")
      .requires(source -> PermissionApi.hasPermission(source, List.of(
        CobbleWonderTrade.MOD_ID + ".admin",
        CobbleWonderTrade.MOD_ID + ".reload"
      ), 2))
      .executes(context -> {
        CobbleWonderTrade.load();
        WonderTradeUtils.invalidateStats();
        if (context.getSource().isExecutedByPlayer()) {
          PlayerUtils.sendMessage(
            context.getSource().getPlayer(),
            CobbleWonderTrade.language.getReload(),
            CobbleWonderTrade.language.getPrefix(),
            TypeMessage.CHAT
          );
        }
        return 1;
      });
  }
}
