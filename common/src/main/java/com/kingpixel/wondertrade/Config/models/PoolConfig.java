package com.kingpixel.wondertrade.Config.models;

import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.kingpixel.cobbleutils.Model.FilterPokemons;
import com.kingpixel.cobbleutils.Model.PokemonBlackList;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Configuration model managing WonderTrade pool sizing, level constraints, filtering and blacklist.
 */
@Getter
@Setter
public class PoolConfig {
  private int sizePool;
  private int minlvreq;
  private int minlv;
  private int maxlv;
  private boolean poolview;
  private boolean israndom;
  private boolean autoReset;
  private int cooldownReset;
  private FilterPokemons filterGenerationPokemon;
  private PokemonBlackList specialPokemons;
  private PokemonBlackList blackList;

  public PoolConfig() {
    this.sizePool = 72;
    this.minlvreq = 5;
    this.minlv = 5;
    this.maxlv = 36;
    this.poolview = true;
    this.israndom = false;
    this.autoReset = false;
    this.cooldownReset = 30;
    this.filterGenerationPokemon = new FilterPokemons();
    this.blackList = new PokemonBlackList();
    this.specialPokemons = new PokemonBlackList();
    this.specialPokemons.getPokemons().clear();
    this.specialPokemons.getEggGroups().clear();
    this.specialPokemons.getTypes().clear();
    this.specialPokemons.getLabels().clear();
    this.specialPokemons.getForms().clear();
    this.specialPokemons.getLabels().addAll(
      List.of(
        CobblemonPokemonLabels.LEGENDARY,
        CobblemonPokemonLabels.MYTHICAL,
        CobblemonPokemonLabels.PARADOX,
        CobblemonPokemonLabels.ULTRA_BEAST
      )
    );
  }

  public void fix() {
    if (this.sizePool < DatabaseClientFactory.MIN_POOL_SIZE) {
      this.sizePool = DatabaseClientFactory.MIN_POOL_SIZE;
    }
    if (this.filterGenerationPokemon == null) {
      this.filterGenerationPokemon = new FilterPokemons();
    }
    if (this.blackList == null) {
      this.blackList = new PokemonBlackList();
    }
    if (this.specialPokemons == null) {
      this.specialPokemons = new PokemonBlackList();
    }
  }
}
