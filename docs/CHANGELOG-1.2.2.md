# Howling Werewolf 1.2.2 — Minecraft 1.20.1 / Forge

## 简体中文

- 模型切换按钮和内置皮肤目录的署名统一改为 **Classic — By R_Eatch / Modern — By thewolfulti**，中英文同步。
- 修复 Modern 双足、四足及野兽在 Xenon / Embeddium 等实体快速渲染路径下的 UV 错配。导入器在方块构造后保留小数 UV 与镜像，绘制时直接使用最终多边形顶点，不再使用构造时缓存的原始 box UV。
- 同一路径覆盖玩家、预览、NPC、第一人称爪及分段护甲，保留部件显隐、关节变换、光照、颜色与附加纹理。
- 补齐 Modern 皮肤说明的中英文翻译，左侧皮肤列表及悬停说明显示实际描述。

保留 1.2.1 的模型/配色资源、存档、协议 12、NPC 服务端配置、战斗及成长规则。版本升级为 **1.2.2**。本地构建与对应回归检查完成；**实机观感及多人验收仍由维护者进行**。

## English

- Use **Classic — By R_Eatch / Modern — By thewolfulti** for the model switch and built-in coat credits in both languages.
- Fix Modern Werewolf, Quadruped Wolf and Beast UVs with entity batchers such as Xenon / Embeddium. Draw finalized polygon vertices, retaining fractional and mirrored UVs instead of constructor-cached box UVs.
- Use this path for player/world/preview/NPC models, first-person claws and segmented armor, preserving visibility, transforms, light, color and overlay.
- Restore the missing English and Simplified Chinese Modern coat description in the skin list and hover text.

Keep the 1.2.1 model/palette assets, saved data, protocol 12, server-owned NPC appearance, combat and progression. Version is **1.2.2**. Local builds and targeted regressions are complete; **visual in-game and multiplayer acceptance remain with the maintainer**.
