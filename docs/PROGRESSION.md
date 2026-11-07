# Levels, skill tree and skills: the numbers

[简体中文](PROGRESSION-zh.md) · [Back to README](../README.md)

These values apply to all four editions of 1.3.4. The default maximum level is 20 and can be raised to 25 in the [configuration](CONFIG.md); levels 21–25 exist only after raising it.

## Experience sources

| Source | Experience |
|---|---|
| Killing a mob while transformed | Target's maximum health × 50%, rounded up, at least 1; 60% with Hunting Mastery rank 1 and 75% with rank 2. Players and armor stands give none. |
| Alpha Werewolf Badge in the hotbar | 10 per minute, also in human form. |

All of it is multiplied by the `werewolfExperienceGainMultiplier` setting (1.0 by default). Administrator XP commands ignore the multiplier. No experience builds up once you reach the level cap.

## Levels

- Going from level L to L+1 takes `25 × (2L + 1)` experience.
- Base claw damage is `4 + ⌊L ÷ 5⌋`, and applies only while transformed and empty-handed.
- Each level gives 2% natural damage reduction.
- Level 1 gives 2 Skill Points and 5 Skill-tree Points; each further level adds 1 Skill Point and 2 Skill-tree Points.

| Level | Total XP | To next level | Base claw damage | Level reduction | Skill Points (total) | Skill-tree Points (total) |
|---|---:|---:|---:|---:|---:|---:|
| 1 | 0 | 75 | 4 | 2% | 2 | 5 |
| 2 | 75 | 125 | 4 | 4% | 3 | 7 |
| 3 | 200 | 175 | 4 | 6% | 4 | 9 |
| 4 | 375 | 225 | 4 | 8% | 5 | 11 |
| 5 | 600 | 275 | 5 | 10% | 6 | 13 |
| 6 | 875 | 325 | 5 | 12% | 7 | 15 |
| 7 | 1200 | 375 | 5 | 14% | 8 | 17 |
| 8 | 1575 | 425 | 5 | 16% | 9 | 19 |
| 9 | 2000 | 475 | 5 | 18% | 10 | 21 |
| 10 | 2475 | 525 | 6 | 20% | 11 | 23 |
| 11 | 3000 | 575 | 6 | 22% | 12 | 25 |
| 12 | 3575 | 625 | 6 | 24% | 13 | 27 |
| 13 | 4200 | 675 | 6 | 26% | 14 | 29 |
| 14 | 4875 | 725 | 6 | 28% | 15 | 31 |
| 15 | 5600 | 775 | 7 | 30% | 16 | 33 |
| 16 | 6375 | 825 | 7 | 32% | 17 | 35 |
| 17 | 7200 | 875 | 7 | 34% | 18 | 37 |
| 18 | 8075 | 925 | 7 | 36% | 19 | 39 |
| 19 | 9000 | 975 | 7 | 38% | 20 | 41 |
| 20 | 9975 | 1025 | 8 | 40% | 21 | 43 |
| 21 | 11000 | 1075 | 8 | 42% | 22 | 45 |
| 22 | 12075 | 1125 | 8 | 44% | 23 | 47 |
| 23 | 13200 | 1175 | 8 | 46% | 24 | 49 |
| 24 | 14375 | 1225 | 8 | 48% | 25 | 51 |
| 25 | 15600 | 1275 | 9 | 50% | 26 | 53 |

The "To next level" column no longer applies once you are at the current level cap. Each repeat Alpha Trial victory on Hard gives 1 extra Skill Point and 1 extra Skill-tree Point.

## Skill tree

The skill tree has 14 entries and takes 50 points to max out; level 20 gives 43 by default. Unless noted otherwise, the effects apply only while transformed.

| Skill | Max rank | Per rank |
|---|---:|---|
| Claw Damage | 6 | Claw damage +30% (multiplies base claw damage; 2.8× at max rank). |
| Moonrend Claws | 3 | Claw attacks deal extra damage equal to 0.6% of the target's maximum health (1.8% at max rank). |
| Natural Defense | 5 | Natural damage reduction +8%, added to the level reduction. |
| Speed | 5 | Movement speed +0.02 (a player's base speed is 0.1). |
| Resistance | 5 | Ranks 1–3: clear Weakness, Poison and Slowness; rank 4: also Mining Fatigue and Blindness; rank 5: also Wither and Darkness. |
| Regeneration | 5 | Heals 0.5 health every 2.5 seconds. |
| Enhanced Jump | 3 | Jump height +1.2 blocks (about 1.25 blocks base). |
| Knockback Resistance | 2 | Knockback resistance +50%; max rank removes knockback entirely. |
| Landing Control | 2 | Rank 1 halves fall damage; rank 2 removes it. |
| Lifesteal | 3 | Heals 10% / 15% / 20% of final melee damage, up to maximum health. Long Claws splash and Bloody Bite do not trigger it. |
| Satiety | 3 | Lowers transformed hunger use from 1.5× to 1.33× / 1.16× / 1.0×. |
| Claw Efficiency | 3 | Empty-hand block and entity reach +0.8 blocks, and claw kills gain 1 level of Looting. |
| Hunting Mastery | 2 | Kill experience rises from 50% of the target's maximum health to 60% / 75%. |
| Pack Oath | 3 | Unlocks recruitment; a companion's highest tier equals your Pack Oath rank. Before the Alpha Trial you can keep 1 companion; afterwards you get as many as your rank (up to 3). Companion stats are in the [companion guide](COMPANIONS.md). |

**Natural damage reduction**: `level × 2% + Natural Defense rank × 8%`, capped at 90% for the normal Werewolf and Quadruped Wolf forms. Under Armored Instinct, each piece of equipment with an armor value lowers this reduction by 25%.

**Claw damage**: `(4 + ⌊level ÷ 5⌋) × (1 + 0.3 × Claw Damage rank)`, then multiplied by the form multiplier below, plus the Moonrend Claws extra damage.

**Extra damage taken while transformed**: fire damage ×1.5; hits from an enemy holding a silver weapon ×1.5, plus Weakness II for 5 seconds.

## Skills

Skills are unlocked with Skill Points, once each; unlocking all of them takes 25 points. Beast Form costs no points and is unlocked by the first Alpha Trial victory.

| Skill | Cost | Key | Numbers |
|---|---:|---|---|
| Night Vision | 1 | — | Night Vision while transformed. |
| Summon Wolf Spirits | 3 | N | Summons 5 wolf spirits that follow and assist you for 4 minutes; 60-second cooldown. |
| Wildstride Form | 4 | G | Switches between normal Werewolf and Quadruped Wolf; see the form table below. |
| Moonblood Surge | 3 | R | Regeneration III for 45 s, Absorption IV for 60 s, decaying Resistance (III for 10 s → II for 15 s → I for 35 s), and Strength II / Speed II / Jump Boost I / Fire Resistance for 90 s. Afterwards: Weakness II, Mining Fatigue III and Slowness I for 60 s. 4-minute cooldown. |
| Armored Instinct | 2 | — | Lets you wear armor and elytra in normal Werewolf form only. Each piece with an armor value: natural reduction −25%, movement and jump bonuses −17.5%; zero-armor pieces such as elytra do not count. |
| Hard Life | 2 | — | Lets you eat non-meat food while transformed. |
| Empty Claw | 1 | — | Keeps one hotbar slot empty. |
| Long Claws | 2 | — | Empty-hand attacks deal half damage to nearby enemies. Can be switched on the Abilities page. |
| Tool Claws | 2 | — | While transformed and empty-handed, your claws work like iron pickaxes, axes and shovels and can harvest iron-tier blocks. Can be switched on the Abilities page. |
| Fire Claws | 2 | — | Transformed empty-hand attacks work like Fire Aspect II (8 seconds of fire). Can be switched on the Abilities page. |
| Bloody Bite | 3 | B | Bites the aimed target for 1.8× your current claw damage and applies Weakness II, Slowness III, Hunger I and Poison I for 15 seconds each; 60-second cooldown. |

## Form multipliers

| | Werewolf | Quadruped Wolf | Beast |
|---|---:|---:|---:|
| Attack damage | ×1.0 | ×0.75 | ×2.0 |
| Natural damage reduction | ×1.0 | ×1.0 | ×1.25 |
| Movement speed | ×1.0 | ×1.75 | ×1.5 |
| Jump height | ×1.0 | ×1.5 | ×1.5 |
| Maximum health | +0 | +0 | +20 |
| Auto step | 1.5 blocks | 1.0 block | 2.0 blocks |
| Equipment | With Armored Instinct | None | None |

## Reset

- Requires level 6 or higher and costs 5 levels.
- Clears every skill-tree rank and skill, and points are handed out again for the new level.
- Extra points from Hard Alpha Trial victories are cleared too.
- Companions and their belongings are kept.
