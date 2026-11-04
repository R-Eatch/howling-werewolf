# Companions and sorting: 1.2.6

Minecraft 26.1.2, NeoForge 26.1.2.122 and Java 25. Install 1.2.6 on both client and server, using network protocol 16. [简体中文](COMPANIONS-1.2.6-zh.md).

This version adds **Sort**, changes Modern UV submission, and adjusts the Modern Beast quadrupedal sprint. Existing rituals, trading, recruitment, inventories, hunting, upgrades and revival retain their rules. See the [base rules](COMPANIONS-1.2.4-zh.md) and [1.2.5 controls](COMPANIONS-1.2.5-zh.md); current controls and sorting behavior below take precedence over historical guide text.

## Using Sort

1. Open companion management with its configured key (default **C**) or right-click your companion. Select **Sort**, stand near the companion, and select **Register storage**.
2. The initial work point uses the companion's position. Sneak-right-click a vanilla chest, trapped chest or barrel to add it to the allowed list; repeat to remove it.
3. Open companion management to finish registration, which also expires after five minutes. Check the list and sorting method, then select **Start sorting**.
4. The companion stays at work and periodically checks the registered containers. Select **Stop sorting**, change mode, or recall it to end the work.

Sort uses an existing recruited companion and its normal availability. There is no additional skill-tree point, item cost or reward multiplier for this command. Existing saved key bindings are preserved; change them to C or reset them in Controls if needed.

## Work area and methods

The work point reaches eight blocks in each horizontal direction and four vertically. Register up to 24 logical containers per companion. A double chest counts as one; both halves must be accessible and inside the area. Nearby unregistered storage is never selected automatically.

| Method | Behavior |
|---|---|
| Consolidate, the default | Favors registered storage already holding the most of each matching resource and uses its available empty slots. Items that do not fit remain in other containers. |
| Merge stacks only | Fills existing partial stacks without occupying empty slots or rearranging the whole container. |

For example, two stacks of 32 iron ingots can become one stack of 64; two stacks of 48 cobblestone become 64 and 32. Ender pearls retain their limit of 16. Unstackable equipment remains in place. Sorting converges over repeated passes: space released by one resource may be used by another on the next pass. A single scan does not promise an optimal final layout.

Matching requires both the item and every data component to agree. Different names, damage, enchantments, potion contents, books and container contents remain distinct. The companion's nine-slot inventory is never transit storage, and excess items are never dropped on the ground.

**Reset work area** stops sorting, clears registrations and moves the work point to the companion without changing stored items. Use the mouse wheel over the list to view more than eight entries. Changing settings requires being near the companion in the same dimension; stopping remains available through the overview.

## Multiplayer and access

Registrations belong to the companion's owner UUID; only that owner can configure or start its work. Command feedback and dialogue are private to that owner. Explicit registration does not bypass a container's access checks.

- Open, obstructed, locked, interaction-protected, unlooted and unloaded storage is skipped. Both halves of a double chest must pass.
- Before every transfer, the source and destination are checked again. Player interaction, hoppers, replacement blocks and unloads invalidate stale work rather than overwriting an inventory snapshot.
- Work pauses when the owner logs out, leaves the dimension or the companion leaves its work area. It replans from actual contents when work can resume. Death, hunting, recall and mode changes do not continue stale plans.
- No chunks are force-loaded and no other dimension is read. This release supports vanilla chests, trapped chests and barrels; it does not sort furnaces, hoppers, ender chests or other mods' storage.

Access checks consult vanilla permissions/spawn protection and cancellation or refusal through NeoForge's right-click-block event. Claim mods have their own permission models; integrations that do not enforce this event require separate verification.

## Server configuration

In `config/howlingwerewolf-common.toml`:

```toml
[gameplay]
enableCompanions = true
enableCompanionSorting = true
```

`enableCompanionSorting` defaults to `true`. Disabling it stops sorting and its settings while retaining registrations, contents and other companion features. The main `enableCompanions` switch continues to govern the whole companion system and pause its hunt/cooldown clocks. Multiplayer follows the server configuration.

## Algorithm and references

The implementation reads slots, groups exact resources, chooses stable destinations and executes bounded transfers. NeoForge `ItemResource` identifies the item and full components without its count. Hashes accelerate grouping; full equality confirms a match. Fixed position/slot tie-breaks prevent items from bouncing between containers. Each companion executes at most four transfers per tick, under server-wide limits of sixteen transfers and two full scan/planning passes per tick; idle workers periodically rescan.

Each source extraction and destination insertion shares a NeoForge transaction. A quantity mismatch aborts that transfer; successful earlier transfers remain in place and invalid work is replanned. Runtime transactions are not a crash-recovery guarantee for power loss, disk corruption or arbitrary external mod failures. See the [NeoForge transaction documentation](https://docs.neoforged.net/docs/inventories/transactions/).

Research consulted [Inventory Sorter's grouping approach](https://github.com/cpw/inventorysorter/blob/main/src/main/java/cpw/mods/inventorysorter/SortingHandler.java) and [item/component equality](https://github.com/cpw/inventorysorter/blob/main/src/main/java/cpw/mods/inventorysorter/ItemStackHolder.java). The owner allowlist, server planner and transactional execution here are independently implemented; no GPL code from that project is bundled or copied into this mod. The [earlier sorting draft](DRAFT-COMPANION-CHEST-SORTING.md) is historical; this guide describes the implementation.

## Modern rendering and Beast sprint

Modern geometry can have different physical and UV dimensions. Previously, imported cubes changed UVs after vanilla construction, while renderer optimizations could retain the original constructor data. The imported geometry now emits its final polygon vertices, UVs, color, lighting and normals directly, covering all three Modern forms, segmented armor and the rendering paths using them.

The Modern Beast sprint uses paired forelimbs first, followed by the hind pair, with paw lift during recovery, a grounded stance and slight body motion. Standing, crouching, swimming and item-use handling remain separate.

The target compatibility build is [Sodium 0.9.2 for NeoForge 26.1.2](https://modrinth.com/mod/sodium/version/mc26.1.2-0.9.2-neoforge). The [official Xenon file list](https://www.curseforge.com/minecraft/mc-mods/xenon/files/all) currently lists 1.20.1; its behavior cannot establish 26.1.2 compatibility.

## Validation scope

The build and 1323 artifact checks passed. Native NeoForge server checks passed 455 assertions, including a real save/restart, double chests, revoked access, transaction rollback, 120 randomized conservation/convergence cases and work budgets. The model/NPC/UI regression passed 21129 assertions, including the 368 independent UI checks. In the specified game instance on NeoForge 26.1.2.122 and an RTX 4060 Laptop GPU, 16 GPU scenes were captured with ordinary rendering and with Sodium 0.9.2. Static previews of all three forms, nine coats and diamond armor were pixel-identical. Eight stride scenes at four angles differed by at most 47 foreground pixels (under 0.08%), with no UV corruption. The original 1.2.5 corruption was reproduced separately. All 74 client classes in the final JAR are byte-identical to the GPU candidate. This does not certify other optimization mods, shader packs, every claim combination or two real network clients.

The [modern bilingual dialogue proposal](DRAFT-COMPANION-DIALOGUE-MODERN-zh-en.md) remains a review draft. This version does not replace all existing dialogue; the new sorting responses use the current character style.
