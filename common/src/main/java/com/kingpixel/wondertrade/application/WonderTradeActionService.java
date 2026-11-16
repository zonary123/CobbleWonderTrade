package com.kingpixel.wondertrade.application;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.AdvancedItemChance;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.api.action.WonderTradeActionResult;
import com.kingpixel.wondertrade.command.utils.WonderTradeUtils;
import com.kingpixel.wondertrade.database.DatabaseClient;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.joml.Vector4f;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Application service providing headless actions for WonderTrade:
 * executing trades, resetting cooldowns, and managing the Pokémon pool.
 */
public class WonderTradeActionService {

  public CompletableFuture<WonderTradeActionResult> trade(ServerPlayerEntity player, Pokemon pokemon) {
    return trade(player, pokemon, false);
  }

  public CompletableFuture<WonderTradeActionResult> trade(ServerPlayerEntity player, Pokemon pokemon, boolean showAnimation) {
    CompletableFuture<WonderTradeActionResult> future = new CompletableFuture<>();

    if (player == null) {
      future.complete(WonderTradeActionResult.failure(WonderTradeActionResult.Status.PLAYER_OFFLINE, "Player is offline or null"));
      return future;
    }

    if (pokemon == null) {
      future.complete(WonderTradeActionResult.failure(WonderTradeActionResult.Status.ERROR, "Selected Pokémon is null"));
      return future;
    }

    try {
      if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
        future.complete(WonderTradeActionResult.inBattle("Player is currently participating in a battle"));
        return future;
      }
    } catch (Throwable ignored) {}

    if (!pokemon.getTradeable()) {
      future.complete(WonderTradeActionResult.notTradeable("Selected Pokémon is marked as non-tradeable"));
      return future;
    }

    if (CobbleWonderTrade.config != null && CobbleWonderTrade.config.getPool() != null) {
      var blacklist = CobbleWonderTrade.config.getPool().getBlackList();
      if (blacklist != null && blacklist.isBlackListed(pokemon)) {
        future.complete(WonderTradeActionResult.banned("Selected Pokémon is blacklisted from WonderTrade"));
        return future;
      }

      int minLevel = CobbleWonderTrade.config.getPool().getMinlvreq();
      if (pokemon.getLevel() < minLevel) {
        future.complete(WonderTradeActionResult.minLevel("Pokémon level " + pokemon.getLevel() + " is below required min level " + minLevel));
        return future;
      }
    }

    DatabaseClient databaseClient = DatabaseClientFactory.databaseClient;
    if (databaseClient == null) {
      future.complete(WonderTradeActionResult.failure(WonderTradeActionResult.Status.ERROR, "Database client is not available"));
      return future;
    }

    UserInfo userInfo = databaseClient.getUserInfo(player);
    if (userInfo == null) {
      userInfo = new UserInfo(player);
    }

    if (userInfo.hasCooldown()) {
      long remainingMs = userInfo.getDate() - System.currentTimeMillis();
      future.complete(WonderTradeActionResult.cooldown("Player is on WonderTrade cooldown", remainingMs));
      return future;
    }

    final UserInfo finalUserInfo = userInfo;
    Runnable execution = () -> {
      try {
        finalUserInfo.setCooldown(player);
        databaseClient.updateUserInfo(player, finalUserInfo);

        Pokemon pokemonObtained;
        if (!CobbleWonderTrade.config.getPool().isIsrandom()) {
          pokemonObtained = databaseClient.tradePokemon(player, pokemon);
          if (pokemonObtained == null) {
            future.complete(WonderTradeActionResult.failure(WonderTradeActionResult.Status.NO_POKEMON_IN_POOL, "No Pokémon available in pool"));
            return;
          }
          WonderTradeUtils.invalidateStats();
          if (databaseClient.shouldRestartPool()) {
            databaseClient.restartPool();
            WonderTradeUtils.invalidateStats();
          }
        } else {
          pokemonObtained = generateRandomPoolPokemon();
          if (pokemonObtained == null) {
            future.complete(WonderTradeActionResult.failure(WonderTradeActionResult.Status.ERROR, "Failed to generate random Pokémon"));
            return;
          }
        }

        var party = Cobblemon.INSTANCE.getStorage().getParty(player);
        var pc = Cobblemon.INSTANCE.getStorage().getPC(player);

        if (party.remove(pokemon)) {
          party.add(pokemonObtained);
        } else {
          pc.remove(pokemon);
          if (!party.add(pokemonObtained)) {
            pc.add(pokemonObtained);
          }
        }

        sendMessagesAndNotifications(player, pokemon, pokemonObtained, showAnimation);

        future.complete(WonderTradeActionResult.success(
          "WonderTrade exchange successful",
          pokemon,
          pokemonObtained
        ));
      } catch (Exception e) {
        CobbleWonderTrade.LOGGER.error("Error executing WonderTrade action: " + e.getMessage(), e);
        future.complete(WonderTradeActionResult.failure(WonderTradeActionResult.Status.ERROR, "Trade failed: " + e.getMessage()));
      }
    };

    if (CobbleWonderTrade.server != null && !CobbleWonderTrade.server.isOnThread()) {
      CobbleWonderTrade.server.execute(execution);
    } else {
      execution.run();
    }

    return future;
  }

  private Pokemon generateRandomPoolPokemon() {
    Pokemon pokemonObtained = CobbleWonderTrade.config.getPool().getFilterGenerationPokemon().generateRandomPokemon(
      CobbleWonderTrade.MOD_ID,
      "pool"
    );

    if (pokemonObtained == null) return null;

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
    return pokemonObtained;
  }

  private void sendMessagesAndNotifications(ServerPlayerEntity player, Pokemon oldPokemon, Pokemon newPokemon, boolean showAnimation) {
    if (CobbleWonderTrade.language != null) {
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
    }

    WonderTradeUtils.sendWebHook(oldPokemon, newPokemon, player);

    if (showAnimation) {
      try {
        List<Pokemon> animations;
        if (CobbleWonderTrade.config.getPool().isIsrandom()) {
          animations = CobbleWonderTrade.config.getPool().getFilterGenerationPokemon().generateRandomPokemons(
            CobbleWonderTrade.MOD_ID,
            "pool",
            DatabaseClientFactory.POKEMON_ANIMATION_SIZE
          );
        } else {
          animations = DatabaseClientFactory.databaseClient != null
            ? databaseClient().getPokemonsAnimation()
            : List.of();
        }
        if (animations == null) animations = List.of();

        Vector4f tintNotObtained = new Vector4f(0.5f, 0.5f, 0.5f, 1);
        List<ItemStack> animationsItems = animations.stream()
          .filter(Objects::nonNull)
          .map(p -> PokemonItem.from(p, 1, tintNotObtained))
          .toList();

        Vector4f tintObtained = new Vector4f(1, 1, 1, 1);
        List<ItemStack> pokemonItem = List.of(PokemonItem.from(newPokemon, 1, tintObtained));
        AdvancedItemChance.initAnimation(
          CobbleWonderTrade.config.getAnimation(),
          player,
          animationsItems,
          pokemonItem
        );
      } catch (Throwable t) {
        CobbleWonderTrade.LOGGER.warn("Could not display animation: " + t.getMessage());
      }
    }
  }

  private DatabaseClient databaseClient() {
    return DatabaseClientFactory.databaseClient;
  }

  public CompletableFuture<WonderTradeActionResult> resetCooldown(ServerPlayerEntity player) {
    if (player == null) {
      return CompletableFuture.completedFuture(
        WonderTradeActionResult.failure(WonderTradeActionResult.Status.PLAYER_OFFLINE, "Player is null")
      );
    }
    return resetCooldown(player.getUuid());
  }

  public CompletableFuture<WonderTradeActionResult> resetCooldown(UUID playerUuid) {
    CompletableFuture<WonderTradeActionResult> future = new CompletableFuture<>();
    if (playerUuid == null) {
      future.complete(WonderTradeActionResult.failure(WonderTradeActionResult.Status.ERROR, "Player UUID is null"));
      return future;
    }

    UserInfo userInfo = new UserInfo();
    userInfo.setPlayeruuid(playerUuid);
    userInfo.setDate(0);
    userInfo.setMessagesend(false);
    DatabaseClientFactory.userInfoMap.put(playerUuid, userInfo);

    if (DatabaseClientFactory.databaseClient != null && CobbleWonderTrade.server != null && CobbleWonderTrade.server.getPlayerManager() != null) {
      ServerPlayerEntity player = CobbleWonderTrade.server.getPlayerManager().getPlayer(playerUuid);
      if (player != null) {
        DatabaseClientFactory.databaseClient.updateUserInfo(player, userInfo);
      }
    }

    future.complete(WonderTradeActionResult.success("Cooldown reset successfully for " + playerUuid, null, null));
    return future;
  }

  public CompletableFuture<WonderTradeActionResult> restartPool() {
    CompletableFuture<WonderTradeActionResult> future = new CompletableFuture<>();
    if (DatabaseClientFactory.databaseClient != null) {
      DatabaseClientFactory.databaseClient.restartPool();
      WonderTradeUtils.invalidateStats();
      future.complete(WonderTradeActionResult.success("WonderTrade pool reset successfully", null, null));
    } else {
      future.complete(WonderTradeActionResult.failure(WonderTradeActionResult.Status.ERROR, "Database client is not available"));
    }
    return future;
  }
}
