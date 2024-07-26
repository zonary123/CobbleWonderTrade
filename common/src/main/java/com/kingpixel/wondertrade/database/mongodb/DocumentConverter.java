package com.kingpixel.wondertrade.database.mongodb;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.Document;

public class DocumentConverter {
  public static JsonObject documentToJsonObject(Document document) {
    JsonParser parser = new JsonParser();
    return parser.parse(document.toJson()).getAsJsonObject();
  }
}
