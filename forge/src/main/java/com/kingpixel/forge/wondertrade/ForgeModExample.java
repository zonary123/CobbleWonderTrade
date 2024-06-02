package com.kingpixel.forge.wondertrade;

import com.kingpixel.wondertrade.CobbleWonderTrade;
import com.kingpixel.wondertrade.command.CommandTree;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(CobbleWonderTrade.MOD_ID)
public class ForgeModExample {

  public ForgeModExample() {
    CobbleWonderTrade.init();
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void registerCommands(RegisterCommandsEvent event) {
    CommandTree.register(event.getDispatcher());
  }

  @SubscribeEvent
  public void serverStartedEvent(ServerStartedEvent event) {
    CobbleWonderTrade.load();
  }

  @SubscribeEvent
  public void playerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
    CobbleWonderTrade.manager.addPlayer(event.getEntity());
  }

  @SubscribeEvent
  public void worldLoadEvent(LevelEvent.Load event) {
    CobbleWonderTrade.server = event.getLevel().getServer();
  }
}
