package com.kingpixel.wondertrade.api.model;

import lombok.Getter;

import java.util.UUID;

/**
 * Snapshot context viewmodel for a player's WonderTrade status and environment.
 */
@Getter
public class PlayerWonderTradeContext {

  private final UUID playerUuid;
  private final String playerName;
  private final boolean hasCooldown;
  private final long cooldownExpiresAt;
  private final long remainingCooldownSeconds;
  private final String formattedCooldown;
  private final int minLevelRequired;
  private final boolean canTrade;
  private final WonderTradePoolSummaryView poolSummary;

  public PlayerWonderTradeContext(UUID playerUuid,
                                  String playerName,
                                  boolean hasCooldown,
                                  long cooldownExpiresAt,
                                  long remainingCooldownSeconds,
                                  String formattedCooldown,
                                  int minLevelRequired,
                                  boolean canTrade,
                                  WonderTradePoolSummaryView poolSummary) {
    this.playerUuid = playerUuid;
    this.playerName = playerName;
    this.hasCooldown = hasCooldown;
    this.cooldownExpiresAt = cooldownExpiresAt;
    this.remainingCooldownSeconds = remainingCooldownSeconds;
    this.formattedCooldown = formattedCooldown != null ? formattedCooldown : "";
    this.minLevelRequired = minLevelRequired;
    this.canTrade = canTrade;
    this.poolSummary = poolSummary;
  }
}
