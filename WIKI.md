# 🌌 UltraWondertrade — Comprehensive Architectural Manual & Administrator Wiki

---

## 📑 Table of Contents
1. [Executive Overview](#-executive-overview)
2. [Core Architecture & Trading Lifecycle](#-core-architecture--trading-lifecycle)
   - [Trading Modes: Server Pool vs Pure RNG](#trading-modes-server-pool-vs-pure-rng)
   - [Persistence Engines (JSON, SQLite, MySQL, MongoDB)](#persistence-engines-json-sqlite-mysql-mongodb)
   - [Interactive CS:GO Roulette & Reveal Animations](#interactive-csgo-roulette--reveal-animations)
   - [Transaction Safety & Anti-Duplication Safeguards](#transaction-safety--anti-duplication-safeguards)
3. [Special Encounter Probabilities & Guaranteed IVs](#-special-encounter-probabilities--guaranteed-ivs)
   - [RNG Roll Architecture](#rng-roll-architecture)
   - [Configurable Encounter Tiers](#configurable-encounter-tiers)
4. [Command & Subcommand Reference](#-command--subcommand-reference)
5. [Permissions & Tiered Cooldowns](#-permissions--tiered-cooldowns)
6. [Filtering, Generation Rules & Blacklisting](#-filtering-generation-rules--blacklisting)
7. [Discord Webhook Dispatcher](#-discord-webhook-dispatcher)
8. [Configuration Reference (`config.json`)](#-configuration-reference-configjson)
   - [Pool Domain (`pool`)](#pool-domain-pool)
   - [Rates Domain (`rates`)](#rates-domain-rates)
   - [Cooldowns Domain (`cooldowns`)](#cooldowns-domain-cooldowns)
9. [Enterprise Production Templates](#-enterprise-production-templates)

---

## 📌 Executive Overview

**UltraWondertrade** is an enterprise-grade WonderTrade mod engineered for modern **Cobblemon** architectures across Fabric, NeoForge, and Forge. Modeled after the beloved mystery trading mechanics of mainline Pokémon games and modernized with high-stakes casino-style CS:GO roulette animations, UltraWondertrade delivers an exhilarating, community-driven trading ecosystem.

Whether configured as a closed-loop communal trading pool—where Pokémon circulated by players enter the public pool to be claimed by others—or as an infinite procedural randomizer, UltraWondertrade offers server administrators absolute control over Pokémon rarity curves, generation caps, VIP cooldown ladders, and multi-database persistence.

### Key Capabilities
- **Dual Operational Modes**: Run a communal server pool with automatic periodic resets or pure client-side random generation.
- **Visual Roulette Animations**: Real-time sliding CS:GO roll animations built natively with GooeyLibs2 inventory packets.
- **RNG Rarity & Guaranteed IVs**: Fine-tune 1-in-X probabilities for Shiny, Legendary, Mythical, Ultra Beast, and Paradox Pokémon with automatic guaranteed 31-IV floors.
- **Universal Multi-Database Storage**: Plug-and-play support for JSON, SQLite, MySQL/HikariCP, and MongoDB.
- **Tiered VIP Cooldown Engine**: Smoothly scales trading delays based on player permission groups (e.g., 30m base down to 5m for VIPs).
- **Discord Webhook Broadcasting**: Instantly streams rare Shiny and Legendary pull embeds directly into community Discord channels.

---

## 🏗️ Core Architecture & Trading Lifecycle

```
┌────────────────────────────────────────────────────────────────────────┐
│                        WonderTrade Core Engine                         │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
           ┌─────────────────────────┼─────────────────────────┐
           ▼                         ▼                         ▼
┌────────────────────┐    ┌────────────────────┐    ┌────────────────────┐
│  Trading Pipeline  │    │  Pool Lifecycle    │    │ CS:GO Roulette UI  │
│ Player Handshake   │    │ Refill & AutoReset │    │ Custom Frame Ticks │
└──────────┬─────────┘    └──────────┬─────────┘    └──────────┬─────────┘
           │                         │                         │
           └─────────────────────────┼─────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Transaction & Safety Bus                        │
│   • Level Check (`minlvreq`)   • Blacklist Filter   • Cooldown Check   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
           ┌─────────────────────────┴─────────────────────────┐
           ▼                                                   ▼
┌────────────────────────────────────┐    ┌──────────────────────────────┐
│       Relational Databases         │    │      Document & Flat Data    │
│  SQLite • MySQL / MariaDB HikariCP │    │    MongoDB Cluster • JSON    │
└────────────────────────────────────┘    └──────────────────────────────┘
```

### Trading Modes: Server Pool vs Pure RNG
Administrators can choose between two fundamental trading philosophies via `pool.israndom`:

1. **Communal Pool Mode (`israndom: false`)**:
   - The server maintains a persistent pool of Pokémon (default: 72 slots) saved in the database.
   - When a player submits their Pokémon, it is added to the pool.
   - In exchange, the player randomly extracts an existing Pokémon from the pool.
   - **Auto-Reset Daemon**: Can be configured to periodically dump and restock the pool with fresh procedural rolls (`autoReset: true`, `cooldownReset: 30` minutes).

2. **Procedural Random Mode (`israndom: true`)**:
   - The pool acts purely as an internal template generator.
   - Every single trade synthesizes a brand-new, freshly rolled Pokémon according to the server's rate settings and generation rules, while deleting the submitted Pokémon from the player's party.

### Persistence Engines (JSON, SQLite, MySQL, MongoDB)
Persisting pool contents and player trading cooldowns across reboots is handled via `DatabaseClientFactory`:
- **JSON**: Stores data under `config/wondertrade/data/` for simple single-server environments.
- **SQLite**: Zero-configuration embedded relational database engine.
- **MySQL / HikariCP**: Scalable SQL storage with high-performance connection pooling, ideal for large dedicated servers.
- **MongoDB**: High-throughput NoSQL document cluster support for multi-server proxy networks.

### Interactive CS:GO Roulette & Reveal Animations
When a trade is confirmed, UltraWondertrade can display an animated chest GUI mimicking the classic CS:GO crate-opening roulette (`animation: "CSGO"`).
- Pokémon icons scroll across the middle display row, ticking with authentic sound effects.
- The scrolling gradually decelerates until the winner's needle stops precisely on the awarded Pokémon.
- Setting `animation: "NONE"` performs instantaneous trades without delay.

### Transaction Safety & Anti-Duplication Safeguards
- **In-Battle Lock**: Commands and trade confirmations are strictly rejected if the player is actively engaged in a Cobblemon battle.
- **Party Slot Verification**: Evaluates party state immediately before and after trading to prevent desync during concurrent trade attempts.
- **Atomic Database Updates**: Player cooldown timestamps and pool states are updated within synchronized transactions.

---

## 🎲 Special Encounter Probabilities & Guaranteed IVs

### RNG Roll Architecture
When generating a Pokémon for the pool or a random trade, the engine calculates probabilities sequentially:
1. **Shiny Check**: Rolls against `shinyrate` (1 in $N$). If hit, the Pokémon is flagged shiny.
2. **Special Tier Check**: Rolls against Legendary, Mythical, Ultra Beast, and Paradox tables.
3. **Guaranteed IV Injection**: If a special tier is selected, the engine forces the designated number of random IV stats (HP, Attack, Defense, Special Attack, Special Defense, Speed) directly to 31 before rolling remaining stats randomly between 0 and 31.

### Configurable Encounter Tiers

| Rarity Tier | Default Probability | Guaranteed 31 IVs | Discord Embed Alert |
| :--- | :--- | :--- | :--- |
| **Shiny** | 1 in 8,192 | N/A | Yes |
| **Legendary** | 1 in 16,512 | 3 Perfect IVs | Yes |
| **Mythical** | 1 in 16,512 | 3 Perfect IVs | Yes |
| **Ultra Beast** | 1 in 512 | 2 Perfect IVs | Optional |
| **Paradox** | 1 in 256 | 2 Perfect IVs | Optional |

---

## ⌨️ Command & Subcommand Reference

Commands support flexible aliases defined dynamically in `config.json` (defaults: `/wt`, `/wondertrade`).

### User Commands (`/wt`)
| Command | Arguments | Permission Node | Description |
| :--- | :--- | :--- | :--- |
| `/wt` | *None* | `wondertrade.user` | Opens the WonderTrade party selection or roulette GUI. |
| `/wt <slot>` | `<party_slot_1_to_6>` | `wondertrade.user` | Directly initiates WonderTrade with the specified party slot. |
| `/wt pool` | *None* | `wondertrade.user` | Opens the public pool inspection browser (if `poolview: true`). |

### Administrator Commands (`/wt admin`)
| Command | Arguments | Permission Node | Description |
| :--- | :--- | :--- | :--- |
| `/wt admin reload` | *None* | `wondertrade.admin` | Hot-reloads all configuration and language files. |
| `/wt admin restartpool` | *None* | `wondertrade.admin` | Forcefully purges and regenerates the server WonderTrade pool. |
| `/wt admin restartuser` | `<player>` | `wondertrade.admin` | Clears cooldowns and trading history for the targeted player. |
| `/wt admin other` | `<player>` | `wondertrade.admin` | Opens the WonderTrade interface forcefully for the target player. |

---

## ⏱️ Permissions & Tiered Cooldowns

UltraWondertrade uses a permission ladder system to reward VIP tiers with reduced trading cooldowns. The highest matching permission configured under `cooldowns.cooldownPermission` is automatically applied:

```
wondertrade.user                  # Default base player permission (30 min cooldown)
├── wondertrade.vip               # Tier 1 VIP (15 min cooldown)
├── wondertrade.master            # Tier 2 VIP (10 min cooldown)
└── wondertrade.legendary         # Tier 3 VIP (5 min cooldown)

wondertrade.admin                 # Administrative commands & bypass permission
└── wondertrade.bypass.cooldown   # Instant trading with zero delay
```

---

## 🛡️ Filtering, Generation Rules & Blacklisting

Administrators can prevent undesirable Pokémon from polluting the pool or trading cycle:
- **Minimum Requirement Level (`minlvreq`)**: Prevents players from trading newly hatched level 1 Pokémon to dump trash.
- **Generation Bounds (`minlv` / `maxlv`)**: Sets the level range for newly synthesized procedural Pokémon (e.g., Level 5 to Level 36).
- **Species Blacklist (`blackList.pokemons`)**: Explicitly forbids specific species (e.g., Magikarp, Caterpie).
- **Form & Label Blacklist (`blackList.labels`)**: Excludes special event tags, untradeable labels, or custom server forms.

---

## 📢 Discord Webhook Dispatcher

When a player pulls a rare tier from WonderTrade, an embed message is dispatched to the configured Discord channel:

```json
{
  "username": "WonderTrade Announcer",
  "avatar_url": "https://i.imgur.com/wondertrade_icon.png",
  "embeds": [
    {
      "title": "🌟 Legendary WonderTrade Pull!",
      "description": "**PlayerName** just received a **Mewtwo** from the WonderTrade pool!",
      "color": 16766720,
      "fields": [
        {"name": "Level", "value": "70", "inline": true},
        {"name": "Nature", "value": "Timid", "inline": true},
        {"name": "Shiny", "value": "Yes ✨", "inline": true}
      ]
    }
  ]
}
```

---

## ⚙️ Configuration Reference (`config.json`)

### Core Root Settings
| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `debug` | `Boolean` | `false` | Enables verbose console logging for rolls and database calls. |
| `lang` | `String` | `"en"` | Language file identifier (`"en"`, `"es"`). |
| `animation` | `String` | `"CSGO"` | Visual roll style: `"CSGO"` (roulette) or `"NONE"` (instant). |
| `commands` | `List<String>`| `["wt", "wondertrade"]` | Primary command literals and aliases. |
| `databaseConfig.type` | `String` | `"JSON"` | Backend store: `"JSON"`, `"SQLITE"`, `"MYSQL"`, or `"MONGODB"`. |

### Pool Domain (`pool`)
| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `sizePool` | `Integer` | `72` | Total capacity of the communal server trading pool. |
| `minlvreq` | `Integer` | `5` | Minimum level a Pokémon must possess to be accepted into trade. |
| `minlv` | `Integer` | `5` | Minimum level assigned to newly rolled procedural pool Pokémon. |
| `maxlv` | `Integer` | `36` | Maximum level assigned to newly rolled procedural pool Pokémon. |
| `poolview` | `Boolean` | `true` | When true, players can view current pool inventory via `/wt pool`. |
| `israndom` | `Boolean` | `false` | When true, ignores communal pool and generates fresh RNG rolls. |
| `autoReset` | `Boolean` | `false` | Enables scheduled periodic pool wipes and refills. |
| `cooldownReset` | `Integer` | `30` | Interval in minutes between automatic pool refreshes. |

### Rates Domain (`rates`)
| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `shinyrate` | `Integer` | `8192` | 1 in $N$ chance for any generated Pokémon to be Shiny. |
| `legendaryrate` | `Integer` | `16512` | 1 in $N$ chance for a roll to select a Legendary Pokémon. |
| `legendaryperfectivs` | `Integer` | `3` | Number of guaranteed 31 IV stats assigned to Legendaries. |
| `mythicalrate` | `Integer` | `16512` | 1 in $N$ chance for a roll to select a Mythical Pokémon. |
| `mythicalperfectivs` | `Integer` | `3` | Number of guaranteed 31 IV stats assigned to Mythicals. |
| `ultrabeastrate` | `Integer` | `512` | 1 in $N$ chance for an Ultra Beast roll. |
| `ultrabeastperfectivs`| `Integer` | `2` | Number of guaranteed 31 IV stats assigned to Ultra Beasts. |
| `paradoxrate` | `Integer` | `256` | 1 in $N$ chance for a Paradox Pokémon roll. |
| `paradoxperfectivs` | `Integer` | `2` | Number of guaranteed 31 IV stats assigned to Paradoxes. |

### Cooldowns Domain (`cooldowns`)
| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `cooldown` | `DurationValue` | `"30m"` | Base delay required between trades for standard players. |
| `cooldownPermission` | `Map` | *VIP Map* | Map of permission nodes to custom duration strings (`"15m"`, `"5m"`).|
| `cooldownmessage` | `Integer` | `15` | Minutes between reminder messages that trade is ready. |
| `cooldownBroadcast` | `Integer` | `30` | Minutes between server-wide broadcast pool highlights. |

---

## 📋 Enterprise Production Templates

### Production `config.json` Template
```json
{
  "debug": false,
  "lang": "en",
  "animation": "CSGO",
  "commands": [
    "wt",
    "wondertrade",
    "intercambio"
  ],
  "discord_webhook": {
    "url": "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL",
    "avatar": "https://i.imgur.com/wondertrade_icon.png",
    "username": "WonderTrade Broadcast"
  },
  "databaseConfig": {
    "type": "MYSQL",
    "database": "wondertrade",
    "url": "jdbc:mysql://10.0.0.5:3306/wondertrade?useSSL=false&serverTimezone=UTC",
    "user": "wondertrade_user",
    "password": "SecureDatabasePassword789"
  },
  "pool": {
    "sizePool": 72,
    "minlvreq": 10,
    "minlv": 10,
    "maxlv": 45,
    "poolview": true,
    "israndom": false,
    "autoReset": true,
    "cooldownReset": 60,
    "blackList": {
      "pokemons": [
        "caterpie",
        "weedle",
        "magikarp",
        "feebas"
      ],
      "labels": ["untradeable", "event_locked"]
    }
  },
  "rates": {
    "shinyrate": 4096,
    "legendaryrate": 8192,
    "legendaryperfectivs": 3,
    "mythicalrate": 8192,
    "mythicalperfectivs": 3,
    "ultrabeastrate": 1024,
    "ultrabeastperfectivs": 3,
    "paradoxrate": 512,
    "paradoxperfectivs": 2
  },
  "cooldowns": {
    "cooldown": "30m",
    "cooldownPermission": {
      "wondertrade.vip": "15m",
      "wondertrade.master": "10m",
      "wondertrade.legendary": "5m"
    },
    "cooldownmessage": 10,
    "cooldownBroadcast": 30
  }
}
```
