# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.4.0] - 2026-11-16

### Added

- **Custom Menu & Rotom Phone Support**: Added a pluggable menu system (`WonderTradeMenuProvider` & `WonderTradeMenuRegistry`) allowing companion mods such as Rotom Phone, custom graphical screens, or web panels to replace standard chest GUIs with custom interfaces for trading and viewing the pool.
- **Headless Trading & Status API**: Added headless services (`WonderTradeActionService`, `WonderTradeContextService`) that let companion mods trigger trades, inspect active cooldowns, check requirements, and view pool statistics without opening any menus.
- **Developer API & Artifacts**: Published a dedicated `-api.jar` artifact containing all API interfaces, application services, and models for third-party modders and server developers.

### Changed

- Command handlers and menu dispatches now route through the unified `WonderTradeApi` and menu registry.
- Automated release workflow now publishes all release artifacts including developer API jars to GitHub and Discord.

## [1.3.9] - 2026-11-15

### Added
- **Expanded Database Support**: Added full compatibility for MariaDB, H2, SQLite, MySQL, MongoDB, and JSON, letting you connect your preferred database on any server host effortlessly.

### Fixed
- **Startup Crash**: Fixed an issue that could crash the server when reading saved WonderTrade pools on startup.
- **Safe Full-Party Trades**: Traded Pokémon are now safely sent to your PC storage if your party is already full (6/6), guaranteeing you never lose a Pokémon.
- **Accurate Trade Roulette**: The trade animation now correctly shows the actual Pokémon waiting in your server's pool.
- **Server Stability Improvements**: Prevented occasional errors during player logins, disconnects, background resets, and console command usage.

## [1.3.8] - 2026-10-18

### Added
- **Built-in Wiki Guide**: Added a complete, easy-to-read guide directly inside the mod files (`WIKI.md`) that explains all commands, trading rates, pool settings, and cooldown permissions without needing to look up external documentation.
- **Automatic Documentation Setup**: The server now automatically generates the wiki file into your `config/wondertrade/` folder on startup so you always have quick answers handy.

### Changed
- **Updated Core Library**: Upgraded the internal utility engine to version 1.3.8 for improved server stability and performance.

## [1.3.7] - 2026-10-12

### Changed

- **License**: Changed project license to CC-BY-NC-4.0 (Non-Commercial).

## [1.3.6] - 2026-09-10

### Added
- **Cobblemon 1.8.0 Support**: Fully updated to run smoothly on Minecraft 1.21.1 with Cobblemon 1.8.0.
- **Rebranded to UltraWondertrade**: Refreshed project branding, titles, and menus under the new name.
- **Cleaner Configuration**: Settings are now neatly organized into dedicated sections (Pool, Rates, and Cooldowns) for effortless setup.
- **Discord Release Updates**: Automated webhook notifications when new versions are released.
- **Extra Translations**: Added custom messages for missing permissions and rapid-click warnings.

### Changed
- **Instant Menu Loading**: Opening menus is now instant with zero server delay or freezing.
- **Lag-Free Trades & Background Storage**: Pokémon exchanges, pool refreshes, and database saves now run smoothly in the background.
- **Faster Special Rolls**: Greatly optimized Legendary, Mythical, Ultra Beast, and Paradox rolls for higher server performance.
- **Automatic Settings Migration**: Existing config files upgrade automatically without losing your custom values.

### Fixed
- **Database Connection Stability**: Fixed database timeouts and connection drops with automatic reconnects and faster data saving.
- **Player Cooldown Loading**: Fixed an issue where trade cooldowns occasionally failed to load on player join.
- **Memory Leak on Disconnect**: Fixed leftover player cooldown data accumulating when players left the server.
- **Menu Spam Prevention**: Protected trading menus against accidental double trades from rapid clicking.

### Removed
- Cleaned up obsolete settings and leftover legacy files.
