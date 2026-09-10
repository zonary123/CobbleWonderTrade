package com.kingpixel.wondertrade.database;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.CommandTree;
import com.kingpixel.wondertrade.model.UserInfo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database client implementation optimized with WAL mode and high performance PRAGMAs.
 *
 * @author Carlos Varas Alonso
 */
public class SQLiteDatabaseClient extends DatabaseClient {
  private Connection connection;

  public SQLiteDatabaseClient(DataBaseConfig config) {
    try {
      String dbPath = Path.of(CobbleWonderTrade.PATH_DATA, "database.db").toAbsolutePath().toString();
      String url = "jdbc:sqlite:" + dbPath;
      this.connection = DriverManager.getConnection(url);

      try (Statement statement = this.connection.createStatement()) {
        statement.execute("PRAGMA journal_mode = WAL;");
        statement.execute("PRAGMA synchronous = NORMAL;");
        statement.execute("PRAGMA busy_timeout = 5000;");
        statement.execute("PRAGMA temp_store = MEMORY;");
      }

      CobbleWonderTrade.LOGGER.info("SQLite connection established with WAL mode.");
    } catch (SQLException e) {
      throw new RuntimeException("Failed to connect to SQLite database", e);
    }
  }

  @Override
  public void connect() {
    CobbleWonderTrade.LOGGER.info("Connecting to SQLite Database");
    createTables();
    fixPool();
  }

  @Override
  public void disconnect() {
    CobbleWonderTrade.LOGGER.info("Disconnecting from SQLite Database");
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        CobbleWonderTrade.LOGGER.info("SQLite connection closed.");
      }
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error closing SQLite connection", e);
    }
  }

  private void createTables() {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS pokemons (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT)");
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS user_info (uuid TEXT PRIMARY KEY, data TEXT)");
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS restart_info (id INTEGER PRIMARY KEY, restart_at INTEGER)");
      statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_user_info_uuid ON user_info (uuid)");
      statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_pokemons_id ON pokemons (id)");
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error creating SQLite tables", e);
    }
  }

  @Override
  public UserInfo getUserInfo(ServerPlayerEntity player) {
    if (player == null) return null;
    UserInfo userInfo = DatabaseClientFactory.userInfoMap.get(player.getUuid());
    if (userInfo != null) return userInfo;

    try (PreparedStatement statement = connection.prepareStatement("SELECT data FROM user_info WHERE uuid = ?")) {
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
      CobbleWonderTrade.LOGGER.error("Error fetching user info from SQLite", e);
      userInfo = new UserInfo(player);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
    }

    return userInfo;
  }

  @Override
  public Pokemon tradePokemon(ServerPlayerEntity player, Pokemon pokemon) {
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT id, data FROM pokemons ORDER BY RANDOM() LIMIT 1");

      if (!resultSet.next()) {
        CobbleWonderTrade.LOGGER.warn("No Pokémon available in the pool");
        return null;
      }

      int id = resultSet.getInt("id");
      Pokemon tradedPokemon = UtilsFile.getGson().fromJson(resultSet.getString("data"), Pokemon.class);

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
      CobbleWonderTrade.LOGGER.error("Error trading Pokémon in SQLite", e);
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
        pokemons.add(UtilsFile.getGson().fromJson(resultSet.getString("data"), Pokemon.class));
      }
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error querying Pokémon list in SQLite", e);
    }
    return pokemons;
  }

  @Override
  public void restartPool() {
    CobbleWonderTrade.LOGGER.info("Restarting pool in SQLite");
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("DELETE FROM pokemons");
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error clearing pokemons table in SQLite", e);
    }

    List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getPool().getSizePool(), 0);
    try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO pokemons (data) VALUES (?)")) {
      for (Pokemon pokemon : newPokemons) {
        insertStatement.setString(1, UtilsFile.getGson().toJson(pokemon));
        insertStatement.addBatch();
      }
      insertStatement.executeBatch();
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error inserting generated pokemons into SQLite", e);
    }
    CommandTree.invalidateStats();
  }

  @Override
  public void updateUserInfo(ServerPlayerEntity player, UserInfo userinfo) {
    if (player == null || userinfo == null) return;
    try (PreparedStatement statement = connection.prepareStatement(
      "REPLACE INTO user_info (uuid, data) VALUES (?, ?)")) {
      statement.setString(1, player.getUuid().toString());
      statement.setString(2, UtilsFile.getGson().toJson(userinfo));
      statement.executeUpdate();
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error updating user info in SQLite", e);
    }
  }

  @Override
  public void fixPool() {
    try (Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS count FROM pokemons")) {

      if (resultSet.next()) {
        int currentCount = resultSet.getInt("count");
        int sizePool = CobbleWonderTrade.config.getPool().getSizePool();

        if (currentCount < sizePool) {
          List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(sizePool, currentCount);
          DatabaseClientFactory.putLevels(newPokemons);

          try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO pokemons (data) VALUES (?)")) {
            for (Pokemon pokemon : newPokemons) {
              insertStatement.setString(1, UtilsFile.getGson().toJson(pokemon));
              insertStatement.addBatch();
            }
            insertStatement.executeBatch();
          }
        } else {
          long excessCount = currentCount - sizePool;
          if (excessCount > 0) {
            try (PreparedStatement deleteStatement = connection.prepareStatement(
              "DELETE FROM pokemons WHERE id IN (SELECT id FROM pokemons ORDER BY RANDOM() LIMIT ?)")) {
              deleteStatement.setLong(1, excessCount);
              deleteStatement.executeUpdate();
            }
          }
        }
        CobbleWonderTrade.LOGGER.info("Size pool: " + sizePool + ", current count: " + currentCount);
      }
      CommandTree.invalidateStats();
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error fixing pool in SQLite", e);
    }
  }

  @Override
  public boolean shouldRestartPool() {
    if (!super.shouldRestartPool()) return false;
    long currentTime = System.currentTimeMillis();
    long nextRestartTime = CobbleWonderTrade.config.getPool().getCooldownReset() * 60 * 1000L;

    try (Statement statement = connection.createStatement()) {
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
      CobbleWonderTrade.LOGGER.error("Error checking restart pool in SQLite", e);
    }

    return false;
  }
}