package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:02
 */
public interface DatabaseClient {
  void connect();

  List<Pokemon> getSpecialPool(boolean special);

  List<JsonObject> getPokemonList(boolean special);

  Pokemon getRandomPokemon();

  Pokemon putPokemon(Pokemon pokemon);

  UserInfo getUserInfo(ServerPlayerEntity player);

  UserInfo getUserinfo(UUID uuid);

  UserInfo putUserInfo(UserInfo userInfo);

  UserInfo putUserInfo(UserInfo userInfo, boolean update);

  void resetPool(boolean force);

  void disconnect();

  void save();
}
