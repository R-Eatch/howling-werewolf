# 月厄狼人管理员命令指南

本文适用于 Howling Werewolf 1.0.4（Minecraft 1.20.1 / Forge 47.4.16）。这些命令主要用于开发、验收、服务器管理和存档诊断。

## 使用条件

- 根命令为 `/werewolf`。
- 执行者需要命令权限等级 2，通常意味着开启作弊的单人世界、服务器管理员或命令方块。
- `<target>` 必须解析为一名当前在线的玩家，可以使用玩家名或只选中一人的选择器，例如 `@s`、`@p`。
- `true` 表示启用，`false` 表示禁用。
- 所有修改命令均由服务端执行，并在修改后刷新玩家碰撞尺寸、属性修饰器、持久化数据和客户端同步数据。

## 状态与变身命令

### `/werewolf infect <target>`

强制目标进入感染阶段。

- 清除狼人身份、当前变身、满月强制状态和目标已有的召唤狼。
- 设置下一次夜晚觉醒时间并触发感染引导进度。
- 示例：`/werewolf infect Steve`

### `/werewolf awaken <target>`

立即完成觉醒，不等待夜晚。

- 清除感染状态，将目标设为狼人并立即进入普通狼人形态。
- 播放变身效果、刷新碰撞尺寸和狼人属性。
- 示例：`/werewolf awaken Steve`

### `/werewolf cure <target>`

彻底治愈目标并恢复默认人类状态。

- 清除狼人、感染、等级、经验、技能树、能力、冷却、Alpha 击败状态和召唤狼等全部狼人数据。
- 当前结果与 `reset` 相同，但该命令用于表达“治愈”操作。
- 示例：`/werewolf cure Steve`

### `/werewolf transform <target> [state]`

切换或指定目标的变身状态。

- 省略 `[state]`：在当前变身和人类状态之间切换。
- `true`：确保目标具有狼人身份、清除感染状态，并进入狼人形态。
- `false`：解除当前变身；不会把普通人额外标记为狼人。
- 无论哪种方式都会解除 `forcemoon` 锁定。
- 示例：`/werewolf transform Steve`
- 示例：`/werewolf transform Steve true`
- 示例：`/werewolf transform Steve false`

### `/werewolf forcemoon <target> <state>`

设置满月强制变身状态。

- `true`：确保目标具有狼人身份、清除感染状态、立即变身并开启满月强制锁定。
- `false`：只解除满月强制锁定，不会额外授予狼人身份，也不会强制解除现有变身。
- 示例：`/werewolf forcemoon Steve true`

### `/werewolf reset <target>`

将目标的狼人系统数据完全恢复为新玩家默认值。

- 清除召唤狼、属性修饰器、所有成长数据、技能、冷却和变身状态。
- 适合开始一轮全新的完整流程测试。
- 示例：`/werewolf reset Steve`

## 等级与点数命令

### `/werewolf setlevel <target> <level>`

直接设置狼人等级。

- `<level>` 范围为 1 到服务器配置的最高等级；代码允许的绝对上限为 25。
- 自动授予狼人身份并清除互斥的感染状态。
- 设置等级时会把当前等级内经验重置为 0。
- 示例：`/werewolf setlevel Steve 20`

### `/werewolf addxp <target> <amount>`

直接增加狼人经验。

- `<amount>` 范围为 1 到 100000。
- 自动授予狼人身份并清除互斥的感染状态。
- 该管理员命令使用原始经验值，不受正常经验倍率配置影响。
- 达到服务器最高等级后，多余经验不会继续保留。
- 示例：`/werewolf addxp Steve 500`

### `/werewolf setskillpoints <target> <amount>`

设置能力页当前可用点数。

- `<amount>` 范围为 0 到 100。
- 设置的是“当前可用点数”，系统会结合等级已获得点数、已解锁能力消耗和额外点数计算内部值。
- 示例：`/werewolf setskillpoints Steve 20`

### `/werewolf settreepoints <target> <amount>`

设置技能树当前可用点数。

- `<amount>` 范围为 0 到 100。
- 设置的是“当前可用点数”，不是简单增加固定数量。
- 示例：`/werewolf settreepoints Steve 30`

## 技能树命令

### `/werewolf settree <target> <skill> <rank>`

直接设置一项技能树等级，不消耗技能树点数。

- `0` 表示移除该技能。
- 命令会按所选技能的真实最高等级检查参数，不会静默截断。

| `<skill>` | 最高等级 |
|---|---:|
| `damage` | 6 |
| `moon_rend` | 3 |
| `defense` | 5 |
| `speed` | 5 |
| `resistance` | 5 |
| `regeneration` | 5 |
| `jump` | 3 |
| `knockback_resistance` | 2 |
| `fall_resistance` | 2 |
| `lifesteal` | 3 |
| `satiety` | 3 |
| `claw_efficiency` | 3 |
| `hunting_mastery` | 2 |

示例：`/werewolf settree Steve damage 6`

### `/werewolf resettree <target>`

清空目标的全部技能树等级。

- 不重置狼人等级。
- 已投入技能被清除后，相应点数会重新反映到可用点数计算中。
- 示例：`/werewolf resettree Steve`

## 能力命令

### `/werewolf setability <target> <ability> <unlocked>`

直接解锁或移除一项能力，不消耗能力点数。

可用的 `<ability>`：

- `night_vision`
- `summon_wolf_spirit`
- `quadruped_form`
- `moonblood_surge`
- `armored_instinct`
- `hard_life`
- `empty_claw_slot`
- `long_claws`
- `tool_claws`
- `fire_claws`
- `bloody_bite`

移除 `quadruped_form` 时，如果目标正在使用四足形态，会自动返回普通狼人形态并刷新碰撞尺寸。

示例：`/werewolf setability Steve quadruped_form true`

### `/werewolf resetabilities <target>`

移除目标已经解锁的全部能力。

- 不重置狼人等级或技能树。
- 如果目标正在使用四足形态，会自动返回普通狼人形态。
- 示例：`/werewolf resetabilities Steve`

## 状态诊断命令

### `/werewolf status [target]`

显示目标的狼人核心状态。

输出包括：

- 是否为狼人、是否感染、当前形态；
- 是否击败 Alpha；
- 等级、当前经验和升级需求；
- 可用能力点数与技能树点数；
- 当前维度；
- Capability 与玩家持久化镜像是否一致。

玩家执行时可以省略目标：`/werewolf status`。

服务器控制台或命令方块没有自身玩家实体，必须显式提供目标：`/werewolf status Steve`。

`persistedMirror` 的含义：

- `match`：当前服务端 Capability 与持久化镜像一致。
- `DIFF`：两者不一致，需要检查同步、换维度或存档流程。
- `missing`：尚未找到持久化镜像。

## 常用测试流程

快速测试普通狼人：

```text
/werewolf awaken Steve
/werewolf setlevel Steve 20
/werewolf settreepoints Steve 50
/werewolf setskillpoints Steve 30
```

测试四足形态：

```text
/werewolf awaken Steve
/werewolf setability Steve quadruped_form true
```

测试完成后恢复默认状态：

```text
/werewolf reset Steve
```
