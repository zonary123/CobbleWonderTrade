package com.kingpixel.wondertrade.command.admin;

import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.database.DatabaseClientFactory;
import com.kingpixel.wondertrade.model.UserInfo;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.List;

/**
 * Admin subcommand to reset cooldown and user data for target player(s).
 *
 * @author Carlos Varas Alonso
 */
public class RestartUserSubCommand {

  private RestartUserSubCommand() {}

  public static LiteralArgumentBuilder<ServerCommandSource> register() {
    return CommandManager.literal("restartUser")
      .requires(source -> PermissionApi.hasPermission(source, List.of(
        CobbleWonderTrade.MOD_ID + ".admin",
        CobbleWonderTrade.MOD_ID + ".restart.user"
      ), 2))
      .then(
        CommandManager.argument("players", EntityArgumentType.players())
          .executes(context -> {
            CobbleWonderTrade.ASYNC.runAsync(() -> {
              try {
                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                for (ServerPlayerEntity player : players) {
                  var userinfo = new UserInfo(player);
                  DatabaseClientFactory.userInfoMap.put(player.getUuid(), userinfo);
                  DatabaseClientFactory.databaseClient.updateUserInfo(player, userinfo);
                }
              } catch (CommandSyntaxException e) {
                CobbleWonderTrade.LOGGER.error("Error retrieving target players in restartUser", e);
              }
            });
            return 1;
          })
      );
  }
}
