package com.kingpixel.wondertrade.command.utils;

import com.cobblemon.mod.common.pokemon.Pokemon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Cached Pokémon pool statistics to optimize UI rendering and lore generation.
 *
 * @author Carlos Varas Alonso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokemonStats {
  private int shinys;
  private int legendaries;
  private int mythicals;
  private int ultraBeasts;
  private int paradoxes;
  private int ivs31;
  private List<Pokemon> special;
  private List<Pokemon> pokemons;
}
