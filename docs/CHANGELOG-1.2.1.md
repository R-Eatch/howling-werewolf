# Howling Werewolf 1.2.1 — Minecraft 26.1.2 / NeoForge

## 简体中文

- 将模型系列名称统一更正为 **Modern**，模型标记、内置皮肤 ID 和材质路径统一使用 `modern`。
- 皮肤页右上角按钮悬停时分两行显示署名和切换提示：**Classic — By Eatch / 点击切换为 Modern**，或 **Modern — By Leo / 点击切换为 Classic**。中文和英文界面均更新。
- **新玩家默认 Modern / Ashen**。已有玩家继续使用存档中选择的模型和皮肤；缺少模型字段的旧版玩家数据继续按 Classic 读取。
- **Alpha 使用 Modern 野兽模型及 Ashen 材质**；它召唤的双足狼人使用 Modern 狼人模型及 Ashen 材质。兼容旧存档中的四足 Alpha 援军，使用对应的 Modern 四足模型及 Ashen 材质。
- **野生、刷怪蛋及命令生成的狼人使用 Modern 双足模型**，由服务端从九套配色中随机选择并保存。重新进入游戏、区块重新加载和重新追踪不会重新随机配色。
- 新增 `config/howlingwerewolf-common.toml` 中的 **`gameplay.npcModelSet = "modern"`**。默认使用上述 Modern 外观；设为 `"classic"` 时，野生狼人、刷怪蛋实体、Alpha 和召唤援军统一使用对应形态的 **Classic Adrian** 模型和材质。
- 单人游戏由整合服务端配置决定，多人游戏由服务器配置决定。NPC 的模型与配色通过实体数据同步，客户端自己的配置和玩家皮肤选择不会覆盖它们。配置重载后已生成的 NPC 也会更新，原配色保留，切回 Modern 时恢复。
- NPC 复用导入模型及现有动作映射，保留战斗、试炼、掉落、碰撞和成长规则。保留 1.2.0 的玩家动作、自然尾巴摆动、持物、装备与视角适配。

服务端与客户端均需更新至 **1.2.1**。Minecraft 26.1.2、NeoForge 26.1.2.122、Java 25 基线不变。维护者反馈 1.2.0 实机测试效果良好；1.2.1 通过本地构建、独立动作与同步数据回归、包内资源检查，**本次实机和双客户端联机测试仍由维护者进行**。

## English

- Correct the model set name to **Modern**. Use `modern` consistently for model markers, built-in skin IDs and texture paths.
- Add two-line hover tooltips to the skin model button: **Classic — By Eatch / Click to switch to Modern**, or **Modern — By Leo / Click to switch to Classic**. Update both English and Simplified Chinese.
- Default **new players to Modern / Ashen**. Preserve existing saved model and coat choices; read legacy player data without a model field as Classic.
- Give the **Alpha the Modern Beast model and Ashen coat**, and its summoned biped werewolves the Modern Werewolf model and Ashen coat. Support legacy quadruped Alpha minions with the corresponding Modern Quadruped model and Ashen coat.
- Give **wild, spawn-egg and command-spawned werewolves the Modern biped model**. Choose one of the nine coats on the server and persist it across saves, chunk reloads and entity retracking.
- Add **`gameplay.npcModelSet = "modern"`** to `config/howlingwerewolf-common.toml`. Modern is the default; `"classic"` gives wild werewolves, spawn-egg entities, the Alpha and its pack the **Classic Adrian** model and texture for their form.
- Use the integrated server's setting in single-player and the dedicated server's setting in multiplayer. Synchronize NPC models and coats through entity data, independently of observer configs and player skin preferences. Existing NPCs update after config reload; retain their coats when switching back to Modern.
- Reuse imported geometry and established animation mapping for NPCs. Preserve combat, trial, loot, collision and progression rules, along with the player animation, natural tail sway, held-item, equipment and camera support from 1.2.0.

Server and clients must all update to **1.2.1**. Minecraft 26.1.2, NeoForge 26.1.2.122 and Java 25 remain the baseline. The maintainer reported successful 1.2.0 in-game testing. Version 1.2.1 passes local builds, independent animation/synchronization regressions and packaged-resource checks; **its in-game and two-client multiplayer testing remain with the maintainer**.
