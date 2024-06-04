# WonderTrade

## Description

WonderTrade is a Minecraft plugin designed for Cobblemon. It introduces a unique trading system that allows players to
exchange their Pokémon for random ones from a pool. This adds an element of surprise and excitement to the game, as you
never know what Pokémon you might receive in return.

Please test the plugin and report any issues and if anything happens to add or change also mention it it will be a great
help to improve the plugin.

## Configuration

```json
{
  "lang": "en",
  "cooldown": 30,
  "cooldownmessage": 15,
  "sizePool": 50,
  "minlvreq": 5,
  "minlv": 5,
  "maxlv": 50,
  "allowshiny": true,
  "allowlegendary": true,
  "shinyrate": 8192,
  "legendaryrate": 16512,
  "shinys": 0,
  "legendaries": 0,
  "israndom": false,
  "pokeblacklist": [
    "magikarp"
  ],
  "poketradeblacklist": [
    "Magikarp"
  ],
  "legends": [
    "Magikarp"
  ],
  "aliases": [
    "wt",
    "wondertrade"
  ]
}
```

## Commands

- `/wt` - Opens the WonderTrade GUI.
- `/wt other <player>` - Opens the WonderTrade GUI for other player.
- `/wt pool` - Opens the WonderTrade pool GUI.
- `/wt reload` - Reloads the plugin configuration.

## Dependencies

- [Cobblemon](https://modrinth.com/mod/cobblemon) (v1.5.0)
- [Gooeylibs](https://modrinth.com/mod/gooeylibs) (v3.0.0)
- [Architectury API](https://modrinth.com/mod/architectury-api) (v9.2.14)