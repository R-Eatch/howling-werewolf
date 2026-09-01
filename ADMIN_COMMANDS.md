# Howling Werewolf Administrator Command Guide

[简体中文](ADMIN_COMMANDS-zh.md)

This guide applies to Howling Werewolf `1.0.5-beta` for Minecraft 1.21.1 / NeoForge 21.1.248. These commands are intended for development, acceptance testing, server administration, and save diagnostics.

## Requirements

- The root command is `/werewolf`.
- The command source needs permission level 2, normally an operator, a command block, or a single-player world with cheats enabled.
- `<target>` must resolve to one online player. A player name or a single-target selector such as `@s` or `@p` can be used.
- `true` enables a state and `false` disables it.
- State-changing commands run on the server, then refresh player dimensions and werewolf modifiers and synchronize the result to the client.
- `settree` and `setability` provide completion suggestions for valid IDs.

## Command reference

| Command | Behavior and notes |
|---|---|
| `/werewolf infect <target>` | Places the target in the infected-human stage, schedules awakening for a later night, and removes existing summoned spirit wolves. |
| `/werewolf awaken <target>` | Awakens immediately, clears infection, and enters normal Werewolf form. |
| `/werewolf cure <target>` | Clears werewolf identity, infection, form, progression, skills, abilities, cooldowns, and spirit wolves. Its current result matches `reset`, but it remains as the administrator-facing cure action. |
| `/werewolf transform <target> [state]` | Omitting `state` toggles transformation; `true` enters normal Werewolf form and `false` returns to human form. The command clears the full-moon lock. |
| `/werewolf setlevel <target> <level>` | Sets werewolf level and resets experience within that level. Syntax accepts 1–25, but the value cannot exceed the configured server maximum. |
| `/werewolf addxp <target> <amount>` | Adds 1–100000 raw werewolf experience and can trigger level-ups. The normal experience multiplier is not applied. |
| `/werewolf setskillpoints <target> <amount>` | Sets currently available ability points to 0–100; it does not add a delta. |
| `/werewolf settreepoints <target> <amount>` | Sets currently available skill-tree points to 0–100; it does not add a delta. |
| `/werewolf settree <target> <skill> <rank>` | Sets a skill-tree rank without spending points. Rank `0` clears it, and the value cannot exceed that skill's maximum. |
| `/werewolf setability <target> <ability> <unlocked>` | Unlocks or removes an ability without spending points. Removing Quadruped form exits that form. |
| `/werewolf resettree <target>` | Clears all skill-tree ranks while retaining identity, level, and abilities. |
| `/werewolf resetabilities <target>` | Clears all abilities while retaining identity, level, and the skill tree. |
| `/werewolf reset <target>` | Restores all werewolf data to new-player defaults. |
| `/werewolf forcemoon <target> <state>` | `true` forces and locks Werewolf form; `false` only removes the lock and neither returns to human form nor grants the bloodline to an ordinary human. |
| `/werewolf status [target]` | Shows identity, infection, form, Alpha state, level, experience, available points, dimension, and persistent-mirror state. Players may omit the target; the console must provide one. |

## Skill-tree IDs and maximum ranks

| ID | Maximum rank | ID | Maximum rank |
|---|---:|---|---:|
| `damage` | 6 | `moon_rend` | 3 |
| `defense` | 5 | `speed` | 5 |
| `resistance` | 5 | `regeneration` | 5 |
| `jump` | 3 | `knockback_resistance` | 2 |
| `fall_resistance` | 2 | `lifesteal` | 3 |
| `satiety` | 3 | `claw_efficiency` | 3 |
| `hunting_mastery` | 2 |  |  |

## Ability IDs

`night_vision`, `summon_wolf_spirit`, `quadruped_form`, `moonblood_surge`, `armored_instinct`, `hard_life`, `empty_claw_slot`, `long_claws`, `tool_claws`, `fire_claws`, and `bloody_bite`.

## Status diagnostics

The `persistedMirror` field reported by `/werewolf status [target]` means:

- `match`: live server data and the persistent recovery mirror agree.
- `DIFF`: the snapshots differ; inspect saving, dimension changes, or synchronization.
- `missing`: no persistent recovery mirror was found yet.

## Common test flow

```mcfunction
/werewolf awaken @s
/werewolf setlevel @s 20
/werewolf settreepoints @s 50
/werewolf setskillpoints @s 30
/werewolf settree @s damage 6
/werewolf setability @s quadruped_form true
/werewolf status @s
/werewolf reset @s
```
