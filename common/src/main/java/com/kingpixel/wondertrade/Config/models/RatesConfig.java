package com.kingpixel.wondertrade.Config.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration model for special Pokémon appearance probabilities and IV constraints.
 */
@Getter
@Setter
public class RatesConfig {
  private int shinyrate;
  private int mythicalrate;
  private int mythicalperfectivs;
  private int legendaryrate;
  private int legendaryperfectivs;
  private int ultrabeastrate;
  private int ultrabeastperfectivs;
  private int paradoxrate;
  private int paradoxperfectivs;

  public RatesConfig() {
    this.shinyrate = 8192;
    this.mythicalrate = 16512;
    this.mythicalperfectivs = 3;
    this.legendaryrate = 16512;
    this.legendaryperfectivs = 3;
    this.ultrabeastrate = 512;
    this.ultrabeastperfectivs = 2;
    this.paradoxrate = 256;
    this.paradoxperfectivs = 2;
  }
}
