package com.kingpixel.wondertrade.api.model;

import lombok.Getter;

/**
 * Summary viewmodel for the WonderTrade Pokémon pool and rates.
 */
@Getter
public class WonderTradePoolSummaryView {

  private final int totalPokemons;
  private final int shinies;
  private final int legendaries;
  private final int mythicals;
  private final int ultraBeasts;
  private final int paradoxes;
  private final int perfectIvs;
  private final boolean autoResetEnabled;
  private final long cooldownResetMinutes;
  private final boolean randomGeneration;

  public WonderTradePoolSummaryView(int totalPokemons,
                                   int shinies,
                                   int legendaries,
                                   int mythicals,
                                   int ultraBeasts,
                                   int paradoxes,
                                   int perfectIvs,
                                   boolean autoResetEnabled,
                                   long cooldownResetMinutes,
                                   boolean randomGeneration) {
    this.totalPokemons = totalPokemons;
    this.shinies = shinies;
    this.legendaries = legendaries;
    this.mythicals = mythicals;
    this.ultraBeasts = ultraBeasts;
    this.paradoxes = paradoxes;
    this.perfectIvs = perfectIvs;
    this.autoResetEnabled = autoResetEnabled;
    this.cooldownResetMinutes = cooldownResetMinutes;
    this.randomGeneration = randomGeneration;
  }
}
