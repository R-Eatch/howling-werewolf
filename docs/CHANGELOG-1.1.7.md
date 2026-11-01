# Howling Werewolf 1.1.7 — Minecraft 1.21.1 / NeoForge

[简体中文](#简体中文) · [English](#english)

## 简体中文

基于正式 1.1.3 更新，运行环境保持 Minecraft 1.21.1、NeoForge 21.1.248、Java 21。

- 狼人、猎人刷怪蛋改用独立 16×16 图标；新增月冠 Alpha 狼人刷怪蛋，加入创造栏及双语名称。刷怪蛋生成的 Boss 不绑定仪式，也不发放仪式通关奖励。
- 更新 Alpha 徽章、月咒珍珠、乌头花、银矿石和深层银矿石共 7 张贴图；全部 18 张运行时物品／方块图片统一为 16×16。
- 修复初次追踪其他玩家时遗漏服务端等级上限的问题，避免观察者本地配置将远端玩家等级错误截断。
- 修复利爪效率的空手掠夺加成：已变身且空手的服务端玩家，其技能等级现在可以进入普通掉落数量和稀有掉落概率的掠夺查询，并保留更高的已有掠夺等级。
- 保留 1.1.3 的长爪计算、主目标减伤对副伤害的影响及副伤害不吸血规则；保留原有 Alpha 战斗节奏、火焰爪和配方实现。
- 保留四套内置皮肤，以及资源包自定义 PNG 皮肤、预览、模板导出和重载功能；更新对应教程、管理员命令指南与双语介绍。

**测试状态：1.1.3 已由维护者完成全流程实机测试；1.1.7 尚未实机测试。本轮仅完成编译打包与静态核查。** 游戏内掉落、刷怪蛋、贴图、多人同步和存档表现由维护者继续验证。最新消息见 [GitHub](https://github.com/R-Eatch/howling-werewolf)。GitHub Release 和文件发布由维护者处理。

## English

Updated from the released 1.1.3 baseline, retaining Minecraft 1.21.1, NeoForge 21.1.248 and Java 21.

- Dedicated 16×16 Werewolf and Hunter spawn-egg icons; a new Moon-Crowned Alpha Werewolf Spawn Egg in the creative tab with bilingual names. Separately spawned bosses neither bind to a ritual nor grant trial-completion rewards.
- Seven updated textures for the Alpha Badge, Moonbane Pearl, Wolfsbane flower, Silver Ore and Deepslate Silver Ore. All 18 runtime item/block textures are now 16×16.
- Initial observers now receive the server's progression limit, preventing their local configuration from incorrectly clamping remote-player levels.
- Fixed Claw Efficiency's empty-hand Looting bonus. A transformed, empty-handed server player's skill rank now reaches both normal drop-count and rare-drop chance queries, preserving a higher existing Looting level.
- Retained 1.1.3 Long Claws calculations, the primary target's reduction influence on splash damage, and the exclusion of splash damage from lifesteal. Existing Alpha timing, Fire Claws and recipe implementations are retained.
- Retained the four built-in coats and resource-pack custom PNG skins, previews, template exports and reloading; updated tutorials, administrator guides and bilingual introductions.

**Testing status: the maintainer has completed a full gameplay test pass for 1.1.3; 1.1.7 has not yet been tested in game. This update has only been built and statically checked.** Drops, spawn eggs, artwork, multiplayer and save behavior await gameplay verification. Follow [GitHub](https://github.com/R-Eatch/howling-werewolf) for updates. GitHub Releases and file publication are handled by the maintainer.
