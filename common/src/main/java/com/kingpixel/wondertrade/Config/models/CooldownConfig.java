package com.kingpixel.wondertrade.Config.models;

import com.kingpixel.cobbleutils.Model.DurationValue;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Configuration model for trade cooldowns, permission tiers, and scheduled notification timings.
 */
@Getter
@Setter
public class CooldownConfig {
  private DurationValue cooldown;
  private Map<String, DurationValue> cooldownPermission;
  private int cooldownmessage;
  private int cooldownBroadcast;

  public CooldownConfig() {
    this.cooldown = DurationValue.parse("30m");
    this.cooldownPermission = Map.of(
      "wondertrade.vip", DurationValue.parse("15m"),
      "wondertrade.master", DurationValue.parse("10m"),
      "wondertrade.legendary", DurationValue.parse("5m")
    );
    this.cooldownmessage = 15;
    this.cooldownBroadcast = 30;
  }
}
