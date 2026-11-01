# Howling Werewolf 1.1.7 — Minecraft 26.1.2 / NeoForge

## 简体中文

最新源码版本：**1.1.7**。Minecraft 26.1.2、NeoForge 26.1.2.122、Java 25。

### 自 1.1.3 起的累计更新

- 完成 Minecraft 26.1.2 移植，包括实体与装备渲染、皮肤预览、物品动作、网络与存档 API 适配；普通狼人和野兽形态分别使用长矛持握与三叉戟蓄力动作。
- 狼人、猎人刷怪蛋使用独立 16×16 图标；新增 `howlingwerewolf:alpha_werewolf_spawn_egg` 月冠 Alpha 狼人刷怪蛋并加入模组创造栏。单独生成的 Boss 不绑定月誓仪式，不提供仪式通关奖励。
- 保留四套内置毛色、皮肤预览与自定义 PNG 资源包功能；根目录及随包模板中的双语皮肤指南同步至 1.1.7。
- 首次追踪玩家与普通同步均携带服务端等级上限，避免远端玩家副本被观察者的本地上限截断。
- 1.1.6：修复 JEI 请求同步徽章配方时因实例不一致导致的编码错误；无状态配方的 JSON 与网络编码器共用实例，合成材料及外围自由排列规则不变。
- 1.1.6.1：Alpha 近战间隔恢复为 40 游戏 tick（2 秒），游猎后撤恢复为 120 tick（6 秒），迅捷 III 保持 60 tick（3 秒），移动与转向逐 tick 更新。Boss 血量、攻击属性、减伤与奖励不调整。
- 1.1.6.1：更新 Alpha 徽章、月咒珍珠、乌头花及银矿石/深层银矿石顶面和侧面共 7 张图片，全部 18 张物品与方块贴图统一为 16×16；实体皮肤、UV 与原有三种形态模型保留。
- 1.1.6.2：普通长爪副伤害恢复旧版裂月加成及额外形态倍率；虚空路径保持扣除裂月后取一半。长爪副伤害不触发吸血。
- 1.1.7：火焰爪改为在扣血、死亡掉落之前点燃，使首次命中直接击杀牛、猪等生物也能掉落熟肉。8 秒燃烧及技能触发条件保留；正伤害被吸收生命完全吸收时也可点燃。

### 测试与发布状态

**维护者已确认 1.1.3 完成全流程实机测试；1.1.7 尚未实机测试。** 编译、静态源码与资源核查不代表游戏内验证，也不代表完成多人兼容测试。GitHub Release 与 Modrinth / CurseForge 文件由维护者另行发布；下载时请核对游戏、加载器与 Mod 版本。

Forge 1.20.1 与 NeoForge 1.21.1 的 1.1.7 日志位于各自分支。部分修复针对 26.x 接口和时序，旧版按其原生行为维护，不能将此日志视为旧版代码改动清单。

## English

Latest source version: **1.1.7**. Requires Minecraft 26.1.2, NeoForge 26.1.2.122 and Java 25.

### Cumulative changes since 1.1.3

- Complete the Minecraft 26.1.2 port, including entity/equipment rendering, skin previews, item animations, networking and persistence APIs. Werewolf and Beast forms use separate native spear holding and trident charging poses.
- Give Werewolf and Hunter spawn eggs dedicated 16×16 sprites. Add the Moon-Crowned Alpha Werewolf Spawn Egg (`howlingwerewolf:alpha_werewolf_spawn_egg`) to the mod creative tab. Independently spawned bosses do not bind to a Moon-Oath ritual or award ritual completion rewards.
- Retain four built-in coats, skin previews and custom PNG resource-pack skins; update both root and bundled painting-template guides to 1.1.7.
- Include the server progression limit in first-tracking snapshots as well as normal synchronization, preventing remote player copies from being clamped to an observer's local limit.
- 1.1.6: fix the badge recipe instance mismatch that caused encoding failures when JEI requested recipe synchronization. JSON and network codecs share the stateless recipe instance; ingredients and freely arranged outer slots remain unchanged.
- 1.1.6.1: restore Alpha melee intervals to 40 game ticks (2 seconds), prowling to 120 ticks (6 seconds), and keep Speed III at 60 ticks (3 seconds), with per-tick movement and steering. Boss health, attack attributes, reduction and rewards are unchanged.
- 1.1.6.1: update seven badge, pearl, flower and ore textures so all 18 item/block PNGs are 16×16. Retain entity skins, UVs and the original three form models.
- 1.1.6.2: restore Moonrend and the additional form multiplier in ordinary Long Claws splash. Converted void splash still removes Moonrend before halving damage. Secondary hits do not trigger Lifesteal.
- 1.1.7: ignite Fire Claws targets before health/death-loot processing so first-hit kills of cows, pigs and similar mobs can drop cooked meat. Keep the 8-second duration and skill conditions; positive damage fully absorbed by absorption hearts can also ignite a target.

### Testing and publication

**The maintainer confirms a complete in-game playthrough of 1.1.3; 1.1.7 has not yet been tested in game.** Compilation and static source/resource checks do not establish gameplay or comprehensive multiplayer compatibility. The maintainer publishes GitHub Releases and Modrinth / CurseForge binaries separately; check the Minecraft, loader and Mod versions on each download.

Forge 1.20.1 and NeoForge 1.21.1 have their own 1.1.7 changelogs on their respective branches. Some fixes here address 26.x APIs and event timing; older editions retain their native behavior.
