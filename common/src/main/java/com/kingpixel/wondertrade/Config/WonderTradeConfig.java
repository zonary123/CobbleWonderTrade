package com.kingpixel.wondertrade.Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;


public class WonderTradeConfig {

  public static Gson GSON = new GsonBuilder()
    .disableHtmlEscaping()
    .setPrettyPrinting()
    .create();
  @SerializedName("permissionlevels") public PermissionLevels permissionLevels = new PermissionLevels();

  public class PermissionLevels {
    // User
    @SerializedName("command.wondertrade") public int COMMAND_COBBLESTS_PERMISSION_LEVEL = 0;
    // Admin


  }
}