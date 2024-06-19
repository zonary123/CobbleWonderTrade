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
import com.kingpixel.wondertrade.utils.AdventureTranslator;
import com.kingpixel.wondertrade.utils.Utils;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 26/05/2024 16:17
 */
public class WonderTradeConfirm {
  public static GooeyPage open(Pokemon pokemon, int index) {
    GooeyButton fill = GooeyButton.builder()
      .display(Utils.parseItemId(CobbleWonderTrade.language.getFill()))
      .title("")
      .build();
    GooeyButton confirm = GooeyButton.builder()
      .display(Utils.parseItemId(CobbleWonderTrade.language.getConfirm().getId()))
      .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getConfirm().getTitle()))
      .lore(Component.class, AdventureTranslator.toNativeL(CobbleWonderTrade.language.getConfirm().getLore()))
      .onClick(action -> {
        try {
          if (trade(action.getPlayer(), index)) {
            UIManager.closeUI(action.getPlayer());
          }
        } catch (NoPokemonStoreException e) {
          System.out.println(e);
          e.printStackTrace();
        }
      })
      .build();

    GooeyButton pokebutton = GooeyButton.builder()
      .display(PokemonItem.from(pokemon))
      .title(AdventureTranslator.toNative(pokemon.getSpecies().getName()))
      .lore(Component.class, AdventureTranslator.toNativeL(WonderTradeUtil.formatPokemonLore(pokemon)))
      .build();

    GooeyButton cancel = GooeyButton.builder()
      .display(Utils.parseItemId(CobbleWonderTrade.language.getCancel().getId()))
      .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getCancel().getTitle()))
      .lore(Component.class, AdventureTranslator.toNativeL(CobbleWonderTrade.language.getCancel().getLore()))
      .onClick(action -> {
        try {
          UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTrade.open(action.getPlayer())));
        } catch (NoPokemonStoreException e) {
          throw new RuntimeException(e);
        }
      })
      .build();


    ChestTemplate template = ChestTemplate.builder(3)
      .fill(fill)
      .set(1, 2, confirm)
      .set(1, 4, pokebutton)
      .set(1, 6, cancel)
      .build();

    GooeyPage page =
      GooeyPage.builder().title(AdventureTranslator.toNative(CobbleWonderTrade.language.getPrefix())).template(template).build();
    return page;
  }

  private static boolean trade(Player player, int index) throws NoPokemonStoreException {
    if (!CobbleWonderTrade.manager.hasCooldownEnded(player)) {
      player.sendSystemMessage(AdventureTranslator.toNative("&cYou must wait before trading again!"));
      return false;
    }

    PlayerPartyStore partyStorageSlot = Cobblemon.INSTANCE.getStorage().getParty(player.getUUID());
    Pokemon pokemonplayer = partyStorageSlot.get(index);
    Pokemon pokemongive;
    if (pokemonplayer == null) {
      return false;
    }

    if (!CobbleWonderTrade.config.isIsrandom()) {
      pokemongive = CobbleWonderTrade.manager.putPokemon(pokemonplayer);
      if (pokemongive == null) {
        return false;
      }
      partyStorageSlot.set(index, pokemongive);
    } else {
      pokemongive = CobbleWonderTrade.manager.getRandomPokemon();
      if (pokemongive == null) {
        return false;
      }
      partyStorageSlot.set(index, pokemongive);
    }

    WonderTradeUtil.emiteventcaptured(pokemonplayer, player);
    WonderTradeUtil.emiteventcaptured(pokemongive, player);
    player.sendSystemMessage(AdventureTranslator.toNative(CobbleWonderTrade.language.getMessagewondertraderecieved().replace("%pokemon%", pokemongive.getSpecies().getName())
      .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
    CobbleWonderTrade.manager.getUserInfo().get(player.getUUID()).setMessagesend(false);
    CobbleWonderTrade.manager.addPlayerWithDate(player, CobbleWonderTrade.config.getCooldown());
    return true;
  }
}
