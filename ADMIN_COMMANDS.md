# Howling Werewolf Administrator Command Guide

[简体中文](ADMIN_COMMANDS-zh.md)

This guide applies to Howling Werewolf 1.0.5 for Minecraft 1.20.1 / Forge 47.4.16. These commands are intended for development, acceptance testing, server administration, and save diagnostics.

## Requirements and notation

- The root command is `/werewolf`.
- The command source needs permission level 2, normally an operator, a command block, or a single-player world with cheats enabled.
- `<target>` must resolve to one online player. A player name or a single-target selector such as `@s` or `@p` can be used.
- `true` enables a state and `false` disables it.
- State-changing commands run on the server, then refresh player dimensions and werewolf modifiers and synchronize the result to the client.

## State and transformation commands

### `/werewolf infect <target>`

Forces the target into the infected-human stage, removes existing summoned spirit wolves, and schedules awakening for a later night.

Example: `/werewolf infect Steve`

### `/werewolf awaken <target>`

Immediately completes awakening, clears infection state, grants the werewolf bloodline, and enters normal Werewolf form.

Example: `/werewolf awaken Steve`

### `/werewolf cure <target>`

Completely cures the target and restores default human state. This clears werewolf identity, infection, form, progression, abilities, cooldowns, Alpha completion, modifiers, and summoned spirit wolves. Its current result is the same as `reset`, but it remains as the administrator-facing cure action.

Example: `/werewolf cure Steve`

### `/werewolf transform <target> [state]`

- Omit `[state]` to toggle between the current transformed state and human form.
- `true` grants the werewolf bloodline if needed, clears infection, and enters normal Werewolf form.
- `false` returns to human form without granting the bloodline to an ordinary human.
- Every form of this command clears the `forcemoon` lock.

Examples:

```mcfunction
/werewolf transform Steve
/werewolf transform Steve true
/werewolf transform Steve false
```

### `/werewolf forcemoon <target> <state>`

- `true` grants the werewolf bloodline if needed, clears infection, transforms the target, and enables the full-moon lock.
- `false` only removes the lock. It does not grant the bloodline and does not automatically return an already transformed player to human form.

Example: `/werewolf forcemoon Steve true`

### `/werewolf reset <target>`

Restores all werewolf data to new-player defaults, including identity, progression, skills, abilities, cooldowns, forms, modifiers, and summoned spirit wolves.

Example: `/werewolf reset Steve`

## Levels and points

### `/werewolf setlevel <target> <level>`

Sets the werewolf level and resets experience within that level to zero. The syntax accepts levels 1–25, but the value cannot exceed the server's configured maximum. The command grants the werewolf bloodline and clears infection.

Example: `/werewolf setlevel Steve 20`

### `/werewolf addxp <target> <amount>`

Adds 1–100000 raw werewolf experience and can trigger normal level-ups. The administrator value is not multiplied by the normal experience-gain configuration.

Example: `/werewolf addxp Steve 500`

### `/werewolf setskillpoints <target> <amount>`

Sets currently available ability points to a value from 0–100. This sets the displayed available total; it does not add a delta.

Example: `/werewolf setskillpoints Steve 20`

### `/werewolf settreepoints <target> <amount>`

Sets currently available skill-tree points to a value from 0–100. This sets the displayed available total; it does not add a delta.

Example: `/werewolf settreepoints Steve 30`

## Skill-tree commands

### `/werewolf settree <target> <skill> <rank>`

Sets one skill-tree rank without spending points. Rank `0` removes the skill. The command validates the selected skill's actual maximum and rejects an excessive value.

| Skill ID | Maximum rank | Skill ID | Maximum rank |
|---|---:|---|---:|
| `damage` | 6 | `moon_rend` | 3 |
| `defense` | 5 | `speed` | 5 |
| `resistance` | 5 | `regeneration` | 5 |
| `jump` | 3 | `knockback_resistance` | 2 |
| `fall_resistance` | 2 | `lifesteal` | 3 |
| `satiety` | 3 | `claw_efficiency` | 3 |
| `hunting_mastery` | 2 |  |  |

Example: `/werewolf settree Steve damage 6`

### `/werewolf resettree <target>`

Clears all skill-tree ranks without resetting werewolf level or unlocked abilities.

Example: `/werewolf resettree Steve`

## Ability commands

### `/werewolf setability <target> <ability> <unlocked>`

Unlocks or removes one ability without spending points. Valid ability IDs are:

- `night_vision`
- `summon_wolf_spirit`
- `quadruped_form`
- `moonblood_surge`
- `armored_instinct`
- `hard_life`
- `empty_claw_slot`
- `long_claws`
- `tool_claws`
- `fire_claws`
- `bloody_bite`

Removing `quadruped_form` while it is active returns the target to normal Werewolf form and refreshes player dimensions.

Example: `/werewolf setability Steve quadruped_form true`

### `/werewolf resetabilities <target>`

Removes every unlocked ability without resetting level or the skill tree. An active Quadruped form is exited automatically.

Example: `/werewolf resetabilities Steve`

## Status diagnostics

### `/werewolf status [target]`

Displays werewolf identity, infection, current form, Alpha completion, level, experience, available points, dimension, and persistent-mirror state.

A player can omit the target with `/werewolf status`. The server console and command blocks must provide an explicit target.

The `persistedMirror` field means:

- `match`: live server data and the persistent recovery mirror agree.
- `DIFF`: the two snapshots differ; inspect saving, dimension changes, or synchronization.
- `missing`: no persistent recovery mirror was found yet.

## Common test flow

```mcfunction
/werewolf awaken Steve
/werewolf setlevel Steve 20
/werewolf settreepoints Steve 50
/werewolf setskillpoints Steve 30
/werewolf settree Steve damage 6
/werewolf setability Steve quadruped_form true
/werewolf status Steve
/werewolf reset Steve
```
