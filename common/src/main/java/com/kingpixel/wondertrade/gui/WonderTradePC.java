package com.kingpixel.wondertrade.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 25/06/2024 2:56
 */


public class WonderTradePC {
  public static LinkedPage open(Player player) throws ExecutionException, InterruptedException {
    return CompletableFuture.supplyAsync(() -> {
      try {
        ChestTemplate template = ChestTemplate.builder(6).build();
        List<Button> buttons = new ArrayList<>();
        PCStore pcStore = Cobblemon.INSTANCE.getStorage().getPC(player.getUUID());

        pcStore.forEach((pokemon) -> {
          buttons.add(WonderTrade.createButtonPokemon(pokemon));
        });
        GooeyButton fill = GooeyButton.builder()
          .display(Utils.parseItemId(CobbleWonderTrade.language.getFill()))
          .title("")
          .build();

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

        GooeyButton close = GooeyButton.builder()
          .display(CobbleUtils.language.getItemClose().getItemStack())
          .title(AdventureTranslator.toNative(CobbleUtils.language.getItemClose().getDisplayname()))
          .lore(Component.class, AdventureTranslator.toNativeL(CobbleUtils.language.getItemClose().getLore()))
          .onClick((action) -> {
            try {
              UIManager.openUIForcefully(action.getPlayer(), WonderTrade.open(action.getPlayer()));
            } catch (NoPokemonStoreException e) {
              throw new RuntimeException(e);
            }
          })
          .build();

        PlaceholderButton placeholder = new PlaceholderButton();

        template.fill(fill)
          .rectangle(0, 0, 5, 9, placeholder)
          .fillFromList(buttons)
          .set(5, 4, close)
          .set(5, 0, previus)
          .set(5, 8, next);

        LinkedPage.Builder linkedPageBuilder = LinkedPage.builder()
          .title(WonderTradeUtil.toNative(CobbleWonderTrade.language.getTitlePc()));

        LinkedPage firstPage = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
        return firstPage;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).get();
  }
}


