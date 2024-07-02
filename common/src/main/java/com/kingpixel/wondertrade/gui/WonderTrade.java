package com.kingpixel.wondertrade.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.utils.AdventureTranslator;
import com.kingpixel.wondertrade.utils.Utils;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 26/05/2024 4:04
 */
public class WonderTrade {
  public static GooeyPage open(Player player) throws NoPokemonStoreException {
    try {
      PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUUID());

      GooeyButton fill = GooeyButton.builder()
        .display(Utils.parseItemId(CobbleWonderTrade.language.getFill()))
        .title("")
        .build();

      GooeyButton poke1 = createButtonPokemon(partyStore.get(0));
      GooeyButton poke2 = createButtonPokemon(partyStore.get(1));
      GooeyButton poke3 = createButtonPokemon(partyStore.get(2));

      List<String> loreinfo = new ArrayList<>(CobbleWonderTrade.language.getInfo().getLore());
      loreinfo.replaceAll(s -> s.replace("%time%", WonderTradeUtil.getUserCooldown(player.getUUID()))
        .replace("%shinys%", CobbleWonderTrade.manager.getPokemonList().stream().filter(Pokemon::getShiny).count() + "")
        .replace("%legends%",
          CobbleWonderTrade.manager.getPokemonList().stream().filter(Pokemon::isLegendary).count() + ""));

      GooeyButton info = GooeyButton.builder()
        .display(Utils.parseItemId(CobbleWonderTrade.language.getInfo().getId()))
        .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getInfo().getTitle()))
        .lore(Component.class, AdventureTranslator.toNativeL(loreinfo))
        .onClick(action -> {
          if (CobbleWonderTrade.config.isPoolview()) {
            UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradePool.open()));
          } else {
            action.getPlayer().sendSystemMessage(AdventureTranslator.toNative(CobbleWonderTrade.language.getMessageNoPoolView()
              .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
          }
        })
        .build();

      GooeyButton pc = GooeyButton.builder()
        .display(Utils.parseItemId(CobbleWonderTrade.language.getPc().getId()))
        .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getPc().getTitle()))
        .lore(Component.class, AdventureTranslator.toNativeL(CobbleWonderTrade.language.getPc().getLore()))
        .onClick(action -> {
          try {
            UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradePC.open(action.getPlayer())));
          } catch (ExecutionException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        })
        .build();

      GooeyButton poke4 = createButtonPokemon(partyStore.get(3));
      GooeyButton poke5 = createButtonPokemon(partyStore.get(4));
      GooeyButton poke6 = createButtonPokemon(partyStore.get(5));

      ChestTemplate template = ChestTemplate.builder(3)
        .fill(fill)
        .set(1, 1, poke1)
        .set(1, 2, poke2)
        .set(1, 3, poke3)
        .set(0, 4, pc)
        .set(1, 4, info)
        .set(1, 5, poke4)
        .set(1, 6, poke5)
        .set(1, 7, poke6)
        .build();
      GooeyPage page = GooeyPage.builder().template(template).title(AdventureTranslator.toNative(CobbleWonderTrade.language.getTitle())).build();
      page.update();
      return page;
    } catch (NoPokemonStoreException e) {
      System.out.println(e);
      e.printStackTrace();
    }
    return null;
  }

  public static GooeyButton createButtonPokemon(Pokemon pokemon) {

    try {
      if (pokemon == null) {
        return GooeyButton.builder()
          .display(Utils.parseItemId(CobbleWonderTrade.language.getNopokemon().getId()))
          .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getNopokemon().getTitle()))
          .lore(Component.class, AdventureTranslator.toNativeL(CobbleWonderTrade.language.getNopokemon().getLore()))
          .build();
      }
      if (CobbleWonderTrade.config.getPoketradeblacklist().contains(pokemon.showdownId())) {
        return GooeyButton.builder()
          .display(Utils.parseItemId(CobbleWonderTrade.language.getItemnotallowpokemon().getId()))
          .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getItemnotallowpokemon().getTitle()))
          .lore(Component.class,
            AdventureTranslator.toNativeL(PokemonUtils.replace(CobbleWonderTrade.language.getItemnotallowpokemon().getLore(), pokemon)))
          .build();
      }

      if (pokemon.getShiny() && !CobbleWonderTrade.config.isAllowshiny()) {
        return GooeyButton.builder()
          .display(Utils.parseItemId(CobbleWonderTrade.language.getItemnotallowshiny().getId()))
          .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getItemnotallowshiny().getTitle()))
          .lore(Component.class,
            AdventureTranslator.toNativeL(PokemonUtils.replace(CobbleWonderTrade.language.getItemnotallowshiny().getLore(),
              pokemon)))
          .build();
      }

      if ((pokemon.isLegendary() && !CobbleWonderTrade.config.isAllowlegendary()) || CobbleWonderTrade.config.getLegends().contains(pokemon.showdownId())) {
        return GooeyButton.builder()
          .display(Utils.parseItemId(CobbleWonderTrade.language.getItemnotallowlegendary().getId()))
          .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getItemnotallowlegendary().getTitle()))
          .lore(Component.class,
            AdventureTranslator.toNativeL(CobbleWonderTrade.language.getItemnotallowlegendary().getLore()))
          .build();
      }
      List<String> loreinfolevel = new ArrayList<>();
      if (pokemon.getLevel() < CobbleWonderTrade.config.getMinlvreq()) {
        loreinfolevel.add(CobbleWonderTrade.language.getDonthavelevel());
      }
      loreinfolevel.replaceAll(s -> s.replace("%minlevel%", CobbleWonderTrade.config.getMinlvreq() + ""));
      loreinfolevel.addAll(PokemonUtils.replace(CobbleUtils.language.getLorepokemon(), pokemon));
      if (pokemon.getLevel() < CobbleWonderTrade.config.getMinlvreq()) {
        return GooeyButton.builder()
          .display(PokemonItem.from(pokemon))
          .title(AdventureTranslator.toNative(PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(), pokemon)))
          .lore(Component.class, AdventureTranslator.toNativeL(loreinfolevel))
          .build();
      }
      return GooeyButton.builder()
        .display(PokemonItem.from(pokemon))
        .title(AdventureTranslator.toNative(PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(), pokemon)))
        .lore(Component.class, AdventureTranslator.toNativeL(loreinfolevel))
        .onClick((action) -> UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradeConfirm.open(
          pokemon))))
        .build();
    } catch (NullPointerException e) {
      System.err.println("Se produjo un error: " + e.getMessage());
      return null;
    } catch (Exception e) {
      System.err.println("Se produjo un error desconocido: " + e.getMessage());
      return null;
    }
  }
}
