package com.kingpixel.wondertrade.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.RateLimitedButton;
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
import java.util.concurrent.TimeUnit;

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

      RateLimitedButton poke1 = createButtonPokemon(partyStore.get(0), 0);
      RateLimitedButton poke2 = createButtonPokemon(partyStore.get(1), 1);
      RateLimitedButton poke3 = createButtonPokemon(partyStore.get(2), 2);

      List<String> loreinfo = new ArrayList<>(CobbleWonderTrade.language.getInfo().getLore());
      loreinfo.replaceAll(s -> s.replace("%time%", WonderTradeUtil.getUserCooldown(player.getUUID())));

      GooeyButton info = GooeyButton.builder()
        .display(Utils.parseItemId(CobbleWonderTrade.language.getInfo().getId()))
        .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getInfo().getTitle()))
        .lore(Component.class, TextUtil.parseHexCodes(loreinfo))
        .onClick(action -> UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradePool.open())))
        .build();

      RateLimitedButton poke4 = createButtonPokemon(partyStore.get(3), 3);
      RateLimitedButton poke5 = createButtonPokemon(partyStore.get(4), 4);
      RateLimitedButton poke6 = createButtonPokemon(partyStore.get(5), 5);

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

  private static RateLimitedButton createButtonPokemon(Pokemon pokemon, int index) {
    GooeyButton poke;
    try {
      if (pokemon != null) {
        poke = GooeyButton.builder()
          .display(PokemonItem.from(pokemon))
          .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getColorhexnamepoke() + pokemon.getSpecies().getName()))
          .lore(Component.class, TextUtil.parseHexCodes(WonderTradeUtil.formatPokemonLore(pokemon)))
          .onClick((action) -> UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradeConfirm.open(
            pokemon, index))))
          .build();
      } else {
        poke = GooeyButton.builder()
          .display(Utils.parseItemId(CobbleWonderTrade.language.getNopokemon().getId()))
          .title(TextUtil.parseHexCodes(CobbleWonderTrade.language.getNopokemon().getTitle()))
          .lore(Component.class, TextUtil.parseHexCodes(CobbleWonderTrade.language.getNopokemon().getLore()))
          .build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    RateLimitedButton button = RateLimitedButton.builder()
      .button(poke)
      .limit(1)
      .interval(3, TimeUnit.SECONDS)
      .build();
    button.update();
    return button;
  }
}
