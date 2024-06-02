package com.kingpixel.wondertrade.ui;

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
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.utils.TextUtil;
import com.kingpixel.wondertrade.utils.Utils;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 02/06/2024 2:19
 */
public class WonderTradePool {
  public static Page open() {

    List<Button> buttons = new ArrayList<>();

    List<Pokemon> pokemons = CobbleWonderTrade.manager.getPokemonList();

    for (Pokemon p : pokemons) {
      GooeyButton pokemonButton = createPokemonButton(p);
      buttons.add(pokemonButton);
    }

    LinkedPageButton previus = LinkedPageButton.builder()
      .display(Utils.parseItemId(CobbleWonderTrade.language.getItempreviouspage().getId()))
      .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getItempreviouspage().getTitle()))
      .linkType(LinkType.Previous)
      .build();

    LinkedPageButton next = LinkedPageButton.builder()
      .display(Utils.parseItemId(CobbleWonderTrade.language.getItemnextpage().getId()))
      .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getItemnextpage().getTitle()))
      .linkType(LinkType.Next)
      .build();

    GooeyButton close = GooeyButton.builder()
      .display(Utils.parseItemId(CobbleWonderTrade.language.getItemclose().getId()))
      .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getItemclose().getTitle()))
      .lore(Component.class, TextUtil.parseHexCodes(CobbleWonderTrade.language.getItemclose().getLore()))
      .onClick((action) -> {
        try {
          UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTrade.open(action.getPlayer())));
        } catch (NoPokemonStoreException e) {
          System.out.println(e);
          e.printStackTrace();
        }
      })
      .build();

    PlaceholderButton placeholder = new PlaceholderButton();

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder();

    ChestTemplate template = ChestTemplate.builder(6)
      .fill(GooeyButton.builder().display(new ItemStack(Items.GRAY_STAINED_GLASS_PANE).setHoverName(Component.literal(""))).build())
      .rectangle(0, 1, 5, 7, placeholder)
      .fillFromList(buttons)
      .set(5, 4, close)
      .set(5, 0, previus)
      .set(5, 8, next)
      .build();

    linkedPageBuilder.title("WonderTrade Pool");

    LinkedPage firstPage = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
    return firstPage;
  }

  private static GooeyButton createPokemonButton(Pokemon p) {
    GooeyButton pokemonButton;
    pokemonButton = GooeyButton.builder()
      .display(PokemonItem.from(p))
      .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getColorhexnamepoke() + p.getSpecies().getName()))
      .lore(Component.class, TextUtil.parseHexCodes(WonderTradeUtil.formatPokemonLore(p)))
      .build();
    return pokemonButton;
  }
}
