package com.kingpixel.fabric.wondertrade;

import com.kingpixel.wondertrade.CobbleWonderTrade;
import net.fabricmc.api.ModInitializer;


public class WonderTradeFabric implements ModInitializer {

  @Override
  public void onInitialize() {
    CobbleWonderTrade.init();
  }
}