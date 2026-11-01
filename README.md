# Howling Werewolf — NeoForge 1.21.1

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf logo" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/1.21.1-neoforge"><img src="https://img.shields.io/badge/Mod-1.2.2-blue?style=flat-square" alt="Latest mod version 1.2.2"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.248%2B-E04E14?style=flat-square" alt="NeoForge 21.1.248 or later">
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square" alt="Java 21">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="Code license: MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="Asset license: CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README-zh.md">简体中文</a></p>

> This branch contains the NeoForge edition for Minecraft 1.21.1. The Forge edition for Minecraft 1.20.1 is maintained on the [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) branch.

Howling Werewolf **1.2.2** moves Modern hand-held items slightly below the palm center and raises the Quadruped mouth item into the bite line. The model switch and built-in coat credits are **Classic — By R_Eatch / Modern — By thewolfulti**. See the [1.2.2 changelog](docs/CHANGELOG-1.2.2.md).

Howling Werewolf is an independently developed transformation and progression Mod built around infection, lunar transformations, two progression systems, three werewolf forms, Hunters and Silver, Wolfsbane, and the Alpha Trial.

## Requirements

| Component | Version |
|---|---|
| Minecraft Java Edition | 1.21.1 |
| Mod loader | NeoForge 21.1.248 or later compatible 21.1.x release |
| Java | 21 |
| Howling Werewolf | 1.2.2 |

## Werewolf skins

- Open the werewolf menu with **K** by default → **Skins**. Switch **Classic / Modern** in the upper-right corner, then select **Apply** to save. Classic retains Adrian, Ashen, Onyx and Ivory. Modern includes Ashen, Black, Chestnut, Pale, Rusty, Snowy, Spotted, Stripped and Woods. Each coat covers all three forms.
- New players default to **Modern / Ashen**. Existing Classic choices are preserved when upgrading.
- Preview with mouse tracking, drag rotation, scroll-wheel zoom, and an equipment toggle, then apply your selection. Previewing does not unlock or activate a form. Level Reset is below the skill list on the Skill Tree page.
- Modern camera height follows standing, crouching and quadrupedal sprinting. Weapons retain native size and palm anchors; armor and elytra follow the imported bones under the existing equipment rules.
- Your selection persists through death, dimension changes, level resets, and curing/reawakening. Third-person rendering, inventory previews, and first-person claws share the same skin.
- Create a painting template and open Resource Packs from the skin screen to enable your own PNG skins. Reload edits with **F3 + T**. See the [custom skin guide](CUSTOM_SKINS.md) ([简体中文](CUSTOM_SKINS-zh.md)).

Update both server and clients to **1.2.2**. Player model and coat choices are independent: when A selects Modern and B selects Classic, both clients render A with Modern and B with Classic. Built-in coats need no extra pack. Custom coats require the same resource pack on observing clients; a missing pack falls back to Adrian for Classic or Ashen for Modern while preserving the saved choice.

## NPC model configuration

In `config/howlingwerewolf-common.toml`:

```toml
[gameplay]
npcModelSet = "modern"
```

- `modern` (default): Alpha and its summoned pack use Ashen; wild and spawn-egg werewolves receive one of the nine saved coats.
- `classic`: all affected NPCs use the Classic model and Adrian texture for their form.
- Single-player uses the integrated server's config; multiplayer uses the dedicated server's config. Existing NPCs update after the config file reloads. Clients receive the model and coat through entity tracking, regardless of their own config. This setting does not change player-selected skins.

## Spawn eggs and texture updates

![Werewolf, Hunter and Moon-Crowned Alpha Werewolf spawn eggs](docs/spawn-eggs.png)

- Werewolf and Hunter spawn eggs now have dedicated 16×16 icons.
- The new **Moon-Crowned Alpha Werewolf Spawn Egg** is in the mod's creative tab as `howlingwerewolf:alpha_werewolf_spawn_egg`. It spawns the same boss used by the Alpha Trial; a separately spawned boss does not bind to a ritual or award trial-completion rewards.
- The Alpha Badge, Moonbane Pearl, Wolfsbane flower, Silver Ore and Deepslate Silver Ore now use 16×16 artwork. All 18 runtime item and block textures use this size; existing werewolf coats and model appearances are retained.

## Automatic stepping

Automatic stepping uses fixed heights: **Werewolf 1.5 blocks**, **Quadruped Wolf 1 block**, and **Beast 2 blocks**. These heights do not scale with Jump rank, armor or potion effects; manual jumping retains its existing skill effects.

In `config/howlingwerewolf-common.toml`, `[gameplay]` contains `enableWerewolfAutoStep = true` by default. Set it to `false` to disable the added step height. Returning to human form also removes this bonus.

## Supported versions

Available for Minecraft **1.20.1 / Forge**, **1.21.1 / NeoForge**, and **26.1.2 / NeoForge**, with **1.2.2** as the latest mod version. The [GitHub project page](https://github.com/R-Eatch/howling-werewolf) links to each source branch.

## Build from source

Download or clone this branch, then use the Gradle wrapper with a Java 21 toolchain:

```powershell
./gradlew.bat clean build
```

The distributable JAR is written to `build/libs/howlingwerewolf-1.2.2-neoforge.jar`.

## Release status

**The maintainer completed a full gameplay test pass for 1.1.3 and tested the 1.1.8 changes. This 1.2.2 port has been built and checked with independent data/model regressions; in-game and two-client testing remain with the maintainer.** Follow the [GitHub project page](https://github.com/R-Eatch/howling-werewolf) and [version changelog](docs/CHANGELOG-1.2.2.md) for the latest update news and notes.

Back up every important world before adding or updating any Mod.

## Administrator commands

Server operators and testers can consult the [administrator command guide](ADMIN_COMMANDS.md), also available in [Simplified Chinese](ADMIN_COMMANDS-zh.md).

## License

Source code is licensed under the [Mozilla Public License 2.0](LICENSE.md). Original project art is licensed separately under [CC BY-SA 4.0](LICENSE-ASSETS.md). See [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) and [CREDITS.txt](CREDITS.txt) for attribution and provenance.
