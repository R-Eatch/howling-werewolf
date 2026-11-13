# Configuration guide

[简体中文](CONFIG-zh.md) · [Back to README](../README.md)

This page covers all five editions of 1.3.8: Forge 1.20.1, NeoForge 1.21.1, Fabric 1.21.1, Fabric 1.21.11 and NeoForge 26.1.2. They share the same keys and defaults.

## Where the files are

| File | Forge / NeoForge | Fabric | Controlled by |
|---|---|---|---|
| Common config | `config/howlingwerewolf-common.toml` | `config/howlingwerewolf-common.json` | The server (the integrated server in single player) |
| Client config | `config/howlingwerewolf-client.toml` | `config/howlingwerewolf-client.json` | Each player's own client |

- The files are created on first launch, and missing entries are filled in with their defaults.
- The TOML files are split into `[gameplay]` and `[worldGeneration]` sections, with an English comment above each entry.
- Fabric's JSON has no sections; each key carries its prefix, for example `"gameplay.enableCompanions": true`.
- Values that are out of range or malformed do not take effect, and the game uses a valid value instead. If a Fabric file cannot be parsed at all, every entry uses its default and the log reports an error.

## Gameplay (gameplay)

| Key | Default | Range | What it does |
|---|---|---|---|
| `enableCompanions` | `true` | true / false | Master switch for merchants, recruitment and companion orders. When off, existing companions are put away without deleting their belongings, and hunts and cooldowns pause; everything resumes when it is turned back on. |
| `enableCompanionSorting` | `true` | true / false | Switches sorting on its own. Turning it off only stops sorting; registered containers are kept and no items are moved. |
| `companionSortingReach` | `32` | 3–64 | How far, in blocks, a companion can still sort a container it cannot walk up to. It always walks over when there is a way; 3 means it skips anything it cannot reach on foot. |
| `npcModelSet` | `"modern"` | `modern` / `classic` | Model set for wild werewolves, spawn-egg werewolves, the Alpha and its summoned pack. `modern`: the Alpha and pack use the Ashen coat, and wild werewolves keep a random coat; `classic`: all of them use Adrian. Existing entities update after the config reloads; players' own skins are not affected. |
| `wolfInfectionChance` | `0.10` | 0.0–1.0 | Chance that damage from an untamed vanilla wolf infects a human player. |
| `feralWerewolfInfectionChance` | `0.20` | 0.0–1.0 | Chance that damage from a wild Feral Werewolf infects a human player. Werewolves summoned by the Alpha never infect. |
| `werewolfExperienceGainMultiplier` | `1.0` | 0.0–100.0 | Final multiplier on werewolf experience. Administrator XP commands are not affected. |
| `maxWerewolfLevel` | `20` | 1–25 | Maximum werewolf level. If you lower it, saved levels above the new limit are brought down to it on load. |
| `beastModeVoidDamage` | `false` | true / false | When `true`, Beast Form melee uses the out-of-world damage type; when `false`, it uses the same type as normal werewolf attacks. |
| `alphaTrialDamageFrequencyLimitTicks` | `10` | 0 and up | Minimum gap, in ticks, between two hits that damage the Alpha Trial boss. 0 removes the extra limit. Attackerless damage that bypasses invulnerability, and the player's Beast Form void damage, are not limited. |
| `preserveAlphaBadgeOnRevival` | `false` | true / false | When `true`, the Alpha Werewolf Badge is kept after it prevents a fatal hit, whatever the damage, `/kill` and the void included; a player saved from falling out of the world is returned to the last safe ground, else the respawn point (a respawn anchor is not drained), else the world spawn. When `false`, one badge is used up and the revival works like a Totem of Undying. |
| `showWerewolfEquipment` | `true` | true / false | Draws worn armor and elytra on the normal werewolf model. Held items are always shown. `false` hides every werewolf's equipment on this machine and overrides "Equipment: shown" on the skin page. |
| `enableWerewolfAutoStep` | `true` | true / false | Steps up automatically: 1.0 block as a Quadruped Wolf, 1.5 as a Werewolf and 2.0 as a Beast. `false` restores normal stepping. |
| `preventWerewolfFullMoonSleep` | `false` | true / false | When `true`, a player with werewolf blood cannot sleep in the Overworld on a full-moon night (from dusk, tick 12000, until morning), so the full moon cannot be skipped. |

## World generation (worldGeneration)

| Key | Default | Range | What it does |
|---|---|---|---|
| `generateSilverOre` | `true` | true / false | Generates silver ore in newly generated chunks. Existing chunks are not changed. |
| `generateWolfsbane` | `true` | true / false | Generates Wolfsbane in newly generated chunks. Existing chunks are not changed. |
| `hunterSpawnWeightMultiplier` | `1.0` | 0.0–10.0 | Multiplier on the Hunter's natural spawn weight (50 by default); 0 turns natural spawning off. This is a relative weight, not a spawn chance. Rejoin the world after changing it. |
| `feralWerewolfSpawnWeightMultiplier` | `1.0` | 0.0–10.0 | Multiplier on the Feral Werewolf's natural spawn weight (200 by default); 0 turns natural spawning off. The full-moon, night and light rules still apply. Rejoin the world after changing it. |
| `wolfsbaneGenerationWeight` | `100` | 0–200 | How often Wolfsbane generates. 0 turns it off; 100 is the default rate (about one attempt per 8 taiga chunks or 22 forest chunks); 200 doubles it. |

## Client (interface)

| Key | Default | What it does |
|---|---|---|
| `progressionNightfall` | `false` | Shows the K progression screen on the dark Nightfall canvas; `false` uses the white Moonlight one. Switching with the crescent in the screen's top-right corner saves it automatically. On Fabric the key is `interface.progressionNightfall`. |

## Tips

- World-generation settings only affect chunks generated afterwards. Back up important worlds before changing anything that touches world generation or existing saves.
- On a multiplayer server the server's common config applies; the client config only changes your own interface.
- For administrator commands, see the [administrator command guide](ADMIN_COMMANDS.md).
