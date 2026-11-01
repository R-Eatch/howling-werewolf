# Howling Werewolf 1.2.1 — Minecraft 1.21.1 / NeoForge

## 简体中文

- 从 Minecraft 26.1.2 的 1.2.1 同步 Modern 模型及九套配色：Ashen、Black、Chestnut、Pale、Rusty、Snowy、Spotted、Stripped、Woods。保留 Classic 四套皮肤与原有玩法。
- 皮肤页右上角切换 Classic / Modern，三形态预览、应用和绘画模板均跟随所选系列。悬停显示 Classic — By Eatch / Modern — By Leo 及切换提示。
- 三个随包 bbmodel 直接提供几何、骨骼、UV 和 27 张内嵌 PNG。移植新骨骼的行走、攻击、下蹲、游泳、滑翔、乘坐和本版本原生持物动作；Modern 野兽冲刺时四肢着地，尾巴分节摆动。
- Modern 镜头随姿势变化，保留碰撞尺寸。第一人称爪使用新模型，武器保持原生大小并固定在掌心；护甲按关节分段，保留原生染色、附魔光效及纹饰，鞘翅跟随背部。
- 新玩家默认 Modern / Ashen；已有明确选择和缺少模型字段的旧存档保留 Classic。模型及皮肤逐玩家保存、同步，死亡、换维度、治愈和等级重置保留选择。
- Alpha 使用 Modern Beast / Ashen；双足援军使用 Modern Werewolf / Ashen，兼容旧存档四足援军。野生、刷怪蛋及命令生成的狼人从九套配色中随机选择并持久保存。
- `config/howlingwerewolf-common.toml` 的 `gameplay.npcModelSet` 默认 `"modern"`；设为 `"classic"` 时 NPC 使用对应形态的 Classic Adrian。单人由整合服务端、多人由服务器配置决定；配置重载会更新既有 NPC，配色保留，观察者本地设置不覆盖服务器结果。
- 自定义皮肤 JSON 支持 `"model": "classic"` / `"model": "modern"`。缺失资源包时在对应系列内回退，模板导出包含当前系列及选中内置配色。

技术基线保持 Minecraft 1.21.1 / NeoForge 21.1.248 / Java 21。网络协议更新为 12，服务端及客户端均需安装 1.2.1。本地构建、独立数据/模型检查和包内资源核对已完成；**本次移植的实机及双客户端联机测试仍由维护者进行**。原版长矛仅在较新 Minecraft 中存在，本移植使用本版本提供的原生物品及姿势。

## English

- Backport the Modern Werewolf, Quadruped Wolf and Beast models and all nine original palettes from 26.1.2 version 1.2.1. Preserve the four Classic coats and existing gameplay.
- Switch Classic / Modern in Skins, with independent previews, Apply, painting templates and localized Classic — By Eatch / Modern — By Leo hover credits.
- Load all three bundled bbmodel projects, skeletons, UVs and 27 embedded PNGs directly. Retarget native movement, attacks, crouching, riding, swimming, flight and the item poses available in this Minecraft version. Modern Beast sprints on all fours and tails sway in delayed segments.
- Match Modern camera height to the pose without changing collision sizes. Use imported first-person claws, native-sized palm-held items, joint-segmented armor with native dyes/glint/trims, and back-attached elytra.
- Default new players to Modern / Ashen; retain existing choices and interpret legacy nonempty data without a model field as Classic. Persist and synchronize each player's appearance across lifecycle events.
- Give Alpha and its pack Modern Ashen, including legacy quadruped minions. Assign wild/spawn-egg/command werewolves a random persistent coat from all nine palettes.
- Default `gameplay.npcModelSet` to `"modern"`; `"classic"` selects Classic Adrian for every affected NPC form. The server owns tracked appearance, existing NPCs update after config reload, and saved coats survive family switches independently of observer settings.
- Support family-specific custom PNG skins, fallbacks and template exports. Keep this edition's resource-pack format and native loading/networking APIs.

Keep Minecraft 1.21.1, NeoForge 21.1.248 and Java 21. Protocol 12 requires server and clients to update to 1.2.1 together. Local builds, independent model/data checks and artifact inspection are complete; **in-game and two-client testing remain with the maintainer**. Native spears only exist in newer Minecraft versions; this port uses the items and poses available in its target game.
