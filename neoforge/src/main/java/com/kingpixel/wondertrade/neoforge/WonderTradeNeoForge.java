package com.kingpixel.wondertrade.neoforge;

import com.kingpixel.wondertrade.CobbleWonderTrade;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge entrypoint for WonderTrade.
 *
 * @author Carlos Varas Alonso
 */
@Mod(CobbleWonderTrade.MOD_ID)
public final class WonderTradeNeoForge {
  public WonderTradeNeoForge() {
    CobbleWonderTrade.init();
  }
}
