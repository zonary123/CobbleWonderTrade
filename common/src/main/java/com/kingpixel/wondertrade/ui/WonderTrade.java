package com.kingpixel.wondertrade.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.utils.TextUtil;
import com.kingpixel.wondertrade.utils.Utils;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 26/05/2024 4:04
 */
public class WonderTrade {
  public static GooeyPage open(Player player) throws NoPokemonStoreException {
    try {
      WonderTradeUtil.messagePool(CobbleWonderTrade.manager.getPokemonList());

      PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUUID());

      GooeyButton fill = GooeyButton.builder()
        .display(Utils.parseItemId(CobbleWonderTrade.language.getFill()))
        .title("")
        .build();

      GooeyButton poke1 = createButtonPokemon(partyStore.get(0), 0);
      GooeyButton poke2 = createButtonPokemon(partyStore.get(1), 1);
      GooeyButton poke3 = createButtonPokemon(partyStore.get(2), 2);

      List<String> loreinfo = new ArrayList<>(CobbleWonderTrade.language.getInfo().getLore());
      loreinfo.replaceAll(s -> s.replace("%time%", WonderTradeUtil.getUserCooldown(player.getUUID()))
        .replace("%shinys%", CobbleWonderTrade.manager.getPokemonList().stream().filter(Pokemon::getShiny).count() + "")
        .replace("%legends%",
          CobbleWonderTrade.manager.getPokemonList().stream().filter(Pokemon::isLegendary).count() + ""));

      GooeyButton info = GooeyButton.builder()
        .display(Utils.parseItemId(CobbleWonderTrade.language.getInfo().getId()))
        .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getInfo().getTitle()))
        .lore(Component.class, TextUtil.parseHexCodes(loreinfo))
        .onClick(action -> UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradePool.open())))
        .build();

      GooeyButton poke4 = createButtonPokemon(partyStore.get(3), 3);
      GooeyButton poke5 = createButtonPokemon(partyStore.get(4), 4);
      GooeyButton poke6 = createButtonPokemon(partyStore.get(5), 5);

      ChestTemplate template = ChestTemplate.builder(3)
        .fill(fill)
        .set(1, 1, poke1)
        .set(1, 2, poke2)
        .set(1, 3, poke3)
        .set(1, 4, info)
        .set(1, 5, poke4)
        .set(1, 6, poke5)
        .set(1, 7, poke6)
        .build();
      GooeyPage page = GooeyPage.builder().template(template).title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getTitle())).build();
      page.update();
      return page;
    } catch (NoPokemonStoreException e) {
      System.out.println(e);
      e.printStackTrace();
    }
    return null;
  }

  private static GooeyButton createButtonPokemon(Pokemon pokemon, int index) {
    try {
      if (pokemon == null) {
        return GooeyButton.builder()
          .display(Utils.parseItemId(CobbleWonderTrade.language.getNopokemon().getId()))
          .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getNopokemon().getTitle()))
          .lore(Component.class, TextUtil.parseHexCodes(CobbleWonderTrade.language.getNopokemon().getLore()))
          .build();
      }
      if (pokemon.getShiny() && !CobbleWonderTrade.config.isAllowshiny()) {
        return GooeyButton.builder()
          .display(Utils.parseItemId(CobbleWonderTrade.language.getItemnotallowshiny().getId()))
          .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getItemnotallowshiny().getTitle()))
          .lore(Component.class, TextUtil.parseHexCodes(CobbleWonderTrade.language.getItemnotallowshiny().getLore()))
          .build();
      }

      if (pokemon.isLegendary() && !CobbleWonderTrade.config.isAllowlegendary()) {
        return GooeyButton.builder()
          .display(Utils.parseItemId(CobbleWonderTrade.language.getItemnotallowlegendary().getId()))
          .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getItemnotallowlegendary().getTitle()))
          .lore(Component.class, TextUtil.parseHexCodes(CobbleWonderTrade.language.getItemnotallowlegendary().getLore()))
          .build();
      }
      if (pokemon.getLevel() < CobbleWonderTrade.config.getMinlvreq()) {
        return GooeyButton.builder()
          .display(PokemonItem.from(pokemon))
          .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getColorhexnamepoke() + pokemon.getSpecies().getName()))
          .lore(Component.class, TextUtil.parseHexCodes(WonderTradeUtil.formatPokemonLore(pokemon)))
          .build();
      }
      if (pokemon.getLevel() >= CobbleWonderTrade.config.getMinlvreq()) {
        return GooeyButton.builder()
          .display(PokemonItem.from(pokemon))
          .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getColorhexnamepoke() + pokemon.getSpecies().getName()))
          .lore(Component.class, TextUtil.parseHexCodes(WonderTradeUtil.formatPokemonLore(pokemon)))
          .onClick((action) -> UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradeConfirm.open(
            pokemon, index))))
          .build();
      }
    } catch (NullPointerException e) {
      // Manejar la excepción específica aquí
      System.err.println("Se produjo un error: " + e.getMessage());
      return null;
    } catch (Exception e) {
      // Manejar otras excepciones aquí
      System.err.println("Se produjo un error desconocido: " + e.getMessage());
      return null;
    }
    return null;
  }
}
