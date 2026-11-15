package com.kingpixel.wondertrade.database.sql.dialect;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.zaxxer.hikari.HikariConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SQL dialect implementation for MySQL and MariaDB databases.
 *
 * @author Carlos Varas Alonso
 */
public class MySqlDialect implements SqlDialect {

  @Override
  public HikariConfig createHikariConfig(DataBaseConfig config) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.getUrl());
    hikariConfig.setUsername(config.getUser());
    hikariConfig.setPassword(config.getPassword());
    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(2);
    hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));
    hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
    hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(10));
    hikariConfig.setPoolName("UltraWondertrade-MySQLPool");
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
    return hikariConfig;
  }

  @Override
  public List<String> getCreateTablesQueries() {
    return List.of(
      "CREATE TABLE IF NOT EXISTS pokemons (id INT AUTO_INCREMENT PRIMARY KEY, data LONGTEXT)",
      "CREATE TABLE IF NOT EXISTS user_info (uuid VARCHAR(36) PRIMARY KEY, data LONGTEXT)",
      "CREATE TABLE IF NOT EXISTS restart_info (id INT PRIMARY KEY, restart_at BIGINT)"
    );
  }

  @Override
  public String getUpsertUserQuery() {
    return "INSERT INTO user_info (uuid, data) VALUES (?, ?) ON DUPLICATE KEY UPDATE data = VALUES(data)";
  }

  @Override
  public String getRandomPokemonQuery() {
    return "SELECT id, data FROM pokemons ORDER BY RAND() LIMIT 1";
  }

  @Override
  public String getRandomAnimationQuery(int limit) {
    return "SELECT data FROM pokemons ORDER BY RAND() LIMIT " + limit;
  }

  @Override
  public String getDeleteExcessQuery() {
    return "DELETE FROM pokemons ORDER BY RAND() LIMIT ?";
  }
}
