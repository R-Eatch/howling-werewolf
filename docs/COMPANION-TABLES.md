# Companion hunt and trade tables

For Minecraft 1.21.1 / Fabric. The hunt table comes from the JSON built into the mod, and once a data pack overrides it, what the server loaded is what counts; the merchant's trades are defined in code. [Companion guide](COMPANIONS.md) · [简体中文](COMPANION-TABLES-zh.md)

## Hunt weights and counts

**30 entries, total weight 235.** Each roll picks one entry independently; “chance” is per roll, not per hunt. Counts are drawn evenly within the range, and an entry can come up more than once.

| Item | Resource ID | Weight | Chance per roll | Count |
|---|---|---:|---:|---:|
| Coal | `minecraft:coal` | 18 | 7.660% | 1–4 |
| Raw Copper | `minecraft:raw_copper` | 16 | 6.809% | 1–4 |
| Raw Iron | `minecraft:raw_iron` | 14 | 5.957% | 1–3 |
| Raw Gold | `minecraft:raw_gold` | 6 | 2.553% | 1–2 |
| Redstone Dust | `minecraft:redstone` | 9 | 3.830% | 1–4 |
| Lapis Lazuli | `minecraft:lapis_lazuli` | 8 | 3.404% | 1–4 |
| Diamond | `minecraft:diamond` | 1 | 0.426% | 1 |
| Emerald | `minecraft:emerald` | 2 | 0.851% | 1 |
| Rotten Flesh | `minecraft:rotten_flesh` | 14 | 5.957% | 1–4 |
| Bone | `minecraft:bone` | 14 | 5.957% | 1–4 |
| String | `minecraft:string` | 12 | 5.106% | 1–3 |
| Gunpowder | `minecraft:gunpowder` | 7 | 2.979% | 1–2 |
| Spider Eye | `minecraft:spider_eye` | 6 | 2.553% | 1–2 |
| Ender Pearl | `minecraft:ender_pearl` | 2 | 0.851% | 1 |
| Slimeball | `minecraft:slime_ball` | 4 | 1.702% | 1–2 |
| Leather | `minecraft:leather` | 10 | 4.255% | 1–3 |
| Feather | `minecraft:feather` | 10 | 4.255% | 1–4 |
| Raw Beef | `minecraft:beef` | 10 | 4.255% | 1–3 |
| Raw Porkchop | `minecraft:porkchop` | 10 | 4.255% | 1–3 |
| Raw Chicken | `minecraft:chicken` | 10 | 4.255% | 1–3 |
| Raw Mutton | `minecraft:mutton` | 10 | 4.255% | 1–3 |
| White Wool | `minecraft:white_wool` | 7 | 2.979% | 1–2 |
| Raw Rabbit | `minecraft:rabbit` | 4 | 1.702% | 1–2 |
| Rabbit Hide | `minecraft:rabbit_hide` | 4 | 1.702% | 1–2 |
| Rabbit's Foot | `minecraft:rabbit_foot` | 1 | 0.426% | 1 |
| Ink Sac | `minecraft:ink_sac` | 5 | 2.128% | 1–3 |
| Glow Ink Sac | `minecraft:glow_ink_sac` | 2 | 0.851% | 1–2 |
| Raw Cod | `minecraft:cod` | 6 | 2.553% | 1–3 |
| Raw Salmon | `minecraft:salmon` | 5 | 2.128% | 1–3 |
| Wolfsbane | `howlingwerewolf:wolfsbane` | 8 | 3.404% | 1–3 |

The list includes a few common Overworld monster drops. Badges, Moonbane Pearls and Nether or boss rewards come from other sources.

## How tier and time affect a hunt

- A hunt saves a planned length of 36000–72000 game ticks (1.5–3 Minecraft days) when it sets out.
- Rewards are drawn once, when it ends and can return safely. Time counted T = min(elapsed ticks, planned length). Delays from an offline owner or a lack of space add no rolls.
- Rolls = floor(T × tier factor ÷ 24000), using the tier active at settlement: the smaller of the tier bought and the owner's current Pack Oath rank.
- Tier and time change **only the number of rolls**. Weights and counts stay the same, and no looting, luck or gear multiplier applies.

| Active tier | Rolls per MC day | 1.5-day hunt | 2-day hunt | 3-day hunt | Cost of this tier |
|---|---:|---:|---:|---:|---|
| 0 | 4 | 6 | 8 | 12 | Starting tier |
| 1 | 5 | 7 | 10 | 15 | 2 Moonbane Pearls |
| 2 | 6 | 9 | 12 | 18 | 4 Moonbane Pearls |
| 3 | 8 | 12 | 16 | 24 | 8 Moonbane Pearls |

A tier 0 companion on a 2-day hunt rolls 8 times and a tier 3 companion rolls 16. Wolfsbane comes up 8/235 ≈ 3.404% per roll, so at least one in n rolls is 1 − (227/235)^n. An immediate recall rolls nothing.

Rewards merge into existing stacks in the nine slots, then fill empty slots, and anything left drops where the companion returns. Settling clears the hunt at once, so repeated recalls pay out once.

## The two-day cooldown

Each companion keeps its own cooldown of **48000 game ticks**, 40 minutes at 20 TPS.

- Natural end: counted from the planned end. If delivery was delayed by an offline owner or a lack of space, that time still counts.
- Early recall: counted from the recall, even when an immediate recall earned nothing.
- During the cooldown a companion can follow, guard, rest, be recalled, open its inventory, upgrade and change its name or coat; only another hunt waits. Falling and revival leave the cooldown running.
- It uses server `gameTime`, so sleeping and `/time set` cannot skip it, and it stops while the server is off. Disabling companions pauses both hunts and cooldowns until they are enabled again.

## Merchant trades

The default pool holds **13 trades**. All three traders draw at least one offer to buy and one offer to sell Werewolf Pelts. The stock limits below apply to each offer.

| Trader | Offers | Supplies | Pelts | Pearls | Potions |
|---|---:|---:|---:|---:|---:|
| Summoned Werewolf Brother | 8 | 12 | 8 | 4 | 3 |
| Silver-Oath Moonwarden | 5 | 4 | 3 | 2 | 1 |
| Tribe chief | 6 | 5 | 4 | 2 | 1 |

The Moonwarden uses a moderate 2:1 weight preference for silver-ingot and silver-for-pelt offers and can only roll the Wolfsbane Potion (never the Werewolf Potion). The Tribe Chief uses a moderate 2:1 weight preference for Wolfsbane and Moonbane Pearl offers and can only roll the Werewolf Potion (never the Wolfsbane Potion). The summoned Werewolf Brother draws uniformly from all 13 offers. Both settlement leaders can restock when near their work spot during working hours, up to twice per game day. The summoned Werewolf Brother stays ten minutes at 20 TPS and leaves when his chunk unloads. Trading does not pause his timer.

| You give | You get | Category |
|---|---|---|
| 6 Emeralds | 4 Silver Ingots | Supplies |
| 16 Iron Ingots | 2 Silver Ingots | Supplies |
| 4 Wolfsbane | 2 Silver Ingots | Supplies |
| 4 Silver Ingots | 2 Wolfsbane | Supplies |
| 4 Emeralds | 4 Wolfsbane | Supplies |
| 4 Wolfsbane | 1 Werewolf Pelt | Buy pelt |
| 4 Silver Ingots | 1 Werewolf Pelt | Buy pelt |
| 1 Werewolf Pelt | 2 Wolfsbane | Sell pelt |
| 1 Werewolf Pelt | 2 Silver Ingots | Sell pelt |
| 12 Emeralds + 4 Wolfsbane | 1 Moonbane Pearl | Pearl |
| 6 Wolfsbane + 1 Diamond | 1 Moonbane Pearl | Pearl |
| 4 Wolfsbane + 2 Emeralds | 1 Werewolf Potion | Potion |
| 4 Silver Ingots + 2 Emeralds | 1 Wolfsbane Potion | Potion |

## Data pack paths and code

- The hunt table is at `data/howlingwerewolf/loot_table/gameplay/companion_hunt.json`.
- Trade prices and offer selection are defined in [WerewolfTrades.java](../src/main/java/com/howlingwerewolf/entity/WerewolfTrades.java).
- Tier, length and cooldown rules are defined in [CompanionRules.java](../src/main/java/com/howlingwerewolf/companion/CompanionRules.java).
- Hunt settlement, inventory merging and overflow are handled by [CompanionManager.java](../src/main/java/com/howlingwerewolf/companion/CompanionManager.java).
