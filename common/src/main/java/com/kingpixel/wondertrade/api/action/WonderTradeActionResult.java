package com.kingpixel.wondertrade.api.action;

import com.cobblemon.mod.common.pokemon.Pokemon;
import lombok.Getter;

/**
 * Result representation for WonderTrade headless trading and operations.
 */
@Getter
public class WonderTradeActionResult {

  public enum Status {
    SUCCESS,
    COOLDOWN,
    MIN_LEVEL_NOT_MET,
    BANNED_POKEMON,
    NOT_TRADEABLE,
    IN_BATTLE,
    NO_POKEMON_IN_POOL,
    PLAYER_OFFLINE,
    ERROR
  }

  private final boolean success;
  private final Status status;
  private final String message;
  private final Pokemon tradedPokemon;
  private final Pokemon receivedPokemon;
  private final long remainingCooldownMs;

  private WonderTradeActionResult(boolean success,
                                  Status status,
                                  String message,
                                  Pokemon tradedPokemon,
                                  Pokemon receivedPokemon,
                                  long remainingCooldownMs) {
    this.success = success;
    this.status = status;
    this.message = message != null ? message : "";
    this.tradedPokemon = tradedPokemon;
    this.receivedPokemon = receivedPokemon;
    this.remainingCooldownMs = Math.max(0, remainingCooldownMs);
  }

  public static WonderTradeActionResult success(String message, Pokemon traded, Pokemon received) {
    return new WonderTradeActionResult(true, Status.SUCCESS, message, traded, received, 0);
  }

  public static WonderTradeActionResult cooldown(String message, long remainingCooldownMs) {
    return new WonderTradeActionResult(false, Status.COOLDOWN, message, null, null, remainingCooldownMs);
  }

  public static WonderTradeActionResult minLevel(String message) {
    return new WonderTradeActionResult(false, Status.MIN_LEVEL_NOT_MET, message, null, null, 0);
  }

  public static WonderTradeActionResult banned(String message) {
    return new WonderTradeActionResult(false, Status.BANNED_POKEMON, message, null, null, 0);
  }

  public static WonderTradeActionResult notTradeable(String message) {
    return new WonderTradeActionResult(false, Status.NOT_TRADEABLE, message, null, null, 0);
  }

  public static WonderTradeActionResult inBattle(String message) {
    return new WonderTradeActionResult(false, Status.IN_BATTLE, message, null, null, 0);
  }

  public static WonderTradeActionResult failure(Status status, String message) {
    return new WonderTradeActionResult(false, status, message, null, null, 0);
  }
}
