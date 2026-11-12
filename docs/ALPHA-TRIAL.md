# Alpha Trial: the numbers

[简体中文](ALPHA-TRIAL-zh.md) · [Back to README](../README.md)

This page lists numbers only and applies to all four editions of 1.3.7. For the ritual itself and tactics, see "The Moon-Oath and Alpha Trial" in the README.

Damage below is the mob's attack attribute. Vanilla also scales mob damage to players by game difficulty: about half plus 1 on Easy, unchanged on Normal and ×1.5 on Hard.

## Starting conditions

| Condition | Value |
|---|---|
| Dimension | Overworld |
| Time | Night, ticks 13000–22999 of the day |
| Werewolf level | 10 or higher |
| Form | Normal Werewolf form (not Quadruped Wolf or Beast) |
| Players | No other player within the 18-block barrier radius |
| Altars | Alpha Werewolf Badge on the central altar; 1 Moonbane Pearl on each of the four altars 3 blocks away to the north, south, east and west; all five at the same height |
| Game difficulty | Not Peaceful |

## Phases

| Phase | Value |
|---|---|
| Ritual start-up | 12 seconds |
| Barrier | Centred on the central altar, 18 blocks horizontal radius. The challenger is pushed back near the edge, other players are pushed out, and trial mobs are pulled back in. |
| Phase 1 | 7 Silver-Oath Watchers appear 8 blocks out. Killing all of them starts phase 2. |
| Phase 2 | The Moon-Crowned Alpha Werewolf |

## Failure

Any of the following ends the trial at once:

- the challenger goes offline, dies or leaves the dimension;
- the challenger is more than 18.5 blocks from the central altar;
- any of the five altars is broken;
- the game difficulty is set to Peaceful.

On failure the Alpha Werewolf Badge on the central altar drops as an item; the four Moonbane Pearls are not returned. Watchers, the Alpha and summoned werewolves are removed.

## Silver-Oath Watchers

| Attribute | Value |
|---|---|
| Count | 7 |
| Health | 30 |
| Armor | 4 |
| Base attack | 3, holding a Silver Sword (iron-sword tier) |
| Movement speed | 0.27 |
| Permanent effects | Strength I, Speed I, Regeneration I, Resistance I, Glowing |
| On hitting a transformed werewolf | Slowness I and Mining Fatigue I for 10 seconds; the Silver Sword also deals ×1.5 damage and applies Weakness II for 5 seconds |
| Drops | 1 Wolfsbane, plus a 50% chance of 1 more; no normal loot, and the Silver Sword never drops |

## Moon-Crowned Alpha Werewolf

| Attribute | Easy | Normal | Hard |
|---|---:|---:|---:|
| Maximum health | 300 | 400 | 400 |
| Attack | 19 | 21 | 25 |
| Revival | None | Back to 20% (80) | Back to 42% (168) |

| Shared attribute | Value |
|---|---|
| Armor | 10 |
| Knockback resistance | 0.70 |
| Movement speed | 0.31 |
| Follow range | 48 blocks |
| Permanent effects | Strength I (II after revival), Speed I |
| Natural healing | 4 health every 2.5 seconds |
| Damage interval | At least 10 ticks between two hits that hurt it (`alphaTrialDamageFrequencyLimitTicks`) |
| Drops | No loot; rewards go straight to the challenger |

### Moves

| Move | Value |
|---|---|
| Melee | Tries a melee attack every 2 seconds, reaching 2 blocks further than a normal mob. |
| Prowl (before revival) | After a melee hit lands, a 25% chance to back off for 6 seconds while still facing you, with Speed III for the first 3 seconds. |
| Blink Step | 1-second warning, then it teleports 2 blocks behind you and hits for attack ×1.25, with Slowness I for 10 seconds. About every 20 seconds, or every 15 seconds after revival. |
| Summon | A 5-second channel during which it neither moves nor attacks, then 4 werewolves appear around it. It summons once when the Alpha phase starts and then every 45 seconds, counted from the start of each channel. Dropping to 50% health and reviving each add one extra summon, which still waits for the 45-second cooldown. |
| Revival (Normal, Hard) | The first fatal hit is cancelled and health returns to the share in the table above. It gains Regeneration II and Resistance I for 60 seconds, Strength rises to II, the Blink Step cooldown drops to 15 seconds, it stops prowling, and it adds one summon. |

### Summoned werewolves

| Attribute | Easy / Normal | Hard |
|---|---:|---:|
| Health | 40 | 40 |
| Attack | 6 | 9 |

- Movement speed 0.30, knockback resistance 0.20.
- Permanent Strength I, Speed I and Glowing; Resistance I for their first 30 seconds.
- They take ×0.9 damage (except damage that bypasses invulnerability).
- Their hits apply Hunger II for 15 seconds and Poison II for 5 seconds.
- They attack any player but never infect; killing them gives no vanilla experience and no loot.
- They are removed when the trial fails; any still standing after the Alpha falls stay in the world.

## Rewards

| Result | Reward |
|---|---|
| First victory (any difficulty) | Beast Form unlocked for good, plus 1 Werewolf Potion, 1 Moonbane Pearl and 1 Alpha Werewolf Badge |
| Repeat victory: Easy / Normal | 1 Moonbane Pearl |
| Repeat victory: Hard | 2 Moonbane Pearls, 1 Skill Point and 1 Skill-tree Point |

With a full inventory, rewards drop at the challenger's feet. Resetting your progression clears the extra points from Hard victories; see [levels and skills](PROGRESSION.md#reset).
