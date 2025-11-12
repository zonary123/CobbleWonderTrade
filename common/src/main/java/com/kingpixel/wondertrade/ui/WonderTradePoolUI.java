package com.kingpixel.wondertrade.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.PanelsConfig;
import com.kingpixel.cobbleutils.Model.Rectangle;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.wondertrade.command.CommandTree;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

/**
 * @author Carlos Varas Alonso - 16/04/2025 20:51
 */
public class WonderTradePoolUI {
  private int rows;
  private String title;
  private Rectangle rectangle;
  private ItemModel previous;
  private ItemModel close;
  private ItemModel next;
  private List<PanelsConfig> panels;

  public WonderTradePoolUI() {
    this.rows = 6;
    this.title = "Wonder Trade Pool";
    this.rectangle = new Rectangle(rows);
    this.previous = new ItemModel("minecraft:arrow", "&cPrevious");
    previous.setSlot(45);
    this.close = new ItemModel("minecraft:barrier", "&cClose");
    close.setSlot(49);
    this.next = new ItemModel("minecraft:arrow", "&cNext");
    next.setSlot(53);
    this.panels = List.of(
      new PanelsConfig(rows)
    );
  }

  private static final int COOLDOWN_MS = 250;
  private static final Map<UUID, Long> cooldowns = new HashMap<>();

  public void open(ServerPlayerEntity player, List<Pokemon> pokemons) {

    if (player == null || pokemons == null || pokemons.isEmpty()) return;
    long currentTime = System.currentTimeMillis();
    if (cooldowns.containsKey(player.getUuid())) {
      long lastTime = cooldowns.get(player.getUuid());
      if (currentTime - lastTime < COOLDOWN_MS) {
        player.sendMessage(AdventureTranslator.toNative("You are clicking too fast!"));
        return;
      }
    } else {
      cooldowns.put(player.getUuid(), currentTime);
    }
    var template = ChestTemplate
      .builder(rows)
      .build();

    PanelsConfig.applyConfig(template, panels);
    rectangle.apply(template);

    List<Button> buttons = new ArrayList<>();

    for (Pokemon pokemon : pokemons) {
      GooeyButton button = GooeyButton.builder()
        .display(PokemonItem.from(pokemon))
        .with(DataComponentTypes.CUSTOM_NAME, AdventureTranslator.toNative(PokemonUtils.replace(pokemon)))
        .with(DataComponentTypes.LORE, new LoreComponent(
          AdventureTranslator.toNativeL(
            PokemonUtils.replaceLore(pokemon)
          )
        ))
        .build();
      buttons.add(button);
    }
    previous.applyTemplate(template, LinkedPageButton.builder()
      .display(previous.getItemStack())
      .linkType(LinkType.Previous)
      .build());
    next.applyTemplate(template, LinkedPageButton.builder()
      .display(next.getItemStack())
      .linkType(LinkType.Next)
      .build());
    close.applyTemplate(template, close.getButton(action -> CommandTree.open(action.getPlayer())));

    var builder = LinkedPage.builder()
      .title(AdventureTranslator.toNative(title));

    GooeyPage page = PaginationHelper.createPagesFromPlaceholders(
      template,
      buttons,
      builder
    );

    UIManager.openUIForcefully(player, page);
  }
}
