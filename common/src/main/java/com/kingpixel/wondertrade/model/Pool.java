package com.kingpixel.wondertrade.model;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * In-memory and serialized pool model for WonderTrade.
 *
 * @author Carlos Varas Alonso
 */
@Getter
@Setter
public class Pool {
  private Long cooldown;
  private List<Pokemon> pokemons;

  public Pool() {
    this.cooldown = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getPool().getCooldownReset());
    this.pokemons = new ArrayList<>(DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getPool().getSizePool(), 0));
  }

  public synchronized void fix() {
    if (this.pokemons == null) {
      this.pokemons = new ArrayList<>(DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getPool().getSizePool(), 0));
    } else {
      this.pokemons = new ArrayList<>(this.pokemons);
      pokemons.removeIf(pokemon -> pokemon == null || CobbleWonderTrade.config.getPool().getBlackList().isBlackListed(pokemon));
      int sizePool = CobbleWonderTrade.config.getPool().getSizePool();
      if (pokemons.size() > sizePool) {
        this.pokemons = new ArrayList<>(this.pokemons.subList(0, sizePool));
      } else if (pokemons.size() < sizePool) {
        List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(sizePool, this.pokemons.size());
        this.pokemons.addAll(newPokemons);
      }
    }
  }

  public synchronized Pokemon tradePokemon(Pokemon pokemon) {
    if (this.pokemons == null || this.pokemons.isEmpty()) fix();
    int index = Utils.getRandom().nextInt(this.pokemons.size());
    Pokemon trade = this.pokemons.remove(index);
    this.pokemons.add(pokemon);
    return trade;
  }

  public synchronized List<Pokemon> getPokemonsAnimation() {
    if (this.pokemons == null || this.pokemons.isEmpty()) return List.of();
    return Utils.getRandom().ints(0, pokemons.size())
      .distinct()
      .limit(DatabaseClientFactory.POKEMON_ANIMATION_SIZE)
      .mapToObj(pokemons::get)
      .toList();
  }

  public boolean hasCooldown() {
    if (!CobbleWonderTrade.config.getPool().isAutoReset()) return true;
    boolean hasCooldown = this.cooldown != null && this.cooldown > System.currentTimeMillis();
    if (!hasCooldown) {
      setCooldown(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getPool().getCooldownReset()));
    }
    return hasCooldown;
  }
}
