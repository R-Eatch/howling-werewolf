# Howling Werewolf 管理员命令指南

[English](ADMIN_COMMANDS.md)

本文适用于 Howling Werewolf `1.0.5-beta`（Minecraft 1.21.1 / NeoForge 21.1.248）。这些命令用于开发、验收、服务器管理和存档诊断。

## 使用条件

- 根命令为 `/werewolf`。
- 执行者需要权限等级 2，通常意味着开启作弊的单人世界、服务器管理员或命令方块。
- `<target>` 必须解析为一名在线玩家，可使用玩家名或 `@s`、`@p` 等只选中一人的选择器。
- `true` 表示启用，`false` 表示禁用。
- 修改命令由服务端执行，之后会刷新玩家碰撞尺寸和狼人属性，并将结果同步到客户端。
- `settree` 与 `setability` 会提供合法 ID 的命令补全。

## 命令一览

| 命令 | 功能与注意事项 |
|---|---|
| `/werewolf infect <target>` | 将目标设为感染期人类，安排之后的夜间觉醒，并清除已有召唤狼。 |
| `/werewolf awaken <target>` | 立即觉醒，清除感染状态并进入普通狼人形态。 |
| `/werewolf cure <target>` | 清除狼人身份、感染、形态、成长、技能、能力、冷却和召唤狼；当前结果与 `reset` 相同，但保留为管理员“治愈”入口。 |
| `/werewolf transform <target> [state]` | 省略 `state` 时切换形态；`true` 进入普通狼人形态，`false` 回到人形。该命令会解除满月强制锁定。 |
| `/werewolf setlevel <target> <level>` | 设置狼人等级并清空当前等级经验。语法范围为 1–25，但不能超过服务器配置的最高等级。 |
| `/werewolf addxp <target> <amount>` | 增加 1–100000 点原始狼人经验，可触发升级，不套用正常经验倍率。 |
| `/werewolf setskillpoints <target> <amount>` | 将当前可用能力点设置为 0–100；不是追加点数。 |
| `/werewolf settreepoints <target> <amount>` | 将当前可用技能树点数设置为 0–100；不是追加点数。 |
| `/werewolf settree <target> <skill> <rank>` | 直接设置技能树等级，不消耗点数；`0` 表示清除，等级不能超过该技能上限。 |
| `/werewolf setability <target> <ability> <unlocked>` | 直接解锁或移除能力，不消耗点数。移除四足形态能力时会退出四足形态。 |
| `/werewolf resettree <target>` | 清空所有技能树等级，保留狼人身份、等级和能力。 |
| `/werewolf resetabilities <target>` | 清空所有能力，保留狼人身份、等级和技能树。 |
| `/werewolf reset <target>` | 将全部狼人数据恢复为新玩家默认值。 |
| `/werewolf forcemoon <target> <state>` | `true` 强制进入并保持狼人形态；`false` 只解除锁定，不会自动变回人形，也不会把普通人设为狼人。 |
| `/werewolf status [target]` | 显示身份、感染、形态、Alpha 状态、等级、经验、可用点数、维度和持久化镜像状态。玩家执行时可省略目标，控制台必须指定目标。 |

## 技能树 ID 与最高等级

| ID | 最高等级 | ID | 最高等级 |
|---|---:|---|---:|
| `damage` | 6 | `moon_rend` | 3 |
| `defense` | 5 | `speed` | 5 |
| `resistance` | 5 | `regeneration` | 5 |
| `jump` | 3 | `knockback_resistance` | 2 |
| `fall_resistance` | 2 | `lifesteal` | 3 |
| `satiety` | 3 | `claw_efficiency` | 3 |
| `hunting_mastery` | 2 |  |  |

## 能力 ID

`night_vision`、`summon_wolf_spirit`、`quadruped_form`、`moonblood_surge`、`armored_instinct`、`hard_life`、`empty_claw_slot`、`long_claws`、`tool_claws`、`fire_claws`、`bloody_bite`。

## 状态诊断

`/werewolf status [target]` 输出中的 `persistedMirror` 表示：

- `match`：当前服务端数据与持久化恢复镜像一致。
- `DIFF`：两者不一致，需要检查保存、换维度或同步流程。
- `missing`：尚未找到持久化恢复镜像。

## 常用测试流程

```mcfunction
/werewolf awaken @s
/werewolf setlevel @s 20
/werewolf settreepoints @s 50
/werewolf setskillpoints @s 30
/werewolf settree @s damage 6
/werewolf setability @s quadruped_form true
/werewolf status @s
/werewolf reset @s
```
