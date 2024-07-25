package com.kingpixel.wondertrade.model;

import lombok.Getter;
import lombok.ToString;

/**
 * @author Carlos Varas Alonso - 24/07/2024 4:01
 */
@Getter
@ToString
public class DataBaseData {
  private String url;
  private String database;
  private String user;
  private String password;

  public DataBaseData() {
    this.url = "mongodb://localhost:27017";
    this.database = "wondertrade";
    this.user = "admin";
    this.password = "admin";
  }

  public DataBaseData(String url, String database) {
    this.url = url;
    this.database = database;
  }
}
