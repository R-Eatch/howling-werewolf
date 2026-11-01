# 月厄狼人 / Howling Werewolf

<p align="center">
  <img src="src/main/resources/logo.png" alt="月厄狼人 Logo" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/main"><img src="https://img.shields.io/badge/latest_source-1.1.7-blue?style=flat-square" alt="最新源码： 1.1.7"></a>
  <a href="https://github.com/R-Eatch/howling-werewolf/releases/latest"><img src="https://img.shields.io/github/v/release/R-Eatch/howling-werewolf?display_name=tag&amp;label=published%20release&amp;style=flat-square" alt="已发布的 GitHub Release"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.20.1_%7C_1.21.1_%7C_26.1.2-62B47A?style=flat-square" alt="Minecraft 1.20.1, 1.21.1 and 26.1.2">
  <img src="https://img.shields.io/badge/loaders-Forge_%7C_NeoForge-E04E14?style=flat-square" alt="Forge and NeoForge">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="代码许可证： MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="美术资源许可证： CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README.md">English</a></p>

> `main` 当前使用 **Minecraft 26.1.2 / NeoForge** 源码。本介绍覆盖下列三个受支持版本；开发或构建其他版本时，请切换到对应分支。

> 一款面向 Minecraft Java 版的狼人变身与成长 Mod，围绕感染、月相变身、两套成长体系、三种狼人形态、猎人与银、狼毒花以及 Alpha 试炼展开。

## 当前版本与支持版本

**当前最新 Mod 源码版本为 1.1.7。** GitHub Release 与 Modrinth、CurseForge 的模组文件由维护者另行发布，实际可下载的文件版本请以各下载页面为准。

| Minecraft Java 版 | 加载器基线 | Java | 最新 Mod 版本 | 源码与更新日志 |
|---|---|---|---|---|
| 1.20.1 | Forge 47.4.16 | 17 | **1.1.7** | [1.20.1-forge](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) · [1.1.7 日志](https://github.com/R-Eatch/howling-werewolf/blob/1.20.1-forge/docs/CHANGELOG-1.1.7.md) |
| 1.21.1 | NeoForge 21.1.248 | 21 | **1.1.7** | [1.21.1-neoforge](https://github.com/R-Eatch/howling-werewolf/tree/1.21.1-neoforge) · [1.1.7 日志](https://github.com/R-Eatch/howling-werewolf/blob/1.21.1-neoforge/docs/CHANGELOG-1.1.7.md) |
| 26.1.2 | NeoForge 26.1.2.122 | 25 | **1.1.7** | [26.1.2-neoforge](https://github.com/R-Eatch/howling-werewolf/tree/26.1.2-neoforge) · [1.1.7 日志](https://github.com/R-Eatch/howling-werewolf/blob/26.1.2-neoforge/docs/CHANGELOG-1.1.7.md) |

可以使用同一 Minecraft 版本下兼容的较新加载器版本；请勿混用不同游戏版本或加载器的 JAR。`main` 的源码构建环境为 **Java 25**。

**实测状态：1.1.3 已由维护者确认完成全流程实机测试；1.1.7 尚未实机测试。** 这不代表三个版本的所有加载器组合或多人场景都已完成测试。

**两平台累计下载 2,713 次**：[Modrinth](https://modrinth.com/mod/howling-werewolf) 750 次 + [CurseForge](https://www.curseforge.com/minecraft/mc-mods/howling-werewolf) 1,963 次。查询快照：**2026-11-01 00:12:17（Asia/Shanghai）**；计数来源为 [Modrinth 官方 API](https://api.modrinth.com/v2/project/WMXcBjen) 与 [CFWidget 的 CurseForge 公共镜像](https://api.cfwidget.com/1672944)。镜像可能存在延迟；下载次数不等于独立玩家人数。

## 当第一声嚎叫尚未响起

森林里留下的一道爪伤，起初或许和其他伤口没有区别。直到又一个夜晚来临，沉睡的血脉开始苏醒，月亮替你作出了第一次选择。

最初，成为狼人是一场需要熬过去的意外；此后，它才逐渐成为一种可以掌握的力量。你会学着分辨何时顺从满月、何时凭自己的意志变身，也会在一次次狩猎中磨利爪牙、唤醒新的本能，最终以四足踏过荒野。

当你的力量足够强大，开启月誓仪式，进入 Alpha 试炼，Alpha 会决定你是否有资格唤醒血脉深处的野兽。

**月厄狼人（Howling Werewolf）** 是一款为 Minecraft Java 版独立开发的狼人变身与成长 Mod。它把感染、觉醒、月相变身、等级成长、技能树、可学习技能、三种狼人形态、猎人与银制武器、狼毒花，以及最终的 Alpha 试炼连接成一段完整的生存旅程。

本 Mod 受到 Minecraft 社区多个优秀狼人 Mod 的启发，但从代码、玩法系统到项目内容均独立开发。它并非任何其他项目的移植、分支、官方续作或官方重制版，也不隶属于或代表其创作者。

## 这段旅程会怎样开始

1. **狼人血脉的侵入。** 遭遇失控狼人或森林中的狼并在攻击中受到感染，等待之后的夜晚完成觉醒；也可以通过狼人药水立即踏上这条路。
2. **升级与技能成长路线** 按 `K` 打开狼人进阶界面，在这里查看等级、技能树、可学习技能、形态说明和月誓仪式。
3. **学会控制变身。** 当前规则允许时，按 `J` 可以变身或恢复人形；月圆高悬，狼人的血脉让你无法控制自己的形态。
4. **向旧日 Alpha 发起挑战。** 按进阶界面中的说明构筑月誓仪式，击败守夜人与月冠 Alpha，最终唤醒野兽形态。

## 狼人血脉带来了什么

- 感染、延迟觉醒、主动变身，以及满月强制变身。
- 人形、普通狼人、四足狼，以及击败 Alpha 后解锁的野兽形态。
- 兼具角色状态和游戏内指南作用的狼人进阶界面。
- 技能树、主动与被动技能、形态解锁条件，以及月誓仪式指引。
- 空手利爪战斗，以及随成长提升的伤害与自然减伤。
- 吸血、长爪、火焰爪、工具爪、血腥撕咬和月血沸腾。
- 野生失控狼人、中立猎人、村庄巡猎队、银制装备、狼毒花和两种对立药水。
- 完整的单人 Alpha 试炼：祭坛结构、猎人阶段、多阶段首领战、重复奖励，以及永久野兽形态成长。
- 内置狼人皮肤、游戏内预览，以及资源包自定义皮肤和绘画模板。
- 狼人、猎人与月冠 Alpha 狼人的独立刷怪蛋图标。
- 完整英文与简体中文本地化。
- 可配置感染率、经验倍率、最高等级等多项玩法参数。

## 狼人皮肤与自定义皮肤

默认按 **K** 打开狼人菜单 → **皮肤**，可以选择原棕色 **Adrian**、琥珀眼灰狼 **Ashen**、金眼黑狼 **Onyx** 与红眼白狼 **Ivory**；每套都覆盖双足、四足和野兽三种形态。

预览支持鼠标跟随、拖动旋转、滚轮缩放和装备显示开关。点击应用后保存选择；预览不会解锁或切换实际形态。皮肤选择会随玩家数据保存，死亡、换维度、重置等级以及治愈后再觉醒均保留，第三人称、背包预览与第一人称爪使用同一套皮肤。

在皮肤页选择 **创建模板**，即可获得 PNG、Blockbench 绘画模板和资源包说明。导出自己的 PNG，启用对应资源包，再按 **F3 + T** 重载资源，就能在皮肤界面选择作品；无需额外 Mod。

联机只同步皮肤 ID，不会自动上传图片。其他玩家需要启用相同资源包；缺少资源时会显示 Adrian，保存的选择仍然保留。各游戏版本的资源包格式不同，请使用对应分支的模板。

完整教程见[自定义皮肤指南](CUSTOM_SKINS-zh.md)（[English](CUSTOM_SKINS.md)）。

## 新刷怪蛋与物品贴图

![狼人、猎人与月冠 Alpha 狼人刷怪蛋](docs/spawn-eggs.png)

狼人和猎人刷怪蛋现在使用独立的 **16×16** 生物主题图标，并新增 **月冠 Alpha 狼人刷怪蛋**；全部物品与方块贴图也统一为 **16×16**。

Alpha 刷怪蛋位于模组创造栏，物品 ID 为 `howlingwerewolf:alpha_werewolf_spawn_egg`。它会生成 Alpha 试炼中的同一首领实体；单独使用刷怪蛋不会绑定仪式，也不会发放仪式通关奖励。正常解锁野兽形态仍需完成 Alpha 试炼。

## 安装

1. 从上方支持矩阵选择 Minecraft 版本，安装对应加载器及所需 Java。
2. 从 [GitHub Releases](https://github.com/R-Eatch/howling-werewolf/releases)、[Modrinth](https://modrinth.com/mod/howling-werewolf) 或 [CurseForge](https://www.curseforge.com/minecraft/mc-mods/howling-werewolf) 下载与该版本对应的 Mod JAR，放入游戏的 `mods` 目录。
3. 启动游戏。联机时客户端与服务端应安装对应游戏版本的相同 Mod 版本。

1.1.7 的构建产物命名如下；文件是否已发布，请以下载页为准：

| Minecraft / 加载器 | 对应 JAR |
|---|---|
| 1.20.1 Forge | `howlingwerewolf-1.1.7-forge.jar` |
| 1.21.1 NeoForge | `howlingwerewolf-1.1.7-neoforge.jar` |
| 26.1.2 NeoForge | `howlingwerewolf-1.1.7-neoforge-26.1.2.jar` |

添加或更新任何 Mod 前，建议先备份重要世界。

## 最初的几个夜晚

获得狼人血脉主要有两种方式：

- 承受森林里的野生狼或狼人攻击并感染。默认感染概率可以配置，感染者会在之后的夜晚觉醒。
- 酿造或获得狼人药水，饮用后立即完成觉醒。

觉醒后，在血脉规则允许时按 `J` 可以变身或恢复人形；主世界的满月则会强制狼人变身，即使你并不想在那个夜晚显露兽性。

变身状态下击杀生物可以获得狼人经验。随着等级提升，你可以将成长所得投入技能树与新的能力。

不知道下一步该做什么时，按 `K` 打开狼人进阶界面即可。这里不仅记录等级与成长进度，也集中提供技能树、主动与被动技能、形态解锁条件，以及开启 Alpha 试炼所需的月誓结构、条件与奖励。

狼毒花代表与狼人血脉相反的道路。狼毒花药水可以治愈狼人；猎人使用银剑，并提供可持续获得狼毒花的途径。

## 狼人形态

| 形态 | 解锁与切换 | 特点 |
|---|---|---|
| 人形 | 默认状态，或按 `J` 恢复 | 使用普通装备与生存规则，不享受变身加成。 |
| 普通狼人 | 觉醒后按 `J`，或在满月被强制变身 | 利爪战斗、随等级成长的自然减伤、移动加成，以及大部分狼人能力。 |
| 四足狼 | 学习“荒野奔行”后按 `G` | 移速和跳跃明显提高，但攻击伤害略低，不能穿戴装备。 |
| 野兽 | 首次完成 Alpha 试炼后按 `H` | 战斗、速度、跳跃和防御显著增强，但饥饿消耗更快，不能穿戴装备。 |

狼人变身后默认不能穿戴护甲或鞘翅。**披甲本能** 只允许普通狼人形态穿戴装备。装备默认显示（`showWerewolfEquipment=true`），可在配置文件中关闭显示。

只有提供正护甲值的装备会削弱该能力提供的自然减伤和移动增益；鞘翅等零护甲装备不会触发这一削弱。

## 等级、技能树与技能

狼人等级会提高基础利爪伤害和自然减伤。默认最高等级为 20，可配置到 25。

技能树用于强化长期属性和被动效果，包括：

- 利爪伤害与按目标最大生命计算的裂月伤害；
- 自然防御、速度、跳跃、再生、击退抗性和摔落抗性；
- 吸血、饱食控制、利爪距离/抢夺，以及更多狩猎经验。

能力用于解锁新的操作和规则，包括：

- 夜视；
- 狼灵召唤；
- 荒野奔行 / 四足狼形态；
- 月血沸腾；
- 披甲本能；
- 扩展饮食；
- 利爪空槽管理；
- 长爪；
- 工具爪；
- 火焰爪；
- 血腥撕咬。

### Alpha 狼人徽章

Alpha 狼人徽章放在快捷栏任意位置时具有两种效果：

- 只要拥有狼人血脉，即使处于人形，每分钟也会获得 **10 点基础狼人经验**；
- 抵挡一次致命伤害还要求玩家已经处于变身状态。

默认情况下，致命伤害保护会消耗一枚徽章；这一行为可以通过通用配置调整。

将徽章移出快捷栏会关闭两种效果，并清零尚未完成的一分钟被动经验计时。

## 默认按键

所有按键都可以在 Minecraft 的控制设置中修改。

| 按键 | 功能 |
|---|---|
| `K` | 打开狼人进阶界面 |
| `J` | 变身或恢复人形 |
| `G` | 切换四足狼形态 |
| `H` | 切换野兽形态 |
| `V` | 开关狼人自动夜视 |
| `N` | 召唤狼灵 |
| `B` | 使用血腥撕咬 |
| `R` | 释放月血沸腾 |

## 猎人、银、狼毒花、月厄珍珠与野外遭遇

猎人对普通人类保持中立，对已经变身的狼人敌对。

他们会以很低的权重单独出现在森林群系，也会在玩家到访的村庄周围组成 **4–6 人** 的持久巡猎队。村庄首次达到目标人数后，死亡缺员可以在一个 Minecraft 日冷却后逐名补充。

银剑攻击会对狼人造成额外伤害并施加虚弱。普通猎人至少掉落一株狼毒花，并有概率额外掉落一株，且有低概率掉落银锭。

狼毒花自然生成在森林群系，稳定的获取方式仍为击杀出现在村庄的猎人巡猎队。

失控狼人只会在主世界符合条件的森林中，于黑暗的满月夜自然生成。严格的生成条件使其成为危险而少见的遭遇。

月厄珍珠由末影珍珠和狼毒花合成，用于月誓，也可用于合成狼人徽章。

## 月誓与 Alpha 试炼

Alpha 试炼是狼人养成的终局挑战。

按 `K` 打开狼人进阶界面后，可以随时查看月誓页面；祭坛布局、进入条件、所需祭品和挑战奖励都记录在其中。

1. 达到狼人 10 级，并准备一座月誓中央祭坛、四座普通月誓祭坛、一枚 Alpha 狼人徽章和四颗月厄珍珠。
2. 将四座普通祭坛放在中央祭坛的四个正方向，相距正好三格，并保持相同高度。
3. 在中央祭坛献上徽章，在四座外侧祭坛各献上一颗珍珠。
4. 于主世界夜间、普通狼人形态下开始仪式。该试炼每次只为一名参与玩家设计。
5. 先击败强化猎人，再挑战月冠 Alpha 狼人。

首次胜利会永久解锁野兽形态，并给予对应的成长奖励。

重复胜利会根据难度奖励月厄珍珠；困难难度下还会提供额外技能点与技能树点。

## 配置与数据包

各版本的通用配置中可以调整感染率、经验倍率、最高等级等多个参数，你可以根据自己的喜好来改变整段狼人旅程的节奏。

配置文件：`config/howlingwerewolf-common.toml`。

涉及世界生成或已有存档的设置应谨慎修改；进行较大调整前，建议先备份重要世界。

数据包可以扩展以下物品标签：

- `howlingwerewolf:silver_weapons`
- `howlingwerewolf:werewolf_meat`

服务器管理员与测试人员可以查阅[管理员命令指南](ADMIN_COMMANDS-zh.md)，该文档也提供[英文版本](ADMIN_COMMANDS.md)。

## 测试与兼容性

**1.1.3 已由维护者确认完成全流程实机测试；当前最新 1.1.7 尚未实机测试。** 编译打包、源码检查或资源核对不能替代游戏内验证。上述 1.1.3 测试结论不自动覆盖 1.1.7，也不代表三个版本的所有加载器组合都已完成验证。

完整流程测试基线包括从新存档进行常规生存、击败末影龙并取得鞘翅。

**本 Mod 尚未完成完整的多人游戏测试。** 长期多人存档的表现仍需进一步验证；用于多人世界时请保留备份，反馈多人问题时请附上服务器日志。

反馈问题时，请提供：

- Minecraft 版本；
- 加载器名称与版本（Forge 或 NeoForge）；
- 月厄狼人版本；
- 相关配置；
- 已安装 Mod 列表；
- 最新日志或崩溃报告。

## 源码、更新日志与构建

项目持续维护 **Minecraft 1.20.1 Forge**、**Minecraft 1.21.1 NeoForge** 与 **Minecraft 26.1.2 NeoForge**。各版本的源码和 1.1.7 日志见顶部支持矩阵；版本相关的修复以各分支日志为准。

`main` 当前使用 26.1.2 源码。安装 **Java 25 JDK** 后在仓库根目录运行：

```sh
./gradlew build -x test
```

Windows 使用 `gradlew.bat build -x test`。产物位于 `build/libs/howlingwerewolf-1.1.7-neoforge-26.1.2.jar`。该命令跳过测试，构建成功不表示完成实机验收。其他两个版本请从各自分支构建，并使用顶部矩阵中的 Java 版本。

[GitHub 项目与最新动态](https://github.com/R-Eatch/howling-werewolf) · [版本说明与已发布文件](https://github.com/R-Eatch/howling-werewolf/releases) · [反馈问题](https://github.com/R-Eatch/howling-werewolf/issues)

## 许可与版权

Copyright © 2026 R_Eatch.

除另有说明外，《月厄狼人（Howling Werewolf）》的原创源代码采用 Mozilla Public License 2.0（MPL-2.0）开源。

除另有说明外，本项目原创美术资源采用 Creative Commons Attribution-ShareAlike 4.0 International（CC BY-SA 4.0）授权。

欢迎创建分支、移植版本以及继续开发，无需事先获得授权。如果你发布了移植版、分支或重要衍生项目，也欢迎通过 GitHub 或邮件告诉我。

官方 Logo、Howling Werewolf / 月厄狼人项目品牌标识不自动包含在 CC BY-SA 4.0 授权范围内。第三方代码与素材继续适用其各自的原始许可证。详细规则见 [LICENSE.md](LICENSE.md)、[LICENSE-ASSETS.md](LICENSE-ASSETS.md) 与 [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md)。

## 项目独立性声明

月厄狼人是非官方社区 Mod。

本项目与 Microsoft、Mojang Studios、Minecraft Forge，以及为本项目提供灵感的其他狼人 Mod 创作者不存在隶属、认可、授权或官方合作关系。

本项目并非任何其他狼人 Mod 的移植、分支、官方续作或官方重制版。
