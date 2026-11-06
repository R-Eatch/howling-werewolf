# 随从狩猎与交易表

适用 Minecraft 1.21.1 / NeoForge。狩猎表来自模组内置的 JSON，数据包覆盖后以服务器加载的内容为准；商人交易写在代码中。[随从指南](COMPANIONS-zh.md) · [English](COMPANION-TABLES.md)

## 狩猎权重与数量

共 **30 项，总权重 235**。每次抽取从下表独立选择一项；“概率”是单次抽取的概率，并非整趟狩猎的概率。有数量范围时，在范围内等概率取整数。可重复抽中同一项。

| 物品 | 资源 ID | 权重 | 单次概率 | 一次抽中的数量 |
|---|---|---:|---:|---:|
| 煤炭 | `minecraft:coal` | 18 | 7.660% | 1–4 |
| 粗铜 | `minecraft:raw_copper` | 16 | 6.809% | 1–4 |
| 粗铁 | `minecraft:raw_iron` | 14 | 5.957% | 1–3 |
| 粗金 | `minecraft:raw_gold` | 6 | 2.553% | 1–2 |
| 红石粉 | `minecraft:redstone` | 9 | 3.830% | 1–4 |
| 青金石 | `minecraft:lapis_lazuli` | 8 | 3.404% | 1–4 |
| 钻石 | `minecraft:diamond` | 1 | 0.426% | 1 |
| 绿宝石 | `minecraft:emerald` | 2 | 0.851% | 1 |
| 腐肉 | `minecraft:rotten_flesh` | 14 | 5.957% | 1–4 |
| 骨头 | `minecraft:bone` | 14 | 5.957% | 1–4 |
| 线 | `minecraft:string` | 12 | 5.106% | 1–3 |
| 火药 | `minecraft:gunpowder` | 7 | 2.979% | 1–2 |
| 蜘蛛眼 | `minecraft:spider_eye` | 6 | 2.553% | 1–2 |
| 末影珍珠 | `minecraft:ender_pearl` | 2 | 0.851% | 1 |
| 黏液球 | `minecraft:slime_ball` | 4 | 1.702% | 1–2 |
| 皮革 | `minecraft:leather` | 10 | 4.255% | 1–3 |
| 羽毛 | `minecraft:feather` | 10 | 4.255% | 1–4 |
| 生牛肉 | `minecraft:beef` | 10 | 4.255% | 1–3 |
| 生猪排 | `minecraft:porkchop` | 10 | 4.255% | 1–3 |
| 生鸡肉 | `minecraft:chicken` | 10 | 4.255% | 1–3 |
| 生羊肉 | `minecraft:mutton` | 10 | 4.255% | 1–3 |
| 白色羊毛 | `minecraft:white_wool` | 7 | 2.979% | 1–2 |
| 生兔肉 | `minecraft:rabbit` | 4 | 1.702% | 1–2 |
| 兔子皮 | `minecraft:rabbit_hide` | 4 | 1.702% | 1–2 |
| 兔子脚 | `minecraft:rabbit_foot` | 1 | 0.426% | 1 |
| 墨囊 | `minecraft:ink_sac` | 5 | 2.128% | 1–3 |
| 荧光墨囊 | `minecraft:glow_ink_sac` | 2 | 0.851% | 1–2 |
| 生鳕鱼 | `minecraft:cod` | 6 | 2.553% | 1–3 |
| 生鲑鱼 | `minecraft:salmon` | 5 | 2.128% | 1–3 |
| 狼毒花 | `howlingwerewolf:wolfsbane` | 8 | 3.404% | 1–3 |

“狼毒草”和“狼毒花”在本系统中指同一个物品 `howlingwerewolf:wolfsbane`，游戏内名称为“狼毒花”。狩猎与交易均可获取它。表内有少量主世界常见敌对生物材料，不包含徽章、月厄珍珠、下界或 Boss 专属奖励。

## 阶数与时长如何影响狩猎

- 出发时均匀随机保存 36000–72000 游戏刻的计划时长，即 1.5–3 个 MC 日。
- 只在实际结束并能安全归来时抽取。结算时间 T = min(已经过的游戏刻, 计划时长)，最小为零。主人离线或身边没有空间导致的延迟不会增加抽取次数。
- 抽取次数 = floor(T × 阶数系数 ÷ 24000)。阶数使用结算时的生效阶数，即已购买阶数和主人当前“狼群誓约”技能等级的较小值。
- 阶数与时间**只改变抽取次数**，不改变表内各物品的权重或一次抽中的数量，也不额外提高稀有物品权重。没有抢夺、幸运或装备倍率。

| 生效阶数 | 每 MC 日抽取系数 | 1.5 日结束 | 2 日结束 | 3 日结束 | 本次升阶费用 |
|---|---:|---:|---:|---:|---|
| 0 | 4 | 6 | 8 | 12 | 招募初始阶数 |
| 1 | 5 | 7 | 10 | 15 | 2 月厄珍珠 |
| 2 | 6 | 9 | 12 | 18 | 4 月厄珍珠 |
| 3 | 8 | 12 | 16 | 24 | 8 月厄珍珠 |

例如 0 阶狩猎 2 日抽 8 次，3 阶狩猎 2 日抽 16 次。狼毒花单次概率均为 8/235 ≈ 3.404%；抽 n 次至少获得一次狼毒花的概率为 1 − (227/235)^n。立即召回为 0 次抽取，没有保底。

先合并九格随从背包中的现有堆叠，再使用空格；装不下的物品在归来位置掉落。结算成功后立即清除本次狩猎状态，重复召回不会重复发放。

## 两个 MC 日冷却

冷却按**每名随从**独立保存，持续 **48000 游戏刻**，正常 20 TPS 为 40 分钟。

- 自然完成：从计划结束时刻起算两日；若因离线或没有归来空间延迟交付，已经过去的冷却时间仍算数。
- 提前召回：从成功提前结束的时刻起算两日；即使立即召回没有奖励，也进入完整冷却。
- 冷却期间仍可跟随、守护、休息、召回、开背包、升级和改名换肤，只禁止再次出发狩猎。死亡与复活不会清除冷却。
- 使用服务器 `gameTime`，睡眠和 `/time set` 不跳过；服务器关闭时不流逝。禁用随从系统时，狩猎与冷却一同暂停，重新启用后继续。

## 商人交易

交易池共 **9 种**；每位商人均匀抽出 **6 种不同交易**，因此每项被选入该商人列表的概率为 6/9 = 2/3。交易没有狩猎式的权重，不受随从阶数、技能或狩猎时长影响。次数耗尽后不补货；下表的次数是每位商人每项交易的上限。

| 玩家交付 | 玩家获得 | 次数上限 | 配方文件 |
|---|---|---:|---|
| 6 绿宝石 | 4 银锭 | 8 | `silver_for_emeralds.json` |
| 16 铁锭 | 2 银锭 | 8 | `silver_for_iron.json` |
| 12 绿宝石 ＋ 4 狼毒花 | 1 月厄珍珠 | 4 | `pearl_for_emeralds.json` |
| 6 狼毒花 ＋ 1 钻石 | 1 月厄珍珠 | 4 | `pearl_for_wolfsbane.json` |
| 4 银锭 | 2 狼毒花 | 8 | `wolfsbane_for_silver.json` |
| 4 狼毒花 ＋ 2 绿宝石 | 1 狼人药水 | 2 | `werewolf_potion.json` |
| 4 银锭 ＋ 2 绿宝石 | 1 狼毒花药水 | 2 | `wolfsbane_potion.json` |
| 4 绿宝石 | 4 狼毒花 | 8 | `wolfsbane.json` |
| 4 狼毒花 | 2 银锭 | 8 | `silver_for_wolfsbane.json` |

月厄珍珠有两条交易：**6 狼毒花＋1 钻石**或 **12 绿宝石＋4 狼毒花**，每项最多四次。

商人自然存在 **12000 游戏刻**，正常 20 TPS 时为 **10 分钟**，交易不暂停寿命，区块卸载仍会消失。召唤时先问候，再告知只停留十分钟。全部对白见[对白目录](COMPANION-DIALOGUE.md)。

银锭与狼毒花的两条互换交易都是 4 换 2，不能循环套利。

## 数据包入口与代码

- 狩猎表：`data/howlingwerewolf/loot_table/gameplay/companion_hunt.json`。
- 商人交易与抽选：[WerewolfCompanionEntity.java](../src/main/java/com/howlingwerewolf/entity/WerewolfCompanionEntity.java)。
- 阶数、时长与冷却规则：[CompanionRules.java](../src/main/java/com/howlingwerewolf/companion/CompanionRules.java)。
- 结算、背包合并和溢出：[CompanionManager.java](../src/main/java/com/howlingwerewolf/companion/CompanionManager.java)。
