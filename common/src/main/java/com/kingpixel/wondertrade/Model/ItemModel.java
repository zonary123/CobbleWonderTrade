package com.kingpixel.wondertrade.Model;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 26/05/2024 4:15
 */
@Getter
@ToString
public class ItemModel {
  private String item;
  private int slot;
  private String title;
  private List<String> lore;

  public ItemModel() {
  }

  public ItemModel(String item) {
    this.item = item;
  }

  public ItemModel(String item, String title) {
    this.item = item;
    this.title = title;
  }

  public ItemModel(String item, String title, List<String> lore) {
    this.item = item;
    this.title = title;
    this.lore = lore;
  }

  public ItemModel(String item, int slot, String title, List<String> lore) {
    this.item = item;
    this.slot = slot;
    this.title = title;
    this.lore = lore;
  }

  public String getId() {
    return item;
  }
}
