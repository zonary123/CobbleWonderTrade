package com.kingpixel.wondertrade.model;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
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
    this.cooldown = null;
    this.pokemons = CobbleWonderTrade.config.getFilterGenerationPokemon().generateRandomPokemons(
      CobbleWonderTrade.MOD_ID,
      "pool",
      CobbleWonderTrade.config.getSizePool()
    );
  }

  public void fix() {
    if (this.pokemons == null) {
      this.pokemons = CobbleWonderTrade.config.getFilterGenerationPokemon().generateRandomPokemons(
        CobbleWonderTrade.MOD_ID,
        "pool",
        CobbleWonderTrade.config.getSizePool()
      );
    } else {
      int sizePool = CobbleWonderTrade.config.getSizePool();
      if (pokemons.size() > sizePool) {
        // Eliminar el exceso de elementos
        this.pokemons = this.pokemons.subList(0, sizePool);
      } else if (pokemons.size() < sizePool) {
        // Generar nuevos elementos para completar el tamaño
        List<Pokemon> newPokemons = CobbleWonderTrade.config.getFilterGenerationPokemon().generateRandomPokemons(
          CobbleWonderTrade.MOD_ID,
          "pool",
          sizePool - pokemons.size()
        );
        this.pokemons.addAll(newPokemons);
      }
    }
  }

  public Pokemon tradePokemon(Pokemon pokemon) {
    if (this.pokemons == null || this.pokemons.isEmpty()) {
      fix();
    }
    var trade = this.pokemons.remove(Utils.RANDOM.nextInt(this.pokemons.size()));
    this.pokemons.add(pokemon);
    return trade;
  }

  public List<Pokemon> getPokemonsAnimation() {
    if (this.pokemons == null || this.pokemons.isEmpty()) return List.of();
    return Utils.RANDOM.ints(0, pokemons.size())
      .distinct()
      .limit(5)
      .mapToObj(pokemons::get)
      .toList();
  }

  public boolean hasCooldown() {
    boolean hasCooldown = this.cooldown != null && this.cooldown > System.currentTimeMillis();
    if (hasCooldown) this.cooldown =
      System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getCooldownReset());
    return hasCooldown;
  }
}
