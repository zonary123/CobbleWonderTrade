package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.model.UserInfo;
import com.kingpixel.wondertrade.utils.WonderTradeUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
@Setter
public class JSONClient implements DatabaseClient {

  private static Map<UUID, UserInfo> userInfoMap = new HashMap<>();
  private static List<Pokemon> pool = new ArrayList<>();
  private static Gson gson = Utils.newWithoutSpacingGson();

  public JSONClient(String uri, String user, String password) {
    // Configuraciones adicionales si es necesario
  }

  @Override
  public void connect() {
    try {
      CobbleWonderTrade.LOGGER.info("Connected to JSON Database");
      File datafolder = Utils.getAbsolutePath(CobbleWonderTrade.PATH_DATA);
      if (!datafolder.exists() && !datafolder.mkdirs()) {
        throw new IOException("Failed to create data folder");
      }

      File datauserfolder = Utils.getAbsolutePath(CobbleWonderTrade.PATH_DATA_USER);
      if (!datauserfolder.exists() && !datauserfolder.mkdirs()) {
        throw new IOException("Failed to create user data folder");
      }

      loadPool();
    } catch (IOException e) {
      CobbleWonderTrade.LOGGER.error("Error while connecting to JSON Database: ", e);
    }
  }

  @Override
  public List<Pokemon> getSpecialPool(boolean special) {
    if (!special) return pool;
    List<Pokemon> specialPool = new ArrayList<>();
    pool.forEach(pokemon -> {
      if (WonderTradeUtil.isSpecial(pokemon)) specialPool.add(pokemon);
    });
    return specialPool;
  }

  @Override
  public List<JsonObject> getPokemonList(boolean special) {
    List<JsonObject> pokemonList = new ArrayList<>();
    pool.forEach(pokemon -> {
      if (WonderTradeUtil.isSpecial(pokemon) && special) {
        pokemonList.add(pokemon.saveToJSON(DynamicRegistryManager.EMPTY,new JsonObject()));
      } else {
        pokemonList.add(pokemon.saveToJSON(DynamicRegistryManager.EMPTY,new JsonObject()));
      }
    });
    return pokemonList;
  }

  @Override
  public Pokemon getRandomPokemon() {
    try {
      return pool.get(Utils.RANDOM.nextInt(pool.size()));
    } catch (IndexOutOfBoundsException e) {
      CobbleWonderTrade.LOGGER.error("Error getting random Pokemon: Pool is empty.", e);
      return null; // Devuelve null si no hay Pokémon en el pool
    }
  }

  @Override
  public Pokemon putPokemon(Pokemon pokemon) {
    try {
      Pokemon random = getRandomPokemon();
      if (random != null) {
        pool.remove(random);
      }
      pool.add(pokemon);
      savePool();
      return random;
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error adding Pokemon to pool: ", e);
      return PokemonProperties.Companion.parse("rattata").create();
    }
  }

  @Override
  public UserInfo getUserInfo(ServerPlayerEntity player) {
    if (userInfoMap.containsKey(player.getUuid())) {
      return userInfoMap.get(player.getUuid());
    } else {
      return readUserInfo(player);
    }
  }

  @Override
  public UserInfo getUserinfo(UUID uuid) {
    if (userInfoMap.containsKey(uuid)) {
      return userInfoMap.get(uuid);
    } else {
      ServerPlayerEntity player = CobbleWonderTrade.server.getPlayerManager().getPlayer(uuid);
      return readUserInfo(player);
    }
  }

  @Override
  public UserInfo putUserInfo(UserInfo userInfo) {
    try {
      userInfoMap.put(userInfo.getPlayeruuid(), userInfo);
      saveUserInfo(userInfo);
      return userInfo;
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error saving user info: ", e);
      return null;
    }
  }

  @Override
  public UserInfo putUserInfo(UserInfo userInfo, boolean update) {
    try {
      if (update) {
        userInfoMap.put(userInfo.getPlayeruuid(), userInfo);
        saveUserInfo(userInfo);
      }
      return userInfo;
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error saving user info: ", e);
      return null;
    }
  }


  @Override
  public void disconnect() {
    try {
      CobbleWonderTrade.LOGGER.info("Disconnected from JSON Database");
      saveAllUserInfo();
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error during disconnect: ", e);
    }
  }

  @Override
  public void save() {
    try {
      CobbleWonderTrade.LOGGER.info("Saving JSON Database");
      saveAllUserInfo();
      savePool();
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error during save: ", e);
    }
  }

  private void loadPool() {
    try {
      String content = Utils.readFileSync(Utils.getAbsolutePath(CobbleWonderTrade.PATH_DATA + "pool.json"));
      List<JsonObject> list = gson.fromJson(content, new TypeToken<List<JsonObject>>() {
      }.getType());
      list.forEach(json -> pool.add(Pokemon.Companion.loadFromJSON(DynamicRegistryManager.EMPTY,json)));
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error loading pool: ", e);
      resetPool(true);
      savePool();
    }
  }

  @Override
  public void resetPool(boolean force) {
    try {
      if (pool.isEmpty() || force || pool.size() != CobbleWonderTrade.config.getSizePool()) {
        pool.clear();
        pool = CobbleWonderTrade.config.getFilterGenerationPokemon()
          .generateRandomPokemons(CobbleWonderTrade.MOD_ID, "pool", CobbleWonderTrade.config.getSizePool());
        pool.forEach(pokemon -> pokemon.setLevel(Utils.RANDOM.nextInt(CobbleWonderTrade.config.getMinlv(),
          CobbleWonderTrade.config.getMaxlv())));
        savePool();
      }
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error resetting pool: ", e);
    }
  }

  private void savePool() {
    try {
      Utils.writeFileAsync(CobbleWonderTrade.PATH_DATA, "pool.json", gson.toJson(getListJsonObject()));
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error saving pool: ", e);
    }
  }

  private void saveUserInfo(UserInfo userInfo) {
    try {
      Utils.writeFileAsync(CobbleWonderTrade.PATH_DATA_USER, userInfo.getPlayeruuid() + ".json", gson.toJson(userInfo));
    } catch (Exception e) {
      CobbleWonderTrade.LOGGER.error("Error saving user info: ", e);
    }
  }

  private void saveAllUserInfo() {
    userInfoMap.forEach((uuid, userInfo) -> saveUserInfo(userInfo));
  }

  private UserInfo readUserInfo(ServerPlayerEntity player) {
    try {
      File file = Utils.getAbsolutePath(CobbleWonderTrade.PATH_DATA_USER + player.getUuid() + ".json");
      if (!file.exists()) {
        UserInfo newUserInfo = new UserInfo(player.getUuid());
        userInfoMap.put(player.getUuid(), newUserInfo);
        saveUserInfo(newUserInfo);
        return newUserInfo;
      } else {
        String content = Utils.readFileSync(file);
        UserInfo userInfo = gson.fromJson(content, UserInfo.class);
        userInfoMap.put(player.getUuid(), userInfo);
        return userInfo;
      }
    } catch (Exception e) {
      e.printStackTrace();
      UserInfo newUserInfo = new UserInfo(player.getUuid());
      userInfoMap.put(player.getUuid(), newUserInfo);
      saveUserInfo(newUserInfo);
      return newUserInfo;
    }
  }

  private List<JsonObject> getListJsonObject() {
    List<JsonObject> list = new ArrayList<>();
    pool.forEach(pokemon -> list.add(pokemon.saveToJSON(DynamicRegistryManager.EMPTY,new JsonObject())));
    return list;
  }
}
