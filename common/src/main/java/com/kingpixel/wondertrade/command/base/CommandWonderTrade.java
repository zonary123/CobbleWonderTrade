package com.kingpixel.wondertrade.command.base;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.gui.WonderTrade;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 26/05/2024 4:33
 */
public class CommandWonderTrade implements Command<CommandSourceStack> {

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
    try {
      UIManager.openUIForcefully(Objects.requireNonNull(CobbleWonderTrade.server.getPlayerList().getPlayer(player.getUUID())), Objects.requireNonNull(WonderTrade.open(player)));
    } catch (NoPokemonStoreException e) {
      throw new RuntimeException(e);
    }
    return 1;
  }
}
