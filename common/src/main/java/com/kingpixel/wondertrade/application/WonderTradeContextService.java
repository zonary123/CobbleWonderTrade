package com.kingpixel.wondertrade.application;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.api.model.PlayerWonderTradeContext;
import com.kingpixel.wondertrade.api.model.WonderTradePoolSummaryView;
import com.kingpixel.wondertrade.command.utils.PokemonStats;
import com.kingpixel.wondertrade.command.utils.WonderTradeUtils;
import com.kingpixel.wondertrade.database.DatabaseClient;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Application service providing query and projection views for WonderTrade.
 */
public class WonderTradeContextService {

  public CompletableFuture<PlayerWonderTradeContext> getPlayerContext(ServerPlayerEntity player) {
    if (player == null) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.supplyAsync(() -> {
      DatabaseClient client = DatabaseClientFactory.databaseClient;
      UserInfo userInfo = client != null ? client.getUserInfo(player) : null;
      if (userInfo == null) {
        userInfo = new UserInfo(player);
      }

      boolean hasCooldown = userInfo.hasCooldown();
      long expiresAt = userInfo.getDate();
      long remainingSeconds = hasCooldown ? Math.max(0, (expiresAt - System.currentTimeMillis()) / 1000) : 0;
      String formattedCooldown = PlayerUtils.getCooldown(expiresAt);

      int minLevel = CobbleWonderTrade.config != null && CobbleWonderTrade.config.getPool() != null
        ? CobbleWonderTrade.config.getPool().getMinlvreq()
        : 1;

      WonderTradePoolSummaryView summary = buildPoolSummary();

      return new PlayerWonderTradeContext(
        player.getUuid(),
        player.getGameProfile().getName(),
        hasCooldown,
        expiresAt,
        remainingSeconds,
        formattedCooldown,
        minLevel,
        !hasCooldown,
        summary
      );
    });
  }

  public CompletableFuture<WonderTradePoolSummaryView> getPoolSummary() {
    return CompletableFuture.supplyAsync(this::buildPoolSummary);
  }

  public CompletableFuture<List<Pokemon>> getPoolPokemons() {
    return CompletableFuture.supplyAsync(() -> {
      DatabaseClient client = DatabaseClientFactory.databaseClient;
      if (client == null) return Collections.emptyList();
      List<Pokemon> pokemons = client.getAllPokemons();
      return pokemons != null ? pokemons : Collections.emptyList();
    });
  }

  private WonderTradePoolSummaryView buildPoolSummary() {
    PokemonStats stats = WonderTradeUtils.getPokemonStats();
    int total = stats != null && stats.getPokemons() != null ? stats.getPokemons().size() : 0;
    int shinies = stats != null ? stats.getShinys() : 0;
    int legendaries = stats != null ? stats.getLegendaries() : 0;
    int mythicals = stats != null ? stats.getMythicals() : 0;
    int ultraBeasts = stats != null ? stats.getUltraBeasts() : 0;
    int paradoxes = stats != null ? stats.getParadoxes() : 0;
    int perfectIvs = stats != null ? stats.getIvs31() : 0;

    boolean autoReset = CobbleWonderTrade.config != null && CobbleWonderTrade.config.getPool() != null
      && CobbleWonderTrade.config.getPool().isAutoReset();
    long resetMinutes = CobbleWonderTrade.config != null && CobbleWonderTrade.config.getPool() != null
      ? CobbleWonderTrade.config.getPool().getCooldownReset()
      : 0;
    boolean isRandom = CobbleWonderTrade.config != null && CobbleWonderTrade.config.getPool() != null
      && CobbleWonderTrade.config.getPool().isIsrandom();

    return new WonderTradePoolSummaryView(
      total,
      shinies,
      legendaries,
      mythicals,
      ultraBeasts,
      paradoxes,
      perfectIvs,
      autoReset,
      resetMinutes,
      isRandom
    );
  }
}
