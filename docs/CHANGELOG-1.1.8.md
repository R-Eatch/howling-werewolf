# Howling Werewolf 1.1.8 — Minecraft 1.21.1 / NeoForge

## 简体中文

基于 1.1.7 更新，运行环境保持 Minecraft 1.21.1、NeoForge 21.1.248、Java 21。

- 新增固定高度自动跨步：普通双足狼人 1.5 格、四足狼 1 格、野兽 2 格。跨步高度不随跳跃技能、护甲或药水变化。
- 在 `config/howlingwerewolf-common.toml` 的 `[gameplay]` 下新增 `enableWerewolfAutoStep`，默认 `true`，配置注释说明适用形态及高度规则；设为 `false` 关闭额外跨步。恢复人形时也会移除这项加成。
- 手动跳跃的数值与操作、1.1.7 空手掠夺修复、长爪和其他战斗规则保持原样。
- 同步双语介绍、管理员指南和根目录／随包皮肤指南中的当前版本说明。

**1.1.3 已完成全流程实机测试；当前版本 1.1.8 对更新内容已经完成测试。**

## English

Updated from 1.1.7, retaining Minecraft 1.21.1, NeoForge 21.1.248 and Java 21.

- Add fixed-height automatic stepping: Werewolf 1.5 blocks, Quadruped Wolf 1 block, and Beast 2 blocks. Step height does not scale with Jump rank, armor or potion effects.
- Add `enableWerewolfAutoStep` under `[gameplay]` in `config/howlingwerewolf-common.toml`, defaulting to `true`, with comments explaining the forms and height rule. Set it to `false` to disable the added step height. Returning to human form also removes this bonus.
- Keep manual jumping values and controls, the 1.1.7 empty-hand Looting fix, Long Claws and all other combat rules.
- Update current-version information in bilingual introductions, administrator guides and root/bundled custom-skin guides.

**1.1.3 has completed a full in-game playthrough; the changes introduced in the current version, 1.1.8, have been tested.**
