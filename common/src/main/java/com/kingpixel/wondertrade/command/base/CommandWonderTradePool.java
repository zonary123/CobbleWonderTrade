package com.kingpixel.wondertrade.command.base;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.Cobblemon;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.gui.WonderTradePool;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 02/06/2024 2:18
 */
public class CommandWonderTradePool implements Command<ServerCommandSource> {

  @Override public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    if (!context.getSource().isExecutedByPlayer()) {
      context.getSource().getServer().sendMessage(WonderTradeUtil.toNative("You must be a player to use " +
        "this command"));
      return 0;
    }
    ServerPlayerEntity player = context.getSource().getPlayer();
    if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
      player.sendMessage(WonderTradeUtil.toNative("&cYou can't use this command while in battle!"));
      return 0;
    }
    if (CobbleWonderTrade.config.isPoolview()) {
      try {
        UIManager.openUIForcefully(Objects.requireNonNull(CobbleWonderTrade.server.getPlayerManager().getPlayer(player.getUuid())),
          WonderTradePool.open(false));
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    } else {
      player.sendMessage(WonderTradeUtil.toNative(CobbleWonderTrade.language.getMessageNoPoolView()
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
    }
    return 1;
  }
}