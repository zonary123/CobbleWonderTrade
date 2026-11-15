package com.kingpixel.wondertrade.database.sql;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.wondertrade.database.sql.dialect.SqliteDialect;

/**
 * SQLite database client implementation.
 *
 * @author Carlos Varas Alonso
 */
public class SQLiteDatabaseClient extends SqlDatabaseClient {

  public SQLiteDatabaseClient(DataBaseConfig config) {
    super(config, new SqliteDialect());
  }
}
