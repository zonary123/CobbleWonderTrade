import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.wondertrade.api.WonderTradeApi;
import com.kingpixel.wondertrade.api.action.WonderTradeActionResult;
import com.kingpixel.wondertrade.api.model.PlayerWonderTradeContext;
import com.kingpixel.wondertrade.api.model.WonderTradePoolSummaryView;
import com.kingpixel.wondertrade.api.ui.WonderTradeMenuProvider;
import com.kingpixel.wondertrade.api.ui.WonderTradeMenuRegistry;
import com.kingpixel.wondertrade.application.WonderTradeActionService;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestWonderTradeApi {

  @AfterEach
  void tearDown() {
    WonderTradeMenuRegistry.unregister();
  }

  @Test
  void testActionResultSuccess() {
    WonderTradeActionResult result = WonderTradeActionResult.success("Trade completed", null, null);
    assertTrue(result.isSuccess());
    assertEquals(WonderTradeActionResult.Status.SUCCESS, result.getStatus());
    assertEquals("Trade completed", result.getMessage());
    assertEquals(0, result.getRemainingCooldownMs());
  }

  @Test
  void testActionResultCooldown() {
    WonderTradeActionResult result = WonderTradeActionResult.cooldown("On cooldown", 15000);
    assertFalse(result.isSuccess());
    assertEquals(WonderTradeActionResult.Status.COOLDOWN, result.getStatus());
    assertEquals(15000, result.getRemainingCooldownMs());
  }

  @Test
  void testActionResultFailures() {
    WonderTradeActionResult minLvl = WonderTradeActionResult.minLevel("Level too low");
    assertFalse(minLvl.isSuccess());
    assertEquals(WonderTradeActionResult.Status.MIN_LEVEL_NOT_MET, minLvl.getStatus());

    WonderTradeActionResult banned = WonderTradeActionResult.banned("Pokemon blacklisted");
    assertFalse(banned.isSuccess());
    assertEquals(WonderTradeActionResult.Status.BANNED_POKEMON, banned.getStatus());

    WonderTradeActionResult notTradeable = WonderTradeActionResult.notTradeable("Cannot trade");
    assertFalse(notTradeable.isSuccess());
    assertEquals(WonderTradeActionResult.Status.NOT_TRADEABLE, notTradeable.getStatus());

    WonderTradeActionResult inBattle = WonderTradeActionResult.inBattle("In battle");
    assertFalse(inBattle.isSuccess());
    assertEquals(WonderTradeActionResult.Status.IN_BATTLE, inBattle.getStatus());

    WonderTradeActionResult error = WonderTradeActionResult.failure(WonderTradeActionResult.Status.ERROR, "Unexpected error");
    assertFalse(error.isSuccess());
    assertEquals(WonderTradeActionResult.Status.ERROR, error.getStatus());
  }

  @Test
  void testMenuRegistryCustomProviderLifecycle() {
    assertFalse(WonderTradeMenuRegistry.hasCustomProvider());

    WonderTradeMenuProvider custom = new WonderTradeMenuProvider() {
      @Override public void openTradeMenu(ServerPlayerEntity player) {}
      @Override public void openPoolMenu(ServerPlayerEntity player, List<Pokemon> pokemons) {}
    };

    WonderTradeMenuRegistry.register(custom);
    assertTrue(WonderTradeMenuRegistry.hasCustomProvider());
    assertEquals(custom, WonderTradeMenuRegistry.getProvider());

    // Register null reverts to default
    WonderTradeMenuRegistry.register(null);
    assertFalse(WonderTradeMenuRegistry.hasCustomProvider());

    // Re-register and unregister
    WonderTradeMenuRegistry.register(custom);
    assertTrue(WonderTradeMenuRegistry.hasCustomProvider());
    WonderTradeMenuRegistry.unregister();
    assertFalse(WonderTradeMenuRegistry.hasCustomProvider());
  }

  @Test
  void testPoolSummaryView() {
    WonderTradePoolSummaryView summary = new WonderTradePoolSummaryView(
      50,
      5,
      2,
      1,
      1,
      3,
      4,
      true,
      60,
      false
    );

    assertEquals(50, summary.getTotalPokemons());
    assertEquals(5, summary.getShinies());
    assertEquals(2, summary.getLegendaries());
    assertEquals(1, summary.getMythicals());
    assertEquals(1, summary.getUltraBeasts());
    assertEquals(3, summary.getParadoxes());
    assertEquals(4, summary.getPerfectIvs());
    assertTrue(summary.isAutoResetEnabled());
    assertEquals(60, summary.getCooldownResetMinutes());
    assertFalse(summary.isRandomGeneration());
  }

  @Test
  void testPlayerWonderTradeContext() {
    UUID uuid = UUID.randomUUID();
    WonderTradePoolSummaryView summary = new WonderTradePoolSummaryView(
      10, 1, 0, 0, 0, 0, 0, false, 0, true
    );

    PlayerWonderTradeContext ctx = new PlayerWonderTradeContext(
      uuid,
      "Red",
      false,
      0,
      0,
      "Ready",
      10,
      true,
      summary
    );

    assertEquals(uuid, ctx.getPlayerUuid());
    assertEquals("Red", ctx.getPlayerName());
    assertFalse(ctx.isHasCooldown());
    assertEquals(0, ctx.getCooldownExpiresAt());
    assertEquals(0, ctx.getRemainingCooldownSeconds());
    assertEquals("Ready", ctx.getFormattedCooldown());
    assertEquals(10, ctx.getMinLevelRequired());
    assertTrue(ctx.isCanTrade());
    assertNotNull(ctx.getPoolSummary());
  }

  @Test
  void testActionServiceNullSafety() throws ExecutionException, InterruptedException {
    WonderTradeActionService service = new WonderTradeActionService();

    WonderTradeActionResult r1 = service.trade(null, null).get();
    assertNotNull(r1);
    assertEquals(WonderTradeActionResult.Status.PLAYER_OFFLINE, r1.getStatus());

    WonderTradeActionResult r2 = service.resetCooldown((ServerPlayerEntity) null).get();
    assertNotNull(r2);
    assertEquals(WonderTradeActionResult.Status.PLAYER_OFFLINE, r2.getStatus());

    WonderTradeActionResult r3 = service.resetCooldown((UUID) null).get();
    assertNotNull(r3);
    assertEquals(WonderTradeActionResult.Status.ERROR, r3.getStatus());
  }

  @Test
  void testApiFacadeAccessors() {
    assertNotNull(WonderTradeApi.getActionService());
    assertNotNull(WonderTradeApi.getContextService());
    assertNotNull(WonderTradeApi.getMenuProvider());
    assertFalse(WonderTradeApi.hasCustomMenuProvider());
  }
}
