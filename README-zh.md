# Howling Werewolf — NeoForge 26.1.2

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf 标志" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge"><img src="https://img.shields.io/badge/Mod-1.2.5-blue?style=flat-square" alt="开发版本"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.1.2-62B47A?style=flat-square" alt="Minecraft 26.1.2">
  <img src="https://img.shields.io/badge/NeoForge-26.1.2.122%2B-E04E14?style=flat-square" alt="NeoForge 26.1.2.122 或更高版本">
  <img src="https://img.shields.io/badge/Java-25-007396?style=flat-square" alt="Java 25">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="代码许可证：MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="美术资源许可证：CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README.md">English</a></p>

> 当前分支提供 Minecraft 26.1.2 的 NeoForge 版；Minecraft 1.20.1 的 Forge 版维护在 [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) 分支。

Howling Werewolf **1.2.5** 改进随从管理：默认 **C** 打开总控，右键随从打开带形象预览的单体控制页，潜行右键保留九格背包快捷入口。教程集中说明仪式、招募、指令、升级和复活；主人称呼可自定义，对白仅归属玩家可见。跟随状态拒绝怪物主动选定随从，且受击不反击。见[1.2.5 随从指南](docs/COMPANIONS-1.2.5-zh.md)、[完整掉落与交易表](docs/COMPANION-TABLES-1.2.4.md)及[架构来源核查](docs/COMPANION-PROVENANCE.md)。

1.2.3 引入的祭坛召唤商人、徽章确认招募、九格背包、跟随/守护/休息/狩猎、跨维度召回、升级、复活及改名换肤继续保留。档案绑定主人 UUID，模型跟随世界 NPC 设置。1.2.3 历史框架说明见[随从框架说明](docs/COMPANIONS-1.2.3-zh.md)。

Howling Werewolf **1.2.2** 将 Modern 双足/野兽的持物锚点略微下移，并抬高四足狼的口持物品，使其落在嘴部咬合线。模型切换与内置皮肤署名统一为 **Classic — By R_Eatch / Modern — By thewolfulti**。详见 [1.2.2 更新日志](docs/CHANGELOG-1.2.2.md)。

Howling Werewolf 1.1.8 新增固定高度自动跨步：四足狼（wolf）为 **1.0 格**、双足狼人（werewolf）为 **1.5 格**、野兽（beast）为 **2.0 格**。配置文件 `config/howlingwerewolf-common.toml` 中的 `gameplay.enableWerewolfAutoStep` 默认为 `true`；设为 `false` 关闭额外跨步，恢复原生跨步。手动跳跃继续沿用原有技能和形态规则。保留 1.1.7 的火焰爪首击熟肉修复、Alpha 战斗节奏及 16×16 物品和方块贴图。

保留 NeoForge 1.21.1 的长爪副伤害公式：普通狼人副伤害重新包含裂月利爪加成；长爪副伤害仍不触发吸血。

Howling Werewolf 是一款独立开发的狼人变身与成长 Mod，内容围绕感染、月相变身、两套成长系统、三种狼人形态、猎人与银制品、狼毒草以及 Alpha 试炼展开。

## 环境要求

| 组件 | 版本 |
|---|---|
| Minecraft Java 版 | 26.1.2 |
| Mod 加载器 | NeoForge 26.1.2.122 或兼容的更高 26.1.2.x 版本 |
| Java | 25 |
| Howling Werewolf | 1.2.5 (Companion improvements) |

## 狼人皮肤

- 默认 **K** 打开狼人菜单 → **皮肤**。右上角 **Classic / Modern** 切换模型和皮肤列表，点击 **应用** 保存。Classic 保留 Adrian、Ashen、Onyx、Ivory；Modern 提供 Ashen、Black、Chestnut、Pale、Rusty、Snowy、Spotted、Stripped、Woods。每套覆盖三个形态。
- 新玩家默认使用 **Modern / Ashen**。升级时保留已有存档的 Classic 外观选择。
- 预览支持鼠标跟随、拖动旋转、滚轮缩放和装备显示开关，点击应用后保存。预览不会解锁或切换实际形态。等级重置入口位于技能树列表下方。
- Modern 视角跟随站立、下蹲和四肢冲刺；手持物保持原版大小并对齐掌心，护甲和鞘翅跟随新骨骼，继续遵守原有装备规则。
- 皮肤选择会随玩家数据保存，死亡、换维度、重置等级、治愈后再觉醒均保留；第三人称、背包预览和第一人称爪使用同一套皮肤。
- 可在皮肤页创建绘画模板、打开资源包列表，启用自己的 PNG 皮肤；编辑后按 **F3 + T** 重载。详见[自定义皮肤教程](CUSTOM_SKINS-zh.md)（[English](CUSTOM_SKINS.md)）。

联机时服务端与客户端都需更新至 **1.2.5**（协议 15）。每个玩家的模型系列和皮肤独立同步：A 使用 Modern、B 使用 Classic 时，双方都能看到 A 的 Modern 模型和 B 的 Classic 模型。内置九套配色无需额外资源包。自定义皮肤仍需观察者启用同一资源包；缺少资源包时，Classic 回退 Adrian，Modern 回退 Ashen，保存的选择仍保留。

## NPC 模型配置

在 `config/howlingwerewolf-common.toml` 中设置：

```toml
[gameplay]
npcModelSet = "modern"
```

- `modern`（默认）：Alpha 及其召唤的狼人固定 Ashen；野生与刷怪蛋生成的狼人从九套材质中随机选择并保存。
- `classic`：上述 NPC 使用对应形态的 Classic 模型和 Adrian 材质。
- 狼人商人和随从也跟随该模型设置：商人随机配色，招募后可在管理界面改名、选皮肤，Classic 与 Modern 分别记住选择。
- 单人由本地整合服务端配置决定，多人由服务器配置决定。配置文件重载后，已生成的 NPC 也会更新；模型和材质通过实体追踪同步，观察者的本地配置不会覆盖服务端选择。该参数不改变玩家自己选用的皮肤。

商人自然停留 **10 分钟**，召来时会告知期限；月厄珍珠可用 **6 狼毒花＋1 钻石**或 **12 绿宝石＋4 狼毒花**兑换。[完整商人／随从台词](docs/COMPANION-DIALOGUE-1.2.4-zh.md)。

## 随从开关与按键

默认 **C** 打开随从管理，可在控制选项中重绑定；K 菜单的“随从”入口位于“技能树”上方。服务器在 `config/howlingwerewolf-common.toml` 中设置：

```toml
[gameplay]
enableCompanions = true
```

设为 `false`收起随从并关闭其相关操作，保留档案/背包，狩猎与冷却暂停。默认启用；多人游戏使用服务器设置。[完整行为](docs/COMPANIONS-1.2.4-zh.md#服务器禁用开关)。

## 刷怪蛋与持物动作

![狼人、猎人与月冠 Alpha 狼人刷怪蛋](docs/spawn-eggs.png)

- 狼人和猎人刷怪蛋改为独立的 16×16 图标，与 Minecraft 26.1.2 的生物专属刷怪蛋风格同步。
- 新增 **月冠 Alpha 狼人刷怪蛋**，位于模组创造栏，物品 ID 为 `howlingwerewolf:alpha_werewolf_spawn_egg`。生成的是 Alpha 试炼使用的同一 Boss 实体；通过刷怪蛋单独生成不会绑定仪式，也不会发放仪式通关奖励。
- 普通狼人和野兽形态使用长矛时采用原版持矛准备动作；三叉戟使用独立的投掷蓄力姿势。

## 支持版本

- [Minecraft 1.20.1 · Forge](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge)
- [Minecraft 1.21.1 · NeoForge](https://github.com/R-Eatch/howling-werewolf/tree/1.21.1-neoforge)
- [Minecraft 26.1.2 · NeoForge](https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge)：本分支，版本 **1.2.5**（随从优化）。

## 从源码构建

安装 Java 25 JDK 后运行：

```sh
./gradlew build -x test
```

Windows 使用 `gradlew.bat build -x test`。可分发 JAR 位于 `build/libs/howlingwerewolf-1.2.5-neoforge-26.1.2.jar`。

当前 Mod 源码版本为 **1.2.5**。构建、178 项原生服务端检查、1391 项模型/NPC/UI 检查及 992 项成品资源检查已完成；GPU 画面和双真实客户端验收尚未完成。旧安装保留已保存的按键绑定，请在控制选项中改为 C 或恢复默认。GitHub Release 和各平台文件由维护者另行发布。

## 管理员命令

服务器管理员与测试人员可以查阅[管理员命令指南](ADMIN_COMMANDS-zh.md)，该文档也提供[英文版本](ADMIN_COMMANDS.md)。

## 许可证

源代码使用 [Mozilla Public License 2.0](LICENSE.md) 许可证。项目原创美术资源另行使用 [CC BY-SA 4.0](LICENSE-ASSETS.md) 许可证。署名与来源信息请参阅 [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) 和 [CREDITS.txt](CREDITS.txt)。
