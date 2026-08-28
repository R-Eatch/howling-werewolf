# Howling Werewolf

[简体中文](README-zh.md)

## Before the first howl

A claw mark in a dark forest may look like just another wound. Then another night
arrives, the blood wakes, and the moon begins making choices for you.

At first, becoming a werewolf is something to survive. In time, it becomes something to
master. You learn when to surrender to the full moon and when to transform by your own
will. Each hunt feeds a growing bloodline: new instincts appear, claws take on strange
properties, and the wolf learns to run on four legs. Far beyond that first transformation
waits a five-altar moon-oath, a company of Silver-Oath Watchers, and the Moon-Crowned
Alpha whose defeat awakens the Beast within.

Howling Werewolf is an independently developed progression Mod for Minecraft Java
Edition. It turns lycanthropy into a survival journey rather than a single status effect:
infection and awakening lead into levels, a skill tree, learnable abilities, three
werewolf forms, Hunters, Silver, Wolfsbane, and a repeatable endgame trial.

The Mod was inspired by several outstanding werewolf Mods created by the Minecraft
community. It is not a port, fork, official continuation, or official remake of any
other project and is not affiliated with or endorsed by their creators.

## The journey in brief

1. **Survive the bloodline.** Become infected by a Feral Werewolf and awaken on a later
   night, or use a Werewolf Potion to begin immediately.
2. **Learn what the moon changed.** Press `K` to open the Werewolf Progression screen.
   It is the in-game guide to your level, skill tree, abilities, forms, and the Moon-Oath
   ritual.
3. **Master the transformation.** Press `J` to transform or return to human form when
   the current rules allow it. A full moon can take that choice away.
4. **Challenge the old Alpha.** Build the moon-oath shown in the progression screen,
   survive its Watchers, and earn the right to awaken Beast Form.

## Requirements

| Component | Version |
|---|---|
| Minecraft Java Edition | 1.20.1 |
| Mod loader | Forge 47.4.16 or a compatible Forge 47.x build |
| Java | 17 |
| Howling Werewolf | 1.0.0 |

## What the bloodline brings

- Infection, delayed awakening, voluntary transformation, and forced full-moon shifts.
- Normal Werewolf, quadruped wolf, and Alpha-unlocked Beast forms.
- A progression screen that serves as both character sheet and in-game guide, with
  separate tree skills, learnable abilities, form information, and Moon-Oath guidance.
- Empty-claw combat, scaling damage and defense, lifesteal, long claws, fire claws,
  tool-like claws, Bloody Bite, and Moonblood Surge.
- Wild Feral Werewolves, neutral Hunters, persistent village patrols, Silver equipment,
  Wolfsbane, and two opposing potions.
- A complete single-player Alpha Trial with ritual construction, a Hunter phase, a
  multi-stage boss fight, repeat rewards, and permanent Beast Form progression.
- English and Simplified Chinese localization.
- Infection chance, experience gain, maximum level, and many other values can be tuned
  to suit the experience you want.
- Datapack tags for compatible silver weapons and werewolf food.

## Installation

1. Install Minecraft Java Edition 1.20.1 and Forge 47.4.16.
2. Place `howlingwerewolf-1.0.0.jar` in the instance's `mods` directory.
3. Start the game. Silver ore, Wolfsbane, and natural entities appear in newly generated
   eligible chunks according to the server configuration.

Back up important worlds before adding or updating any Mod.

## Your first nights

There are two main ways to enter the bloodline:

- Survive a successful attack from a naturally spawned Feral Werewolf. The default
  infection chance is configurable and awakening occurs on a later night.
- Brew or obtain a Werewolf Potion to awaken immediately.

Once awakened, press `J` to transform or return to human form whenever the bloodline's
current rules permit it. In the Overworld, a full moon can force the transformation even
if you would rather remain human. Hunt creatures while transformed to gain werewolf
experience and turn that experience into levels and new choices.

Press `K` whenever you need to understand those choices. The Werewolf Progression
screen is the central guide for the Mod: it shows your current growth, the skill tree,
learnable active and passive abilities, form requirements, and the Moon-Oath ritual used
to begin the Alpha Trial. You do not need to discover the trial structure from an
external wiki before playing.

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

## Progression, skill tree, and abilities

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

Feral Werewolves naturally spawn only in eligible Overworld forests during a dark
full-moon night. Their deliberately strict conditions make them dangerous encounters
rather than routine hostile mobs.

## The Moon-Oath and Alpha Trial

The Alpha Trial is the end of the first progression arc. Its Moon-Oath page can be found
at any time from the progression screen opened with `K`; the page includes the altar
layout, entry conditions, costs, and rewards.

1. Reach werewolf level 10 and prepare one Central Moon-Oath Altar, four ordinary
   Moon-Oath Altars, one Alpha Werewolf Badge, and four Moonbane Pearls.
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

The common Forge configuration exposes infection chance, experience multiplier, maximum
level, and many other values. Adjust them to fit the kind of werewolf journey you want
to play. World-specific changes should be made carefully, and important saves should be
backed up before large configuration changes.

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

## Testing and compatibility

Version 1.0.0 was validated through a normal single-player survival progression from a
new start through defeating the Ender Dragon and obtaining elytra, followed by targeted
tests for transformations, death/respawn, Peaceful mode, shaders, village Hunter
replacement, natural spawning, and the three playable forms.

**Multiplayer has not yet completed a full test pass.** Version 1.0.0 should therefore
be treated as unverified for long-running multiplayer worlds. Keep backups and include
the server log when reporting multiplayer problems.

When reporting a problem, include the Minecraft version, Forge version, Howling Werewolf
version, relevant configuration, installed Mod list, and the latest log or crash report.

## Future plans

The current release target remains Forge for Minecraft 1.20.1. Future development is
planned to explore NeoForge editions for Minecraft 1.21.1 and Minecraft 26.1. These are
roadmap intentions rather than announced release dates.

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
