package com.kingpixel.wondertrade.command.admin;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.gui.WonderTrade;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 26/05/2024 4:33
 */
public class CommandWonderTradeAdmin implements Command<CommandSourceStack> {

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Player player = EntityArgument.getPlayer(context, "player");
    try {
      UIManager.openUIForcefully(Objects.requireNonNull(CobbleWonderTrade.server.getPlayerList().getPlayer(player.getUUID())), Objects.requireNonNull(WonderTrade.open(player)));
    } catch (NoPokemonStoreException e) {
      throw new RuntimeException(e);
    }
    return 1;
  }
}
