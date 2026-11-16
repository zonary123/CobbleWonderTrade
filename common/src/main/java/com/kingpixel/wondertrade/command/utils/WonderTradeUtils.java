package com.kingpixel.wondertrade.command.utils;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.ui.PartyPcMenu;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.cobbleutils.util.UIUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.api.ui.WonderTradeMenuRegistry;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for WonderTrade UI rendering, Pokémon exchange processing, and stats calculations.
 *
 * @author Carlos Varas Alonso
 */
public class WonderTradeUtils {

  private static volatile PokemonStats cachedStats;

  private WonderTradeUtils() {}

  public static PokemonStats getPokemonStats() {
    PokemonStats stats = cachedStats;
    if (stats == null) {
      synchronized (WonderTradeUtils.class) {
        stats = cachedStats;
        if (stats == null) {
          var databaseClient = DatabaseClientFactory.databaseClient;
          var pokemons = databaseClient != null ? databaseClient.getAllPokemons() : List.<Pokemon>of();
          stats = calculatePokemonStats(pokemons);
          cachedStats = stats;
        }
      }
    }
    return stats;
  }

  public static void invalidateStats() {
    cachedStats = null;
  }

  public static void open(ServerPlayerEntity player) {
    if (player == null) return;
    var battle = BattleRegistry.getBattleByParticipatingPlayer(player);
    if (battle != null) return;

    var build = PartyPcMenu.builder()
      .setPlayer(player)
      .setTemplateConsumer(template -> {
        if (CobbleWonderTrade.config.getPool().isIsrandom()) return;
        long currentTime = System.currentTimeMillis();
        ItemModel itemModelInfo = CobbleWonderTrade.language.getInfo();
        if (UIUtils.isInside(itemModelInfo, CobbleWonderTrade.language.getPartyPcMenu().getRowsParty()) && CobbleWonderTrade.config.getPool().isPoolview()) {
          var stats = getPokemonStats();
          var databaseClient = DatabaseClientFactory.databaseClient;
          var userinfo = databaseClient != null ? databaseClient.getUserInfo(player) : null;
          List<String> lore = prepareLore(CobbleWonderTrade.language.getInfo().getLore(), stats, userinfo);

          GooeyButton button = itemModelInfo.getButton(1, itemModelInfo.getDisplayname(), lore, action -> {
            CobbleWonderTrade.ASYNC.runAsync(() -> {
              if (!PermissionApi.hasPermission(player, List.of(CobbleWonderTrade.MOD_ID + ".admin", CobbleWonderTrade.MOD_ID + ".info"), 2)) {
                PlayerUtils.sendMessage(
                  player,
                  CobbleWonderTrade.language.getNoPermission(),
                  CobbleWonderTrade.language.getPrefix(),
                  TypeMessage.CHAT
                );
                return;
              }
              List<Pokemon> list;
              switch (action.getClickType()) {
                case RIGHT_CLICK, SHIFT_RIGHT_CLICK -> list = stats.getSpecial();
                default -> list = stats.getPokemons();
              }
              WonderTradeMenuRegistry.openPoolMenu(player, list);
            });
          });
          itemModelInfo.applyTemplate((ChestTemplate) template, button);
        }
        long time = System.currentTimeMillis() - currentTime;
        if (CobbleWonderTrade.config.isDebug()) {
          CobbleWonderTrade.LOGGER.info("Info render time: " + time + "ms");
        }
      })
      .setPokemonAction(action -> handlePokemonAction(player, action.getPokemon()))
      .setPartyPcMenu(CobbleWonderTrade.language.getPartyPcMenu())
      .setConfirmMenu(CobbleWonderTrade.language.getConfirmMenu())
      .setCloseAction(close -> open(player))
      .setCustomFilter(pokemon -> !pokemon.getTradeable())
      .setBlackList(CobbleWonderTrade.config.getPool().getBlackList())
      .build();

    build.getPartyPcMenu().openParty(build);
  }

  public static PokemonStats calculatePokemonStats(List<Pokemon> pokemons) {
    int shinys = 0;
    int legendaries = 0;
    int mythicals = 0;
    int ultraBeasts = 0;
    int paradoxes = 0;
    int ivs31 = 0;
    List<Pokemon> special = new ArrayList<>();

    if (pokemons == null || pokemons.isEmpty()) {
      return new PokemonStats(0, 0, 0, 0, 0, 0, special, List.of());
    }

    for (Pokemon pokemon : pokemons) {
      if (pokemon == null) continue;
      boolean isSpecial = false;
      if (pokemon.getShiny()) {
        shinys++;
        isSpecial = true;
      }
      if (pokemon.isLegendary()) {
        legendaries++;
        isSpecial = true;
      }
      if (pokemon.isMythical()) {
        mythicals++;
        isSpecial = true;
      }
      if (pokemon.isUltraBeast()) {
        ultraBeasts++;
        isSpecial = true;
      }
      boolean isParadox = pokemon.getForm() != null && pokemon.getForm().getLabels() != null
        && pokemon.getForm().getLabels().contains(CobblemonPokemonLabels.PARADOX);
      if (isParadox) {
        paradoxes++;
        isSpecial = true;
      }
      if (pokemon.getIvs() != null && PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31) {
        ivs31++;
        isSpecial = true;
      }
      if (isSpecial) {
        special.add(pokemon);
      }
    }

    return new PokemonStats(shinys, legendaries, mythicals, ultraBeasts, paradoxes, ivs31, special, pokemons);
  }

  public static List<String> prepareLore(List<String> loreTemplate, PokemonStats stats, UserInfo userinfo) {
    String cooldown = userinfo != null ? PlayerUtils.getCooldown(userinfo.getDate()) : "";
    return loreTemplate.stream()
      .map(s ->
        prepareLore(s, stats)
          .replace("%cooldown%", cooldown)
          .replace("%time%", cooldown))
      .toList();
  }

  public static String prepareLore(String s, PokemonStats stats) {
    return s
      .replace("%shinys%", String.valueOf(stats.getShinys()))
      .replace("%legends%", String.valueOf(stats.getLegendaries()))
      .replace("%mythicals%", String.valueOf(stats.getMythicals()))
      .replace("%ultrabeast%", String.valueOf(stats.getUltraBeasts()))
      .replace("%paradox%", String.valueOf(stats.getParadoxes()))
      .replace("%ivs%", String.valueOf(stats.getIvs31()));
  }

  public static void handlePokemonAction(ServerPlayerEntity player, Pokemon pokemon) {
    long currentTime = System.currentTimeMillis();
    var databaseClient = DatabaseClientFactory.databaseClient;
    if (databaseClient == null) return;

    UserInfo userInfo = databaseClient.getUserInfo(player);
    if (userInfo == null) {
      userInfo = new UserInfo(player);
    }

    if (userInfo.hasCooldown()) {
      sendCooldownMessage(player, userInfo);
      UIManager.closeUI(player);
      return;
    }

    if (pokemon.getLevel() < CobbleWonderTrade.config.getPool().getMinlvreq()) {
      sendMinLevelMessage(player, pokemon);
      return;
    }

    UIManager.closeUI(player);
    userInfo.setCooldown(player);
    databaseClient.updateUserInfo(player, userInfo);
    Pokemon pokemonObtained;
    if (!CobbleWonderTrade.config.getPool().isIsrandom()) {
      pokemonObtained = databaseClient.tradePokemon(player, pokemon);
      if (pokemonObtained == null) {
        CobbleWonderTrade.LOGGER.warn("Trade aborted: no Pokémon returned from pool");
        return;
      }
      invalidateStats();
      if (databaseClient.shouldRestartPool()) {
        if (CobbleWonderTrade.config.isDebug()) {
          CobbleWonderTrade.LOGGER.info("Resetting Pool");
        }
        databaseClient.restartPool();
        invalidateStats();
      }
    } else {
      pokemonObtained = CobbleWonderTrade.config.getPool().getFilterGenerationPokemon().generateRandomPokemon(
        CobbleWonderTrade.MOD_ID,
        "pool"
      );

      if (pokemonObtained == null) {
        CobbleWonderTrade.LOGGER.warn("Trade aborted: generation returned null Pokémon");
        return;
      }

      if (CobbleWonderTrade.config.getRates().getParadoxrate() > 0) {
        int paradox = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getParadoxrate());
        boolean isParadox = pokemonObtained.getForm() != null && pokemonObtained.getForm().getLabels() != null
          && pokemonObtained.getForm().getLabels().contains(CobblemonPokemonLabels.PARADOX);
        if (paradox == 0 && !isParadox) {
          Pokemon newPokemon = DatabaseClientFactory.getParadox();
          if (newPokemon != null) {
            pokemonObtained = newPokemon;
            DatabaseClientFactory.applyPerfectIvs(pokemonObtained, CobbleWonderTrade.config.getRates().getParadoxperfectivs());
          }
        }
      }

      if (CobbleWonderTrade.config.getRates().getUltrabeastrate() > 0) {
        int ultraBeast = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getUltrabeastrate());
        boolean isUltraBeast = pokemonObtained.getForm() != null && pokemonObtained.getForm().getLabels() != null
          && pokemonObtained.getForm().getLabels().contains(CobblemonPokemonLabels.ULTRA_BEAST);
        if (ultraBeast == 0 && !isUltraBeast) {
          Pokemon newPokemon = DatabaseClientFactory.getUltraBeast();
          if (newPokemon != null) {
            pokemonObtained = newPokemon;
            DatabaseClientFactory.applyPerfectIvs(pokemonObtained, CobbleWonderTrade.config.getRates().getUltrabeastperfectivs());
          }
        }
      }

      if (CobbleWonderTrade.config.getRates().getLegendaryrate() > 0) {
        int legendary = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getLegendaryrate());
        boolean isLegendary = pokemonObtained.getForm() != null && pokemonObtained.getForm().getLabels() != null
          && pokemonObtained.getForm().getLabels().contains(CobblemonPokemonLabels.LEGENDARY);
        if (legendary == 0 && !isLegendary) {
          Pokemon newPokemon = DatabaseClientFactory.getLegendary();
          if (newPokemon != null) {
            pokemonObtained = newPokemon;
            DatabaseClientFactory.applyPerfectIvs(pokemonObtained, CobbleWonderTrade.config.getRates().getLegendaryperfectivs());
          }
        }
      }

      if (CobbleWonderTrade.config.getRates().getMythicalrate() > 0) {
        int mythical = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getMythicalrate());
        boolean isMythical = pokemonObtained.getForm() != null && pokemonObtained.getForm().getLabels() != null
          && pokemonObtained.getForm().getLabels().contains(CobblemonPokemonLabels.MYTHICAL);
        if (mythical == 0 && !isMythical) {
          Pokemon newPokemon = DatabaseClientFactory.getMythical();
          if (newPokemon != null) {
            pokemonObtained = newPokemon;
            DatabaseClientFactory.applyPerfectIvs(pokemonObtained, CobbleWonderTrade.config.getRates().getMythicalperfectivs());
          }
        }
      }

      if (CobbleWonderTrade.config.getRates().getShinyrate() > 0) {
        int shiny = Utils.getRandom().nextInt(CobbleWonderTrade.config.getRates().getShinyrate());
        if (shiny == 0) {
          pokemonObtained.setShiny(true);
        }
      }

      DatabaseClientFactory.setLevel(pokemonObtained);
    }

    updatePlayerStorage(player, pokemon, pokemonObtained);
    long time = System.currentTimeMillis() - currentTime;
    if (CobbleWonderTrade.config.isDebug()) {
      CobbleWonderTrade.LOGGER.info("Trade completed in " + time + "ms");
    }
  }

  public static void sendCooldownMessage(ServerPlayerEntity player, UserInfo userInfo) {
    String cooldown = PlayerUtils.getCooldown(userInfo.getDate());
    PlayerUtils.sendMessage(
      player,
      CobbleWonderTrade.language.getMessagewondertradecooldown()
        .replace("%cooldown%", cooldown)
        .replace("%time%", cooldown),
      CobbleWonderTrade.language.getPrefix(),
      TypeMessage.CHAT
    );
  }

  public static void sendMinLevelMessage(ServerPlayerEntity player, Pokemon pokemon) {
    PlayerUtils.sendMessage(
      player,
      PokemonUtils.replace(CobbleWonderTrade.language.getMessageThePokemonNotHaveMinLevel(), pokemon)
        .replace("%minlevel%", String.valueOf(CobbleWonderTrade.config.getPool().getMinlvreq())),
      CobbleWonderTrade.language.getPrefix(),
      TypeMessage.CHAT
    );
  }

  public static void updatePlayerStorage(ServerPlayerEntity player, Pokemon oldPokemon, Pokemon newPokemon) {
    PlayerUtils.sendMessage(
      player,
      PokemonUtils.replace(CobbleWonderTrade.language.getMessagePokemonToWondertrade(), oldPokemon)
        .replace("%player%", player.getGameProfile().getName()),
      CobbleWonderTrade.language.getPrefix(),
      TypeMessage.BROADCAST
    );

    PlayerUtils.sendMessage(
      player,
      PokemonUtils.replace(CobbleWonderTrade.language.getMessagewondertraderecieved(), newPokemon)
        .replace("%player%", player.getGameProfile().getName()),
      CobbleWonderTrade.language.getPrefix(),
      TypeMessage.CHAT
    );

    sendWebHook(oldPokemon, newPokemon, player);
    List<Pokemon> animations;
    if (CobbleWonderTrade.config.getPool().isIsrandom()) {
      animations = CobbleWonderTrade.config.getPool().getFilterGenerationPokemon().generateRandomPokemons(
        CobbleWonderTrade.MOD_ID,
        "pool",
        DatabaseClientFactory.POKEMON_ANIMATION_SIZE
      );
    } else {
      animations = DatabaseClientFactory.databaseClient != null
        ? DatabaseClientFactory.databaseClient.getPokemonsAnimation()
        : List.of();
    }
    if (animations == null) animations = List.of();

    var tintNotObtained = new Vector4f(0.5f, 0.5f, 0.5f, 1);
    var animationsItems = animations.stream()
      .filter(p -> p != null)
      .map(pokemon -> PokemonItem.from(pokemon, 1, tintNotObtained))
      .toList();

    var tintObtained = new Vector4f(1, 1, 1, 1);
    List<ItemStack> pokemonItem = new ArrayList<>();
    pokemonItem.add(PokemonItem.from(newPokemon, 1, tintObtained));
    AdvancedItemChance.initAnimation(
      CobbleWonderTrade.config.getAnimation(),
      player,
      animationsItems,
      pokemonItem
    );

    var party = Cobblemon.INSTANCE.getStorage().getParty(player);
    var pc = Cobblemon.INSTANCE.getStorage().getPC(player);

    if (party.remove(oldPokemon)) {
      party.add(newPokemon);
    } else {
      pc.remove(oldPokemon);
      if (!party.add(newPokemon)) {
        pc.add(newPokemon);
      }
    }
  }

  public static void sendWebHook(Pokemon oldPokemon, Pokemon newPokemon, ServerPlayerEntity player) {
    if (!CobbleWonderTrade.config.getDiscord_webhook().isENABLED()) return;
    var webHook = CobbleWonderTrade.config.getDiscord_webhook();
    boolean specialPut = oldPokemon.getShiny() || CobbleWonderTrade.config.getPool().getSpecialPokemons().isBlackListed(oldPokemon);
    if (!specialPut) {
      webHook.sendWebHook(CobbleWonderTrade.MOD_ID, CobbleWonderTrade.language.getWebHookPutPool(), List.of(player), List.of(oldPokemon));
    } else {
      webHook.sendWebHook(CobbleWonderTrade.MOD_ID, CobbleWonderTrade.language.getWebHookSpecialPutPool(), List.of(player), List.of(oldPokemon));
    }
    boolean specialObtained = newPokemon.getShiny() || CobbleWonderTrade.config.getPool().getSpecialPokemons().isBlackListed(newPokemon);
    if (!specialObtained) {
      webHook.sendWebHook(CobbleWonderTrade.MOD_ID, CobbleWonderTrade.language.getWebHookObtainedPool(), List.of(player), List.of(newPokemon));
    } else {
      webHook.sendWebHook(CobbleWonderTrade.MOD_ID, CobbleWonderTrade.language.getWebHookSpecialObtainedPool(), List.of(player), List.of(newPokemon));
    }
  }
}
