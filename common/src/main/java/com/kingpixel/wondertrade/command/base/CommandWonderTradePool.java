package com.kingpixel.wondertrade.command.base;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.Cobblemon;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.gui.WonderTradePool;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 02/06/2024 2:18
 */
public class CommandWonderTradePool implements Command<CommandSourceStack> {

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    if (!context.getSource().isPlayer()) {
      context.getSource().getServer().sendSystemMessage(WonderTradeUtil.toNative("You must be a player to use " +
        "this command"));
      return 0;
    }
    ServerPlayer player = context.getSource().getPlayer();
    if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
      player.sendSystemMessage(WonderTradeUtil.toNative("&cYou can't use this command while in battle!"));
      return 0;
    }
    if (CobbleWonderTrade.config.isPoolview()) {
      try {
        UIManager.openUIForcefully(Objects.requireNonNull(CobbleWonderTrade.server.getPlayerList().getPlayer(player.getUUID())), WonderTradePool.open(false));
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    } else {
      player.sendSystemMessage(WonderTradeUtil.toNative(CobbleWonderTrade.language.getMessageNoPoolView()
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
    }
    return 1;
  }
}