package com.kingpixel.wondertrade.api.ui;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.utils.WonderTradeUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * Default GooeyLibs-based menu provider implementation for WonderTrade.
 */
public class DefaultGooeyWonderTradeMenuProvider implements WonderTradeMenuProvider {

  @Override
  public void openTradeMenu(ServerPlayerEntity player) {
    if (player == null) return;
    WonderTradeUtils.open(player);
  }

  @Override
  public void openPoolMenu(ServerPlayerEntity player, List<Pokemon> pokemons) {
    if (player == null || pokemons == null || pokemons.isEmpty()) return;
    if (CobbleWonderTrade.language != null && CobbleWonderTrade.language.getPool() != null) {
      CobbleWonderTrade.language.getPool().open(player, pokemons);
    }
  }
}
