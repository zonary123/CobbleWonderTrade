package com.kingpixel.wondertrade.command.base;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.gui.WonderTradePool;
import com.kingpixel.wondertrade.utils.AdventureTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 02/06/2024 2:18
 */
public class CommandWonderTradePool implements Command<CommandSourceStack> {

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    if (!context.getSource().isPlayer()) {
      context.getSource().getServer().sendSystemMessage(AdventureTranslator.toNative("You must be a player to use " +
        "this command"));
      return 0;
    }
    Player player = context.getSource().getPlayer();
    if (CobbleWonderTrade.config.isPoolview()) {
      UIManager.openUIForcefully(Objects.requireNonNull(CobbleWonderTrade.server.getPlayerList().getPlayer(player.getUUID())), WonderTradePool.open());
    } else {
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleWonderTrade.language.getMessageNoPoolView()
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
    }
    return 1;
  }
}