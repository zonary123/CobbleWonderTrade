package com.kingpixel.wondertrade.model;

import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * User data representation for WonderTrade player state and cooldowns.
 *
 * @author Carlos Varas Alonso
 */
@Data
@NoArgsConstructor
public class UserInfo {
  private UUID playeruuid;
  private boolean messagesend;
  private long date;

  public UserInfo(ServerPlayerEntity player) {
    this.playeruuid = player.getUuid();
    this.messagesend = false;
    this.date = 0;
  }

  public void setCooldown(ServerPlayerEntity player) {
    this.messagesend = false;
    this.date = System.currentTimeMillis() + PlayerUtils.getCooldown(
      CobbleWonderTrade.config.getCooldowns().getCooldownPermission(),
      CobbleWonderTrade.config.getCooldowns().getCooldown(),
      player
    );
  }

  public boolean hasCooldown() {
    return System.currentTimeMillis() < date;
  }
}
