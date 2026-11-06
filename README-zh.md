# Howling Werewolf — NeoForge 26.1.2

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf 标志" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge"><img src="https://img.shields.io/badge/Mod-1.3.2-blue?style=flat-square" alt="开发版本"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.1.2-62B47A?style=flat-square" alt="Minecraft 26.1.2">
  <img src="https://img.shields.io/badge/NeoForge-26.1.2.122%2B-E04E14?style=flat-square" alt="NeoForge 26.1.2.122 或更高版本">
  <img src="https://img.shields.io/badge/Java-25-007396?style=flat-square" alt="Java 25">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="代码许可证：MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="美术资源许可证：CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README.md">English</a></p>

> 当前分支提供 Minecraft 26.1.2 的 NeoForge 版；Minecraft 1.20.1 的 Forge 版维护在 [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) 分支。

Howling Werewolf 是一款独立开发的狼人变身与成长 Mod，内容围绕感染、月相变身、两套成长系统、三种狼人形态、猎人与银制品、狼毒花、Alpha 试炼以及随从狼群展开。

## 1.3.1 更新内容

- **攻击动作恢复为 1.2.2 的样式。** 1.3.0 的新挥爪动作撤下，狼人、野兽和四足狼重新使用原来的单臂挥击，第一人称也恢复原样；随从、Alpha 和野生狼人一起恢复。伤害和攻击冷却没有变化。
- **长爪、工具爪和火焰爪可以关闭。** 按 **K** → 技能页选中它们，用下方的“关闭／开启”切换。默认开启；关闭不退还也不消耗技能点，随时可以重新开启，开关随存档保存。
- **随从可以遣散。** 狼群页指令栏下方新增“遣散”，点下后进入单独的确认页。遣散后档案、阶数和外观一并消失，背包里的物品交还给你（放不下的落在脚边），名额随即空出。阵亡或正在狩猎的随从也可以遣散。
- **随从更强，休整更短。** 0–3 阶攻击改为 6／8／10／12；每次狩猎后的休整从三个 MC 日缩短为两个。

## 早期版本

- **1.3.0**：月光风格的进阶界面和随从窗口，狩猎进度条，展示框指定整理，陷阱箱和潜影盒不参与整理，古雅风格对白，肘部毛发修复。
- **1.2.7**：随从亲自走动整理；四页签随从窗口；Beast 改为狼式奔跑；`/werewolf unlockalpha`。
- **1.2.6**：随从“整理”指令；Modern UV 提交方式调整。
- **1.2.5**：**C** 打开随从窗口，右键打开单个随从，自定义称呼，对白只发给主人。
- **1.2.3–1.2.4**：随从系统：祭坛召唤商人、徽章确认招募、九格背包、跟随/守护/休息/狩猎、跨维度召回、升级、复活、改名换肤。
- **1.2.2**：Modern 持物锚点，署名统一为 **Classic — By R_Eatch / Modern — By thewolfulti**。详见 [1.2.2 更新日志](docs/CHANGELOG-1.2.2.md)。
- **1.1.8**：固定高度自动跨步：四足狼 1.0 格、狼人 1.5 格、野兽 2.0 格，`gameplay.enableWerewolfAutoStep` 默认开启。

## 文档

| 指南 | 内容 |
|---|---|
| [随从指南](docs/COMPANIONS-zh.md)（[English](docs/COMPANIONS.md)） | 召唤、招募、指令、整理、狩猎、设置 |
| [狩猎与交易表](docs/COMPANION-TABLES-zh.md)（[English](docs/COMPANION-TABLES.md)） | 全部狩猎条目、阶数系数和商人交易 |
| [对白目录](docs/COMPANION-DIALOGUE.md) | 商人和随从的全部中英文对白 |
| [自定义皮肤](CUSTOM_SKINS-zh.md)（[English](CUSTOM_SKINS.md)） | 绘画模板和皮肤资源包 |
| [管理员命令](ADMIN_COMMANDS-zh.md)（[English](ADMIN_COMMANDS.md)） | 面向测试者和服主的 `/werewolf` |
| [架构来源核查](docs/COMPANION-PROVENANCE.md) | 随从系统的参考来源和实现差异 |

## 环境要求

| 组件 | 版本 |
|---|---|
| Minecraft Java 版 | 26.1.2 |
| Mod 加载器 | NeoForge 26.1.2.122 或兼容的更高 26.1.2.x 版本 |
| Java | 25 |
| Howling Werewolf | 1.3.2 |

## 狼人皮肤

- 默认 **K** 打开狼人菜单 → **皮肤**。右上角 **Classic / Modern** 切换模型和皮肤列表，点击 **应用** 保存。Classic 保留 Adrian、Ashen、Onyx、Ivory；Modern 提供 Ashen、Black、Chestnut、Pale、Rusty、Snowy、Spotted、Stripped、Woods。每套覆盖三个形态。
- 新玩家默认使用 **Modern / Ashen**。升级时保留已有存档的 Classic 外观选择。
- 预览支持鼠标跟随、拖动旋转、滚轮缩放和装备显示开关，点击应用后保存。预览不会解锁或切换实际形态。等级重置入口位于技能树页的下方黑色栏。
- Modern 视角跟随站立、下蹲和四肢冲刺；手持物保持原版大小并对齐掌心，护甲和鞘翅跟随新骨骼，继续遵守原有装备规则。
- 皮肤选择会随玩家数据保存，死亡、换维度、重置等级、治愈后再觉醒均保留；第三人称、背包预览和第一人称爪使用同一套皮肤。
- 可在皮肤页创建绘画模板、打开资源包列表，启用自己的 PNG 皮肤；编辑后按 **F3 + T** 重载。详见[自定义皮肤教程](CUSTOM_SKINS-zh.md)（[English](CUSTOM_SKINS.md)）。

联机时服务端与客户端都需更新至 **1.3.2**（协议 19）。每个玩家的模型系列和皮肤独立同步：A 使用 Modern、B 使用 Classic 时，双方都能看到 A 的 Modern 模型和 B 的 Classic 模型。内置九套配色无需额外资源包。自定义皮肤仍需观察者启用同一资源包；缺少资源包时，Classic 回退 Adrian，Modern 回退 Ashen，保存的选择仍保留。

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

商人自然停留 **10 分钟**，召来时会告知期限；月厄珍珠可用 **6 狼毒花＋1 钻石**或 **12 绿宝石＋4 狼毒花**兑换。[完整商人和随从对白](docs/COMPANION-DIALOGUE.md)。

## 随从开关与按键

默认 **C** 打开随从窗口，可在控制选项中重绑定；K 界面上方的“随从”也能进入。服务器在 `config/howlingwerewolf-common.toml` 中设置：

```toml
[gameplay]
enableCompanions = true
enableCompanionSorting = true
```

`enableCompanions = false` 收起随从并关闭相关操作，保留档案和背包，狩猎与冷却暂停。`enableCompanionSorting = false` 只关闭整理。多人游戏使用服务器设置。完整规则见[随从指南](docs/COMPANIONS-zh.md)。

“月白”和“夜幕”画布是个人偏好，保存在 `config/howlingwerewolf-client.toml`。

## 刷怪蛋与持物动作

![狼人、猎人与月冠 Alpha 狼人刷怪蛋](docs/spawn-eggs.png)

- 狼人和猎人刷怪蛋改为独立的 16×16 图标，与 Minecraft 26.1.2 的生物专属刷怪蛋风格同步。
- 新增 **月冠 Alpha 狼人刷怪蛋**，位于模组创造栏，物品 ID 为 `howlingwerewolf:alpha_werewolf_spawn_egg`。生成的是 Alpha 试炼使用的同一 Boss 实体；通过刷怪蛋单独生成不会绑定仪式，也不会发放仪式通关奖励。
- 普通狼人和野兽形态使用长矛时采用原版持矛准备动作；三叉戟使用独立的投掷蓄力姿势。

## 支持版本

- [Minecraft 1.20.1 · Forge](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge)
- [Minecraft 1.21.1 · NeoForge](https://github.com/R-Eatch/howling-werewolf/tree/1.21.1-neoforge)
- [Minecraft 26.1.2 · NeoForge](https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge)：本分支，版本 **1.3.2**。

## 从源码构建

安装 Java 25 JDK 后运行：

```sh
./gradlew build -x test
```

Windows 使用 `gradlew.bat build -x test`。可分发 JAR 位于 `build/libs/howlingwerewolf-1.3.2-neoforge-26.1.2.jar`。

当前 Mod 源码版本为 **1.3.2**，网络协议 19。

构建通过；模型/NPC/UI 回归 21089 项、成品检查 2704 项通过：与 1.3.0 相比，文本只改了三段随从教程并新增 15 个键，其余逐字节一致。原生 NeoForge 服务端 793 项通过：先从 1.3.0 留下的真实存档启动，再做一次真实重启，覆盖遣散（物品交还、背包满时掉在脚边、阵亡和狩猎中的随从）、三种爪技能的开关（真实的伤害、点燃和挖掘事件，以及存档和同步）和新的冷却与攻击数值。这一版没有在真实客户端中运行，攻击动作、遣散确认页和技能开关的实际观感由作者实机验收；没有测试 Sodium、光影包、领地插件或两个真实客户端。

## 管理员命令

服务器管理员与测试人员可以查阅[管理员命令指南](ADMIN_COMMANDS-zh.md)，该文档也提供[英文版本](ADMIN_COMMANDS.md)。

## 许可证

源代码使用 [Mozilla Public License 2.0](LICENSE.md) 许可证。项目原创美术资源另行使用 [CC BY-SA 4.0](LICENSE-ASSETS.md) 许可证。署名与来源信息请参阅 [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) 和 [CREDITS.txt](CREDITS.txt)。
