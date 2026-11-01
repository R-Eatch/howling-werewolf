# Howling Werewolf 1.2.0 — Minecraft 26.1.2 / NeoForge

## 简体中文

- 皮肤页右上角新增 **Classic / Modren** 切换按钮。切换后显示对应系列的皮肤列表和三形态预览，继续使用原有的旋转、缩放、装备开关和“应用”流程。
- Classic 保留原四套皮肤。Modren 新增 **Ashen、Black、Chestnut、Pale、Rusty、Snowy、Spotted、Stripped、Woods** 九套皮肤，名称与源贴图一致，默认选中 Ashen。
- Modren 的双足狼人、四足狼、野兽从三个随包分发的 **Blockbench 5.0 bbmodel** 读取模型、骨骼、旋转、UV 和内嵌贴图，不再需要把这套几何体手写为 Java 模型。
- 原始三个文件不含动画关键帧。本版将现有行走、攻击、下蹲、乘坐、游泳、滑翔和持物动作映射到新骨骼，保留模型初始姿势；第一人称使用新模型本身的手臂与爪子。
- **Modren 野兽冲刺时四肢着地奔跑**，包含身体前倾、对角步态、抬爪与落地、进入/退出跑姿的过渡。冲刺攻击抬起攻击侧前爪；使用物品、游泳、滑翔和乘坐时优先保留对应动作。
- Modren 镜头按姿势调整：野兽站立 2.55 格、下蹲 1.8 格、四肢冲刺 1.45 格，四足狼 0.6 格；游泳和滑翔使用低位镜头。客户端与服务端使用同一眼高规则，保留原版镜头平滑。碰撞尺寸不因模型系列改变。
- 武器固定在新模型掌心，保持原版物品大小；修正导入归一化缩放影响武器的问题。长矛使用原版举矛和突刺方向，重锤与副手攻击采用对应原版挥击。
- Modren 双足狼人的弓、弩（装填/持弩）、望远镜、三叉戟和长矛使用 Steve 的原版双臂姿势与物品朝向，并映射到新模型的实际掌心。
- 待机尾巴自然摆动，三节分别使用延迟相位和小幅起伏；移动、下蹲和冲刺时调整幅度，避免整条尾巴僵硬地同相摆动。
- 护甲袖子、裤腿与靴子按肘、膝和足部分段，保留原有 UV、染色、附魔光效和锻造纹饰；鞘翅跟随新模型背部，移除重复下蹲偏移。装备显示继续遵循原有形态、技能和配置限制。
- 模型系列和皮肤由服务端逐玩家保存，并同步给本人及观察者。A 选择 Modren、B 选择 Classic 时，双方看到的 A 仍为 Modren、B 仍为 Classic。死亡、换维度、治愈及等级重置保留外观；旧存档默认 Classic。
- 自定义 PNG 皮肤支持 `"model": "classic"` 或 `"model": "modren"`，模板导出跟随当前系列。缺失自定义资源包时分别回退 Classic Adrian / Modren Ashen。内置九套无需额外资源包。
- Alpha、野生狼人和 Classic 的模型与动作保持原样。既有战斗、成长和碰撞规则保持原样。

服务端与客户端均需安装 **1.2.0**。Minecraft 26.1.2、NeoForge 26.1.2.122、Java 25 基线不变。本地编译与独立动作/数据校验完成；**实机及双客户端联机测试由维护者进行，尚未标记完成**。

## English

- Add a **Classic / Modren** button in the upper-right corner of Skins. Each set has its own coat list and three-form previews, with the existing rotation, zoom, equipment toggle and Apply flow.
- Retain the four Classic coats. Add nine Modren coats named after the source textures: **Ashen, Black, Chestnut, Pale, Rusty, Snowy, Spotted, Stripped, Woods**. Ashen is the initial Modren selection.
- Load the Modren Werewolf, Quadruped Wolf and Beast geometry, bone hierarchy, rotations, UVs and embedded PNGs directly from three bundled **Blockbench 5.0 bbmodel** projects.
- The supplied projects contain no animation keyframes. Retarget established movement, attacks, crouching, riding, swimming, flight and item-use actions while preserving the authored rest poses. First-person claws use the imported arms and paws.
- Make **Modren Beast sprint on all fours**, with a forward body pose, diagonal gait, paw lift/ground contact and transitions. Sprint attacks lift the attacking front paw. Item use, swimming, flight and riding retain their corresponding actions.
- Match Modren eye height to the pose: Beast standing 2.55 blocks, crouching 1.8, quadrupedal sprinting 1.45; Quadruped Wolf 0.6. Use a low eye origin when swimming/flying. Share the rules between client and server, retain native camera smoothing, and preserve collision dimensions across model sets.
- Anchor items at the imported palms without inheriting body normalization scale. Keep native spear raising/thrust direction and mace/off-hand swing behavior.
- Use Steve's native coordinated arm poses and item orientation for Modren Werewolf bows, charging/holding crossbows, spyglasses, tridents and spears, anchored to the imported palms.
- Add natural idle tail sway with delayed phases across three sections and small vertical motion. Adjust the amplitude for movement, crouching and sprinting.
- Split armor sleeves, leggings and boots across the imported joints while retaining UVs, dye, glint and trim rendering. Attach elytra to the back and remove duplicate crouch offsets. Existing equipment/skill/config restrictions still apply.
- Persist and synchronize each player's model and coat independently. Both clients render player A as Modren and player B as Classic when those are their saved choices. Preserve preferences across player lifecycle events; legacy saves default to Classic.
- Add `"model": "classic"` / `"model": "modren"` to custom PNG skin definitions. Template export follows the selected model set. Missing custom packs fall back to Classic Adrian / Modren Ashen. Built-in Modren coats need no extra pack.
- Keep Alpha, feral werewolves and Classic models/animations as before, with existing combat, progression and collision rules.

Server and clients must all run **1.2.0**. Minecraft 26.1.2, NeoForge 26.1.2.122 and Java 25 remain the baseline. Compilation and independent animation/data checks are complete; **in-game and two-client multiplayer testing remain with the maintainer**.
