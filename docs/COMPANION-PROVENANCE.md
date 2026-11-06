# 随从系统架构来源核查

核查对象为本项目 Minecraft 26.1.2 的 1.2.3 随从框架及 1.2.4 修订。结论：**参考 Vampirism 的架构思路，独立实现本模组的业务代码；在本次检查范围内未发现复制其业务代码或美术资源。** 不能将“完全没有参考”作为本模块的来源描述。

## 参考范围与实现差异

按开发需求调研了 Team Lapen / Vampirism 的以下开源文件，主要借鉴“随从资料绑定主人、资料和世界实体分离”的组织方式：

- [PlayerMinionController.java](https://github.com/TeamLapen/Vampirism/blob/version/1.21/latest/src/main/java/de/teamlapen/vampirism/entity/minion/management/PlayerMinionController.java)
- [MinionData.java](https://github.com/TeamLapen/Vampirism/blob/version/1.21/latest/src/main/java/de/teamlapen/vampirism/entity/minion/management/MinionData.java)
- [MinionInventory.java](https://github.com/TeamLapen/Vampirism/blob/version/1.21/latest/src/main/java/de/teamlapen/vampirism/entity/minion/MinionInventory.java)

| 项目 | 所查 Vampirism 实现 | Howling Werewolf 实现 |
|---|---|---|
| 身份与主人绑定 | 主人控制器内按槽位索引组织 MinionInfo，并使用认领令牌 | 服务端 SavedData 中以随从 UUID 为主键，每条记录带主人 UUID；不以名称或世界实体 ID 作为身份 |
| 实体生命周期 | 实体持久化并按槽位、令牌签入签出 | 世界实体为不单独保存的临时呈现；含乘客、区块卸载与传送门限制，必要时按档案重建 |
| 重复实体防护 | 随机整数令牌与槽位校验 | 持久代次与当前实体对象同时校验；替换生成成功后才切换认领 |
| 行为组织 | IMinionTask 注册与前台/后台任务描述 | FOLLOW / GUARD / REST / HUNT / DEAD 枚举，绝对游戏刻截止时间、独立狩猎冷却和服务器暂停计时 |
| 背包 | 专用背包与手、护甲等分区，开放格数受规则控制 | 档案唯一持有九格 SimpleContainer，原版 ChestMenu 同步，不拾取掉落物 |
| 成长与玩法 | 吸血鬼阵营、领主和任务系统 | 狼群誓约、Alpha 条件、0–3 阶、徽章招募复活、五祭坛仪式及原版数据包交易 |
| 客户端同步 | 上游自己的随从控制与任务界面 | 服务端校验动作，主人专属概要、原版九格菜单和附近实体外观追踪分别同步 |

使用服务器级 SavedData 持久保存按主人索引的资料，是本项目对多人游戏、跨维度和死亡换实体问题的具体选择；没有把完整资料复制进每个玩家实体，也没有让客户端决定物品、奖励或归属。SavedData 属于 [NeoForge 官方存档 API](https://docs.neoforged.net/docs/datastorage/saveddata/)。

## 检查依据与边界

- 检查本地随从 core/entity 的身份、状态、背包、生成、归来结算、同步和卸载逻辑，并与上述三个上游文件比较。
- 源码与构建文件没有 Vampirism 的包引用或依赖；未复制上游类、任务注册表、配置和界面资源。名称、对话及仪式为本模组独立实现。
- 去除注释和导入后比较连续相同词法 token，所查三个文件与本模块的最长连续相同片段分别为 11、11、8 个 token，均是 ArrayList 初始化、for 循环开头或标准 Override/setChanged 声明等普通 Java 写法。这仅支持人工审查，不作为独立的“原创性百分比”或绝对证明。
- 成品中现有 PNG 与 bbmodel 已与改版前的 1.2.3 基线逐字节比较，保持一致；模型/材质仍署名 R_Eatch 与 thewolfulti。

本次结论覆盖实际调研的上游文件和本地随从模块，不声称审查过 Vampirism 全库。这里回答代码来源与实现关系，不把有限技术核查等同于对“抄袭”一词的绝对法律裁定。

## 上游署名与许可证

参考项目：**Vampirism / Team Lapen**，代码许可证见上游的 [CODE_LICENSE（LGPL v3）](https://github.com/TeamLapen/Vampirism/blob/version/1.21/latest/CODE_LICENSE)。本次没有引入其受许可代码；本项目的原有代码与美术许可证维持不变。若未来实际复制上游代码，应重新按当时的代码许可证处理，不能仅凭“参考”署名替代许可要求。

English: The companion module references Vampirism's separation of owner-bound records and live minions. Its UUID records, non-persistent projections, generation checks, native nine-slot inventory, enum modes and timed hunts were independently implemented for this mod. The scoped review of the three consulted source files and the local companion module found no copied business logic or assets. This is a bounded technical provenance assessment.
