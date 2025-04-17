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

public class MySQLDatabaseClient extends DatabaseClient {
  private Connection connection;

  public MySQLDatabaseClient(DataBaseConfig config) {
    try {
      String url = config.getUrl();
      String username = config.getUser();
      String password = config.getPassword();
      this.connection = DriverManager.getConnection(url, username, password);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to connect to MySQL database", e);
    }
  }

  @Override
  public void connect() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Connecting to MySQL Database");
    fixPool();
  }

  @Override
  public void disconnect() {
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Disconnecting from MySQL Database");
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public UserInfo getUserInfo(ServerPlayerEntity player) {
    var userInfo = DatabaseClientFactory.userInfoMap.get(player.getUuid());
    if (userInfo != null) return userInfo;

    try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_info WHERE uuid = ?")) {
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
      ResultSet resultSet = statement.executeQuery("SELECT * FROM pokemons ORDER BY RAND() LIMIT 1");

      if (!resultSet.next()) {
        CobbleUtils.LOGGER.warn(CobbleWonderTrade.MOD_ID, "No Pokémon available in the pool");
        return null;
      }

      Pokemon tradedPokemon = Utils.newWithoutSpacingGson().fromJson(resultSet.getString("data"), Pokemon.class);

      try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM pokemons WHERE id = ?")) {
        deleteStatement.setInt(1, resultSet.getInt("id"));
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
    return getPokemons("SELECT * FROM pokemons ORDER BY RAND() LIMIT 5");
  }

  @Override
  public List<Pokemon> getAllPokemons() {
    return getPokemons("SELECT * FROM pokemons");
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
    CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Restarting pool in MySQL");
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("DELETE FROM pokemons");
    } catch (SQLException e) {
      e.printStackTrace();
    }

    List<Pokemon> newPokemons = CobbleWonderTrade.config.getFilterGenerationPokemon().generateRandomPokemons(
      CobbleWonderTrade.MOD_ID,
      "pool",
      CobbleWonderTrade.config.getSizePool()
    );
    DatabaseClientFactory.putLevels(newPokemons);

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
          List<Pokemon> newPokemons = CobbleWonderTrade.config.getFilterGenerationPokemon().generateRandomPokemons(
            CobbleWonderTrade.MOD_ID,
            "pool",
            sizePool - currentCount
          );
          DatabaseClientFactory.putLevels(newPokemons);

          // Construir un único INSERT con múltiples valores
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
        }
        CobbleUtils.LOGGER.info(CobbleWonderTrade.MOD_ID, "Size pool: " + sizePool + ", current count: " + currentCount);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}