# 配置文件指南

[English](CONFIG.md) · [返回 README](../README-zh.md)

本页适用于 1.3.7 的全部四个版本：1.20.1 Forge、1.21.1 NeoForge、1.21.11 Fabric 和 26.1.2 NeoForge，各版本的配置项和默认值相同。

## 文件位置

| 文件 | Forge / NeoForge | Fabric | 由谁决定 |
|---|---|---|---|
| 通用配置 | `config/howlingwerewolf-common.toml` | `config/howlingwerewolf-common.json` | 服务端（单人游戏为内置服务端） |
| 客户端配置 | `config/howlingwerewolf-client.toml` | `config/howlingwerewolf-client.json` | 每位玩家自己的客户端 |

- 首次启动游戏后自动生成文件，缺少的项会按默认值补上。
- TOML 按 `[gameplay]`、`[worldGeneration]` 分节，每项上方带英文说明。
- Fabric 的 JSON 没有分节，键名直接带前缀，例如 `"gameplay.enableCompanions": true`。
- 超出范围或格式不对的值不会生效，游戏会改用合法值；Fabric 的文件整体无法解析时，全部使用默认值，并在日志里报错。

## 玩法（gameplay）

| 键 | 默认值 | 范围 | 说明 |
|---|---|---|---|
| `enableCompanions` | `true` | true / false | 商人、招募和随从指令的总开关。关闭后已有随从会暂时收起，背包物品不会删除，狩猎和冷却暂停；重新开启后恢复。 |
| `enableCompanionSorting` | `true` | true / false | 单独开关整理功能。关闭只停止整理，不删除已登记的容器，也不移动物品。 |
| `companionSortingReach` | `32` | 3–64 | 随从走不到跟前时，仍可隔空整理的最远距离（格）。能走到时随从总会走过去；设为 3 表示走不到就跳过。 |
| `npcModelSet` | `"modern"` | `modern` / `classic` | 野生狼人、刷怪蛋狼人、Alpha 及其召唤狼群使用的模型。`modern`：Alpha 与狼群用 Ashen 毛色，野生狼人随机一种毛色并保存；`classic`：全部用 Adrian。配置重载后已有实体随之更新；玩家自己选的皮肤不受影响。 |
| `wolfInfectionChance` | `0.10` | 0.0–1.0 | 未驯服的原版狼伤害人类玩家时的感染几率。 |
| `feralWerewolfInfectionChance` | `0.20` | 0.0–1.0 | 野生失控狼人伤害人类玩家时的感染几率。Alpha 召唤的狼人不会感染。 |
| `werewolfExperienceGainMultiplier` | `1.0` | 0.0–100.0 | 狼人经验的最终倍率，不影响管理员经验命令。 |
| `maxWerewolfLevel` | `20` | 1–25 | 狼人等级上限。调低后，存档里超出上限的等级会在加载时降到上限。 |
| `beastModeVoidDamage` | `false` | true / false | 为 `true` 时，野兽形态的近战改用虚空伤害类型；为 `false` 时与普通狼人攻击相同。 |
| `alphaTrialDamageFrequencyLimitTicks` | `10` | 0 起 | Alpha 试炼 Boss 两次受伤之间的最短间隔（游戏刻）。0 表示不额外限制。无来源且无视无敌时间的伤害、玩家野兽形态造成的虚空伤害不受此限。 |
| `preserveAlphaBadgeOnRevival` | `false` | true / false | 为 `true` 时，Alpha 狼人徽章抵挡致命伤害后不被消耗，且不看伤害类型（包括 `/kill` 和虚空）；掉出世界被救下的玩家会回到最近的安全地面，其次是重生点（不消耗重生锚），再次是世界出生点。为 `false` 时消耗一枚，复活效果与不死图腾相同。 |
| `showWerewolfEquipment` | `true` | true / false | 是否在普通狼人形态的模型上画出护甲和鞘翅。手持物品总会显示。设为 `false` 会在本机隐藏所有狼人的装备，优先于皮肤页的“装备：显示”。 |
| `enableWerewolfAutoStep` | `true` | true / false | 自动跨越台阶：四足狼 1.0 格、狼人 1.5 格、野兽 2.0 格。`false` 恢复原版高度。 |
| `preventWerewolfFullMoonSleep` | `false` | true / false | 为 `true` 时，拥有狼人血脉的玩家在主世界满月之夜（黄昏 12000 刻起到天亮）无法入睡，不能靠睡觉跳过满月。 |

## 世界生成（worldGeneration）

| 键 | 默认值 | 范围 | 说明 |
|---|---|---|---|
| `generateSilverOre` | `true` | true / false | 在新生成的区块中生成银矿，不改动已有区块。 |
| `generateWolfsbane` | `true` | true / false | 在新生成的区块中生成狼毒花，不改动已有区块。 |
| `hunterSpawnWeightMultiplier` | `1.0` | 0.0–10.0 | 猎人自然生成权重（默认 50）的倍率；0 关闭自然生成。这是相对权重，不是生成概率。修改后需重新进入世界。 |
| `feralWerewolfSpawnWeightMultiplier` | `1.0` | 0.0–10.0 | 失控狼人自然生成权重（默认 200）的倍率；0 关闭自然生成。满月、夜晚和亮度规则仍然生效。修改后需重新进入世界。 |
| `wolfsbaneGenerationWeight` | `100` | 0–200 | 狼毒花生成频率。0 不生成；100 为默认频率（约每 8 个针叶林区块或 22 个森林区块尝试一次）；200 为两倍。 |

## 客户端（interface）

| 键 | 默认值 | 说明 |
|---|---|---|
| `progressionNightfall` | `false` | K 进阶界面使用深色“夜幕”画布；`false` 为白色“月白”。在界面右上角的月牙上切换时会自动保存。Fabric 中键名为 `interface.progressionNightfall`。 |

## 修改建议

- 世界生成类设置只影响之后新生成的区块；涉及世界生成或已有存档的修改，请先备份重要世界。
- 多人服务器以服务端的通用配置为准，客户端配置只影响自己的界面。
- 管理员命令见[管理员命令指南](ADMIN_COMMANDS-zh.md)。
