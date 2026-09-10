# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
