package com.kingpixel.wondertrade.Manager;

/**
 * @author Carlos Varas Alonso - 24/07/2024 4:01
 */
public class MongoDBDataBase {
  private final String host;
  private final int port;
  private final String database;
  private final String user;
  private final String password;

  public MongoDBDataBase() {
    this.host = "localhost";
    this.port = 27017;
    this.database = "wondertrade";
    this.user = "admin";
    this.password = "admin";
  }

  public MongoDBDataBase(String host, int port, String database, String user, String password) {
    this.host = host;
    this.port = port;
    this.database = database;
    this.user = user;
    this.password = password;
  }
}
