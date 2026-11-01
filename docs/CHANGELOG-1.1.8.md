# Howling Werewolf 1.1.8 — Minecraft 26.1.2 / NeoForge

## 简体中文

- 新增固定高度自动跨步：四足狼 1.0 格、双足狼人 1.5 格、野兽 2.0 格。仍需足够的碰撞空间。
- 跨步高度仅由形态决定；手动跳跃继续使用原有技能、形态和药水规则。
- 通用配置 `config/howlingwerewolf-common.toml` 新增 `gameplay.enableWerewolfAutoStep`，默认 `true`，附英文注释；设为 `false` 恢复原生跨步。恢复人形时同样移除狼人额外跨步。
- 继续使用原版碰撞检查和属性同步，不新增按键或能力解锁条件。

Minecraft 26.1.2、NeoForge 26.1.2.122 与 Java 25 基线不变。**1.1.3 已完成全流程实机测试；当前版本 1.1.8 对更新内容已经完成测试。**

## English

- Add fixed-height Auto Step: 1.0 block for Quadruped Wolf, 1.5 blocks for Werewolf and 2.0 blocks for Beast. Collision space must still permit movement.
- Step height depends only on the current form. Manual jumping retains its existing skill, form and potion rules.
- Add `gameplay.enableWerewolfAutoStep` to `config/howlingwerewolf-common.toml`, defaulting to `true`, with an English comment. Set it to `false` to restore normal stepping. Returning to human form also removes the werewolf step bonus.
- Use native collision checks and attribute synchronization, without a new key binding or skill unlock.

The Minecraft 26.1.2, NeoForge 26.1.2.122 and Java 25 baselines are unchanged. **1.1.3 has completed a full in-game playthrough; the changes introduced in the current version, 1.1.8, have been tested.**
