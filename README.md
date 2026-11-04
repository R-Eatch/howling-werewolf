# Howling Werewolf

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf logo" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/main"><img src="https://img.shields.io/badge/latest_source-1.2.2-blue?style=flat-square" alt="Latest source: 1.2.2"></a>
  <a href="https://github.com/R-Eatch/howling-werewolf/releases/latest"><img src="https://img.shields.io/github/v/release/R-Eatch/howling-werewolf?display_name=tag&amp;label=published%20release&amp;style=flat-square" alt="Published GitHub release"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.20.1_%7C_1.21.1_%7C_1.21.11_%7C_26.1.2-62B47A?style=flat-square" alt="Minecraft 1.20.1, 1.21.1, 1.21.11 and 26.1.2">
  <img src="https://img.shields.io/badge/loaders-Forge_%7C_NeoForge_%7C_Fabric-E04E14?style=flat-square" alt="Forge, NeoForge and Fabric">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="Code license: MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="Asset license: CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README-zh.md">简体中文</a></p>

> This branch contains **1.2.3-beta.1 for Minecraft 1.21.11 / Fabric** (`1.21.11-fabric`). The complete gameplay introduction and information about the other released editions are retained below.

> A werewolf transformation and progression Mod for Minecraft Java Edition, centered on infection, lunar transformations, two progression systems, three werewolf forms, Hunters and Silver, Wolfsbane, and the Alpha Trial.

## Fabric 1.21.11 Beta

**1.2.3-beta.1** makes every item easy to find in Creative mode: alongside the Howling Werewolf tab, which Fabric places on the second page of the creative inventory, items now also appear on the first page in Ingredients, Combat, Food & Drinks, Spawn Eggs, Natural Blocks and Functional Blocks.

This port uses the published **1.2.2** baseline, retaining Classic / Modern forms, abilities and skill trees, hunters, the Alpha Trial, skins and template export. The unpublished NeoForge companion system is outside this baseline.

- Requires Minecraft **1.21.11**, Java **21**, Fabric Loader **0.20.1 or newer** and Fabric API **0.141.6+1.21.11**.
- Test artifact: `howlingwerewolf-1.2.3-fabric-1.21.11-beta.jar`. Install the same version and matching Fabric API on the client and server.
- Fabric configuration: `config/howlingwerewolf-common.json`, with `gameplay.*` and `worldGeneration.*` keys. Restart the world/server after changes. Existing Forge/NeoForge TOML files are preserved.
- This JAR declares Minecraft `~1.21.11`. Later Minecraft releases require their own port, build and validation.
- Build with Java 21 and `./gradlew build`; the distributable JAR is in `build/libs/`. Pins Loom 1.16.1 / Gradle 9.4.1, with official Mojang mappings and separate common/client source sets.

## 1.2.2 update

This update adds **Modern models for all three forms: Werewolf, Quadruped Wolf, and Beast**, with **9 skins for each model**. The models and skins were designed and created by **thewolfulti**. The Classic set remains available through the skin-screen switch.

The nine palettes are **Ashen, Black, Chestnut, Pale, Rusty, Snowy, Spotted, Stripped, and Woods**. This version also includes animation, held-item, equipment, and camera support for the new models, plus server-controlled NPC models and coats. See the [1.2.2 changelog](docs/CHANGELOG-1.2.2.md).

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

## What the bloodline brings

- Infection, delayed awakening, voluntary transformation, and forced full-moon transformations.
- Human form, normal Werewolf form, Quadruped Wolf form, and Alpha-unlocked Beast Form.
- A Werewolf Progression screen that serves as both a character sheet and an in-game guide.
- A skill tree, active and passive skills, form-unlock requirements, and Moon-Oath ritual guidance.
- Empty-hand claw combat, with damage and natural damage reduction that grow as you progress.
- Lifesteal, Long Claws, Fire Claws, Tool Claws, Bloody Bite, and Moonblood Surge.
- Wild Feral Werewolves, neutral Hunters, village patrols, Silver equipment, Wolfsbane, and two opposing potions.
- A complete single-player Alpha Trial: altar construction, a Hunter phase, a multi-stage boss fight, repeat rewards, and permanent Beast Form progression.
- Built-in werewolf skins, in-game previews, and resource-pack custom skins with drawing templates.
- Dedicated Werewolf, Hunter, and Moon-Crowned Alpha Werewolf spawn egg icons.
- Complete English and Simplified Chinese localization.
- Configurable infection chance, experience multiplier, maximum level, and many other gameplay values.

## Werewolf skins and custom skins

Press **K** → **Skins**, switch **Classic / Modern** in the upper-right corner, choose a coat, and select **Apply** to save it. Classic retains the original brown **Adrian**, amber-eyed gray **Ashen**, gold-eyed black **Onyx**, and red-eyed white **Ivory**. Modern offers nine palettes, each covering Werewolf, Quadruped Wolf, and Beast.

New players default to **Modern / Ashen**. Existing Classic choices and legacy saves without a model field remain Classic. Preview with mouse tracking, drag rotation, scroll zoom, and equipment visibility; previewing does not unlock or switch your actual form. Each player's model and coat are saved independently across death, dimension travel, level resets, and curing followed by reawakening. Third-person rendering, inventory previews, and first-person claws use the matching appearance.

Choose **Create Template** to export PNGs, Blockbench drawing templates, and resource-pack instructions for the selected model set. Modern templates use the selected built-in coat, or Ashen when exporting from a custom coat. Export your own PNGs, enable the pack, and press **F3 + T** to reload resources. Your work can then be selected in Skins.

Multiplayer synchronizes each player's model set and skin ID without uploading images. Built-in coats need no extra pack. Custom coats require the same pack on observing clients; missing resources fall back to Classic Adrian or Modern Ashen while preserving the saved selection. Resource-pack formats differ between Minecraft editions, so use templates from the matching branch.

See the [custom-skin guide](CUSTOM_SKINS.md), also available in [Simplified Chinese](CUSTOM_SKINS-zh.md).

## Installation

1. For this branch, install Minecraft 1.21.11, Java 21, Fabric Loader 0.20.1 or newer, and Fabric API for 1.21.11.
2. Build this Beta from source and place `build/libs/howlingwerewolf-1.2.3-fabric-1.21.11-beta.jar` in `mods`. Other released editions are available from [GitHub Releases](https://github.com/R-Eatch/howling-werewolf/releases), [Modrinth](https://modrinth.com/mod/howling-werewolf), or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/howling-werewolf).
3. Start the game. Clients and servers should use the same Mod version for their Minecraft edition.

Back up important worlds before adding or updating any Mod.

## Your first nights

There are two main ways to acquire the werewolf bloodline:

- Become infected while being attacked by a wild wolf or Feral Werewolf in the forest. The default infection chance is configurable, and an infected player will awaken on a later night.
- Brew or obtain a Werewolf Potion, then drink it to awaken immediately.

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

Transformed werewolves cannot wear armor or elytra by default. **Armored Instinct** allows equipment only in normal Werewolf form. Equipped items are shown by default (`showWerewolfEquipment=true`); their display can be disabled in the configuration.

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

## Hunters, Silver, Wolfsbane, Moonbane Pearls, and wild encounters

Hunters are neutral toward ordinary humans and hostile toward transformed werewolves.

They can appear alone at a very low weight in forest biomes and can also form persistent patrols of **4–6 Hunters** around villages visited by players. Once a village first reaches its target patrol size, each Hunter lost can be replaced individually after a cooldown of one Minecraft day.

Silver Sword attacks deal additional damage to werewolves and inflict Weakness. Every ordinary Hunter drops at least one Wolfsbane Flower, with a chance to drop one more and a low chance to drop a Silver Ingot.

Wolfsbane Flowers generate naturally in forest biomes. Village Hunter patrols remain the most reliable renewable source.

Feral Werewolves naturally spawn only in eligible Overworld forests on dark full-moon nights. Their strict spawning conditions make them dangerous and uncommon encounters.

A Moonbane Pearl is crafted from an Ender Pearl and Wolfsbane Flowers. It is used in the Moon-Oath and can also be used to craft an Alpha Werewolf Badge.

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

## Struggling with the Alpha Trial? Here is the author's reference guide:

The Alpha Trial opens at werewolf **level 10**, but Beast Form is a powerful reward, so the challenge is demanding. Boss health and combat phases vary with the game difficulty. Your first victory on any difficulty unlocks Beast Form.

### The Alpha's moves

1. **Melee attacks:** The Alpha attacks targets within melee reach.
2. **Summoning:** It periodically calls minions that inflict negative effects. Additional summons are queued when it first reaches half health and after revival. The Alpha pauses its attacks while channeling a summon, giving you an opening to deal damage, but existing minions still need your attention.
3. **Blink strike:** It teleports behind you and attempts a heavy strike that also applies a negative effect. Keep enough health in reserve to survive this burst.
4. **Prowling:** This occurs only before revival. Each successful melee hit has a **25%** chance to trigger a retreat intended to evade your attacks. These retreats give you some breathing room and reduce the pressure of continuous melee combat.
5. **Revival:** This occurs only on Normal and Hard, with the restored health depending on difficulty. The revived Alpha no longer prowls, making the final phase more relentless.

### Easy and Normal: levels and build

Consider reaching **level 15 on Easy** or **level 18 on Normal** before attempting the trial, with the following build:

- **Skill tree:** Max out Claw Damage, Moonrend Claws, Natural Defense, Regeneration, and Lifesteal. Raise Speed, Knockback Resistance, Resistance, and Claw Efficiency as far as you can.
- **Skills:** Learn Night Vision, Moonblood Surge, Bloody Bite, Empty Claw, and Long Claws.

### Hard: recommended at level 20

Hard is intended for late-game progression, with stronger boss bonuses and a more dangerous revival phase. The following approach also works on Easy and Normal:

1. **Hunter phase:** Use Long Claws' area damage while moving around the group to avoid being surrounded.
2. **Open against the Alpha:** Apply Weakness II with Bloody Bite, then focus on damage while the debuff lasts. Aim to remove roughly one fifth to one quarter of the boss's health during this opening.
3. **Follow with Moonblood Surge:** Depending on your health, activate Moonblood Surge as the bite's debuff is about to expire. Use its strong healing, absorption, and resistance to keep attacking, and try to finish the fight during the buff.
4. **Prepare another opening:** If the boss survives, stabilize your health and keep moving. Once Bloody Bite's cooldown finishes, reapply the debuff and look for another opening. Practice the attack rhythm, and try falling critical hits: when vanilla critical-hit conditions are met, base melee damage receives a **1.5×** multiplier.

You can also make use of Lifesteal by keeping other attackable creatures in the arena and attacking or killing them when you need health. Golden Apples, Totems of Undying, Alpha Werewolf Badges, and other healing supplies can provide more room for error. A badge must be in your hotbar, and its fatal-hit protection requires you to be transformed.

I have tested these recommended levels and builds in actual in-game fights on all three difficulties and defeated the Alpha. With practice, you can overcome the challenge too. Good luck defeating the Alpha!

### **Cheese strat**

I do not recommend the following methods, since they skip part of the gameplay experience, but I have included some strategies for players who want them.

- **Unlock the form first:** Defeat the Alpha on Easy, then use the unlocked Beast Form for Normal or Hard attempts.
- **Experiment with the arena:** Try a shelter with only two blocks of vertical clearance and fight beneath it, using a similar idea to an Enderman shelter. Adjust the layout around the Alpha's blink positions and movement.
- **Use a boat:** Trap the Alpha in a boat, just as you would force an Enderman into one. (Poor Alpha, stuck in a boat and toyed with at your leisure—how humiliating. I left the Alpha able to ride boats partly to give some players a cheese option, though I might prevent it from being trapped in boats in a future update :>)
- **Tune the challenge to your pace:** The configuration includes powerful optional settings that are disabled by default, such as keeping a badge after revival. Some settings can substantially reduce the challenge, making them useful for a relaxed playthrough or practicing the fight.

## Configuration and datapacks

Each edition's common configuration allows you to adjust infection chance, experience multiplier, maximum level, and many other values, letting you change the pace of the entire werewolf journey to suit your preferences.

Configuration file: `config/howlingwerewolf-common.json` on this Fabric branch (Forge/NeoForge editions still use TOML).

Take care when changing settings that affect world generation or existing saves, and back up important worlds before making major adjustments.

Datapacks can extend the following item tags:

- `howlingwerewolf:silver_weapons`
- `howlingwerewolf:werewolf_meat`

Server operators and testers can consult the [administrator command guide](ADMIN_COMMANDS.md), also available in [Simplified Chinese](ADMIN_COMMANDS-zh.md).

## Testing and compatibility

**This Fabric Beta has native GameTest and real graphics-client checks covering attributes/skills, the trial, persistence, respawn, dimension synchronization, model previews and resource reloads. Observer tests use a real client and a simulated second server player. Sodium 0.8.14, exported resource-pack activation and real world reopen checks also pass.** Historical released-edition coverage includes the 1.1.3 playthrough and the 1.1.8 changes. Please report bugs through [GitHub Issues](https://github.com/R-Eatch/howling-werewolf/issues).

The full-playthrough testing baseline includes starting normal survival in a new world, defeating the Ender Dragon, and obtaining elytra.

**The Mod has not yet completed comprehensive multiplayer testing.** Long-running multiplayer worlds still need further validation. Keep backups and include the server log when reporting multiplayer issues.

When reporting an issue, provide:

- the Minecraft version;
- the loader name and version (Fabric, Forge or NeoForge);
- the Howling Werewolf version;
- the relevant configuration;
- the list of installed Mods;
- the latest log or crash report.

## Source, changelogs, and building

The project maintains **Forge for Minecraft 1.20.1**, **NeoForge for Minecraft 1.21.1**, and **NeoForge for Minecraft 26.1.2**. This branch adds the **Minecraft 1.21.11 Fabric / 1.2.3-beta.1** port; the latest published source on the other release branches is **1.2.2**.

[GitHub project and latest news](https://github.com/R-Eatch/howling-werewolf) · [Release notes and available files](https://github.com/R-Eatch/howling-werewolf/releases) · [Report an issue](https://github.com/R-Eatch/howling-werewolf/issues)

## Credits

Special thanks to **thewolfulti** for designing, creating, and sharing the **Modern Werewolf, Quadruped Wolf, and Beast models**, with **9 skins for each model**. Thank you for the work and for sharing these new appearances with the project and its players.

These models and skins follow the project's existing **CC BY-SA 4.0** asset license. When sharing or adapting them, credit **thewolfulti**, identify your changes, and follow the ShareAlike requirements. Classic models and artwork are by **R_Eatch**. See [LICENSE-ASSETS.md](LICENSE-ASSETS.md) for the terms.

## License and copyright

Copyright © 2026 R_Eatch.

Unless otherwise stated, the original source code of **Howling Werewolf** is open source under the Mozilla Public License 2.0 (`MPL-2.0`).

Unless otherwise stated, the project's original art assets are licensed under the Creative Commons Attribution-ShareAlike 4.0 International License (`CC BY-SA 4.0`).

Forks, ports, and continued development are welcome and do not require prior authorization. If you publish a port, fork, or significant derivative project, you are also welcome to let me know through GitHub or email.

The official logo and the Howling Werewolf project branding are not automatically included within the scope of the CC BY-SA 4.0 license. Third-party code and materials remain subject to their original licenses. See [LICENSE.md](LICENSE.md), [LICENSE-ASSETS.md](LICENSE-ASSETS.md), and [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) for details.

## Project independence

Howling Werewolf is an unofficial community-created Mod.

The project is not affiliated with, endorsed by, authorized by, or officially partnered with Microsoft, Mojang Studios, Minecraft Forge, or the creators of other werewolf Mods that inspired it.

It is not a port, fork, official continuation, or official remake of any other werewolf Mod.
