# Howling Werewolf — NeoForge 1.21.1

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf 标志" width="128">
</p>

<p align="center">
  <a href="https://github.com/R-Eatch/howling-werewolf/releases/latest"><img src="https://img.shields.io/github/v/release/R-Eatch/howling-werewolf?display_name=tag&amp;style=flat-square" alt="最新版本"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.248%2B-E04E14?style=flat-square" alt="NeoForge 21.1.248 或更高版本">
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square" alt="Java 21">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="代码许可证：MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="美术资源许可证：CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README.md">English</a></p>

> 当前分支提供 Minecraft 1.21.1 的 NeoForge 版；Minecraft 1.20.1 的 Forge 版维护在 [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) 分支。

当前内容已同步至 Forge 1.1.3，皮肤功能及本次双眼、野兽前臂纹理调整等待 NeoForge 实机测试。

Howling Werewolf 是一款独立开发的狼人变身与成长 Mod，内容围绕感染、月相变身、两套成长系统、三种狼人形态、猎人与银制品、狼毒草以及 Alpha 试炼展开。

## 环境要求

| 组件 | 版本 |
|---|---|
| Minecraft Java 版 | 1.21.1 |
| Mod 加载器 | NeoForge 21.1.248 或兼容的更高 21.1.x 版本 |
| Java | 21 |
| Howling Werewolf | 1.1.3 |

## 狼人皮肤

- 默认 **K** 打开狼人菜单 → **皮肤**，可选择原棕色 Adrian、琥珀眼灰狼 Ashen、金眼黑狼 Onyx、红眼白狼 Ivory；每套覆盖双足、四足和野兽三种形态。
- 预览支持鼠标跟随、拖动旋转、滚轮缩放和装备显示开关，点击应用后保存。预览不会解锁或切换实际形态。等级重置入口位于技能树列表下方。
- 皮肤选择会随玩家数据保存，死亡、换维度、重置等级、治愈后再觉醒均保留；第三人称、背包预览和第一人称爪使用同一套皮肤。
- 可在皮肤页创建绘画模板、打开资源包列表，启用自己的 PNG 皮肤；编辑后按 **F3 + T** 重载。详见[自定义皮肤教程](CUSTOM_SKINS-zh.md)（[English](CUSTOM_SKINS.md)）。

联机时服务端与客户端都需更新至 1.1.3。自定义皮肤只同步 ID，观察者需要启用相同资源包；缺少资源包时显示 Adrian，保存的选择仍保留。

## 从源码构建

下载或克隆此分支，然后使用 Java 21 工具链运行 Gradle Wrapper：

```powershell
./gradlew.bat clean build
```

构建生成的可分发 JAR 位于 `build/libs/howlingwerewolf-1.1.3-neoforge.jar`。

## 发布状态

此前发布的 NeoForge 版已完成全套实机测试。本轮新增皮肤功能的界面、渲染、存档和联机表现由维护者继续实机验证。

添加或更新任何 Mod 前，请备份所有重要存档。

## 管理员命令

服务器管理员与测试人员可以查阅[管理员命令指南](ADMIN_COMMANDS-zh.md)，该文档也提供[英文版本](ADMIN_COMMANDS.md)。

## 许可证

源代码使用 [Mozilla Public License 2.0](LICENSE.md) 许可证。项目原创美术资源另行使用 [CC BY-SA 4.0](LICENSE-ASSETS.md) 许可证。署名与来源信息请参阅 [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) 和 [CREDITS.txt](CREDITS.txt)。
