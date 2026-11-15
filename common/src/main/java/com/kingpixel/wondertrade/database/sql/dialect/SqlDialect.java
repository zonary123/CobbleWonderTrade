package com.kingpixel.wondertrade.database.sql.dialect;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.zaxxer.hikari.HikariConfig;

import java.util.List;

/**
 * Strategy interface providing database-specific SQL dialect queries and connection pool configurations.
 * Allows seamless compatibility across MySQL, MariaDB, SQLite, H2, and other SQL engines.
 *
 * @author Carlos Varas Alonso
 */
public interface SqlDialect {

  /**
   * Builds the database-specific HikariCP configuration.
   *
   * @param config the user configuration for the database
   * @return fully populated HikariConfig
   */
  HikariConfig createHikariConfig(DataBaseConfig config);

  /**
   * Tables creation queries for the pool, user_info, and restart tracking.
   *
   * @return list of table creation SQL strings
   */
  List<String> getCreateTablesQueries();

  /**
   * Engine-specific initialization queries executed when connecting (e.g. SQLite PRAGMAs or index creations).
   *
   * @return list of init SQL strings
   */
  default List<String> getInitQueries() {
    return List.of();
  }

  /**
   * Dialect-specific upsert query for user info.
   * Parameter indices: 1 = uuid (String), 2 = data (JSON String)
   *
   * @return SQL query string
   */
  String getUpsertUserQuery();

  /**
   * Query to fetch a single random Pokémon from the pool.
   *
   * @return SQL query string
   */
  String getRandomPokemonQuery();

  /**
   * Query to fetch N random Pokémon from the pool for trade animation.
   *
   * @param limit number of records
   * @return SQL query string
   */
  String getRandomAnimationQuery(int limit);

  /**
   * Query to delete excess Pokémon from the pool when shrinking.
   * Parameter 1: excess count limit (int)
   *
   * @return SQL query string
   */
  String getDeleteExcessQuery();
}
