package com.kingpixel.wondertrade.model;

import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.wondertrade.CobbleWonderTrade;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 25/07/2024 1:10
 */
@Getter
@Setter
@Data
@ToString
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
    this.date = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(
      PlayerUtils.getCooldown(
        CobbleWonderTrade.config.getCooldownPermission(),
        CobbleWonderTrade.config.getCooldown(),
        player
      )
    );
  }

  public boolean hasCooldown() {
    return System.currentTimeMillis() < date;
  }
}
