# Howling Werewolf 1.1.7 — Forge 1.20.1

Date: 2026-11-01. Requires Minecraft 1.20.1, Forge 47.4.16 or a compatible newer 47.x version, and Java 17.

Changes from the official Forge 1.1.3 baseline:

- Add the Moon-Crowned Alpha Werewolf Spawn Egg to the creative tab. Spawning the boss this way does not start a ritual or grant ritual-completion rewards.
- Replace the Werewolf and Hunter eggs with dedicated 16×16 sprites. All three sprites display their original colors.
- Synchronize seven item/block textures from the NeoForge 26.1.2 edition. All 18 item/block PNGs are now 16×16; entity skins and custom-skin template dimensions are unchanged.
- Include the authoritative server maximum level in initial tracking snapshots, matching ordinary player synchronization.
- Update English/Chinese introductions, custom-skin guides, administrator guides and asset provenance. The four built-in coats and PNG/JSON resource-pack customization remain available.

Long Claws retains the 1.1.3 calculation, including the permitted influence of primary-target reduction; secondary hits still do not trigger lifesteal. Forge's existing claw Looting event, Alpha timing and pre-death Fire Claws behavior are retained.

**Testing:** v1.1.3 completed the owner's full in-game survival playthrough. v1.1.7 has not yet been tested in game. The build skips tests; static checks and packaging verification do not establish in-game or multiplayer compatibility. GitHub Releases and downloadable-file publication are handled separately by the maintainer.

---

# 月厄狼人 1.1.7 — Forge 1.20.1

日期：2026-11-01。需要 Minecraft 1.20.1、Forge 47.4.16 或更高的兼容 47.x 版本，以及 Java 17。

相对正式 Forge 1.1.3 的更新：

- 新增月冠 Alpha 狼人刷怪蛋并加入创造栏；用蛋生成 Boss 不会启动仪式，也不发放仪式通关奖励。
- 狼人、猎人刷怪蛋改为独立的 16×16 图标；三个蛋均保留图标原色。
- 同步 NeoForge 26.1.2 的七张物品／方块贴图，全部 18 张物品／方块 PNG 统一为 16×16；实体皮肤及自定义模板尺寸保持原样。
- 初次追踪其他玩家时同步服务端等级上限，与常规同步保持一致。
- 更新中英文介绍、自定义皮肤指南、管理员指南和美术来源说明；继续提供四套内置皮肤及 PNG／JSON 资源包自定义功能。

长爪保持 1.1.3 的计算，包括允许主目标减伤影响副伤害；副伤害仍不吸血。Forge 原有利爪掠夺事件、Alpha 计时及死亡前点燃的火焰爪行为保留。

**测试状态：** v1.1.3 已由作者完成全流程实机生存测试；v1.1.7 尚未实机测试。本次构建跳过测试，静态核查和打包验证不代表已完成实机或多人兼容测试。GitHub Release 和平台文件发布由维护者另行处理。
