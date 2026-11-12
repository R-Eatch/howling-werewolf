# Companion guide

For Minecraft 1.21.11, Fabric Loader 0.20.1, Fabric API 0.141.6 and Java 21, Howling Werewolf 1.3.7. Client and server need the same version. [简体中文](COMPANIONS-zh.md)

Related: [hunt and trade tables](COMPANION-TABLES.md) · [architecture provenance](COMPANION-PROVENANCE.md)

Press **C** in game to open the companion window; its Guide tab covers the same ground in short form.

## One-minute start

1. Learn **Pack Oath** (one skill-tree point per rank).
2. Build five altars: one in the center and one three blocks away to the north, south, east and west, all at one height. Put a Wolfsbane on each outer altar and a Moonbane Pearl in the center, then right-click the center with an empty hand.
3. A Werewolf Brother arrives and stays ten minutes. Right-click to trade. Hold an Alpha Werewolf Badge and right-click to recruit, then confirm.
4. Press **C** to give orders. Right-click a companion to open the window with it selected, or sneak-right-click to open its nine-slot inventory.

## Summoning the merchant

The merchant and the Alpha Trial share one altar layout, and the offerings decide who answers:

| Rite | Center | Outer altars |
|---|---|---|
| Summon a Werewolf Brother | Moonbane Pearl | Wolfsbane |
| Alpha Trial | Alpha Werewolf Badge | Moonbane Pearl |

Right-click the center altar with an empty hand to begin. Before it starts, sneak-right-click takes the offerings back. The rite lasts four seconds: the flowers' shadows drift to the center, the pearl rises and a ring of moonlight closes in.

The Brother runs in from safe ground at least 16 blocks away. If the way is blocked he comes straight to the altar, and he also returns there after 15 seconds of being stuck. If the altars are broken, you leave the dimension or there is no room, the rite stops and returns its offerings.

The rite works at any hour and moon phase. The merchant stays 12000 game ticks (ten minutes at 20 TPS). The timer keeps running while you trade, and he also leaves if his chunk unloads.

## Trading and recruiting

Each merchant draws six of nine trades, each with limited uses. See the [trade table](COMPANION-TABLES.md#merchant-trades).

Hold an Alpha Werewolf Badge and right-click the merchant, then confirm to spend one badge and recruit him. The prompt lasts ten seconds. On confirming, the server checks distance, health, skill, party size and the badge again, and takes the badge once everything passes. He then stays with you for good, named after his coat by default.

## Pack Oath and growth

| Skill rank | Party limit before Alpha | Party limit after Alpha | Companion tier limit |
|---|---:|---:|---:|
| 1 | 1 | 1 | 1 |
| 2 | 1 | 2 | 2 |
| 3 | 1 | 3 | 3 |

Companions start at tier 0. Tiers 1, 2 and 3 cost 2, 4 and 8 Moonbane Pearls, 14 in all.

| Tier | Max health | Melee damage | Natural reduction | Regen per second | Rolls on a full hunt |
|---|---:|---:|---:|---:|---:|
| 0 | 30 | 6 | 0% | 1 | 6–12 |
| 1 | 40 | 8 | 10% | 1.5 | 7–15 |
| 2 | 50 | 10 | 20% | 2 | 9–18 |
| 3 | 60 | 12 | 30% | 2.5 | 12–24 |

Regeneration starts five seconds after the last hit. An upgrade raises the health cap and leaves current health as it is.

Companions out hunting or fallen still count toward the party limit. Respeccing or curing keeps every record and item: companions over the limit rest, and their active tier follows the skill rank until you relearn it, when purchased tiers return.

## Orders

| Order | Behavior |
|---|---|
| Follow | Stays close and keeps moving. Monsters ignore it, and switching to Follow clears existing aggro. Area, environmental and player damage still apply, so use Guard in dangerous places. It returns to you when too far away or across dimensions. |
| Guard | Follows and fights: it joins whatever you attack or whatever attacks you, and strikes back when attacked itself. It spares creepers, you, your companions and pets, teammates and PvP-protected players. |
| Rest | Waits on the spot and may look at nearby players. Like a follower, monsters ignore it and switching to Rest clears existing aggro; it does not strike back. |
| Sort | See the next section. Monsters ignore a sorting companion too, and starting a job clears existing aggro. |
| Hunt | Leaves the world while its record and timer carry on. You can recall it at any time. |
| Recall | Works from any dimension and any chunk. It returns to safe ground beside you, and recalling a hunter settles the hunt early. A fallen companion needs reviving first. |
| Upgrade | Spends Moonbane Pearls, within the skill's tier limit. |
| Revive | Spends one Alpha Werewolf Badge and returns it at full health beside you, with tier, name, coat and inventory intact. |
| Ignore My Hits | A switch below the orders, off by default and kept per companion. While it is on, nothing you cause can hurt this companion: melee, projectiles, potions and your Beast void claws all miss, with no hurt sound or knockback. Other players, monsters and `/kill` still can. |
| Dismiss | Below the orders; opens a separate confirmation page. Once confirmed, the companion leaves the pack and its record, tier, name and coat are gone, with nothing spent on it refunded. Whatever it carries is handed back to you, and anything that does not fit drops at your feet. Its slot is free again. Fallen and hunting companions can be dismissed too; a hunt in progress is not settled. |

When there is no safe ground nearby, recall, revival and returning hunters wait for space. Badges and rewards are settled when it succeeds.

## Sorting

Bring your companion to the middle of your storage, open the Sorting tab and choose **Start sorting**.

The work area is a sphere with a radius of 16 blocks around where the companion stood when it started, and it stays put as the companion moves. That reaches 16 blocks straight up or down and less toward the corners. **Chests, copper chests and barrels** inside it join the job, up to the nearest 64, with a double chest counting once.

Your companion works the way you would: it walks over, opens the lid with the real animation and sound, takes the items, closes up, then walks to the destination, opens it and sets them down. After a full round it lets you know and stays on site, tidying anything added later. Any other order ends the job.

### Two methods

| Method | Effect |
|---|---|
| Consolidate (default) | Gathers each item into the container holding the most of it. Whatever does not fit stays in its other containers. |
| Merge stacks | Tops up existing partial stacks and leaves empty slots as they are. |

Items count as the same when the item and all its data match, so differing names, durability, enchantments and potion contents stay apart. Unstackable gear stays where it is unless a frame calls for it.

### Item frames: choosing what a chest keeps

Hang a filled item frame, plain or glowing, on a container and that container keeps only the framed item:

- Matching items come in from other containers in the area, under either method.
- Anything else inside moves out to plain containers. If there is no room yet it waits, and moves once space opens up.
- Frames match by item type: frame an iron sword and every iron sword belongs here, whatever its name or enchantments.
- One container can wear several frames, and frames on both halves of a double chest add up. An empty frame is only decoration.

When several containers frame the same item, the one holding the most fills first. Framed containers show in green on the radar, and hovering shows what each one keeps.

### Chests kept for yourself

Your companion passes over **trapped chests and shulker boxes**, so keep anything you want left as it is in those.

### Your items are safe

The stack in the companion's hand is only for show. Items change hands the moment they are set down, checked and committed within one server tick, with any failed half rolled back, so if you stop the job, the companion falls or the server restarts, everything is still in its original container.

### What gets skipped

Gray dots on the radar are containers skipped for now, and hovering shows their position and the reason:

- open by another player;
- locked, obstructed or interaction-protected;
- loot chests yet to be opened;
- impossible to walk up to and beyond the sorting reach (32 blocks by default; tried again after a minute, and your companion mentions it once).

A container your companion cannot walk up to is still tidied from where it stands, as long as it lies within the sorting reach.

Protection checks cover vanilla spawn and access rules plus Fabric API's block-use callback. Whether a claim plugin uses that event depends on the plugin, so try it on yours.

### Pausing and multiplayer

Work pauses while you are offline or in another dimension and resumes when you return. It also pauses if the companion is taken far outside the area. Only the owner can start or adjust a job, and its messages and dialogue go only to the owner.

## Inventory

Each companion has nine slots, reached from its window or directly with sneak-right-click. It must be alive, loaded, in your dimension and within eight blocks. The slots hold what you put in and what it brings back from hunts.

## Hunting

A hunt lasts a random 1.5–3 Minecraft days (36000–72000 game ticks, about 30–60 minutes at normal speed). A bar on the Pack tab and the ring around the moon show how far along it is. Time is measured in real elapsed server ticks, so sleeping and `/time set` make no difference. A hunt can end while its owner is offline, and rewards and the return wait until they come back.

Rewards are drawn when it ends: `floor(elapsed ticks × tier factor / 24000)` rolls, with tier factors 4, 5, 6 and 8. An early recall uses the same formula. Rewards fill the nine slots first and the rest drop where the companion returns.

Each companion then rests for two Minecraft days (48000 game ticks), early recalls included, and that rest keeps counting through ordinary recalls, falling and revival. See the [hunt table](COMPANION-TABLES.md) for odds.

## Looks and address

- **Name**: up to 32 characters; leave it blank for the coat's name.
- **Coat**: one click previews it on the Appearance tab, then apply. Classic has four, Modern has nine, and each model family remembers its own choice. The world setting `gameplay.npcModelSet` decides which model companions use.
- **Address**: set how your companions address you on the Pack tab, up to 16 characters, for all of yours. Default restores “my Alpha” (“主人” in Chinese).

## Window and keys

**C** opens the companion window (rebind it in Controls), and **Pack** at the top of the **K** progression screen leads there too. It is a panel floating over the game, quick to open, and shares its Moonlight or Nightfall canvas with the progression screen. Four tabs keep your selected companion as you move between them:

- **Pack**: roster on the left, the companion before the moon in the middle, orders on the right. Number keys 1–5 give orders and ↑↓ switches companions. The dark band below explains whatever the pointer rests on, including why an order is unavailable.
- **Sorting**: the moon becomes a live map, beside progress and the method choice.
- **Appearance**: coat previews and name.
- **Guide**: the in-game guide, by chapter.

## Server settings

`config/howlingwerewolf-common.toml`:

```toml
[gameplay]
enableCompanions = true
enableCompanionSorting = true
companionSortingReach = 32
npcModelSet = "modern"
```

- `enableCompanions = false` stops rites, trading, recruiting, inventories and all companion actions. Entities are put away, records and items are kept, and hunt and rest timers pause. The window still opens and explains why. Pack Oath takes new points again once this is back on, and learned ranks are kept. Other werewolf features carry on.
- `enableCompanionSorting = false` turns off sorting alone.
- `companionSortingReach` is how far a companion can still reach into a container it cannot walk up to: 3–64 blocks, 32 by default. At 3, anything it cannot reach on foot is skipped.
- Data packs can override the hunt table at `data/howlingwerewolf/loot_table/gameplay/companion_hunt.json`. The merchant's nine trades are defined in [code](../src/main/java/com/howlingwerewolf/entity/WerewolfCompanionEntity.java).

## Combat detail

The splash damage of a player's Long Claws passes over every recruited companion. A companion hit directly takes that hit under the usual rules, unless its Ignore My Hits switch is on.

When you cast Moonblood Surge, your recruited companions within 16 blocks in the same dimension share its effects and the crash that follows. Your Resistance rank clears the matching penalties for them too. Each affected companion keeps its remaining time when recalled or loaded again. Temporary hearts already spent stay spent.

## Data and multiplayer

Each companion has its own UUID bound to its owner's. Records live in the save at `data/howlingwerewolf/companions.dat`, so they survive player death and entity replacement. An entity in the world is a projection of its record, and the inventory lives in the record; a recall confirms the new entity joined the world before switching over.

Clients send an action, a companion UUID and length-limited arguments. The server checks ownership and works out costs, times and rewards. Management data and dialogue go to the owner alone, while looks and names reach nearby players through entity tracking.

The architecture draws on Vampirism's separation of records and entities, with independently written code. See the [provenance review](COMPANION-PROVENANCE.md).
