package com.kingpixel.wondertrade.database.sql.dialect;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.zaxxer.hikari.HikariConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SQL dialect implementation for SQLite databases with WAL mode and memory PRAGMAs.
 *
 * @author Carlos Varas Alonso
 */
public class SqliteDialect implements SqlDialect {

  @Override
  public HikariConfig createHikariConfig(DataBaseConfig config) {
    String dbPath = Path.of(CobbleWonderTrade.PATH_DATA, "database.db").toAbsolutePath().toString();
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbPath);
    hikariConfig.setDriverClassName("org.sqlite.JDBC");
    hikariConfig.setMaximumPoolSize(1);
    hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));
    hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
    hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(15));
    hikariConfig.setPoolName("UltraWondertrade-SQLitePool");
    hikariConfig.setConnectionInitSql("PRAGMA journal_mode = WAL; PRAGMA synchronous = NORMAL; PRAGMA busy_timeout = 5000; PRAGMA temp_store = MEMORY;");
    return hikariConfig;
  }

  @Override
  public List<String> getCreateTablesQueries() {
    return List.of(
      "CREATE TABLE IF NOT EXISTS pokemons (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT)",
      "CREATE TABLE IF NOT EXISTS user_info (uuid TEXT PRIMARY KEY, data TEXT)",
      "CREATE TABLE IF NOT EXISTS restart_info (id INTEGER PRIMARY KEY, restart_at INTEGER)"
    );
  }

  @Override
  public List<String> getInitQueries() {
    return List.of(
      "CREATE INDEX IF NOT EXISTS idx_user_info_uuid ON user_info (uuid);",
      "CREATE INDEX IF NOT EXISTS idx_pokemons_id ON pokemons (id);"
    );
  }

  @Override
  public String getUpsertUserQuery() {
    return "INSERT INTO user_info (uuid, data) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET data = excluded.data";
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
