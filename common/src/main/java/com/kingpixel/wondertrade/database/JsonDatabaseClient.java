package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.CommandTree;
import com.kingpixel.wondertrade.model.Pool;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * JSON file-based database client using UtilsFile for safe async and atomic disk operations.
 *
 * @author Carlos Varas Alonso
 */
public class JsonDatabaseClient extends DatabaseClient {
  private static final Path PATH_POOL = Path.of(CobbleWonderTrade.PATH_DATA, "pool.json");
  private static Pool pool;

  public JsonDatabaseClient(DataBaseConfig config) {
  }

  @Override
  public void connect() {
    CobbleWonderTrade.LOGGER.info("Connecting to JSON Database");
    createPool();
    fixPool();
  }

  @Override
  public void disconnect() {
    updatePool();
    CobbleWonderTrade.LOGGER.info("Disconnecting from JSON Database");
  }

  private void createPool() {
    try {
      pool = UtilsFile.readOrCreate(PATH_POOL, Pool.class, Pool::new);
      if (pool == null) {
        pool = new Pool();
      }
      pool.fix();
      UtilsFile.write(PATH_POOL, pool);
    } catch (IOException e) {
      CobbleWonderTrade.LOGGER.error("Error creating/reading pool.json", e);
      pool = new Pool();
    }
  }

  @Override
  public UserInfo getUserInfo(ServerPlayerEntity player) {
    if (player == null) return null;
    UserInfo userInfo = DatabaseClientFactory.userInfoMap.get(player.getUuid());
    if (userInfo != null) return userInfo;

    Path userPath = Path.of(CobbleWonderTrade.PATH_DATA_USER, player.getUuidAsString() + ".json");
    try {
      userInfo = UtilsFile.readOrCreate(userPath, UserInfo.class, () -> new UserInfo(player));
      if (userInfo == null) {
        userInfo = new UserInfo(player);
      }
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
      return userInfo;
    } catch (IOException e) {
      CobbleWonderTrade.LOGGER.error("Error loading user info for " + player.getUuidAsString(), e);
      userInfo = new UserInfo(player);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
      return userInfo;
    }
  }

  @Override
  public boolean shouldRestartPool() {
    if (!super.shouldRestartPool()) return false;
    return pool != null && !pool.hasCooldown();
  }

  @Override
  public Pokemon tradePokemon(ServerPlayerEntity player, Pokemon pokemon) {
    if (pool == null) fixPool();
    Pokemon trade = pool.tradePokemon(pokemon);
    updatePool();
    CommandTree.invalidateStats();
    return trade;
  }

  private void updatePool() {
    if (pool == null) return;
    if (CobbleWonderTrade.config.isDebug()) {
      CobbleWonderTrade.LOGGER.info("Saving WonderTrade pool to JSON");
    }
    UtilsFile.writeAsync(PATH_POOL, pool);
  }

  @Override
  public List<Pokemon> getPokemonsAnimation() {
    if (pool == null) return List.of();
    return pool.getPokemonsAnimation();
  }

  @Override
  public List<Pokemon> getAllPokemons() {
    if (pool == null) return List.of();
    return pool.getPokemons();
  }

  @Override
  public void restartPool() {
    CobbleWonderTrade.LOGGER.info("Restarting WonderTrade pool");
    pool = new Pool();
    try {
      UtilsFile.write(PATH_POOL, pool);
      CommandTree.invalidateStats();
    } catch (IOException e) {
      CobbleWonderTrade.LOGGER.error("Error saving restarted pool to JSON", e);
    }
  }

  @Override
  public void updateUserInfo(ServerPlayerEntity player, UserInfo userinfo) {
    if (player == null || userinfo == null) return;
    DatabaseClientFactory.userInfoMap.put(player.getUuid(), userinfo);
    Path userPath = Path.of(CobbleWonderTrade.PATH_DATA_USER, player.getUuidAsString() + ".json");
    UtilsFile.writeAsync(userPath, userinfo);
  }

  @Override
  public void fixPool() {
    if (pool == null) {
      pool = new Pool();
    }
    pool.fix();
    try {
      UtilsFile.write(PATH_POOL, pool);
      CommandTree.invalidateStats();
    } catch (IOException e) {
      CobbleWonderTrade.LOGGER.error("Error writing fixed pool to JSON", e);
    }
  }
}
