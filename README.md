# Howling Werewolf

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf logo" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/releases/latest"><img src="https://img.shields.io/github/v/release/R-Eatch/howling-werewolf?display_name=tag&amp;style=flat-square" alt="Latest release"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=flat-square" alt="Minecraft 1.20.1">
  <img src="https://img.shields.io/badge/Forge-47.4.16%2B-E04E14?style=flat-square" alt="Forge 47.4.16 or later">
  <img src="https://img.shields.io/badge/Java-17-007396?style=flat-square" alt="Java 17">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="Code license: MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="Asset license: CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README-zh.md">简体中文</a></p>

> A werewolf transformation and progression Mod for Minecraft Java Edition, centered on infection, lunar transformations, two progression systems, three werewolf forms, Hunters and Silver, Wolfsbane, and the Alpha Trial.

## Before the first howl

A claw wound left in the forest may seem no different from any other injury at first. Then another night falls, the dormant bloodline begins to awaken, and the moon makes its first choice for you.

At first, becoming a werewolf is an ordeal to survive. Only later does it become a power you can master. You will learn when to submit to the full moon and when to transform by your own will. With every hunt, you will sharpen your claws and fangs, awaken new instincts, and ultimately race across the wilds on all fours.

Once you are strong enough, begin the Moon-Oath ritual and enter the Alpha Trial. The Alpha will decide whether you are worthy of awakening the Beast buried deep within your bloodline.

**Howling Werewolf** is an independently developed werewolf transformation and progression Mod for Minecraft Java Edition. It connects infection, awakening, lunar transformations, levels, a skill tree, learnable skills, three werewolf forms, Hunters and Silver weapons, Wolfsbane, and the final Alpha Trial into a complete survival journey.

The Mod was inspired by several outstanding werewolf Mods created by the Minecraft community, but its code, gameplay systems, and project content were all developed independently. It is not a port, fork, official continuation, or official remake of any other project, nor is it affiliated with or acting on behalf of their creators.

## How the journey begins

1. **Let the werewolf bloodline take hold.** Become infected when attacked by a Feral Werewolf or a wolf in the forest, then wait for a later night to awaken. Alternatively, use a Werewolf Potion to begin immediately.
2. **Level up and develop your skills.** Press `K` to open the Werewolf Progression screen, where you can view your level, skill tree, learnable skills, form descriptions, and the Moon-Oath ritual.
3. **Learn to control the transformation.** When the current rules allow it, press `J` to transform or return to human form. Under a full moon, your werewolf blood will leave you unable to control your form.
4. **Challenge the Alpha of old.** Build the Moon-Oath ritual shown in the progression screen, defeat the Silver-Oath Watchers and the Moon-Crowned Alpha Werewolf, and finally awaken Beast Form.

## Requirements

| Component | Version |
|---|---|
| Minecraft Java Edition | 1.20.1 |
| Mod loader | Forge 47.4.16 or later recommended |
| Java | 17 |
| Howling Werewolf | 1.0.0 |

Forge 47.4.16 or a newer compatible Forge 47.x release is recommended. Forge 47.4.16 remains the primary test target for the current official release.

## What the bloodline brings

- Infection, delayed awakening, voluntary transformation, and forced full-moon transformations.
- Human form, normal Werewolf form, Quadruped Wolf form, and Alpha-unlocked Beast Form.
- A Werewolf Progression screen that serves as both a character sheet and an in-game guide.
- A skill tree, active and passive skills, form-unlock requirements, and Moon-Oath ritual guidance.
- Empty-hand claw combat, with damage and natural damage reduction that grow as you progress.
- Lifesteal, Long Claws, Fire Claws, Tool Claws, Bloody Bite, and Moonblood Surge.
- Wild Feral Werewolves, neutral Hunters, village patrols, Silver equipment, Wolfsbane, and two opposing potions.
- A complete single-player Alpha Trial: altar construction, a Hunter phase, a multi-stage boss fight, repeat rewards, and permanent Beast Form progression.
- Complete English and Simplified Chinese localization.
- Configurable infection chance, experience multiplier, maximum level, and many other gameplay values.

## Installation

1. Install Minecraft Java Edition 1.20.1 and Forge 47.4.16 or a newer compatible Forge 47.x release.
2. Place `howlingwerewolf-1.0.0.jar` in the game's `mods` directory.
3. Start the game.

Back up important worlds before adding or updating any Mod.

## Your first nights

There are two main ways to acquire the werewolf bloodline:

- Become infected while being attacked by a wild wolf or Feral Werewolf in the forest. The default infection chance is configurable, and an infected player will awaken on a later night.
- Brew or obtain a Werewolf Potion to awaken immediately.

Once awakened, press `J` to transform or return to human form whenever the bloodline's rules allow it. In the Overworld, a full moon will force a werewolf to transform, even if you would rather keep your bestial nature hidden that night.

Killing creatures while transformed grants werewolf experience. As you gain levels, you can invest the resulting progression points in the skill tree and new abilities.

Whenever you are unsure what to do next, press `K` to open the Werewolf Progression screen. It records your level and progression while also providing the skill tree, active and passive skills, form-unlock requirements, and the Moon-Oath structure, conditions, and rewards needed to begin the Alpha Trial.

Wolfsbane represents the path opposed to the werewolf bloodline. A Wolfsbane Potion can cure lycanthropy, while Hunters wield Silver Swords and provide a renewable source of Wolfsbane Flowers.

## Werewolf forms

| Form | Unlock and switch | Features |
|---|---|---|
| Human | Default form, or return with `J` | Uses normal equipment and survival rules and receives no transformed bonuses. |
| Werewolf | Press `J` after awakening, or be forced to transform under a full moon | Claw combat, level-scaled natural damage reduction, movement bonuses, and most werewolf abilities. |
| Quadruped Wolf | Learn **Wildstride Form**, then press `G` | Greatly increased movement speed and jump height, but slightly lower attack damage; cannot wear equipment. |
| Beast | Complete the Alpha Trial for the first time, then press `H` | Greatly enhanced combat, speed, jumping, and defense, but faster hunger drain; cannot wear equipment. |

Transformed werewolves cannot wear armor or elytra by default. **Armored Instinct** allows equipment only in normal Werewolf form.

Only equipment with a positive armor value weakens the natural damage reduction and movement bonuses provided by this ability. Zero-armor equipment such as elytra does not trigger this penalty.

## Levels, skill tree, and skills

Werewolf levels increase base claw damage and natural damage reduction. The default maximum level is 20 and can be configured up to 25.

The skill tree strengthens long-term attributes and passive effects, including:

- claw damage and Moonrend damage based on the target's maximum health;
- natural defense, speed, jumping, regeneration, knockback resistance, and fall resistance;
- lifesteal, satiety control, claw reach and looting, and additional hunting experience.

Skills unlock new actions and rules, including:

- Night Vision;
- Summon Wolf Spirits;
- Wildstride Form / Quadruped Wolf form;
- Moonblood Surge;
- Armored Instinct;
- Hard Life;
- Empty Claw;
- Long Claws;
- Tool Claws;
- Fire Claws;
- Bloody Bite.

### Alpha Werewolf Badge

An Alpha Werewolf Badge has two effects while placed in any hotbar slot:

- as long as you have werewolf blood, you gain **10 base werewolf XP per minute**, even while in human form;
- preventing a fatal hit additionally requires you to be transformed.

By default, fatal-hit protection consumes one badge. This behavior can be changed in the common configuration.

Moving the badge out of the hotbar disables both effects and resets any incomplete minute of passive experience progress.

## Default controls

All key bindings can be changed in Minecraft's Controls settings.

| Key | Action |
|---|---|
| `K` | Open Werewolf Progression |
| `J` | Transform or return to human form |
| `G` | Toggle Quadruped Wolf form |
| `H` | Toggle Beast Form |
| `V` | Toggle automatic Werewolf Night Vision |
| `N` | Summon Wolf Spirits |
| `B` | Use Bloody Bite |
| `R` | Release Moonblood Surge |

## Hunters, Silver, and wild encounters

Hunters are neutral toward ordinary humans and hostile toward transformed werewolves.

They can appear alone at a very low weight in forest biomes and can also form persistent patrols of **4–6 Hunters** around villages visited by players. Once a village first reaches its target patrol size, each Hunter lost can be replaced individually after a cooldown of one Minecraft day.

Silver Sword attacks deal additional damage to werewolves and inflict Weakness. Every ordinary Hunter drops at least one Wolfsbane Flower, with a chance to drop one more.

Feral Werewolves naturally spawn only in eligible Overworld forests on dark full-moon nights. Their strict spawning conditions make them dangerous and uncommon encounters.

## The Moon-Oath and Alpha Trial

The Alpha Trial is the final challenge of werewolf progression.

After opening the Werewolf Progression screen with `K`, you can view the Moon-Oath page at any time. It records the altar layout, entry requirements, necessary offerings, and challenge rewards.

1. Reach werewolf level 10 and prepare one Central Moon-Oath Altar, four ordinary Moon-Oath Altars, one Alpha Werewolf Badge, and four Moonbane Pearls.
2. Place the four ordinary altars at the four cardinal points, exactly three blocks from the central altar and at the same height.
3. Offer the badge at the central altar and one pearl at each of the four outer altars.
4. Begin the ritual at night in the Overworld while in normal Werewolf form. Each trial is designed for a single participating player.
5. Defeat the Silver-Oath Watchers, then challenge the Moon-Crowned Alpha Werewolf.

Your first victory permanently unlocks Beast Form and grants its corresponding progression rewards.

Repeat victories award Moonbane Pearls based on difficulty. On Hard difficulty, they also grant additional Skill Points and Skill-tree Points.

## Configuration and datapacks

The common Forge configuration allows you to adjust infection chance, experience multiplier, maximum level, and many other values, letting you change the pace of the entire werewolf journey to suit your preferences.

Take care when changing settings that affect world generation or existing saves, and back up important worlds before making major adjustments.

Datapacks can extend the following item tags:

- `howlingwerewolf:silver_weapons`
- `howlingwerewolf:werewolf_meat`

## Testing and compatibility

Version 1.0.0 has been tested through a complete normal-survival playthrough on a new world, including defeating the Ender Dragon and obtaining elytra.

**The Mod has not yet completed comprehensive multiplayer testing.** Its behavior in long-running multiplayer worlds therefore remains not fully verified for version 1.0.0.

Keep backups when using the Mod in multiplayer worlds, and include the server log when reporting multiplayer issues.

When reporting an issue, provide:

- the Minecraft version;
- the Forge version;
- the Howling Werewolf version;
- the relevant configuration;
- the list of installed Mods;
- the latest log or crash report.

## Future plans

The current official release target remains **Forge for Minecraft 1.20.1**.

Future development is planned to explore NeoForge editions for **Minecraft 1.21.1** and **Minecraft 26.1**. These are development directions, not confirmed release dates.

## License and copyright

Copyright © 2026 R_Eatch.

Unless otherwise stated, the original source code of **Howling Werewolf** is open source under the Mozilla Public License 2.0 (`MPL-2.0`).

Subject to the MPL-2.0, you may use, study, modify, distribute, port, and continue developing the project, including for commercial purposes, without obtaining prior permission from the author.

Unless otherwise stated, the project's original art assets are licensed under the Creative Commons Attribution-ShareAlike 4.0 International License (`CC BY-SA 4.0`).

Forks, ports, and continued development are welcome and do not require prior authorization. If you publish a port, fork, or significant derivative project, you are also welcome to let me know through GitHub or email.

The official logo and the Howling Werewolf project branding are not automatically included within the scope of the CC BY-SA 4.0 license. Third-party code and materials remain subject to their original licenses. See [LICENSE.md](LICENSE.md), [LICENSE-ASSETS.md](LICENSE-ASSETS.md), and [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) for details.

## Project independence

Howling Werewolf is an unofficial community-created Mod.

The project is not affiliated with, endorsed by, authorized by, or officially partnered with Microsoft, Mojang Studios, Minecraft Forge, or the creators of other werewolf Mods that inspired it.

It is not a port, fork, official continuation, or official remake of any other werewolf Mod.
