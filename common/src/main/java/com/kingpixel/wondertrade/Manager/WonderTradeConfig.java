package com.kingpixel.wondertrade.Manager;

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
    @SerializedName("command.wondertrade") public int COMMAND_WONDERTRADE_BASE_PERMISSION_LEVEL = 0;
    // Admin
    @SerializedName("command.wondertrade.other") public int COMMAND_WONDERTRADE_OTHER_PERMISSION_LEVEL = 2;
    @SerializedName("command.wondertrade.reload") public int COMMAND_WONDERTRADE_RELOAD_PERMISSION_LEVEL = 2;

  }
}