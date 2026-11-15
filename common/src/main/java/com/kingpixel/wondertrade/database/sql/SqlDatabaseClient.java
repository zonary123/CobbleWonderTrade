package com.kingpixel.wondertrade.database.sql;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.CommandTree;
import com.kingpixel.wondertrade.database.DatabaseClient;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.database.sql.dialect.SqlDialect;
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
 * Universal SQL database client leveraging HikariCP connection pooling and pluggable SqlDialect strategy.
 *
 * @author Carlos Varas Alonso
 */
public class SqlDatabaseClient extends DatabaseClient {
  protected final SqlDialect dialect;
  protected final HikariDataSource dataSource;

  public SqlDatabaseClient(DataBaseConfig config, SqlDialect dialect) {
    this.dialect = dialect;
    try {
      HikariConfig hikariConfig = dialect.createHikariConfig(config);
      this.dataSource = new HikariDataSource(hikariConfig);
      CobbleWonderTrade.LOGGER.info("HikariCP initialized for SQL database (" + dialect.getClass().getSimpleName() + ")");
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize HikariCP connection pool for SQL database", e);
    }
  }

  @Override
  public void connect() {
    CobbleWonderTrade.LOGGER.info("Connecting to SQL Database (" + dialect.getClass().getSimpleName() + ")");
    createTables();
    fixPool();
  }

  @Override
  public void disconnect() {
    CobbleWonderTrade.LOGGER.info("Disconnecting from SQL Database (" + dialect.getClass().getSimpleName() + ")");
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
      CobbleWonderTrade.LOGGER.info("HikariCP connection pool closed.");
    }
  }

  protected void createTables() {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      for (String initQuery : dialect.getInitQueries()) {
        statement.execute(initQuery);
      }
      for (String tableQuery : dialect.getCreateTablesQueries()) {
        statement.executeUpdate(tableQuery);
      }
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error creating SQL tables", e);
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
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          userInfo = UtilsFile.getGson().fromJson(resultSet.getString("data"), UserInfo.class);
          DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
        } else {
          userInfo = new UserInfo(player);
          DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
          updateUserInfo(player, userInfo);
        }
      }
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error fetching user info from SQL database", e);
      userInfo = new UserInfo(player);
      DatabaseClientFactory.userInfoMap.put(player.getUuid(), userInfo);
    }

    return userInfo;
  }

  @Override
  public Pokemon tradePokemon(ServerPlayerEntity player, Pokemon pokemon) {
    if (pokemon == null) return null;
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(dialect.getRandomPokemonQuery())) {

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
      CobbleWonderTrade.LOGGER.error("Error trading Pokemon in SQL database", e);
      return null;
    }
  }

  @Override
  public List<Pokemon> getPokemonsAnimation() {
    return getPokemons(dialect.getRandomAnimationQuery(DatabaseClientFactory.POKEMON_ANIMATION_SIZE));
  }

  @Override
  public List<Pokemon> getAllPokemons() {
    return getPokemons("SELECT data FROM pokemons");
  }

  protected List<Pokemon> getPokemons(String query) {
    List<Pokemon> pokemons = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(query)) {

      while (resultSet.next()) {
        String data = resultSet.getString("data");
        if (data != null && !data.isBlank()) {
          Pokemon pokemon = UtilsFile.getGson().fromJson(data, Pokemon.class);
          if (pokemon != null) {
            pokemons.add(pokemon);
          }
        }
      }
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error executing SQL query: " + query, e);
    }
    return pokemons;
  }

  @Override
  public void restartPool() {
    CobbleWonderTrade.LOGGER.info("Restarting pool in SQL database");
    try (Connection connection = dataSource.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate("DELETE FROM pokemons");
      }

      List<Pokemon> newPokemons = DatabaseClientFactory.getGeneratedPool(CobbleWonderTrade.config.getPool().getSizePool(), 0);
      try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO pokemons (data) VALUES (?)")) {
        for (Pokemon pokemon : newPokemons) {
          if (pokemon != null) {
            insertStatement.setString(1, UtilsFile.getGson().toJson(pokemon));
            insertStatement.addBatch();
          }
        }
        insertStatement.executeBatch();
      }
      CommandTree.invalidateStats();
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error restarting pool in SQL database", e);
    }
  }

  @Override
  public void updateUserInfo(ServerPlayerEntity player, UserInfo userinfo) {
    if (player == null || userinfo == null) return;
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(dialect.getUpsertUserQuery())) {
      statement.setString(1, player.getUuid().toString());
      statement.setString(2, UtilsFile.getGson().toJson(userinfo));
      statement.executeUpdate();
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error updating user info in SQL database", e);
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
              if (pokemon != null) {
                insertStatement.setString(1, UtilsFile.getGson().toJson(pokemon));
                insertStatement.addBatch();
              }
            }
            insertStatement.executeBatch();
          }
        } else {
          int excessCount = currentCount - sizePool;
          if (excessCount > 0) {
            try (PreparedStatement deleteStatement = connection.prepareStatement(dialect.getDeleteExcessQuery())) {
              deleteStatement.setInt(1, excessCount);
              deleteStatement.executeUpdate();
            }
          }
        }
        CobbleWonderTrade.LOGGER.info("Size pool: " + sizePool + ", current count: " + currentCount);
      }
      CommandTree.invalidateStats();
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error fixing pool in SQL database", e);
    }
  }

  @Override
  public boolean shouldRestartPool() {
    if (!super.shouldRestartPool()) return false;
    long currentTime = System.currentTimeMillis();
    long nextRestartTime = TimeUnit.MINUTES.toMillis(CobbleWonderTrade.config.getPool().getCooldownReset());

    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      try (ResultSet resultSet = statement.executeQuery("SELECT restart_at FROM restart_info WHERE id = 1 LIMIT 1")) {
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
      }
    } catch (SQLException e) {
      CobbleWonderTrade.LOGGER.error("Error checking restart pool in SQL database", e);
    }

    return false;
  }
}
