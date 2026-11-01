# Howling Werewolf — NeoForge 1.21.1

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf 标志" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/tree/1.21.1-neoforge"><img src="https://img.shields.io/badge/Mod-1.1.8-blue?style=flat-square" alt="最新 Mod 版本 1.1.8"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.248%2B-E04E14?style=flat-square" alt="NeoForge 21.1.248 或更高版本">
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square" alt="Java 21">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="代码许可证：MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="美术资源许可证：CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README.md">English</a></p>

> 当前分支提供 Minecraft 1.21.1 的 NeoForge 版；Minecraft 1.20.1 的 Forge 版维护在 [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) 分支。

当前最新版本为 **1.1.8**，新增三种变身形态的固定高度自动跨步。保留 1.1.7 的刷怪蛋、贴图、观察者同步和空手掠夺修复。**1.1.3 已由维护者完成全流程实机测试；当前版本 1.1.8 对更新内容已经完成测试。** 详见[本版本更新日志](docs/CHANGELOG-1.1.8.md)。

Howling Werewolf 是一款独立开发的狼人变身与成长 Mod，内容围绕感染、月相变身、两套成长系统、三种狼人形态、猎人与银制品、狼毒草以及 Alpha 试炼展开。

## 环境要求

| 组件 | 版本 |
|---|---|
| Minecraft Java 版 | 1.21.1 |
| Mod 加载器 | NeoForge 21.1.248 或兼容的更高 21.1.x 版本 |
| Java | 21 |
| Howling Werewolf | 1.1.8 |

## 狼人皮肤与自定义皮肤

- 默认 **K** 打开狼人菜单 → **皮肤**，可选择原棕色 Adrian、琥珀眼灰狼 Ashen、金眼黑狼 Onyx、红眼白狼 Ivory；每套覆盖双足、四足和野兽三种形态。
- 预览支持鼠标跟随、拖动旋转、滚轮缩放和装备显示开关，点击应用后保存。预览不会解锁或切换实际形态。等级重置入口位于技能树列表下方。
- 皮肤选择会随玩家数据保存，死亡、换维度、重置等级、治愈后再觉醒均保留；第三人称、背包预览和第一人称爪使用同一套皮肤。
- 可在皮肤页创建绘画模板、打开资源包列表，启用自己的 PNG 皮肤；编辑后按 **F3 + T** 重载。详见[自定义皮肤教程](CUSTOM_SKINS-zh.md)（[English](CUSTOM_SKINS.md)）。

联机时服务端与客户端都需更新至 1.1.8。自定义皮肤只同步 ID，观察者需要启用相同资源包；缺少资源包时显示 Adrian，保存的选择仍保留。

## 刷怪蛋与贴图更新

![狼人、猎人与月冠 Alpha 狼人刷怪蛋](docs/spawn-eggs.png)

- 狼人和猎人刷怪蛋使用独立的 16×16 图标。
- 新增 **月冠 Alpha 狼人刷怪蛋**，位于模组创造栏，物品 ID 为 `howlingwerewolf:alpha_werewolf_spawn_egg`。它生成与 Alpha 试炼相同的 Boss；通过刷怪蛋单独生成不会绑定仪式，也不会发放仪式通关奖励。
- Alpha 徽章、月咒珍珠、乌头花、银矿石和深层银矿石更新为 16×16；全部 18 张运行时物品和方块贴图统一为这一尺寸，狼人皮肤及模型保持原有外观。

## 自动跨步

自动跨步使用固定高度：**普通双足狼人 1.5 格、四足狼 1 格、野兽 2 格**，不随跳跃技能、护甲或药水变化；手动跳跃仍沿用原有技能效果。

`config/howlingwerewolf-common.toml` 的 `[gameplay]` 下新增 `enableWerewolfAutoStep = true`，默认开启；设为 `false` 即关闭额外跨步。恢复人形也会移除这项加成。

## 支持版本

当前提供 Minecraft **1.20.1 / Forge**、**1.21.1 / NeoForge**、**26.1.2 / NeoForge**，最新 Mod 版本为 **1.1.8**。各版本源码入口见 [GitHub 主页面](https://github.com/R-Eatch/howling-werewolf)。

## 从源码构建

下载或克隆此分支，然后使用 Java 21 工具链运行 Gradle Wrapper：

```powershell
./gradlew.bat clean build
```

构建生成的可分发 JAR 位于 `build/libs/howlingwerewolf-1.1.8-neoforge.jar`。

## 发布状态

**1.1.3 已由维护者完成全流程实机测试；当前版本 1.1.8 对更新内容已经完成测试。**最新更新消息和版本说明请查看 [GitHub 主页面](https://github.com/R-Eatch/howling-werewolf)及[本版本更新日志](docs/CHANGELOG-1.1.8.md)。

添加或更新任何 Mod 前，请备份所有重要存档。

## 管理员命令

服务器管理员与测试人员可以查阅[管理员命令指南](ADMIN_COMMANDS-zh.md)，该文档也提供[英文版本](ADMIN_COMMANDS.md)。

## 许可证

源代码使用 [Mozilla Public License 2.0](LICENSE.md) 许可证。项目原创美术资源另行使用 [CC BY-SA 4.0](LICENSE-ASSETS.md) 许可证。署名与来源信息请参阅 [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) 和 [CREDITS.txt](CREDITS.txt)。
