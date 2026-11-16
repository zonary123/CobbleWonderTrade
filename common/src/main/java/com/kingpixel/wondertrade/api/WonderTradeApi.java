package com.kingpixel.wondertrade.api;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.wondertrade.api.action.WonderTradeActionResult;
import com.kingpixel.wondertrade.api.model.PlayerWonderTradeContext;
import com.kingpixel.wondertrade.api.model.WonderTradePoolSummaryView;
import com.kingpixel.wondertrade.api.ui.WonderTradeMenuProvider;
import com.kingpixel.wondertrade.api.ui.WonderTradeMenuRegistry;
import com.kingpixel.wondertrade.application.WonderTradeActionService;
import com.kingpixel.wondertrade.application.WonderTradeContextService;
import com.kingpixel.wondertrade.command.utils.PokemonStats;
import com.kingpixel.wondertrade.command.utils.WonderTradeUtils;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Unified Public API for WonderTrade.
 * Primary entry point for companion mods (Rotom Phone, custom GUIs, Web panels)
 * to interact with the WonderTrade system headlessly or via pluggable UI providers.
 */
public final class WonderTradeApi {

  private static final WonderTradeActionService ACTION_SERVICE = new WonderTradeActionService();
  private static final WonderTradeContextService CONTEXT_SERVICE = new WonderTradeContextService();

  private WonderTradeApi() {}

  // ================= UI / Menu Provider Methods =================

  /**
   * Opens the main WonderTrade selection menu using the active provider.
   *
   * @param player the player opening the menu
   */
  public static void openTradeMenu(ServerPlayerEntity player) {
    WonderTradeMenuRegistry.openTradeMenu(player);
  }

  /**
   * Opens the pool view menu using the active provider.
   *
   * @param player   the player opening the pool
   * @param pokemons the list of Pokémon in the pool
   */
  public static void openPoolMenu(ServerPlayerEntity player, List<Pokemon> pokemons) {
    WonderTradeMenuRegistry.openPoolMenu(player, pokemons);
  }

  /**
   * Registers a custom menu provider.
   *
   * @param provider the custom provider
   */
  public static void registerMenuProvider(WonderTradeMenuProvider provider) {
    WonderTradeMenuRegistry.register(provider);
  }

  /**
   * Unregisters the custom menu provider, reverting back to the default GooeyLibs implementation.
   */
  public static void unregisterMenuProvider() {
    WonderTradeMenuRegistry.unregister();
  }

  /**
   * Checks whether a custom menu provider is currently registered.
   *
   * @return true if a custom provider is active
   */
  public static boolean hasCustomMenuProvider() {
    return WonderTradeMenuRegistry.hasCustomProvider();
  }

  /**
   * Retrieves the active menu provider.
   *
   * @return active menu provider
   */
  public static WonderTradeMenuProvider getMenuProvider() {
    return WonderTradeMenuRegistry.getProvider();
  }

  // ================= Headless Action Methods =================

  /**
   * Performs a WonderTrade exchange headlessly.
   *
   * @param player  the player
   * @param pokemon the Pokémon to trade away
   * @return future containing the trade result
   */
  public static CompletableFuture<WonderTradeActionResult> trade(ServerPlayerEntity player, Pokemon pokemon) {
    return ACTION_SERVICE.trade(player, pokemon);
  }

  /**
   * Performs a WonderTrade exchange headlessly with optional client animation.
   *
   * @param player        the player
   * @param pokemon       the Pokémon to trade away
   * @param showAnimation true to trigger reward animation for player
   * @return future containing the trade result
   */
  public static CompletableFuture<WonderTradeActionResult> trade(ServerPlayerEntity player, Pokemon pokemon, boolean showAnimation) {
    return ACTION_SERVICE.trade(player, pokemon, showAnimation);
  }

  /**
   * Resets cooldown for a player headlessly.
   *
   * @param player the player
   * @return future containing the action result
   */
  public static CompletableFuture<WonderTradeActionResult> resetCooldown(ServerPlayerEntity player) {
    return ACTION_SERVICE.resetCooldown(player);
  }

  /**
   * Resets cooldown for a player UUID headlessly.
   *
   * @param playerUuid player UUID
   * @return future containing the action result
   */
  public static CompletableFuture<WonderTradeActionResult> resetCooldown(UUID playerUuid) {
    return ACTION_SERVICE.resetCooldown(playerUuid);
  }

  /**
   * Forces a reset of the WonderTrade pool.
   *
   * @return future containing the action result
   */
  public static CompletableFuture<WonderTradeActionResult> restartPool() {
    return ACTION_SERVICE.restartPool();
  }

  // ================= Query / View Context Methods =================

  /**
   * Retrieves the complete WonderTrade context for a player.
   *
   * @param player the player
   * @return future containing PlayerWonderTradeContext
   */
  public static CompletableFuture<PlayerWonderTradeContext> getPlayerContext(ServerPlayerEntity player) {
    return CONTEXT_SERVICE.getPlayerContext(player);
  }

  /**
   * Retrieves the summary view of the WonderTrade pool.
   *
   * @return future containing WonderTradePoolSummaryView
   */
  public static CompletableFuture<WonderTradePoolSummaryView> getPoolSummary() {
    return CONTEXT_SERVICE.getPoolSummary();
  }

  /**
   * Retrieves all Pokémon currently available in the WonderTrade pool.
   *
   * @return future containing list of pool Pokémon
   */
  public static CompletableFuture<List<Pokemon>> getPoolPokemons() {
    return CONTEXT_SERVICE.getPoolPokemons();
  }

  // ================= Service & Data Access =================

  public static WonderTradeActionService getActionService() {
    return ACTION_SERVICE;
  }

  public static WonderTradeContextService getContextService() {
    return CONTEXT_SERVICE;
  }

  public static PokemonStats getPokemonStats() {
    return WonderTradeUtils.getPokemonStats();
  }

  public static UserInfo getUserInfo(ServerPlayerEntity player) {
    if (DatabaseClientFactory.databaseClient == null || player == null) return null;
    return DatabaseClientFactory.databaseClient.getUserInfo(player);
  }

  public static UserInfo getUserInfo(UUID playerUuid) {
    return DatabaseClientFactory.userInfoMap.get(playerUuid);
  }
}
