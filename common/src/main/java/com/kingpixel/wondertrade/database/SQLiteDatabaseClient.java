package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabaseClient extends DatabaseClient {
  private Connection connection;

  public SQLiteDatabaseClient(DataBaseConfig config) {
    try {
      String url = "jdbc:sqlite:" + Utils.getAbsolutePath(CobbleWonderTrade.PATH_DATA + "database.db");
      this.connection = DriverManager.getConnection(url);
      CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "SQLite connection established.");
    } catch (SQLException e) {
      throw new RuntimeException("Failed to connect to SQLite database", e);
    }
  }

  @Override
  public void connect() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "SQLite Database already connected.");
    createTables();
    fixPool();
  }

  @Override
  public void disconnect() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Disconnecting from SQLite Database");
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "SQLite connection closed.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void createTables() {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS pokemons (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT)");
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS user_info (uuid TEXT PRIMARY KEY, data TEXT)");
      statement.executeUpdate("CREATE INDEX if NOT EXISTS idx_user_info_uuid ON user_info (UUID)");
      statement.executeUpdate("CREATE INDEX if NOT EXISTS idx_pokemons_id ON pokemons (id)");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public UserInfo getUserInfo(ServerPlayerEntity player) {
    var userInfo = DatabaseClientFactory.userInfoMap.get(player.getUuid());
    if (userInfo != null) return userInfo;

    try (PreparedStatement statement = connection.prepareStatement("SELECT data FROM user_info WHERE uuid = ?")) {
      statement.setString(1, player.getUuid().toString());
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        userInfo = Utils.newWithoutSpacingGson().fromJson(resultSet.getString("data"), UserInfo.class);
        DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
      } else {
        userInfo = new UserInfo(player);
        DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
        updateUserInfo(player, userInfo);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return userInfo;
  }

  @Override
  public Pokemon tradePokemon(ServerPlayerEntity player, Pokemon pokemon) {
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT id, data FROM pokemons ORDER BY random() LIMIT 1");

      if (!resultSet.next()) {
        CobbleUtils.LOGGER.warn(CobbleWonderTrade.MOD_ID, "No Pokémon available in the pool");
        return null;
      }

      int id = resultSet.getInt("id");
      Pokemon tradedPokemon = Utils.newWithoutSpacingGson().fromJson(resultSet.getString("data"), Pokemon.class);

      try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM pokemons WHERE id = ?")) {
        deleteStatement.setInt(1, id);
        deleteStatement.executeUpdate();
      }

      try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO pokemons (data) VALUES (?)")) {
        insertStatement.setString(1, Utils.newWithoutSpacingGson().toJson(pokemon));
        insertStatement.executeUpdate();
      }

      return tradedPokemon;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public List<Pokemon> getPokemonsAnimation() {
    return getPokemons("SELECT data FROM pokemons ORDER BY RANDOM() LIMIT " + DatabaseClientFactory.POKEMON_ANIMATION_SIZE);
  }

  @Override
  public List<Pokemon> getAllPokemons() {
    return getPokemons("SELECT data FROM pokemons");
  }

  private List<Pokemon> getPokemons(String query) {
    List<Pokemon> pokemons = new ArrayList<>();
    try (Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(query)) {

      while (resultSet.next()) {
        pokemons.add(Utils.newWithoutSpacingGson().fromJson(resultSet.getString("data"), Pokemon.class));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return pokemons;
  }

  @Override
  public void restartPool() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Restarting pool in SQLite");
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("DELETE FROM pokemons");
    } catch (SQLException e) {
      e.printStackTrace();
    }

    List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getSizePool(), 0);
    try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO pokemons (data) VALUES (?)")) {
      for (Pokemon pokemon : newPokemons) {
        insertStatement.setString(1, Utils.newWithoutSpacingGson().toJson(pokemon));
        insertStatement.addBatch();
      }
      insertStatement.executeBatch();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void updateUserInfo(ServerPlayerEntity player, UserInfo userinfo) {
    try (PreparedStatement statement = connection.prepareStatement(
      "REPLACE INTO user_info (uuid, data) VALUES (?, ?)")) {
      statement.setString(1, player.getUuid().toString());
      statement.setString(2, Utils.newWithoutSpacingGson().toJson(userinfo));
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void fixPool() {
    try (Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS count FROM pokemons")) {

      if (resultSet.next()) {
        int currentCount = resultSet.getInt("count");
        int sizePool = CobbleWonderTrade.config.getSizePool();

        if (currentCount < sizePool) {
          List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(sizePool, currentCount);
          DatabaseClientFactory.putLevels(newPokemons);

          StringBuilder queryBuilder = new StringBuilder("INSERT INTO pokemons (data) VALUES ");
          List<String> values = new ArrayList<>();
          for (int i = 0; i < newPokemons.size(); i++) {
            values.add("(?)");
          }
          queryBuilder.append(String.join(", ", values));

          try (PreparedStatement insertStatement = connection.prepareStatement(queryBuilder.toString())) {
            int index = 1;
            for (Pokemon pokemon : newPokemons) {
              insertStatement.setString(index++, Utils.newWithoutSpacingGson().toJson(pokemon));
            }
            insertStatement.executeUpdate();
          }
        } else {
          long excessCount = currentCount - sizePool;
          if (excessCount > 0) {
            try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM pokemons WHERE id IN (SELECT id FROM pokemons ORDER BY RANDOM() LIMIT ?)")) {
              deleteStatement.setLong(1, excessCount);
              deleteStatement.executeUpdate();
            }
          }
        }
        CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Size pool: " + sizePool + ", current count: " + currentCount);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}