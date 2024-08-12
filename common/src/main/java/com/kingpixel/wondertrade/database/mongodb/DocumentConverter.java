package com.kingpixel.wondertrade.database.mongodb;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.Document;

public class DocumentConverter {
  public static JsonObject documentToJsonObject(Document document) {
    return JsonParser.parseString(document.toJson()).getAsJsonObject();
  }
}
