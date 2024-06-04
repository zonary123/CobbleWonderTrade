package com.kingpixel.wondertrade.Manager;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import net.minecraft.commands.CommandSourceStack;

/**
 * @author Carlos Varas Alonso - 10/05/2024 20:37
 */
public class WonderTradePermission {

  // User
  public final CobblemonPermission WONDERTRADE_BASE_PERMISSION;
  // Admin
  public final CobblemonPermission WONDERTRADE_OTHER_PERMISSION;
  public final CobblemonPermission WONDERTRADE_RELOAD_PERMISSION;

  public WonderTradePermission() {
    this.WONDERTRADE_BASE_PERMISSION = new CobblemonPermission("wondertrade.command.wondertrade.base",
      toPermLevel(CobbleWonderTrade.dexpermission.permissionLevels.COMMAND_WONDERTRADE_BASE_PERMISSION_LEVEL));
    // Admin
    this.WONDERTRADE_OTHER_PERMISSION = new CobblemonPermission("wondertrade.command.wondertrade.other", toPermLevel(CobbleWonderTrade.dexpermission.permissionLevels.COMMAND_WONDERTRADE_OTHER_PERMISSION_LEVEL));
    this.WONDERTRADE_RELOAD_PERMISSION = new CobblemonPermission("wondertrade.command.wondertrade.reload",
      toPermLevel(CobbleWonderTrade.dexpermission.permissionLevels.COMMAND_WONDERTRADE_RELOAD_PERMISSION_LEVEL));

  }

  public PermissionLevel toPermLevel(int permLevel) {
    for (PermissionLevel value : PermissionLevel.values()) {
      if (value.ordinal() == permLevel) {
        return value;
      }
    }
    return PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS;
  }

  public static boolean checkPermission(CommandSourceStack source, CobblemonPermission permission) {
    return Cobblemon.INSTANCE.getPermissionValidator().hasPermission(source, permission);
  }
}

