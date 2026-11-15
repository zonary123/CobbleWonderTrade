package com.kingpixel.wondertrade.database.sql;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.wondertrade.database.sql.dialect.MySqlDialect;

/**
 * MySQL & MariaDB database client implementation.
 *
 * @author Carlos Varas Alonso
 */
public class MySQLDatabaseClient extends SqlDatabaseClient {

  public MySQLDatabaseClient(DataBaseConfig config) {
    super(config, new MySqlDialect());
  }
}
