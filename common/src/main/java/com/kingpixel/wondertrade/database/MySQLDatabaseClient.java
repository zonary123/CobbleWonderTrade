package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.CommandTree;
import com.kingpixel.wondertrade.model.UserInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MySQL database client implementation using HikariCP connection pooling.
 *
 * @author Carlos Varas Alonso
 */
public class MySQLDatabaseClient extends DatabaseClient {
  private final HikariDataSource dataSource;

  public MySQLDatabaseClient(DataBaseConfig config) {
    try {
      HikariConfig hikariConfig = new HikariConfig();
      hikariConfig.setJdbcUrl(config.getUrl());
      hikariConfig.setUsername(config.getUser());
      hikariConfig.setPassword(config.getPassword());
      hikariConfig.setMaximumPoolSize(10);
      hikariConfig.setMinimumIdle(2);
      hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));
      hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
      hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(10));
      hikariConfig.setPoolName("UltraWondertrade-HikariPool");
      hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
      hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
      hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
      this.dataSource = new HikariDataSource(hikariConfig);
      CobbleWonderTrade.LOGGER.info("HikariCP connection pool initialized for MySQL.");
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize HikariCP for MySQL database", e);
    }
  }

  @Override
  public void connect() {
    CobbleWonderTrade.LOGGER.info("Connecting to MySQL Database");
    createTables();
    fixPool();
  }

  @Override
  public void disconnect() {
    CobbleWonderTrade.LOGGER.info("Disconnecting from MySQL Database");
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
      CobbleWonderTrade.LOGGER.info("HikariCP connection pool closed.");
    }
  }

  private void createTables() {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS pokemons (id INT AUTO_INCREMENT PRIMARY KEY, data LONGTEXT)");
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS user_info (uuid VARCHAR(36) PRIMARY KEY, data LONGTEXT)");
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS restart_info (id INT PRIMARY KEY, restart_at BIGINT)");
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error creating MySQL tables", e);
    }
  }

  @Override
  public UserInfo getUserInfo(ServerPlayerEntity player) {
    if (player == null) return null;
    UserInfo userInfo = DatabaseClientFactory.userInfoMap.get(player.getUuid());
    if (userInfo != null) return userInfo;

    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement("SELECT data FROM user_info WHERE uuid = ?")) {
      statement.setString(1, player.getUuid().toString());
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        userInfo = UtilsFile.getGson().fromJson(resultSet.getString("data"), UserInfo.class);
        DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
      } else {
        userInfo = new UserInfo(player);
        DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
        updateUserInfo(player, userInfo);
      }
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error fetching user info from MySQL", e);
      userInfo = new UserInfo(player);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
    }

    return userInfo;
  }

  @Override
  public Pokemon tradePokemon(ServerPlayerEntity player, Pokemon pokemon) {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT * FROM pokemons ORDER BY RAND() LIMIT 1");

      if (!resultSet.next()) {
        CobbleWonderTrade.LOGGER.warn("No Pokémon available in the pool");
        return null;
      }

      Pokemon tradedPokemon = UtilsFile.getGson().fromJson(resultSet.getString("data"), Pokemon.class);
      int id = resultSet.getInt("id");

      try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM pokemons WHERE id = ?")) {
        deleteStatement.setInt(1, id);
        deleteStatement.executeUpdate();
      }

      try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO pokemons (data) VALUES (?)")) {
        insertStatement.setString(1, UtilsFile.getGson().toJson(pokemon));
        insertStatement.executeUpdate();
      }

      CommandTree.invalidateStats();
      return tradedPokemon;
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error trading Pokemon in MySQL", e);
      return null;
    }
  }

  @Override
  public List<Pokemon> getPokemonsAnimation() {
    return getPokemons("SELECT data FROM pokemons ORDER BY RAND() LIMIT " + DatabaseClientFactory.POKEMON_ANIMATION_SIZE);
  }

  @Override
  public List<Pokemon> getAllPokemons() {
    return getPokemons("SELECT data FROM pokemons");
  }

  private List<Pokemon> getPokemons(String query) {
    List<Pokemon> pokemons = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(query)) {

      while (resultSet.next()) {
        pokemons.add(UtilsFile.getGson().fromJson(resultSet.getString("data"), Pokemon.class));
      }
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error executing query: " + query, e);
    }
    return pokemons;
  }

  @Override
  public void restartPool() {
    CobbleWonderTrade.LOGGER.info("Restarting pool in MySQL");
    try (Connection connection = dataSource.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate("DELETE FROM pokemons");
      }

      List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getPool().getSizePool(), 0);
      try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO pokemons (data) VALUES (?)")) {
        for (Pokemon pokemon : newPokemons) {
          insertStatement.setString(1, UtilsFile.getGson().toJson(pokemon));
          insertStatement.addBatch();
        }
        insertStatement.executeBatch();
      }
      CommandTree.invalidateStats();
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error restarting pool in MySQL", e);
    }
  }

  @Override
  public void updateUserInfo(ServerPlayerEntity player, UserInfo userinfo) {
    if (player == null || userinfo == null) return;
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(
           "REPLACE INTO user_info (uuid, data) VALUES (?, ?)")) {
      statement.setString(1, player.getUuid().toString());
      statement.setString(2, UtilsFile.getGson().toJson(userinfo));
      statement.executeUpdate();
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error updating user info in MySQL", e);
    }
  }

  @Override
  public void fixPool() {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS count FROM pokemons")) {

      if (resultSet.next()) {
        int currentCount = resultSet.getInt("count");
        int sizePool = CobbleWonderTrade.config.getPool().getSizePool();

        if (currentCount < sizePool) {
          List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(sizePool, currentCount);
          try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO pokemons (data) VALUES (?)")) {
            for (Pokemon pokemon : newPokemons) {
              insertStatement.setString(1, UtilsFile.getGson().toJson(pokemon));
              insertStatement.addBatch();
            }
            insertStatement.executeBatch();
          }
        } else {
          int excessCount = currentCount - sizePool;
          if (excessCount > 0) {
            try (PreparedStatement deleteStatement = connection.prepareStatement(
              "DELETE FROM pokemons ORDER BY RAND() LIMIT ?")) {
              deleteStatement.setInt(1, excessCount);
              deleteStatement.executeUpdate();
            }
          }
        }
        CobbleWonderTrade.LOGGER.info("Size pool: " + sizePool + ", current count: " + currentCount);
      }
      CommandTree.invalidateStats();
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error fixing pool in MySQL", e);
    }
  }

  @Override
  public boolean shouldRestartPool() {
    if (!super.shouldRestartPool()) return false;
    long currentTime = System.currentTimeMillis();
    long nextRestartTime = CobbleWonderTrade.config.getPool().getCooldownReset() * 60 * 1000L;

    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT restart_at FROM restart_info WHERE id = 1 LIMIT 1");

      if (resultSet.next()) {
        long restartAt = resultSet.getLong("restart_at");
        if (currentTime >= restartAt) {
          try (PreparedStatement updateStatement = connection.prepareStatement(
            "UPDATE restart_info SET restart_at = ? WHERE id = 1")) {
            updateStatement.setLong(1, currentTime + nextRestartTime);
            updateStatement.executeUpdate();
          }
          return true;
        }
      } else {
        try (PreparedStatement insertStatement = connection.prepareStatement(
          "INSERT INTO restart_info (id, restart_at) VALUES (1, ?)")) {
          insertStatement.setLong(1, currentTime + nextRestartTime);
          insertStatement.executeUpdate();
        }
      }
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error checking restart pool in MySQL", e);
    }

    return false;
  }
}