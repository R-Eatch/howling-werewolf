# Howling Werewolf — NeoForge 26.1.2

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf logo" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge"><img src="https://img.shields.io/badge/Mod-1.3.0-blue?style=flat-square" alt="Development version"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.1.2-62B47A?style=flat-square" alt="Minecraft 26.1.2">
  <img src="https://img.shields.io/badge/NeoForge-26.1.2.122%2B-E04E14?style=flat-square" alt="NeoForge 26.1.2.122 or later">
  <img src="https://img.shields.io/badge/Java-25-007396?style=flat-square" alt="Java 25">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="Code license: MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="Asset license: CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README-zh.md">简体中文</a></p>

> This branch contains the NeoForge edition for Minecraft 26.1.2. The Forge edition for Minecraft 1.20.1 is maintained on the [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) branch.

Howling Werewolf is an independently developed transformation and progression mod built around infection, lunar transformations, two progression systems, three werewolf forms, Hunters and Silver, Wolfsbane, the Alpha Trial and a pack of companions.

## What's new in 1.3.0

- **A new progression screen.** Press **K** and the world falls away to moonlight: your own form stands before tonight's moon, ringed by experience, with your skills floating around you. A crescent at the top right switches between the white **Moonlight** canvas and the dark **Nightfall** canvas. The Skins and Reset screens match it.
- **A new companion window.** The same look in a lighter form: a panel floating over the game that opens at once, with orders on number keys and a dark band that explains whatever the pointer rests on. Hunts now show a progress bar.
- **Attacks with weight and rhythm.** With both hands free, the Werewolf rakes with the right claw and then the left. The Beast smashes down with both arms, then answers with a wide sweep. The Quadruped lunges and bites, then tears. Each form has its own timing, chained blows trade the lead, and first-person view shows both claws. Companions fight the same way.
- **Item frames direct sorting.** Hang a filled item frame on a chest or barrel and it keeps only that item: matching items come in and everything else moves out.
- **Trapped chests and shulker boxes stay yours.** Sorting passes over them, so they make a safe place for anything you want left as it is.
- **Companions speak in an older voice.** Every merchant and companion line is rewritten in both languages, with new lines for sorting. See the [dialogue catalog](docs/COMPANION-DIALOGUE.md).
- **Elbow fur fix.** The fur on the forearm now lies along the arm when it folds in a crouch, gallop or strike.

## Earlier versions

- **1.2.7**: companions walk to containers and sort by hand; four-tab companion window; wolf-like Beast gallop; `/werewolf unlockalpha`.
- **1.2.6**: companion Sort order and Modern UV submission rework.
- **1.2.5**: **C** opens the companion window, right-click opens a companion, custom address, private dialogue.
- **1.2.3–1.2.4**: the companion system: altar merchants, badge-confirmed recruiting, nine-slot inventories, follow, guard, rest and hunt, cross-dimension recall, upgrades, revival, names and coats.
- **1.2.2**: Modern hand-held item anchors and the **Classic — By R_Eatch / Modern — By thewolfulti** credits. See the [1.2.2 changelog](docs/CHANGELOG-1.2.2.md).
- **1.1.8**: fixed-height Auto Step: 1.0 block for Quadruped, 1.5 for Werewolf, 2.0 for Beast. `gameplay.enableWerewolfAutoStep` defaults to `true`.

## Documentation

| Guide | Contents |
|---|---|
| [Companion guide](docs/COMPANIONS.md) ([简体中文](docs/COMPANIONS-zh.md)) | Summoning, recruiting, orders, sorting, hunting, settings |
| [Hunt and trade tables](docs/COMPANION-TABLES.md) ([简体中文](docs/COMPANION-TABLES-zh.md)) | Every hunt entry, tier factor and merchant trade |
| [Dialogue catalog](docs/COMPANION-DIALOGUE.md) | Every merchant and companion line in both languages |
| [Custom skins](CUSTOM_SKINS.md) ([简体中文](CUSTOM_SKINS-zh.md)) | Painting templates and skin resource packs |
| [Administrator commands](ADMIN_COMMANDS.md) ([简体中文](ADMIN_COMMANDS-zh.md)) | `/werewolf` for testers and server operators |
| [Architecture provenance](docs/COMPANION-PROVENANCE.md) | What the companion system drew on and how it differs |

## Requirements

| Component | Version |
|---|---|
| Minecraft Java Edition | 26.1.2 |
| Mod loader | NeoForge 26.1.2.122 or later compatible 26.1.2.x release |
| Java | 25 |
| Howling Werewolf | 1.3.0 |

## Werewolf skins

- Open the werewolf menu with **K** by default → **Skins**. Switch **Classic / Modern** in the upper-right corner, then select **Apply** to save. Classic retains Adrian, Ashen, Onyx and Ivory. Modern includes Ashen, Black, Chestnut, Pale, Rusty, Snowy, Spotted, Stripped and Woods. Each coat covers all three forms.
- New players default to **Modern / Ashen**. Existing Classic choices are preserved when upgrading.
- Preview with mouse tracking, drag rotation, scroll-wheel zoom, and an equipment toggle, then apply your selection. Previewing does not unlock or activate a form. Level Reset is in the lower band of the Skill Tree page.
- Modern camera height follows standing, crouching and quadrupedal sprinting. Weapons retain native size and palm anchors; armor and elytra follow the imported bones under the existing equipment rules.
- Your selection persists through death, dimension changes, level resets, and curing/reawakening. Third-person rendering, inventory previews, and first-person claws share the same skin.
- Create a painting template and open Resource Packs from the skin screen to enable your own PNG skins. Reload edits with **F3 + T**. See the [custom skin guide](CUSTOM_SKINS.md) ([简体中文](CUSTOM_SKINS-zh.md)).

Update both server and clients to **1.3.0** (protocol 18). Player model and coat choices are independent: when A selects Modern and B selects Classic, both clients render A with Modern and B with Classic. Built-in coats need no extra pack. Custom coats require the same resource pack on observing clients; a missing pack falls back to Adrian for Classic or Ashen for Modern while preserving the saved choice.

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

Merchants stay **10 minutes** and announce this when summoned. A Moonbane Pearl costs either **6 Wolfsbane + 1 Diamond** or **12 Emeralds + 4 Wolfsbane**. See the [dialogue catalog](docs/COMPANION-DIALOGUE.md).

## Companion settings and controls

Press **C** to manage companions, remappable in Controls; **Pack** at the top of the K screen leads there too. In the server's `config/howlingwerewolf-common.toml`:

```toml
[gameplay]
enableCompanions = true
enableCompanionSorting = true
```

`enableCompanions = false` parks companions and blocks interactions while keeping records and inventories and pausing hunt and cooldown timers. `enableCompanionSorting = false` turns off sorting alone. Multiplayer uses the server's setting. The [companion guide](docs/COMPANIONS.md) has the full rules.

The Moonlight or Nightfall choice is a personal preference, stored in `config/howlingwerewolf-client.toml`.

## Spawn eggs and item animations

![Werewolf, Hunter, and Moon-Crowned Alpha Werewolf spawn eggs](docs/spawn-eggs.png)

- Werewolf and Hunter spawn eggs now use distinct 16×16 artwork following Minecraft 26.1.2's mob-specific egg style.
- The new **Moon-Crowned Alpha Werewolf Spawn Egg** is in the mod's creative tab and is available as `howlingwerewolf:alpha_werewolf_spawn_egg`. It spawns the same boss entity used by the Alpha Trial. A boss spawned this way is independent of the ritual and does not grant ritual completion rewards.
- Spear use now follows the native ready/holding animation in Werewolf and Beast forms. Tridents retain their separate throwing charge pose.

## Supported editions

- [Minecraft 1.20.1 · Forge](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge)
- [Minecraft 1.21.1 · NeoForge](https://github.com/R-Eatch/howling-werewolf/tree/1.21.1-neoforge)
- [Minecraft 26.1.2 · NeoForge](https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge) — this branch, version **1.3.0**.

## Build from source

Install a Java 25 JDK, then run:

```sh
./gradlew build -x test
```

On Windows, use `gradlew.bat build -x test`. The distributable JAR is `build/libs/howlingwerewolf-1.3.0-neoforge-26.1.2.jar`.

Current source version is **1.3.0**, protocol 18.

The final build passes, as do 21028 model, NPC and UI checks and 2624 artifact checks, including every line of progression text matching 1.2.7 byte for byte. 759 native NeoForge server checks pass, first upgrading from the real save 1.2.7 left behind, then through a real save and restart; they cover item frames on real chests, trapped chests and shulker boxes. On NeoForge 26.1.2.122 with an RTX 4060 Laptop GPU, a real client in an isolated game directory ran the full script in Chinese at GUI scale 2 and in English at scale 3: the companion walked, opened containers and carried items, item totals were conserved, the framed chest ended with only its item, the trapped chest stayed untouched, and first- and third-person strikes were captured for all three forms. A few small client-side adjustments made after those runs passed the build and regression only. Attack feel and long play sessions await the author's in-game review; this version was not checked with Sodium, shader packs, claim plugins or two real clients.

## Administrator commands

Server operators and testers can consult the [administrator command guide](ADMIN_COMMANDS.md), also available in [Simplified Chinese](ADMIN_COMMANDS-zh.md).

## License

Source code is licensed under the [Mozilla Public License 2.0](LICENSE.md). Original project art is licensed separately under [CC BY-SA 4.0](LICENSE-ASSETS.md). See [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) and [CREDITS.txt](CREDITS.txt) for attribution and provenance.
