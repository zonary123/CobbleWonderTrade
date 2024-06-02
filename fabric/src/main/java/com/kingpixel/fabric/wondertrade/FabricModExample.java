package com.kingpixel.fabric.wondertrade;

import com.kingpixel.wondertrade.CobbleWonderTrade;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;


public class FabricModExample implements ModInitializer {

  @Override
  public void onInitialize() {
    CobbleWonderTrade.init();
    ServerLifecycleEvents.SERVER_STARTED.register(t -> CobbleWonderTrade.load());
    ServerWorldEvents.LOAD.register((t, e) -> CobbleWonderTrade.server = t);
  }
}