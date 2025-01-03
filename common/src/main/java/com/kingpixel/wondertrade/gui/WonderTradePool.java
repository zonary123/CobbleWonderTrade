package com.kingpixel.wondertrade.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.UIUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 02/06/2024 2:19
 */
public class WonderTradePool {
  public static Page open(boolean special) throws ExecutionException, InterruptedException {
    GooeyButton fill = GooeyButton.builder()
      .display(Utils.parseItemId(CobbleWonderTrade.language.getFill()))
      .title("")
      .build();
    List<Button> buttons = new ArrayList<>();

    List<Pokemon> pokemons = new ArrayList<>(DatabaseClientFactory.databaseClient.getSpecialPool(special));

    for (Pokemon p : pokemons) {
      GooeyButton pokemonButton = createPokemonButton(p);
      buttons.add(pokemonButton);
    }

    LinkedPageButton previus = LinkedPageButton.builder()
      .display(CobbleUtils.language.getItemPrevious().getItemStack())
      .title(AdventureTranslator.toNative(CobbleUtils.language.getItemPrevious().getDisplayname()))
      .linkType(LinkType.Previous)
      .build();

    LinkedPageButton next = LinkedPageButton.builder()
      .display(CobbleUtils.language.getItemNext().getItemStack())
      .title(AdventureTranslator.toNative(CobbleUtils.language.getItemNext().getDisplayname()))
      .linkType(LinkType.Next)
      .build();

    GooeyButton close = UIUtils.getCloseButton(action -> {
      try {
        UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTrade.open(action.getPlayer())));
      } catch (NoPokemonStoreException e) {
        e.printStackTrace();
      }
    });

    PlaceholderButton placeholder = new PlaceholderButton();

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder();

    ChestTemplate template = ChestTemplate.builder(6)
      .fill(fill)
      .rectangle(0, 0, 4, 9, placeholder)
      .fillFromList(buttons)
      .set(5, 0, previus)
      .set(5, 4, close)
      .set(5, 8, next)
      .build();

    linkedPageBuilder.title(AdventureTranslator.toNative(CobbleWonderTrade.language.getTitlepool()));

    return PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
  }

  private static GooeyButton createPokemonButton(Pokemon p) {
    try {
      GooeyButton pokemonButton = GooeyButton.builder()
        .display(PokemonItem.from(p))
        .title(AdventureTranslator.toNative(PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(), p)))
        .lore(Text.class,
          AdventureTranslator.toNativeL(PokemonUtils.replace(CobbleUtils.language.getLorepokemon(), p)))
        .build();
      return pokemonButton;
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error al crear el botón de Pokémon: ", e);

      return GooeyButton.builder()
        .display(new ItemStack(Items.BARRIER))
        .title("Error")
        .build();
    }
  }
}
