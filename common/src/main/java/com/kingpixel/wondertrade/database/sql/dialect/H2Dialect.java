package com.kingpixel.wondertrade.database.sql.dialect;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.zaxxer.hikari.HikariConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SQL dialect implementation for H2 in-memory or file databases.
 *
 * @author Carlos Varas Alonso
 */
public class H2Dialect implements SqlDialect {

  @Override
  public HikariConfig createHikariConfig(DataBaseConfig config) {
    String url = config != null && config.getUrl() != null && !config.getUrl().isBlank()
      ? config.getUrl()
      : "jdbc:h2:file:" + Path.of(CobbleWonderTrade.PATH_DATA, "h2_database").toAbsolutePath();

    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(url);
    if (config != null) {
      if (config.getUser() != null && !config.getUser().isBlank()) {
        hikariConfig.setUsername(config.getUser());
      }
      if (config.getPassword() != null && !config.getPassword().isBlank()) {
        hikariConfig.setPassword(config.getPassword());
      }
    }
    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(1);
    hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));
    hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
    hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(10));
    hikariConfig.setPoolName("UltraWondertrade-H2Pool");
    return hikariConfig;
  }

  @Override
  public List<String> getCreateTablesQueries() {
    return List.of(
      "CREATE TABLE IF NOT EXISTS pokemons (id INT AUTO_INCREMENT PRIMARY KEY, data CLOB)",
      "CREATE TABLE IF NOT EXISTS user_info (uuid VARCHAR(36) PRIMARY KEY, data CLOB)",
      "CREATE TABLE IF NOT EXISTS restart_info (id INT PRIMARY KEY, restart_at BIGINT)"
    );
  }

  @Override
  public String getUpsertUserQuery() {
    return "MERGE INTO user_info (uuid, data) KEY(uuid) VALUES (?, ?)";
  }

  @Override
  public String getRandomPokemonQuery() {
    return "SELECT id, data FROM pokemons ORDER BY RANDOM() LIMIT 1";
  }

  @Override
  public String getRandomAnimationQuery(int limit) {
    return "SELECT data FROM pokemons ORDER BY RANDOM() LIMIT " + limit;
  }

  @Override
  public String getDeleteExcessQuery() {
    return "DELETE FROM pokemons WHERE id IN (SELECT id FROM pokemons ORDER BY RANDOM() LIMIT ?)";
  }
}
