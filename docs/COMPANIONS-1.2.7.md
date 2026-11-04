# Companions and the Beast: 1.2.7

Minecraft 26.1.2, NeoForge 26.1.2.122 and Java 25. Install 1.2.7 on both client and server, using network protocol 17. [简体中文](COMPANIONS-1.2.7-zh.md).

In this version your companion really does the work: it walks to a chest, opens the lid, picks up the items and carries them to their new home. The companion screens are rebuilt, the in-game guide is rewritten, and the Modern Beast gets a new gait based on wolves and big cats. Rituals, merchants, recruiting, inventories, hunting, upgrades and revival keep their rules; see the in-game guide and the [1.2.4 guide](COMPANIONS-1.2.4-zh.md).

## Sorting

### How to use it

1. Bring your companion to the middle of your storage, press C and open the **Sorting** tab.
2. Choose **Start sorting**.

That is all. There is nothing to register. The work area is a **sphere with a radius of 16 blocks around where the companion stood when it started**, and it stays put as the companion moves around. Vanilla chests, trapped chests, copper chests and barrels inside it join the job, up to the nearest 64, with a double chest counting once.

The companion then works like this:

1. It walks to a container and opens the lid, with the real animation and sound. Trapped chests signal as usual.
2. It takes the items in hand and closes the lid.
3. It walks to the destination, opens it, sets the items down and closes up.

After a full round it lets you know and stays on site, tidying anything added later. Give any other order to end the job.

### Two methods

| Method | Effect |
|---|---|
| Consolidate (default) | Gathers each item into the container that already holds the most of it. Anything that does not fit stays in its other containers. |
| Merge stacks | Only tops up existing partial stacks, leaving empty slots as they are. |

Items count as the same only when the item and all its data components match, so differing names, durability, enchantments and potion contents stay apart. Unstackable gear stays where it is.

### Your items are safe

The stack in the companion's hand is only for show. Items change hands at the moment they are set down, in a single NeoForge transaction. Stopping the job, the companion falling, or a server restart all leave items in their original container. Anything that does not fit stays where it is, and sorting never drops items on the ground.

### What gets skipped

Gray dots on the radar are containers skipped for now, and hovering shows their coordinates:

- containers another player has open;
- locked, obstructed or interaction-protected containers;
- loot chests that have never been opened;
- containers the companion cannot reach (tried again after a minute).

Everything else is sorted as usual. Protection checks cover vanilla spawn and access rules, plus refusals from NeoForge's right-click interaction event. Whether a claim plugin uses that event depends on the plugin and needs checking with it.

### Pausing and multiplayer

Work pauses while the owner is offline or in another dimension, and resumes on return. It also pauses if the companion is taken far outside the work area. Sorting keeps chunks unforced and stays within one dimension. Only the owner can start or adjust a job, and its messages and dialogue go only to the owner.

### Server setting

```toml
[gameplay]
enableCompanionSorting = true
```

Set it to `false` to turn off sorting alone; other companion features carry on. Saves from 1.2.6 load directly, and their old registered container lists are simply no longer used.

## New companion screens

One window with four tabs, keeping your selected companion as you move between them:

- **Pack**: the roster on the left, a live portrait in the middle, status and orders on the right. Hover an order for its description. Unavailable buttons tell you why, such as "0.75 MC days to go".
- **Sorting**: a live radar of every container in the work area and the companion's current target, beside a progress bar and the method choice.
- **Appearance**: preview every coat with one click, then apply the one you like.
- **Guide**: chapter navigation with a scrollable reading page and previous and next buttons.

Tip: number keys 1–5 give orders directly, and ↑↓ switches companions.

## Modern Beast running and crouching

The sprint is now a rotary gallop based on wolves and big cats: the hind legs drive off one after the other, the body stretches into flight, the forelegs land in turn to take the weight, and all four limbs gather under a flexed spine. The body rocks fore and aft, the eyes stay level, and the tail streams behind. Leaping stretches the limbs out front and back.

Crouching puts the Beast down on all fours in a low carriage, and when it moves its legs lift one at a time in a stalking sequence. Using items such as a bow or shield keeps the upright pose so aiming stays true. The first-person crouch camera drops to match; the hitbox is unchanged. Classic models keep their existing motion.

## Companion swings

Companions now swing their arms when they attack. Vanilla only advances the swing timer for monsters and players, and companions descend from villagers, so their timer never ran.

## Admin command

`/werewolf unlockalpha <target> [state]` unlocks Alpha in one command. With `state` omitted or `true`, the target becomes a werewolf with Beast form and the Alpha companion slots permanently unlocked; `false` takes it away. It gives no badge and plays no trial rewards. See the [admin command guide](../ADMIN_COMMANDS.md).

## Validation

The build passes, as do 2243 artifact checks, including every line of dialogue matching 1.2.6 byte for byte. 478 native NeoForge server checks pass, first upgrading from the real save 1.2.6 left behind, then through a real save and restart. 20842 model, NPC and UI checks pass. On NeoForge 26.1.2.122 with an RTX 4060 Laptop GPU, a real client in an isolated game directory ran the full script in Chinese at GUI scales 2 and 4 and in English at scale 3: the companion walked, opened containers and carried 99 items, item totals were conserved, containers out of range or out of reach stayed untouched, reachable lids really opened, and the attack swing advanced. Beast sprinting and prowling were checked in the world and in rendered frames. Feel and long play sessions await the author's in-game review; this version was not checked with Sodium, shader packs, claim plugins or two real clients.

A revised companion dialogue draft is in [DRAFT-COMPANION-DIALOGUE-1.2.7-zh-en.md](DRAFT-COMPANION-DIALOGUE-1.2.7-zh-en.md); it is not in the game yet.
