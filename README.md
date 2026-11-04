# Howling Werewolf — NeoForge 26.1.2

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf logo" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge"><img src="https://img.shields.io/badge/Mod-1.2.6-blue?style=flat-square" alt="Development version"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.1.2-62B47A?style=flat-square" alt="Minecraft 26.1.2">
  <img src="https://img.shields.io/badge/NeoForge-26.1.2.122%2B-E04E14?style=flat-square" alt="NeoForge 26.1.2.122 or later">
  <img src="https://img.shields.io/badge/Java-25-007396?style=flat-square" alt="Java 25">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="Code license: MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="Asset license: CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README-zh.md">简体中文</a></p>

> This branch contains the NeoForge edition for Minecraft 26.1.2. The Forge edition for Minecraft 1.20.1 is maintained on the [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) branch.

Howling Werewolf **1.2.6** adds companion **Sort**: register allowed chests or barrels, merge partial stacks and optionally consolidate matching items. Servers can disable sorting independently. Modern models now emit their final authored UV polygons directly; the Beast sprint uses paired forelimbs followed by the hind pair. See the [1.2.6 guide](docs/COMPANIONS-1.2.6.md) ([简体中文](docs/COMPANIONS-1.2.6-zh.md)).

Howling Werewolf **1.2.5** improves companion controls: **C** opens the overview, right-click opens an individual page with an appearance preview, and sneak-right-click retains direct access to the nine-slot inventory. The guide covers rituals, recruitment, commands, upgrades and revival. Owners can customize their title; companion dialogue is private to its owner. Following companions reject hostile target acquisition and never retaliate. See the [1.2.5 companion guide](docs/COMPANIONS-1.2.5-zh.md), [loot and trade tables](docs/COMPANION-TABLES-1.2.4.md), and [implementation provenance](docs/COMPANION-PROVENANCE.md).

The 1.2.3 framework retains altar merchants, badge-confirmed recruitment, nine-slot inventories, follow/guard/rest/hunt, cross-dimension recall, upgrades, revival, names and skins. Owner UUID records persist independently of live entities; models follow the world NPC setting. See the [1.2.3 framework guide (Chinese)](docs/COMPANIONS-1.2.3-zh.md).

Howling Werewolf **1.2.2** moves Modern hand-held items slightly below the palm center and raises the Quadruped mouth item into the bite line. The model switch and built-in coat credits are **Classic — By R_Eatch / Modern — By thewolfulti**. See the [1.2.2 changelog](docs/CHANGELOG-1.2.2.md).

Howling Werewolf 1.1.8 adds fixed-height Auto Step: **1.0 block** for Quadruped Wolf, **1.5 blocks** for Werewolf, and **2.0 blocks** for Beast. In `config/howlingwerewolf-common.toml`, `gameplay.enableWerewolfAutoStep` defaults to `true`; set it to `false` to restore normal stepping. Manual jumping retains its existing skill and form rules. The 1.1.7 Fire Claws cooked-loot fix, Alpha combat timing and 16×16 item/block textures are retained.

This build retains the NeoForge 1.21.1 Long Claws splash formula, including Moonrend in normal-form splash damage. Long Claws secondary hits still do not trigger Lifesteal.

Howling Werewolf is an independently developed transformation and progression Mod built around infection, lunar transformations, two progression systems, three werewolf forms, Hunters and Silver, Wolfsbane, and the Alpha Trial.

## Requirements

| Component | Version |
|---|---|
| Minecraft Java Edition | 26.1.2 |
| Mod loader | NeoForge 26.1.2.122 or later compatible 26.1.2.x release |
| Java | 25 |
| Howling Werewolf | 1.2.6 (Companion sorting) |

## Werewolf skins

- Open the werewolf menu with **K** by default → **Skins**. Switch **Classic / Modern** in the upper-right corner, then select **Apply** to save. Classic retains Adrian, Ashen, Onyx and Ivory. Modern includes Ashen, Black, Chestnut, Pale, Rusty, Snowy, Spotted, Stripped and Woods. Each coat covers all three forms.
- New players default to **Modern / Ashen**. Existing Classic choices are preserved when upgrading.
- Preview with mouse tracking, drag rotation, scroll-wheel zoom, and an equipment toggle, then apply your selection. Previewing does not unlock or activate a form. Level Reset is below the skill list on the Skill Tree page.
- Modern camera height follows standing, crouching and quadrupedal sprinting. Weapons retain native size and palm anchors; armor and elytra follow the imported bones under the existing equipment rules.
- Your selection persists through death, dimension changes, level resets, and curing/reawakening. Third-person rendering, inventory previews, and first-person claws share the same skin.
- Create a painting template and open Resource Packs from the skin screen to enable your own PNG skins. Reload edits with **F3 + T**. See the [custom skin guide](CUSTOM_SKINS.md) ([简体中文](CUSTOM_SKINS-zh.md)).

Update both server and clients to **1.2.6** (protocol 16). Player model and coat choices are independent: when A selects Modern and B selects Classic, both clients render A with Modern and B with Classic. Built-in coats need no extra pack. Custom coats require the same resource pack on observing clients; a missing pack falls back to Adrian for Classic or Ashen for Modern while preserving the saved choice.

## NPC model configuration

In `config/howlingwerewolf-common.toml`:

```toml
[gameplay]
npcModelSet = "modern"
```

- `modern` (default): Alpha and its summoned pack use Ashen; wild and spawn-egg werewolves receive one of the nine saved coats.
- `classic`: the combat NPCs above use the Classic model and Adrian texture for their form.
- Merchants and companions follow this model setting too. Merchants choose random coats; recruited companions support renaming and skin selection, remembering separate Classic and Modern choices.
- Single-player uses the integrated server's config; multiplayer uses the dedicated server's config. Existing NPCs update after the config file reloads. Clients receive the model and coat through entity tracking, regardless of their own config. This setting does not change player-selected skins.

Merchants stay **10 minutes** and announce this when summoned. A Moonbane Pearl costs either **6 Wolfsbane + 1 Diamond** or **12 Emeralds + 4 Wolfsbane**. See the [complete dialogue catalog](docs/COMPANION-DIALOGUE-1.2.4-zh.md).

## Companion setting and controls

Press **C** to manage companions, remappable in Controls. The K-menu shortcut sits directly above Skill Tree. In the server's `config/howlingwerewolf-common.toml`:

```toml
[gameplay]
enableCompanions = true
enableCompanionSorting = true
```

Set to `false` to park companions and block interactions while retaining records/inventories and pausing hunt/cooldown timers. Enabled by default; multiplayer uses the server setting. See the [full rules](docs/COMPANIONS-1.2.4-zh.md).

Set only `enableCompanionSorting = false` to stop sorting while keeping registrations and other companion features. Choose a companion → Sort → Register storage, sneak-right-click allowed chests/barrels, then reopen management to finish registration and start sorting. The work area extends eight blocks horizontally and four vertically, with up to 24 logical containers; a double chest counts as one. See the [sorting guide](docs/COMPANIONS-1.2.6.md).

## Spawn eggs and item animations

![Werewolf, Hunter, and Moon-Crowned Alpha Werewolf spawn eggs](docs/spawn-eggs.png)

- Werewolf and Hunter spawn eggs now use distinct 16×16 artwork following Minecraft 26.1.2's mob-specific egg style.
- The new **Moon-Crowned Alpha Werewolf Spawn Egg** is in the mod's creative tab and is available as `howlingwerewolf:alpha_werewolf_spawn_egg`. It spawns the same boss entity used by the Alpha Trial. A boss spawned this way is independent of the ritual and does not grant ritual completion rewards.
- Spear use now follows the native ready/holding animation in Werewolf and Beast forms. Tridents retain their separate throwing charge pose.

## Supported editions

- [Minecraft 1.20.1 · Forge](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge)
- [Minecraft 1.21.1 · NeoForge](https://github.com/R-Eatch/howling-werewolf/tree/1.21.1-neoforge)
- [Minecraft 26.1.2 · NeoForge](https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge) — this branch, version **1.2.6** (sorting and Modern rendering/gait updates).

## Build from source

Install a Java 25 JDK, then run:

```sh
./gradlew build -x test
```

On Windows, use `gradlew.bat build -x test`. The distributable JAR is `build/libs/howlingwerewolf-1.2.6-neoforge-26.1.2.jar`.

Current source version is **1.2.6**, protocol 16. The 1.2.5 baseline is preserved in a local tag; 455 native server, 21129 model/NPC/UI and 1323 artifact checks passed, along with the Sodium GPU comparison; see the [1.2.6 guide](docs/COMPANIONS-1.2.6.md#validation-scope) for current validation and its limits. Existing saved key bindings are preserved; change them to C or reset them in Controls. Releases are published separately by the maintainer.

## Administrator commands

Server operators and testers can consult the [administrator command guide](ADMIN_COMMANDS.md), also available in [Simplified Chinese](ADMIN_COMMANDS-zh.md).

## License

Source code is licensed under the [Mozilla Public License 2.0](LICENSE.md). Original project art is licensed separately under [CC BY-SA 4.0](LICENSE-ASSETS.md). See [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) and [CREDITS.txt](CREDITS.txt) for attribution and provenance.
