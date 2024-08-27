package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:02
 */
public interface DatabaseClient {
  void connect();

  CompletableFuture<List<Pokemon>> getSpecialPool(boolean special);

  CompletableFuture<List<JsonObject>> getPokemonList(boolean special);

  Pokemon getRandomPokemon();

  CompletableFuture<Pokemon> putPokemon(Pokemon pokemon);

  CompletableFuture<UserInfo> getUserInfo(ServerPlayer player);

  CompletableFuture<UserInfo> getUserinfo(UUID uuid);

  CompletableFuture<UserInfo> putUserInfo(UserInfo userInfo);

  CompletableFuture<UserInfo> putUserInfo(UserInfo userInfo, boolean update);

  void resetPool(boolean force);

  void disconnect();

  void save();
}
