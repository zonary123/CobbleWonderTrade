package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.model.Pool;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 16/04/2025 18:28
 */
public class JsonDatabaseClient extends DatabaseClient {
  private static final String PATH_POOL = CobbleWonderTrade.PATH_DATA + "pool.json";
  private static Pool pool;

  public JsonDatabaseClient(DataBaseConfig config) {
  }

  @Override public void connect() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Connecting to JSON Database");
    createPool();
    fixPool();
  }

  @Override public void disconnect() {
    updatePool();
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Disconnecting from JSON Database");
  }

  private void createPool() {
    File folder = Utils.getAbsolutePath(CobbleWonderTrade.PATH_DATA);
    if (!folder.exists()) folder.mkdirs();
    var futureRead = Utils.readFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json", call -> {
      Pool pool = Utils.newWithoutSpacingGson().fromJson(call, Pool.class);
      if (pool == null) {
        CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Creating new pool.json file");
        pool = new Pool();
        Utils.writeFileSync(Utils.getAbsolutePath(PATH_POOL), Utils.newWithoutSpacingGson().toJson(pool));
        JsonDatabaseClient.pool = pool;
        return;
      }
      pool.fix();
      JsonDatabaseClient.pool = pool;
      Utils.writeFileSync(Utils.getAbsolutePath(PATH_POOL), Utils.newWithoutSpacingGson().toJson(pool));
    });

    if (!futureRead.join()) {
      CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Creating new pool.json file");
      JsonDatabaseClient.pool = new Pool();
      Utils.writeFileSync(Utils.getAbsolutePath(PATH_POOL), Utils.newWithoutSpacingGson().toJson(JsonDatabaseClient.pool));
    }
  }


  @Override public UserInfo getUserInfo(ServerPlayerEntity player) {
    var userInfo = DatabaseClientFactory.userInfoMap.get(player.getUuid());
    if (userInfo != null) return userInfo;
    var file = Utils.getAbsolutePath(CobbleWonderTrade.PATH_DATA_USER + player.getUuidAsString() + ".json");
    var futureRead = Utils.readFileSync(file, call -> {
      UserInfo readUserInfo = Utils.newWithoutSpacingGson().fromJson(call, UserInfo.class);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), readUserInfo);
    });

    if (!futureRead) {
      UserInfo newUserInfo = new UserInfo(player);
      updateUserInfo(player, newUserInfo);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), newUserInfo);
    }
    return null;
  }

  @Override public boolean shouldRestartPool() {
    if (!super.shouldRestartPool()) return false;
    return !pool.hasCooldown();
  }

  @Override public Pokemon tradePokemon(ServerPlayerEntity player, Pokemon pokemon) {
    var trade = pool.tradePokemon(pokemon);
    updatePool();
    return trade;
  }

  private void updatePool() {
    if (CobbleWonderTrade.config.isDebug()) {
      CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Updating pool");
    }
    Utils.writeFileSync(Utils.getAbsolutePath(PATH_POOL), Utils.newWithoutSpacingGson().toJson(pool));
  }

  @Override public List<Pokemon> getPokemonsAnimation() {
    return pool.getPokemonsAnimation();
  }

  @Override public List<Pokemon> getAllPokemons() {
    return pool.getPokemons();
  }

  @Override public void restartPool() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Restarting pool");
    pool = new Pool();
    Utils.writeFileSync(Utils.getAbsolutePath(PATH_POOL), Utils.newWithoutSpacingGson().toJson(pool));
  }

  @Override public void updateUserInfo(ServerPlayerEntity player, UserInfo userinfo) {
    DatabaseClientFactory.userInfoMap.put(player.getUuid(), userinfo);
    var file = Utils.getAbsolutePath(CobbleWonderTrade.PATH_DATA_USER + player.getUuidAsString() + ".json");
    Utils.writeFileSync(file, Utils.newWithoutSpacingGson().toJson(userinfo));
  }

  @Override public void fixPool() {
    if (pool == null) new Pool();
    pool.fix();
  }
}
