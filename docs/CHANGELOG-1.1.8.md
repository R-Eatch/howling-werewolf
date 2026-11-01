# Howling Werewolf 1.1.8 — Forge 1.20.1

## 简体中文

本次基于 1.1.7，运行环境仍为 Minecraft 1.20.1 / Forge 47.4.16 / Java 17。

- 新增三种变身形态的固定自动跨步：四足狼 **1.0 格**、普通狼人 **1.5 格**、野兽 **2.0 格**。
- 自动跨步高度不随技能、护甲或药水变化；手动跳跃继续使用原有技能、形态和药水规则。
- 在 `config/howlingwerewolf-common.toml` 新增带英文注释的 `gameplay.enableWerewolfAutoStep`，默认 `true`；设为 `false` 恢复普通跨步。
- 恢复人形时自动移除狼人跨步加成。手动跳跃与现有战斗规则保持原样。
- 同步版本号、双语介绍、皮肤教程及管理员指南。

**验证状态：1.1.3 已完成全流程实机测试；当前版本 1.1.8 对更新内容已经完成测试。**此前内容见 [1.1.7 更新日志](CHANGELOG-1.1.7.md)。

## English

Updated from 1.1.7, retaining Minecraft 1.20.1 / Forge 47.4.16 / Java 17.

- Add fixed Auto Step heights to all three transformed forms: **1.0 block for Quadruped Wolf**, **1.5 blocks for Werewolf**, and **2.0 blocks for Beast**.
- Skills, armor and potions do not alter Auto Step heights. Manual jumping retains its existing skill, form and potion behavior.
- Add `gameplay.enableWerewolfAutoStep` to `config/howlingwerewolf-common.toml`, with an English comment. It defaults to `true`; set it to `false` to restore normal stepping.
- Remove the werewolf stepping bonus on returning to human form. Preserve manual jumping and existing combat rules.
- Update the version, bilingual introductions, skin guides and administrator guides.

**Validation status: 1.1.3 has completed a full in-game playthrough; the changes introduced in the current version, 1.1.8, have been tested.** See the [1.1.7 changelog](CHANGELOG-1.1.7.md) for earlier changes.
