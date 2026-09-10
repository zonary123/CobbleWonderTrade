package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:02
 */
public abstract class DatabaseClient {
  public abstract void connect();

  public abstract void disconnect();

  public abstract UserInfo getUserInfo(ServerPlayerEntity player);

  public abstract void updateUserInfo(ServerPlayerEntity player, UserInfo userinfo);

  public abstract void fixPool();

  public boolean shouldRestartPool() {
    return CobbleWonderTrade.config.getPool().isAutoReset() && !CobbleWonderTrade.config.getPool().isIsrandom()
      && CobbleWonderTrade.config.getPool().getCooldownReset() > 0;
  }

  public abstract void restartPool();

  public abstract Pokemon tradePokemon(ServerPlayerEntity player, Pokemon pokemon);

  public abstract List<Pokemon> getPokemonsAnimation();

  public void removeIfNecessary(ServerPlayerEntity player) {
    DatabaseClientFactory.userInfoMap.remove(player.getUuid());
  }

  public abstract List<Pokemon> getAllPokemons();
}
