# Changelog

All notable public NeoForge releases of Howling Werewolf are documented here.

## Howling Werewolf NeoForge 1.0.4-beta

- Ports the complete Forge gameplay and content baseline to Minecraft 1.21.1 and NeoForge 21.1.248.
- Replaces Forge Capability persistence with a serializable NeoForge Data Attachment while retaining legacy player-data recovery.
- Replaces `SimpleChannel` networking with validated `CustomPacketPayload` messages.
- Migrates registries, resources, recipes, loot tables, biome modifiers, tags, persistence, rendering, screens, and entity APIs to their 1.21.1 forms.
- Restores the intended Alpha and Alpha-minion damage reductions through NeoForge's authoritative damage container.
- Synchronizes the Forge 1.0.4 quadruped rendering, Beast Form clearance, command-state fixes, textures, and bilingual gameplay content.
- Produces `howlingwerewolf-1.0.4-neoforge-beta.jar` and targets Java 21.

The beta has passed clean offline builds and focused dedicated-server regression checks. Interactive client, multiplayer, full gameplay, and copied-world upgrade validation remain required before a stable release.
