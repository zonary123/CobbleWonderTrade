package com.kingpixel.wondertrade.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.ButtonClick;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author Carlos Varas Alonso - 26/05/2024 4:04
 */
public class WonderTrade {
  public static GooeyPage open(ServerPlayerEntity player) throws NoPokemonStoreException {
    try {
      GooeyButton fill = GooeyButton.builder()
        .display(Utils.parseItemId(CobbleWonderTrade.language.getFill()))
        .with(DataComponentTypes.CUSTOM_NAME,AdventureTranslator.toNative(""))
        .build();

      UserInfo userInfo =
        DatabaseClientFactory.databaseClient.getUserInfo(player);

      List<Pokemon> pokemons = DatabaseClientFactory.databaseClient.getSpecialPool(false);

      PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

      GooeyButton poke1 = createButtonPokemon(partyStore.get(0));
      GooeyButton poke2 = createButtonPokemon(partyStore.get(1));
      GooeyButton poke3 = createButtonPokemon(partyStore.get(2));

      List<String> loreinfo = new ArrayList<>(CobbleWonderTrade.language.getInfo().getLore());
      loreinfo.replaceAll(s -> s.replace("%time%", PlayerUtils.getCooldown(new Date(userInfo.getDate())))
        .replace("%shinys%", pokemons.stream().filter(Pokemon::getShiny).count() + "")
        .replace("%legends%", pokemons.stream().filter(Pokemon::isLegendary).count() + "")
        .replace("%ultrabeast%", pokemons.stream().filter(Pokemon::isUltraBeast).count() + "")
        .replace("%paradox%", pokemons.stream()
          .filter(pokemon -> pokemon.getForm().getSpecies().getLabels().contains("paradox")).count() + "")
        .replace("%ivs%",
          pokemons.stream().filter(pokemon -> PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31).count() + ""));

      GooeyButton info = GooeyButton.builder()
        .display(CobbleWonderTrade.language.getInfo().getItemStack())
        .with(DataComponentTypes.ITEM_NAME,
          AdventureTranslator.toNative(CobbleWonderTrade.language.getInfo().getDisplayname()))
        .with(DataComponentTypes.LORE,new LoreComponent( AdventureTranslator.toNativeL(loreinfo)))
        .onClick(action -> {
          if (CobbleWonderTrade.config.isPoolview()) {
            if (action.getClickType() == ButtonClick.LEFT_CLICK) {
              try {
                UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradePool.open(false)));
              } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
              }
            } else {
              try {
                UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradePool.open(true)));
              } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
              }
            }
          } else {
            action.getPlayer().sendMessage(WonderTradeUtil.toNative(CobbleWonderTrade.language.getMessageNoPoolView()
              .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
          }
        })
        .build();

      GooeyButton pc = GooeyButton.builder()
        .display(CobbleWonderTrade.language.getPc().getItemStack())
        .with(DataComponentTypes.ITEM_NAME,
          AdventureTranslator.toNative(CobbleWonderTrade.language.getPc().getDisplayname()))
        .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(CobbleWonderTrade.language.getPc().getLore())))
        .onClick(action -> {
          try {
            UIManager.openUIForcefully(action.getPlayer(), Objects.requireNonNull(WonderTradePC.open(action.getPlayer())));
          } catch (ExecutionException | InterruptedException e) {
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

      GooeyPage page = GooeyPage
        .builder()
        .template(template)
        .title(AdventureTranslator.toNative(CobbleWonderTrade.language.getTitle())).build();
      page.update();
      return page;
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
    return null;
  }

  public static GooeyButton createButtonPokemon(Pokemon pokemon) {

    try {
      if (pokemon == null) {
        return GooeyButton.builder()
          .display(CobbleUtils.language.getItemNoPokemon().getItemStack())
          .with(DataComponentTypes.ITEM_NAME,
            AdventureTranslator.toNative(CobbleUtils.language.getItemNoPokemon().getDisplayname()))
          .with(DataComponentTypes.LORE,
            new LoreComponent(AdventureTranslator.toNativeL(CobbleUtils.language.getItemNoPokemon().getLore())))
          .build();
      }
      if (CobbleWonderTrade.config.getPoketradeblacklist().contains(pokemon.showdownId())) {
        return GooeyButton.builder()
          .display(CobbleWonderTrade.language.getItemnotallowpokemon().getItemStack())
          .with(DataComponentTypes.ITEM_NAME,
            AdventureTranslator.toNative(CobbleWonderTrade.language.getItemnotallowpokemon().getDisplayname()))
          .with(DataComponentTypes.LORE,
            new LoreComponent(AdventureTranslator.toNativeL(PokemonUtils.replace(CobbleWonderTrade.language.getItemnotallowpokemon().getLore(), pokemon))))
          .build();
      }

      if (pokemon.getShiny() && !CobbleWonderTrade.config.isAllowshiny()) {
        return GooeyButton.builder()
          .display(CobbleWonderTrade.language.getItemnotallowshiny().getItemStack())
          .with(DataComponentTypes.ITEM_NAME,
            AdventureTranslator.toNative(CobbleWonderTrade.language.getItemnotallowshiny().getDisplayname()))
          .with(DataComponentTypes.LORE,
            new LoreComponent(AdventureTranslator.toNativeL(PokemonUtils.replace(CobbleWonderTrade.language.getItemnotallowshiny().getLore(),
              pokemon))))
          .build();
      }

      if (pokemon.isLegendary() && !CobbleWonderTrade.config.isAllowlegendary()) {
        return GooeyButton.builder()
          .display(CobbleWonderTrade.language.getItemnotallowlegendary().getItemStack())
          .with(DataComponentTypes.ITEM_NAME,
            AdventureTranslator.toNative(CobbleWonderTrade.language.getItemnotallowlegendary().getDisplayname()))
          .with(DataComponentTypes.LORE,
            new LoreComponent(AdventureTranslator.toNativeL(CobbleWonderTrade.language.getItemnotallowlegendary().getLore())))
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
          .with(DataComponentTypes.ITEM_NAME,
            AdventureTranslator.toNative(PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(),
            pokemon)))
          .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(loreinfolevel)))
          .onClick(action -> {
            action.getPlayer().sendMessage(WonderTradeUtil.toNative(
              PokemonUtils.replace(
                CobbleWonderTrade.language.getMessageThePokemonNotHaveMinLevel()
                  .replace("%minlevel%", String.valueOf(CobbleWonderTrade.config.getMinlv())),
                pokemon
              )
            ));
          })
          .build();
      }
      return GooeyButton.builder()
        .display(PokemonItem.from(pokemon))
        .with(DataComponentTypes.ITEM_NAME,
          AdventureTranslator.toNative(PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(), pokemon)))
        .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(loreinfolevel)))
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
