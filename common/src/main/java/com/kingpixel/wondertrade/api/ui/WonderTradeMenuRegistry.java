package com.kingpixel.wondertrade.api.ui;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe registry for WonderTrade UI menu providers.
 * Allows companion mods (e.g. Rotom Phone, custom GUIs) to register custom menu providers.
 * If no custom provider is registered, it automatically falls back to {@link DefaultGooeyWonderTradeMenuProvider}.
 */
public final class WonderTradeMenuRegistry {

  private static final WonderTradeMenuProvider DEFAULT_PROVIDER = new DefaultGooeyWonderTradeMenuProvider();
  private static final AtomicReference<WonderTradeMenuProvider> CUSTOM_PROVIDER = new AtomicReference<>(null);

  private WonderTradeMenuRegistry() {}

  /**
   * Registers a custom menu provider. Passing null unregisters any custom provider.
   *
   * @param provider the custom menu provider, or null to revert to default
   */
  public static void register(WonderTradeMenuProvider provider) {
    CUSTOM_PROVIDER.set(provider);
  }

  /**
   * Unregisters the custom menu provider, reverting back to the default GooeyLibs implementation.
   */
  public static void unregister() {
    CUSTOM_PROVIDER.set(null);
  }

  /**
   * Checks whether a custom menu provider is currently registered.
   *
   * @return true if a custom provider is active
   */
  public static boolean hasCustomProvider() {
    return CUSTOM_PROVIDER.get() != null;
  }

  /**
   * Returns the active menu provider (custom if present, otherwise default).
   *
   * @return the active menu provider
   */
  public static WonderTradeMenuProvider getProvider() {
    WonderTradeMenuProvider custom = CUSTOM_PROVIDER.get();
    return custom != null ? custom : DEFAULT_PROVIDER;
  }

  /**
   * Opens the WonderTrade trade selection menu using the active provider.
   *
   * @param player the player
   */
  public static void openTradeMenu(ServerPlayerEntity player) {
    if (player == null) return;
    getProvider().openTradeMenu(player);
  }

  /**
   * Opens the pool view menu using the active provider.
   *
   * @param player   the player
   * @param pokemons the list of Pokémon in the pool
   */
  public static void openPoolMenu(ServerPlayerEntity player, List<Pokemon> pokemons) {
    if (player == null || pokemons == null) return;
    getProvider().openPoolMenu(player, pokemons);
  }
}
