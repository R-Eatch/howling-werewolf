# Howling Werewolf — NeoForge 1.21.1

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf 标志" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/1.21.1-neoforge"><img src="https://img.shields.io/badge/Mod-1.3.2-blue?style=flat-square" alt="最新 Mod 版本 1.3.2"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.248%2B-E04E14?style=flat-square" alt="NeoForge 21.1.248 或更高版本">
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square" alt="Java 21">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="代码许可证：MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="美术资源许可证：CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README.md">English</a></p>

> 当前分支提供 Minecraft 1.21.1 的 NeoForge 版；Minecraft 1.20.1 的 Forge 版维护在 [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) 分支。

Howling Werewolf **1.3.2** 把 1.2.2 之后的更新带到 NeoForge 1.21.1：

- **狼人随从。** 在祭坛召唤狼人商人，用 Alpha 狼人徽章招募随从。随从有九格背包，可以跟随、守护、休息、外出狩猎和整理附近的箱子，能跨维度召回、升阶、复活、改名换毛色，也能遣散（遣散前单独确认，背包物品交还给你）。按 **C** 打开随从窗口。完整玩法见[随从指南](docs/COMPANIONS-zh.md)。
- **整理。** 随从亲自走到箱子和木桶前开盖搬运；挂着放了物品的展示框的容器只存放框里的物品；陷阱箱和潜影盒不参与。走不到跟前的容器，32 格以内也能站在原地整理（服务器可调）。
- **全新的进阶界面与随从窗口。** 按 **K**，世界退入月光，技能漂浮在今夜的月亮四周；右上角月牙切换白色“月白”和深色“夜幕”。皮肤页和重置页同款。
- **长爪、工具爪和火焰爪可以关闭。** 在技能页选中后用“关闭／开启”切换，不消耗也不退还技能点。
- **野兽形态动作。** Modern 野兽冲刺时四足狼式奔跑，下蹲时四足潜行，下蹲视角随之降低。
- 新增技能树“狼群誓约”（决定随从名额和最高阶数）与管理员命令 `/werewolf unlockalpha`。

1.2.2 的持物位置调整见 [1.2.2 更新日志](docs/CHANGELOG-1.2.2.md)。模型切换与内置皮肤署名统一为 **Classic — By R_Eatch / Modern — By thewolfulti**。

Howling Werewolf 是一款独立开发的狼人变身与成长 Mod，内容围绕感染、月相变身、两套成长系统、三种狼人形态、猎人与银制品、狼毒草以及 Alpha 试炼展开。

## 环境要求

| 组件 | 版本 |
|---|---|
| Minecraft Java 版 | 1.21.1 |
| Mod 加载器 | NeoForge 21.1.248 或兼容的更高 21.1.x 版本 |
| Java | 21 |
| Howling Werewolf | 1.3.2 |

## 狼人皮肤

- 默认 **K** 打开狼人菜单 → **皮肤**。右上角 **Classic / Modern** 切换模型和皮肤列表，点击 **应用** 保存。Classic 保留 Adrian、Ashen、Onyx、Ivory；Modern 提供 Ashen、Black、Chestnut、Pale、Rusty、Snowy、Spotted、Stripped、Woods。每套覆盖三个形态。
- 新玩家默认使用 **Modern / Ashen**。升级时保留已有存档的 Classic 外观选择。
- 预览支持鼠标跟随、拖动旋转、滚轮缩放和装备显示开关，点击应用后保存。预览不会解锁或切换实际形态。等级重置入口位于技能树列表下方。
- Modern 视角跟随站立、下蹲和四肢冲刺；手持物保持原版大小并对齐掌心，护甲和鞘翅跟随新骨骼，继续遵守原有装备规则。
- 皮肤选择会随玩家数据保存，死亡、换维度、重置等级、治愈后再觉醒均保留；第三人称、背包预览和第一人称爪使用同一套皮肤。
- 可在皮肤页创建绘画模板、打开资源包列表，启用自己的 PNG 皮肤；编辑后按 **F3 + T** 重载。详见[自定义皮肤教程](CUSTOM_SKINS-zh.md)（[English](CUSTOM_SKINS.md)）。

联机时服务端与客户端都需更新至 **1.3.2**（网络协议 19）。每个玩家的模型系列和皮肤独立同步：A 使用 Modern、B 使用 Classic 时，双方都能看到 A 的 Modern 模型和 B 的 Classic 模型。内置九套配色无需额外资源包。自定义皮肤仍需观察者启用同一资源包；缺少资源包时，Classic 回退 Adrian，Modern 回退 Ashen，保存的选择仍保留。

## NPC 模型配置

在 `config/howlingwerewolf-common.toml` 中设置：

```toml
[gameplay]
npcModelSet = "modern"
```

- `modern`（默认）：Alpha 及其召唤的狼人固定 Ashen；野生与刷怪蛋生成的狼人从九套材质中随机选择并保存。
- `classic`：上述 NPC 使用对应形态的 Classic 模型和 Adrian 材质。
- 单人由本地整合服务端配置决定，多人由服务器配置决定。配置文件重载后，已生成的 NPC 也会更新；模型和材质通过实体追踪同步，观察者的本地配置不会覆盖服务端选择。该参数不改变玩家自己选用的皮肤。

## 随从与界面设置

`config/howlingwerewolf-common.toml` 的 `[gameplay]` 下：

- `enableCompanions = true`：统一开关随从。
- `enableCompanionSorting = true`：单独开关整理。
- `companionSortingReach = 32`（3–64）：随从走不到跟前时，仍能站在原地整理的最远距离。

按 **C** 打开随从窗口，可在控制设置中改键。界面的月白／夜幕选择保存在客户端文件 `config/howlingwerewolf-client.toml`。

## 刷怪蛋与贴图更新

![狼人、猎人与月冠 Alpha 狼人刷怪蛋](docs/spawn-eggs.png)

- 狼人和猎人刷怪蛋使用独立的 16×16 图标。
- 新增 **月冠 Alpha 狼人刷怪蛋**，位于模组创造栏，物品 ID 为 `howlingwerewolf:alpha_werewolf_spawn_egg`。它生成与 Alpha 试炼相同的 Boss；通过刷怪蛋单独生成不会绑定仪式，也不会发放仪式通关奖励。
- Alpha 徽章、月咒珍珠、乌头花、银矿石和深层银矿石更新为 16×16；全部 18 张运行时物品和方块贴图统一为这一尺寸，Classic 皮肤及模型保持原有外观。

## 自动跨步

自动跨步使用固定高度：**普通双足狼人 1.5 格、四足狼 1 格、野兽 2 格**，不随跳跃技能、护甲或药水变化；手动跳跃仍沿用原有技能效果。

`config/howlingwerewolf-common.toml` 的 `[gameplay]` 下新增 `enableWerewolfAutoStep = true`，默认开启；设为 `false` 即关闭额外跨步。恢复人形也会移除这项加成。

## 支持版本

当前提供 Minecraft **1.20.1 / Forge**、**1.21.1 / NeoForge**、**26.1.2 / NeoForge**；本分支为 **1.3.2**。各版本源码入口见 [GitHub 主页面](https://github.com/R-Eatch/howling-werewolf)。

## 从源码构建

下载或克隆此分支，然后使用 Java 21 工具链运行 Gradle Wrapper：

```powershell
./gradlew.bat clean build
```

构建生成的可分发 JAR 位于 `build/libs/howlingwerewolf-1.3.2-neoforge.jar`。

## 发布状态

1.1.3 已由维护者完成全流程实机测试。1.3.1 的内容先在 Minecraft 26.1.2 上由作者实机测试，再移植到本分支。本分支的 1.3.1 移植已通过本地构建，并在用户的 NeoForge 1.21.1 整合包实例（约 35 个模组，含机械动力、铁魔法、精妙背包、JourneyMap 等）中用真实客户端在新建世界跑过脚本检查：随从窗口、整理（物品总数不变）、遣散、商人交易、技能开关、Modern 野兽动作和第一/第三人称挥击。完整生存流程与双客户端联机测试尚未进行。最新更新消息请查看 [GitHub 主页面](https://github.com/R-Eatch/howling-werewolf)。

添加或更新任何 Mod 前，请备份所有重要存档。

## 管理员命令

服务器管理员与测试人员可以查阅[管理员命令指南](ADMIN_COMMANDS-zh.md)，该文档也提供[英文版本](ADMIN_COMMANDS.md)。

## 许可证

源代码使用 [Mozilla Public License 2.0](LICENSE.md) 许可证。项目原创美术资源另行使用 [CC BY-SA 4.0](LICENSE-ASSETS.md) 许可证。署名与来源信息请参阅 [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) 和 [CREDITS.txt](CREDITS.txt)。
