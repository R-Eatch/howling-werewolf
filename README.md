# Howling Werewolf

[简体中文](README-zh.md)

Howling Werewolf is an independently developed werewolf progression Mod for Minecraft
Java Edition. Become infected, survive lunar transformations, grow through a two-part
skill system, master three playable forms, and challenge the Moon-Crowned Alpha.

The Mod was inspired by several outstanding werewolf Mods created by the Minecraft
community. It is not a port, fork, official continuation, or official remake of any
other project and is not affiliated with or endorsed by their creators.

## Requirements

| Component | Version |
|---|---|
| Minecraft Java Edition | 1.20.1 |
| Mod loader | Forge 47.4.16 or a compatible Forge 47.x build |
| Java | 17 |
| Howling Werewolf | 1.0.0 |

No GeckoLib installation is required. Multiplayer installations need the same Mod
version on both the client and dedicated server.

## Features

- Infection, delayed awakening, voluntary transformation, and forced full-moon shifts.
- Normal Werewolf, quadruped wolf, and Alpha-unlocked Beast forms.
- A level-based progression screen with separate tree skills and unlockable abilities.
- Empty-claw combat, scaling damage and defense, lifesteal, long claws, fire claws,
  tool-like claws, Bloody Bite, and Moonblood Surge.
- Wild Feral Werewolves, neutral Hunters, persistent village patrols, Silver equipment,
  Wolfsbane, and two opposing potions.
- A complete single-player Alpha Trial with ritual construction, a Hunter phase, a
  multi-stage boss fight, repeat rewards, and permanent Beast Form progression.
- English and Simplified Chinese localization.
- Configurable infection, experience gain, maximum level, world generation, Alpha
  damage timing, badge consumption, equipment display, and Beast damage behavior.
- Datapack tags for compatible silver weapons and werewolf food.

## Installation

1. Install Minecraft Java Edition 1.20.1 and Forge 47.4.16.
2. Place `howlingwerewolf-1.0.0.jar` in the instance's `mods` directory.
3. For multiplayer, install the same JAR on the dedicated server and every connecting
   client.
4. Start the game. Silver ore, Wolfsbane, and natural entities appear in newly generated
   eligible chunks according to the server configuration.

Back up important worlds before adding or updating any Mod.

## Getting started

There are two main ways to enter the bloodline:

- Survive a successful attack from a naturally spawned Feral Werewolf. The default
  infection chance is configurable and awakening occurs on a later night.
- Brew or obtain a Werewolf Potion to awaken immediately.

Once awakened, press `K` to open the progression screen. A full moon forces a shift in
the Overworld, while `J` controls voluntary transformation when the current rules allow
it. Hunt creatures while transformed to gain werewolf experience and spend the resulting
points on tree skills and abilities.

Wolfsbane represents the opposing path. A Wolfsbane Potion cures lycanthropy, while
Hunters carry Silver Swords and provide a renewable source of Wolfsbane Flowers.

## Forms

| Form | Access | Identity |
|---|---|---|
| Human | Default or revert with `J` | Normal equipment and survival rules; no transformed bonuses. |
| Werewolf | Awaken, then transform with `J` or under a full moon | Balanced claw combat, level-scaled damage reduction, movement bonuses, and most abilities. |
| Quadruped Wolf | Learn Wildstride Form and press `G` | Faster movement and stronger jumping with slightly lower attack damage; cannot wear equipment. |
| Beast | Win the Alpha Trial, then press `H` | Greatly increased combat power and defense with faster hunger drain; cannot wear equipment. |

Transformed players normally cannot wear armor or elytra. Armored Instinct permits them
only in normal Werewolf form. Only equipment that provides positive armor value weakens
the ability's natural-defense and movement bonuses; zero-armor equipment such as elytra
does not apply that penalty.

## Progression and abilities

Werewolf levels increase base claw damage and natural damage reduction. The default
maximum level is 20 and can be configured up to 25.

Tree skills specialize permanent statistics and passive effects, including:

- claw damage and Moonrend maximum-health damage;
- natural defense, speed, jumping, regeneration, and knockback/fall resistance;
- lifesteal, satiety, claw reach/looting, and improved hunting experience.

Abilities unlock new actions and rule changes, including Night Vision, wolf spirits,
Wildstride Form, Moonblood Surge, Armored Instinct, expanded diet, inventory claw-space
management, long/tool/fire claws, and Bloody Bite.

The Alpha Werewolf Badge has two related but distinct effects while placed in any hotbar
slot:

- a player with werewolf blood gains 10 base werewolf XP per minute, even while human;
- fatal-hit protection additionally requires the player to be transformed. By default
  the activation consumes one badge, but this can be changed in the common config.

Moving the badge out of the hotbar disables both effects and resets partial passive-XP
time.

## Default controls

All bindings can be changed in Minecraft's Controls menu.

| Key | Action |
|---|---|
| `K` | Open Werewolf Progression |
| `J` | Transform or revert |
| `G` | Toggle Quadruped Wolf form |
| `H` | Toggle Beast Form |
| `V` | Toggle automatic Werewolf Night Vision |
| `N` | Summon Wolf Spirits |
| `B` | Use Bloody Bite |
| `R` | Release Moonblood Surge |

## Hunters, Silver, and wild encounters

Hunters are neutral toward ordinary humans and hostile toward transformed werewolves.
They can appear alone at a low weight in forest biomes and form persistent 4–6 member
patrols around visited villages. Once a village patrol has reached its initial target,
missing members can be replaced one at a time after a one-Minecraft-day cooldown.

Silver Sword attacks deal increased damage to werewolves and apply Weakness. Every
ordinary Hunter drops at least one Wolfsbane Flower, with a chance for a second.

Feral Werewolves naturally spawn only in eligible Overworld forests during a dark full-
moon night. Their deliberately strict conditions make them dangerous encounters rather
than routine hostile mobs.

## Alpha Trial

The Alpha Trial is the endgame of the first progression arc.

1. Reach werewolf level 10 and prepare one central ritual altar, four ordinary ritual
   altars, one Alpha Werewolf Badge, and four Moonbane Pearls.
2. Place the ordinary altars at the four cardinal points, exactly three blocks from the
   central altar and at the same height.
3. Offer the badge at the center and one pearl at each outer altar.
4. Begin the ritual in the Overworld at night while in normal Werewolf form. The ward is
   intended for one player.
5. Defeat the empowered Hunters and then the Moon-Crowned Alpha.

The first victory permanently unlocks Beast Form and grants its progression rewards.
Repeat victories provide difficulty-dependent Moonbane Pearls and, on Hard, additional
skill and tree points.

## Configuration and datapacks

The common Forge configuration contains settings for:

- Wolf and Feral Werewolf infection chance;
- werewolf experience multiplier and maximum level;
- Silver Ore and Wolfsbane world generation;
- Beast Form void-damage behavior;
- Alpha Trial damage-frequency limit;
- whether badge revival consumes the badge;
- whether equipment is rendered on the normal Werewolf model.

Datapacks can extend these item tags:

- `howlingwerewolf:silver_weapons`
- `howlingwerewolf:werewolf_meat`

## Building from source

The repository uses the Gradle Wrapper and targets Java 17 bytecode.

Windows:

```bat
gradlew.bat clean build
```

Linux or macOS:

```bash
./gradlew clean build
```

The reobfuscated release JAR is written to `build/libs/`. Forge and Minecraft development
dependencies may need to be downloaded during the first build.

## Support and compatibility

Version 1.0.0 was validated through a normal single-player survival progression from a
new start through defeating the Ender Dragon and obtaining elytra, followed by targeted
tests for transformations, death/respawn, Peaceful mode, shaders, village Hunter
replacement, natural spawning, and the three playable forms.

When reporting a problem, include the Minecraft version, Forge version, Howling Werewolf
version, relevant configuration, installed Mod list, and the latest log or crash report.

## License and copyright

Copyright © 2026 R_Eatch. All Rights Reserved.

Howling Werewolf uses a custom proprietary community license. Defined non-commercial
personal use, unmodified redistribution, modpacks, servers, and media are permitted with
attribution. Publishing modified versions, ports, derivative development, or commercial
uses requires prior written authorization from R_Eatch. See [LICENSE.md](LICENSE.md) for
the complete controlling terms.

Runtime asset provenance is documented in
[ASSET_PROVENANCE.md](ASSET_PROVENANCE.md). Minecraft and Minecraft Forge remain subject
to their respective owners' terms and licenses.

Howling Werewolf is an unofficial community-created Mod. It is not affiliated with,
endorsed by, or approved by Microsoft, Mojang Studios, Minecraft Forge, or the creators
of the community werewolf Mods that inspired it.
