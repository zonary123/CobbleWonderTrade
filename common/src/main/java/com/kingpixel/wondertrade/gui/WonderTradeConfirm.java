package com.kingpixel.wondertrade.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.moves.Move;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;
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
      .lore(Text.class, AdventureTranslator.toNativeL(CobbleUtils.language.getItemConfirm().getLore()))
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
      .lore(Text.class, AdventureTranslator.toNativeL(PokemonUtils.replace(CobbleUtils.language.getLorepokemon()
        , pokemon)))
      .build();

    GooeyButton cancel = GooeyButton.builder()
      .display(CobbleUtils.language.getItemCancel().getItemStack())
      .title(AdventureTranslator.toNative(CobbleUtils.language.getItemCancel().getDisplayname()))
      .lore(Text.class, AdventureTranslator.toNativeL(CobbleUtils.language.getItemCancel().getLore()))
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

  public static boolean trade(ServerPlayerEntity player, Pokemon pokemonplayer) throws NoPokemonStoreException,
    ExecutionException, InterruptedException {

    if (PlayerUtils.isCooldown(DatabaseClientFactory.databaseClient.getUserInfo(player).getDate())) {
      player.sendMessage(WonderTradeUtil.toNative(CobbleWonderTrade.language.getMessagewondertradecooldown()
        .replace("%time%", PlayerUtils.getCooldown(new Date(DatabaseClientFactory.databaseClient.getUserInfo(player).getDate())))
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
      return false;
    }

    if (pokemonplayer == null) {
      player.sendMessage(WonderTradeUtil.toNative(CobbleWonderTrade.language.getMessageNoPokemonSlot()
        .replace("%prefix%", CobbleWonderTrade.language.getPrefix())));
      return false;
    }

    if (pokemonplayer.getShiny() && !CobbleWonderTrade.config.isAllowshiny()) return false;
    if (pokemonplayer.isLegendary() && !CobbleWonderTrade.config.isAllowlegendary()) return false;
    if (pokemonplayer.getLevel() < CobbleWonderTrade.config.getMinlvreq()) return false;

    PlayerPartyStore partyStorageSlot = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());

    if (CobbleWonderTrade.config.getPoketradeblacklist().contains(pokemonplayer.showdownId())) {
      player.sendMessage(WonderTradeUtil.toNative(CobbleWonderTrade.language.getMessagePokemonTradeBlackList()
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
        Cobblemon.INSTANCE.getStorage().getPC(player.getUuid()).remove(pokemonplayer);
      }
      partyStorageSlot.add(pokemongive);
    } else {
      if (pokemongive == null) return false;
      if (!partyStorageSlot.remove(pokemonplayer)) {
        Cobblemon.INSTANCE.getStorage().getPC(player.getUuid()).remove(pokemonplayer);
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

    player.sendMessage(WonderTradeUtil.toNative(PokemonUtils.replace(CobbleWonderTrade.language.getMessagewondertraderecieved(),
      pokemongive))
    );

    UserInfo userInfo = DatabaseClientFactory.databaseClient.getUserInfo(player);
    userInfo.setMessagesend(false);
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, CobbleWonderTrade.config.getCooldown(player));
    Date futureDate = calendar.getTime();
    userInfo.setDate(futureDate.getTime());

    DatabaseClientFactory.databaseClient.putUserInfo(userInfo);

    // WebHook message
    if (CobbleWonderTrade.config.getDiscord_webhook().isENABLED()) {
      WebhookMessage webhookMessage = new WebhookMessageBuilder()
        .setUsername(CobbleWonderTrade.config.getDiscord_webhook().getUSERNAME())
        .setAvatarUrl(CobbleWonderTrade.config.getDiscord_webhook().getAVATAR_URL())
        .addEmbeds(new WebhookEmbedBuilder()
          .setAuthor(new WebhookEmbed.EmbedAuthor(CobbleWonderTrade.config.getDiscord_webhook().getUSERNAME(), CobbleWonderTrade.config.getDiscord_webhook().getAVATAR_URL(), null))
          .setTitle(new WebhookEmbed.EmbedTitle(CobbleWonderTrade.language.getTitle_webhook(), null))
          .setTimestamp(new java.util.Date().toInstant())
          .setThumbnailUrl(getGif(pokemonplayer))
          .setColor(Integer.parseInt(CobbleWonderTrade.config.getDiscord_webhook().getCOLOR().substring(1), 16))
          .addField(new WebhookEmbed.EmbedField(true, "Pokemon Added", getValueField(player, pokemonplayer)))
          .addField(new WebhookEmbed.EmbedField(true, "Pokemon Removed", getValueField(player, pokemongive)))
          .setFooter(new WebhookEmbed.EmbedFooter(pokemongive.getDisplayName().getString(), getGif(pokemongive)))
          .build())
        .build();

      CobbleWonderTrade.webhookClient.send(webhookMessage);
    }
    return true;
  }

  private static String getForm(Pokemon pokemon){
    List<String> aspects = pokemon.getAspects().stream().toList();
    String form = "";
    int size = aspects.size();
    if (!aspects.isEmpty()) form = aspects.get(size == 1 ? 0 : size - 1).trim().toLowerCase();
    if (!pokemon.getForm().getName().equalsIgnoreCase("Normal") && (form.equalsIgnoreCase("male") || form.equalsIgnoreCase("female"))){
      return form;
    } else {
      return pokemon.getForm().getName().trim().toLowerCase();
    }
  }

  private static String getGif(Pokemon pokemon) {
    String url = "https://play.pokemonshowdown.com/sprites/%rute%/%pokemon%.gif";
    String form = pokemon.getForm().getName().trim().toLowerCase();
    String pokemonid = pokemon.getSpecies().showdownId().trim().toLowerCase();

    if (CobbleWonderTrade.config.isDebug()){
      CobbleWonderTrade.LOGGER.info("Pokemon ID: " + pokemonid);
      CobbleWonderTrade.LOGGER.info("Form: " + getForm(pokemon));
    }

    if (pokemon.getShiny()) {
      url = url.replace("%rute%", "ani-shiny");
    } else {
      url = url.replace("%rute%", "ani");
    }

    if (form.isEmpty() || form.equalsIgnoreCase("Normal")) {
      url = url.replace("%pokemon%", pokemonid);
    } else {
      url = url.replace("%pokemon%", pokemonid + "-" + getForm(pokemon));
    }
    if (CobbleWonderTrade.config.isDebug()){
      CobbleWonderTrade.LOGGER.info("Gif URL: " + url);
    }
    return url;
  }

  private static String getValueField(ServerPlayerEntity player, Pokemon pokemon) {
    List<String> message = new ArrayList<>(CobbleWonderTrade.language.getMessage_WebHook());
    message.replaceAll(s -> s
      .replace("%prefix%", CobbleWonderTrade.language.getPrefix())
      .replace("%player%", player.getGameProfile().getName())
      .replace("%pokemon%", pokemon.getDisplayName().getString())
      .replace("%ability%", pokemon.getAbility().getName())
      .replace("%nature%", pokemon.getNature().getName().getNamespace())
      .replace("%move1%", getMove(pokemon.getMoveSet().get(0)))
      .replace("%move2%", getMove(pokemon.getMoveSet().get(1)))
      .replace("%move3%", getMove(pokemon.getMoveSet().get(2)))
      .replace("%move4%", getMove(pokemon.getMoveSet().get(3)))
      .replace("&", "§")
      .replaceAll("$.", "")
      .replaceAll("<.*?>", "")
    );
    return PokemonUtils.replace(String.join("\n", message), pokemon);
  }

  private static String getMove(Move move) {
    return move == null ? "None" : move.getName();
  }
}
