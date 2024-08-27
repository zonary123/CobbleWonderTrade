package com.kingpixel.wondertrade.database;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:03
 */
public class DatabaseClientFactory {
  public static DataBaseType type;
  public static DatabaseClient databaseClient;

  public static void createDatabaseClient(DataBaseType type, String uri, String database, String user,
                                          String password) {
    if (databaseClient != null) {
      databaseClient.disconnect();
    }
    switch (type) {
      case MONGODB -> databaseClient = new MongoDBClient(uri, database, user, password);
      case JSON -> databaseClient = new JSONClient(uri, user, password);
      default -> databaseClient = new JSONClient(uri, user, password);
    }
  }
}
