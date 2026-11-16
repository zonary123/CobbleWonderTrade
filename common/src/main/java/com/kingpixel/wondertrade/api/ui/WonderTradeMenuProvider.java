package com.kingpixel.wondertrade.api.ui;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * Service Provider Interface (SPI) for WonderTrade user interfaces.
 * Allows companion mods (e.g. Rotom Phone, custom graphical screens, web dashboards)
 * to intercept and replace default chest GUI menus with custom screens.
 */
public interface WonderTradeMenuProvider {

  /**
   * Opens the WonderTrade trade selection menu for a player.
   *
   * @param player the player opening the trade menu
   */
  void openTradeMenu(ServerPlayerEntity player);

  /**
   * Opens the pool view menu displaying available Pokémon in the WonderTrade pool.
   *
   * @param player   the player opening the pool menu
   * @param pokemons the list of Pokémon currently in the pool
   */
  void openPoolMenu(ServerPlayerEntity player, List<Pokemon> pokemons);
}
