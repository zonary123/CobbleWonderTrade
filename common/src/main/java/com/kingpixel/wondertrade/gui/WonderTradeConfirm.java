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

import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 26/05/2024 16:17
 */
public class WonderTradeConfirm {
  public static GooeyPage open(Pokemon pokemon) {
    GooeyButton fill = GooeyButton.builder()
      .display(Utils.fill())
      .title("")
      .build();
    GooeyButton confirm = GooeyButton.builder()
      .display(Utils.parseItemModel(CobbleWonderTrade.language.getConfirm()))
      .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getConfirm().getTitle()))
      .lore(Component.class, AdventureTranslator.toNativeL(CobbleWonderTrade.language.getConfirm().getLore()))
      .onClick(action -> {
        try {
          if (trade(action.getPlayer(), pokemon)) {
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
      .title(AdventureTranslator.toNative(PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(), pokemon)))
      .lore(Component.class, AdventureTranslator.toNativeL(PokemonUtils.replace(CobbleUtils.language.getLorepokemon()
        , pokemon)))
      .build();

    GooeyButton cancel = GooeyButton.builder()
      .display(Utils.parseItemModel(CobbleWonderTrade.language.getCancel()))
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
      GooeyPage.builder().title(AdventureTranslator.toNative(CobbleWonderTrade.language.getTitleconfirm())).template(template).build();
    return page;
  }

  public static boolean trade(Player player, Pokemon pokemonplayer) throws NoPokemonStoreException {
    if (!CobbleWonderTrade.manager.hasCooldownEnded(player)) {
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleWonderTrade.language.getMessagewondertradecooldown()
        .replace("%time%", CobbleWonderTrade.manager.getCooldown(player.getUUID()))
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
      return false;
    }
    if (pokemonplayer == null) {
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleWonderTrade.language.getMessageNoPokemonSlot()
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
      return false;
    }
    if (pokemonplayer.getShiny() && !CobbleWonderTrade.config.isAllowshiny()) return false;
    if (pokemonplayer.isLegendary() && !CobbleWonderTrade.config.isAllowlegendary()) return false;
    if (pokemonplayer.getLevel() < CobbleWonderTrade.config.getMinlvreq()) return false;

    PlayerPartyStore partyStorageSlot = Cobblemon.INSTANCE.getStorage().getParty(player.getUUID());

    if (CobbleWonderTrade.config.getPoketradeblacklist().contains(pokemonplayer.showdownId())) {
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleWonderTrade.language.getMessagePokemonTradeBlackList()
        .replace("%pokemon%", pokemonplayer.getSpecies().getName())
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
      return false;
    }
    Pokemon pokemongive;


    if (!CobbleWonderTrade.config.isIsrandom()) {
      pokemongive = CobbleWonderTrade.manager.putPokemon(pokemonplayer);
      if (pokemongive == null) {
        return false;
      }
      if (!partyStorageSlot.remove(pokemonplayer)) {
        Cobblemon.INSTANCE.getStorage().getPC(player.getUUID()).remove(pokemonplayer);
      }
      partyStorageSlot.add(pokemongive);
    } else {
      pokemongive = CobbleWonderTrade.manager.getRandomPokemon();
      if (pokemongive == null) {
        return false;
      }
      if (!partyStorageSlot.remove(pokemonplayer)) {
        Cobblemon.INSTANCE.getStorage().getPC(player.getUUID()).remove(pokemonplayer);
      }
      partyStorageSlot.add(pokemongive);
    }

    if (CobbleWonderTrade.config.isEmitcapture()) {
      // Pasarlo a un evento de wondertrade mejor
      WonderTradeUtil.emiteventcaptured(pokemonplayer, player);
      WonderTradeUtil.emiteventcaptured(pokemongive, player);
    }

    // Mensaje pokemon metido en el wondertrade
    if (!CobbleWonderTrade.config.isIsrandom()) {
      Utils.broadcastMessage(PokemonUtils.replace(CobbleWonderTrade.language.getMessagePokemonToWondertrade()
        .replace("%player%", player.getGameProfile().getName())
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix()), pokemonplayer));
    }
    // Mensaje pokemon recibido
    player.sendSystemMessage(AdventureTranslator.toNative(PokemonUtils.replace(CobbleWonderTrade.language.getMessagewondertraderecieved(),
      pokemongive))
    );
    CobbleWonderTrade.manager.getUserInfo().get(player.getUUID()).setMessagesend(false);
    CobbleWonderTrade.manager.addPlayerWithDate(player, CobbleWonderTrade.config.getCooldown());
    CobbleWonderTrade.manager.writeInfo();
    return true;
  }
}
