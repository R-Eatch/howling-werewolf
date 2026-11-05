# Howling Werewolf — NeoForge 26.1.2

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf 标志" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge"><img src="https://img.shields.io/badge/Mod-1.3.0-blue?style=flat-square" alt="开发版本"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.1.2-62B47A?style=flat-square" alt="Minecraft 26.1.2">
  <img src="https://img.shields.io/badge/NeoForge-26.1.2.122%2B-E04E14?style=flat-square" alt="NeoForge 26.1.2.122 或更高版本">
  <img src="https://img.shields.io/badge/Java-25-007396?style=flat-square" alt="Java 25">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="代码许可证：MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="美术资源许可证：CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README.md">English</a></p>

> 当前分支提供 Minecraft 26.1.2 的 NeoForge 版；Minecraft 1.20.1 的 Forge 版维护在 [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) 分支。

Howling Werewolf 是一款独立开发的狼人变身与成长 Mod，内容围绕感染、月相变身、两套成长系统、三种狼人形态、猎人与银制品、狼毒花、Alpha 试炼以及随从狼群展开。

## 1.3.0 更新内容

- **全新的进阶界面。** 按 **K**，世界退入月光：你自己的形态站在今夜的月亮前，经验环绕月轮，技能漂浮在四周。右上角的月牙可以在白色“月白”和深色“夜幕”两种画布之间切换。皮肤页和重置页也换成了同样的样式。
- **全新的随从窗口。** 同一套风格的轻量版：一块浮在游戏画面上的面板，打开很快，数字键直接下令，下方黑色栏说明指针所指的内容。狩猎现在有进度条。
- **更有分量和节奏的攻击。** 双手都空着时，狼人先挥右爪再挥左爪；野兽双臂砸下，连击时接一记横扫；四足狼扑咬，连击时撕扯。三种形态各有节奏，连击时左右爪轮流领先，第一人称也能看到双爪。随从用同样的方式战斗。
- **用展示框指定存放。** 在箱子或木桶上挂一个放了物品的展示框，这个容器就只放框里的物品：同种物品会搬进来，其他物品会搬出去。
- **陷阱箱和潜影盒留给你自己。** 整理时随从会绕开它们，想原样留着的东西放进去就好。
- **随从说话更古雅。** 商人和随从的全部对白都重写了，中英文同步，并给整理加了新台词。见[对白目录](docs/COMPANION-DIALOGUE.md)。
- **肘部毛发修复。** 手臂在下蹲、奔跑和攻击中折叠时，前臂上的毛发顺着手臂贴合。

## 早期版本

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
| Howling Werewolf | 1.3.0 |

## 狼人皮肤

- 默认 **K** 打开狼人菜单 → **皮肤**。右上角 **Classic / Modern** 切换模型和皮肤列表，点击 **应用** 保存。Classic 保留 Adrian、Ashen、Onyx、Ivory；Modern 提供 Ashen、Black、Chestnut、Pale、Rusty、Snowy、Spotted、Stripped、Woods。每套覆盖三个形态。
- 新玩家默认使用 **Modern / Ashen**。升级时保留已有存档的 Classic 外观选择。
- 预览支持鼠标跟随、拖动旋转、滚轮缩放和装备显示开关，点击应用后保存。预览不会解锁或切换实际形态。等级重置入口位于技能树页的下方黑色栏。
- Modern 视角跟随站立、下蹲和四肢冲刺；手持物保持原版大小并对齐掌心，护甲和鞘翅跟随新骨骼，继续遵守原有装备规则。
- 皮肤选择会随玩家数据保存，死亡、换维度、重置等级、治愈后再觉醒均保留；第三人称、背包预览和第一人称爪使用同一套皮肤。
- 可在皮肤页创建绘画模板、打开资源包列表，启用自己的 PNG 皮肤；编辑后按 **F3 + T** 重载。详见[自定义皮肤教程](CUSTOM_SKINS-zh.md)（[English](CUSTOM_SKINS.md)）。

联机时服务端与客户端都需更新至 **1.3.0**（协议 18）。每个玩家的模型系列和皮肤独立同步：A 使用 Modern、B 使用 Classic 时，双方都能看到 A 的 Modern 模型和 B 的 Classic 模型。内置九套配色无需额外资源包。自定义皮肤仍需观察者启用同一资源包；缺少资源包时，Classic 回退 Adrian，Modern 回退 Ashen，保存的选择仍保留。

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
- [Minecraft 26.1.2 · NeoForge](https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge)：本分支，版本 **1.3.0**。

## 从源码构建

安装 Java 25 JDK 后运行：

```sh
./gradlew build -x test
```

Windows 使用 `gradlew.bat build -x test`。可分发 JAR 位于 `build/libs/howlingwerewolf-1.3.0-neoforge-26.1.2.jar`。

当前 Mod 源码版本为 **1.3.0**，网络协议 18。

最终构建通过；模型/NPC/UI 回归 21028 项、成品检查 2624 项通过，其中全部进阶文本与 1.2.7 逐字节一致。原生 NeoForge 服务端 759 项通过：先从 1.2.7 留下的真实存档升级，再做一次真实重启，覆盖真实箱子上的展示框、陷阱箱和潜影盒。在 NeoForge 26.1.2.122 和 RTX 4060 Laptop GPU 上，真实客户端在隔离目录中跑完全部脚本（中文 GUI 缩放 2，英文 3）：随从走动、开箱、搬运，物品总数不变，挂框箱子最后只剩框内物品，陷阱箱保持原样，三种形态的第一人称和第三人称攻击都截取了画面。这些实机测试之后又做了几处客户端小调整，只经过构建和回归。攻击手感和长时间游玩由作者实机验收；这一版没有测试 Sodium、光影包、领地插件或两个真实客户端。

## 管理员命令

服务器管理员与测试人员可以查阅[管理员命令指南](ADMIN_COMMANDS-zh.md)，该文档也提供[英文版本](ADMIN_COMMANDS.md)。

## 许可证

源代码使用 [Mozilla Public License 2.0](LICENSE.md) 许可证。项目原创美术资源另行使用 [CC BY-SA 4.0](LICENSE-ASSETS.md) 许可证。署名与来源信息请参阅 [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) 和 [CREDITS.txt](CREDITS.txt)。
