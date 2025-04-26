package com.kingpixel.wondertrade.model;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import lombok.Data;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 16/04/2025 19:04
 */
@Data
public class Pool {
  private Long cooldown;
  private List<Pokemon> pokemons;

  public Pool() {
    this.cooldown = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getCooldownReset());
    this.pokemons = DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getSizePool(), 0);
  }

  public void fix() {
    if (this.pokemons == null) {
      this.pokemons = DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getSizePool(), 0);
    } else {
      int sizePool = CobbleWonderTrade.config.getSizePool();
      if (pokemons.size() > sizePool) {
        this.pokemons = this.pokemons.subList(0, sizePool);
      } else if (pokemons.size() < sizePool) {
        // Generar nuevos elementos para completar el tamaño
        List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(sizePool, this.pokemons.size());
        this.pokemons.addAll(newPokemons);
      }
    }
  }

  public Pokemon tradePokemon(Pokemon pokemon) {
    if (this.pokemons == null || this.pokemons.isEmpty()) fix();
    var trade = this.pokemons.remove(Utils.RANDOM.nextInt(this.pokemons.size()));
    this.pokemons.add(pokemon);
    return trade;
  }

  public List<Pokemon> getPokemonsAnimation() {
    if (this.pokemons == null || this.pokemons.isEmpty()) return List.of();
    return Utils.RANDOM.ints(0, pokemons.size())
      .distinct()
      .limit(DatabaseClientFactory.POKEMON_ANIMATION_SIZE)
      .mapToObj(pokemons::get)
      .toList();
  }

  public boolean hasCooldown() {
    if (!CobbleWonderTrade.config.isAutoReset()) return true;
    boolean hasCooldown = this.cooldown != null && this.cooldown > System.currentTimeMillis();
    if (!hasCooldown)
      setCooldown(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getCooldownReset()));
    return hasCooldown;
  }
}
