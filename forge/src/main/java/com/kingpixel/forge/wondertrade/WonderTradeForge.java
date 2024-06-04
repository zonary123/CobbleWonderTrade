package com.kingpixel.forge.wondertrade;

import com.kingpixel.wondertrade.CobbleWonderTrade;
import net.minecraftforge.fml.common.Mod;

@Mod(CobbleWonderTrade.MOD_ID)
public class WonderTradeForge {

  public WonderTradeForge() {
    CobbleWonderTrade.init();
  }
}
