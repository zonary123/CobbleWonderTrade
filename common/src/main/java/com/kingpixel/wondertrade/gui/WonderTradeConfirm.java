package com.kingpixel.wondertrade.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.model.UserInfo;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 26/05/2024 16:17
 */
public class WonderTradeConfirm {
  public static GooeyPage open(Pokemon pokemon) {
    GooeyButton fill = GooeyButton.builder()
      .display(Utils.parseItemId(CobbleWonderTrade.language.getFill()))
      .title("")
      .build();

    GooeyButton confirm = GooeyButton.builder()
      .display(CobbleUtils.language.getItemConfirm().getItemStack())
      .title(AdventureTranslator.toNative(CobbleUtils.language.getItemConfirm().getDisplayname()))
      .lore(Component.class, AdventureTranslator.toNativeL(CobbleUtils.language.getItemConfirm().getLore()))
      .onClick(action -> {
        try {
          if (trade(action.getPlayer(), pokemon)) {
            UIManager.closeUI(action.getPlayer());
          }
        } catch (NoPokemonStoreException e) {
          System.out.println(e);
          e.printStackTrace();
        } catch (ExecutionException | InterruptedException e) {
          throw new RuntimeException(e);
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
      .display(CobbleUtils.language.getItemCancel().getItemStack())
      .title(AdventureTranslator.toNative(CobbleUtils.language.getItemCancel().getDisplayname()))
      .lore(Component.class, AdventureTranslator.toNativeL(CobbleUtils.language.getItemCancel().getLore()))
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

  public static boolean trade(ServerPlayer player, Pokemon pokemonplayer) throws NoPokemonStoreException,
    ExecutionException, InterruptedException {

    if (PlayerUtils.isCooldown(DatabaseClientFactory.databaseClient.getUserInfo(player).getDate())) {
      player.sendSystemMessage(WonderTradeUtil.toNative(CobbleWonderTrade.language.getMessagewondertradecooldown()
        .replace("%time%", PlayerUtils.getCooldown(new Date(DatabaseClientFactory.databaseClient.getUserInfo(player).getDate())))
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
      return false;
    }

    if (pokemonplayer == null) {
      player.sendSystemMessage(WonderTradeUtil.toNative(CobbleWonderTrade.language.getMessageNoPokemonSlot()
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
      return false;
    }

    if (pokemonplayer.getShiny() && !CobbleWonderTrade.config.isAllowshiny()) return false;
    if (pokemonplayer.isLegendary() && !CobbleWonderTrade.config.isAllowlegendary()) return false;
    if (pokemonplayer.getLevel() < CobbleWonderTrade.config.getMinlvreq()) return false;

    PlayerPartyStore partyStorageSlot = Cobblemon.INSTANCE.getStorage().getParty(player.getUUID());

    if (CobbleWonderTrade.config.getPoketradeblacklist().contains(pokemonplayer.showdownId())) {
      player.sendSystemMessage(WonderTradeUtil.toNative(CobbleWonderTrade.language.getMessagePokemonTradeBlackList()
        .replace("%pokemon%", pokemonplayer.getSpecies().getName())
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
      return false;
    }

    Pokemon pokemongive = DatabaseClientFactory.databaseClient.putPokemon(pokemonplayer).clone(true, true);

    if (!CobbleWonderTrade.config.isSavepool()) {
      pokemongive.createPokemonProperties(List.of(
        PokemonPropertyExtractor.SHINY,
        PokemonPropertyExtractor.LEVEL,
        PokemonPropertyExtractor.NATURE,
        PokemonPropertyExtractor.ABILITY,
        PokemonPropertyExtractor.POKEBALL
      )).apply(pokemongive);
    }

    if (!CobbleWonderTrade.config.isIsrandom()) {
      if (pokemongive == null) return false;
      if (!partyStorageSlot.remove(pokemonplayer)) {
        Cobblemon.INSTANCE.getStorage().getPC(player.getUUID()).remove(pokemonplayer);
      }
      partyStorageSlot.add(pokemongive);
    } else {
      if (pokemongive == null) return false;
      if (!partyStorageSlot.remove(pokemonplayer)) {
        Cobblemon.INSTANCE.getStorage().getPC(player.getUUID()).remove(pokemonplayer);
      }
      partyStorageSlot.add(pokemongive);
    }

    if (CobbleWonderTrade.config.isEmitcapture()) {
      WonderTradeUtil.emiteventcaptured(pokemonplayer, player);
      WonderTradeUtil.emiteventcaptured(pokemongive, player);
    }

    if (!CobbleWonderTrade.config.isIsrandom()) {
      if (!CobbleWonderTrade.language.isDisablemessagePokemonToWondertrade()) {
        WonderTradeUtil.broadcast(PokemonUtils.replace(CobbleWonderTrade.language.getMessagePokemonToWondertrade()
          .replace("%player%", player.getGameProfile().getName())
          .replace("%prefix%", CobbleWonderTrade.language.getPrefix()), pokemonplayer));
      }
      if (pokemongive.isLegendary() || pokemongive.getShiny()) {
        WonderTradeUtil.broadcast(
          PokemonUtils.replace(CobbleWonderTrade.language.getMessageisLegendaryOrShinyMessage()
              .replace("%player%", player.getGameProfile().getName())
              .replace("%prefix%", CobbleWonderTrade.language.getPrefix()),
            pokemongive)
        );
      }
    }

    player.sendSystemMessage(WonderTradeUtil.toNative(PokemonUtils.replace(CobbleWonderTrade.language.getMessagewondertraderecieved(),
      pokemongive))
    );

    UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
    userInfo.setMessagesend(false);
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, CobbleWonderTrade.config.getCooldown(player));
    Date futureDate = calendar.getTime();
    userInfo.setDate(futureDate.getTime());

    DatabaseClientFactory.databaseClient.putUserInfo(userInfo);
    return true;
  }
}
