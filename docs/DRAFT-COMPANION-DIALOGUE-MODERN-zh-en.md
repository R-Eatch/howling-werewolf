# 商人与随从现代口吻文案草案（中英）

状态：**仅供审阅，未实装**。2026-11-03。不会写入语言 JSON，不会改变现有 NPC 行为。

目标是让角色像可靠的行动伙伴，说话简短、自然、贴近当下发生的事。世界背景和仪式介绍仍可保留文学性，对话减少誓约、月路、此身、吾主等古典表达。

中文默认称呼仍遵从玩家要求使用“主人”；表中的 {称呼} 替换为玩家在总控中保存的称呼。英文默认称呼建议改为 boss，表中的 {address} 同样使用玩家设置；这也是待确认提案，当前游戏英文默认仍为 my lord。商人尚未招募，继续称兄弟 / brother。

这些话只反馈当前动作或状态，不暗示已加入箱子整理、额外寻路判断、危险预警或新奖励。闲聊不按天气和时段声称未经检查的事实。

## 完整 45 条对白提案

同一场景的变体随机选一句；保留现有触发条件、间隔和仅归属玩家可见的规则。普通召回和提前结束狩猎分别使用各自的归来文本。

| 场景 | 翻译键尾部 | 中文提案 | English proposal |
|---|---|---|---|
| 商人问候 | greeting | 嘿，兄弟，找我有什么事？ | Hey, brother. What do you need? |
| 商人停留期限 | stay_notice | 兄弟，我会在这儿待十分钟。需要什么就来看看。 | I'll be here for ten minutes, brother. Take a look if you need anything. |
| 交易完成 | traded | 交易愉快，下次再来。 | Pleasure doing business. See you next time. |
| 商人告别 | farewell.0 | 兄弟，我该走了。下次见。 | Time for me to head out, brother. See you around. |
| 商人告别 | farewell.1 | 今天先到这儿，有需要再叫我。 | That's it for today. Call me if you need anything. |
| 招募成功 | recruited | {称呼}，以后我跟您一起行动。需要帮忙就告诉我。 | {address}, I'll stick with you from now on. Just tell me what you need. |
| 跟随 | follow.0 | {称呼}，我跟着您。 | I'm right behind you, {address}. |
| 跟随 | follow.1 | 您带路就好，{称呼}。 | Lead the way, {address}. |
| 跟随 | follow.2 | 我准备好了，{称呼}，出发吧。 | Ready when you are, {address}. |
| 守护 | guard.0 | {称呼}，我来掩护您。 | I've got your back, {address}. |
| 守护 | guard.1 | 我会留意周围，{称呼}。 | I'll keep an eye out, {address}. |
| 守护 | guard.2 | 有情况我会帮忙，{称呼}。 | I'll back you up if trouble starts, {address}. |
| 休息 | rest.0 | {称呼}，我在这儿等您。 | I'll wait here, {address}. |
| 休息 | rest.1 | 收到，{称呼}。需要时叫我。 | Got it, {address}. Call when you need me. |
| 休息 | rest.2 | 我先休息一下，{称呼}，随时可以出发。 | Taking a breather, {address}. I'll be ready. |
| 出发狩猎 | hunting | {称呼}，我出去打猎了。回来给您带些收获。 | I'm heading out to hunt, {address}. I'll bring back what I find. |
| 狩猎归来 | returned | {称呼}，我回来了。看看这次带回了什么。 | I'm back, {address}. Take a look at what I found. |
| 普通召回 | recalled | {称呼}，我到了。 | I'm here, {address}. |
| 背包 | inventory.0 | {称呼}，东西都在这儿，您看看。 | Here's what I'm carrying, {address}. |
| 背包 | inventory.1 | 需要什么就拿，{称呼}。 | Take what you need, {address}. |
| 升级 | upgrade.0 | {称呼}，感觉更有劲了。 | Feeling stronger already, {address}. |
| 升级 | upgrade.1 | 准备好了，{称呼}，这次能多帮您一些。 | Ready to go, {address}. I can do more now. |
| 改名 | renamed.0 | 记住了，{称呼}，以后就叫我这个名字。 | Got it, {address}. That's my name from now on. |
| 改名 | renamed.1 | 这个名字不错，{称呼}，我收下了。 | I like it, {address}. I'll use that name. |
| 换肤 | reskinned.0 | 换好了，{称呼}。看着还行吗？ | All changed, {address}. How do I look? |
| 换肤 | reskinned.1 | 新造型准备好了，{称呼}。 | New look's ready, {address}. |
| 迎敌 | combat.0 | {称呼}，小心，我来处理。 | Watch out, {address}. I've got this. |
| 迎敌 | combat.1 | 我来帮您，{称呼}。 | On my way, {address}. |
| 迎敌 | combat.2 | 别担心，{称呼}，我在。 | You're not alone, {address}. |
| 重伤 | wounded.0 | {称呼}，我伤得有点重，需要缓一下。 | I'm hurt, {address}. I need a moment. |
| 重伤 | wounded.1 | 还能撑住，{称呼}，但得小心一点。 | I'm still standing, {address}, but we should be careful. |
| 胜利 | victory.0 | {称呼}，解决了。 | That's dealt with, {address}. |
| 胜利 | victory.1 | 周围暂时安全了，{称呼}。 | Looks clear for now, {address}. |
| 阵亡 | fallen.0 | {称呼}，这次得先靠您了…… | You'll have to take it from here, {address}... |
| 阵亡 | fallen.1 | 抱歉，{称呼}，我撑不住了…… | Sorry, {address}. I can't keep going... |
| 复活 | revived | {称呼}，谢谢您把我带回来。我又能帮忙了。 | Thanks for bringing me back, {address}. I'm ready to help again. |
| 跟随闲聊 | idle_follow.0 | {称呼}，前面想去哪里？ | Where are we headed, {address}? |
| 跟随闲聊 | idle_follow.1 | 慢慢来，{称呼}，我跟得上。 | Take your time, {address}. I'm keeping up. |
| 跟随闲聊 | idle_follow.2 | 有我陪着，路上也没那么无聊吧，{称呼}。 | At least you've got some company, {address}. |
| 守护闲聊 | idle_guard.0 | {称呼}，附近暂时没什么动静。 | Nothing unusual nearby, {address}. |
| 守护闲聊 | idle_guard.1 | 周围我看着，{称呼}，您忙您的。 | I'll watch the area, {address}. You do your thing. |
| 守护闲聊 | idle_guard.2 | 目前一切正常，{称呼}。 | All quiet for now, {address}. |
| 休息闲聊 | idle_rest.0 | {称呼}，我就在这儿，有事叫我。 | I'll be right here, {address}, if you need me. |
| 休息闲聊 | idle_rest.1 | 休息得差不多了，{称呼}，随时可以走。 | Feeling rested, {address}. Ready whenever you are. |
| 休息闲聊 | idle_rest.2 | 难得安静一会儿，{称呼}。 | Nice to have a quiet moment, {address}. |

## 界面短文案提案

主界面保留控件标签和实际状态，功能介绍继续集中在教程；不会把长说明重新放回主面板。

| 用途 | 中文提案 | English proposal |
|---|---|---|
| 狼人菜单入口 | 随从 | Pack |
| 总控标题 | 随从管理 | Companions |
| 单个随从标题 | 随从设置 | Companion settings |
| 右上角教程 | 教程 | Guide |
| 返回总控 | 全部随从 | All companions |
| 跟随 | 跟随 | Follow |
| 守护 | 守护 | Guard |
| 休息 | 等待 | Wait |
| 狩猎 | 外出狩猎 | Go hunting |
| 召回 | 叫回身边 | Recall |
| 背包 | 背包 | Inventory |
| 升级 | 升级（费用） | Upgrade (cost) |
| 复活 | 复活 | Revive |
| 名称与皮肤 | 名字和外观 | Name & appearance |
| 自定义称呼 | 对您的称呼 | How they address you |
| 保存称呼 | 保存 | Save |
| 恢复默认称呼 | 恢复默认 | Reset |
| 无随从 | 还没有随从 | No companions yet |
| 狩猎状态 | 预计 %s 个 MC 日后回来 | Back in about %s MC days |
| 狩猎冷却 | 再休整 %s 个 MC 日可狩猎 | Ready to hunt in %s MC days |
| 暂停状态 | 暂时无法行动 | Currently inactive |
| 改名限制 | 名字最多 32 个字符，留空恢复原名。 | Names can have up to 32 characters. Leave blank to restore the original name. |
| 称呼限制 | 称呼最多 16 个字符。 | Use up to 16 characters for the address. |
| 没有安全空间 | 附近没有足够安全的落脚点，换个位置再试试。 | There isn't a safe place nearby. Move a little and try again. |
| 服务器关闭功能 | 这个服务器暂未启用随从功能。 | Companions aren't enabled on this server. |

## 教程九个章节的现代口吻提案

下面保留当前玩法和数值，只调整说明方式。第一人称对白和第三人称教程分开，不让帮助文本扮演随从。

| 章节 | 中文提案 | English proposal |
|---|---|---|
| 召唤仪式 | 先在同一高度放好五座祭坛：中央一座，四个方向各三格处一座。外坛各放一朵狼毒花，中间放一颗月厄珍珠，再空手右键中央祭坛。商人会尽量从远处走来；仪式失败会退还材料。Alpha 的配方仍是中央徽章、外坛珍珠。 | Place five altars on the same level: one in the center and one three blocks away in each cardinal direction. Put one Wolfsbane on each outer altar and a Moonbane Pearl in the center, then right-click the center with an empty hand. The visitor will try to walk over from a distance. Failed rituals return the offerings. The Alpha recipe still uses a central Badge and outer Pearls. |
| 商人与交易 | 商人会停留十分钟，交易时也会继续计时，区块卸载后会离开。每次提供六种交易，次数有限，不会补货。月厄珍珠可以用六朵狼毒花和一颗钻石，或十二颗绿宝石和四朵狼毒花兑换。招募后他会停止交易。 | The merchant stays for ten minutes, including time spent trading, and leaves if his chunk unloads. He offers six recipes with limited uses and no restocking. A Moonbane Pearl costs six Wolfsbane and one Diamond, or twelve Emeralds and four Wolfsbane. Recruitment ends his trading role. |
| 招募与名额 | 成为狼人并学习狼群誓约后，拿着狼人徽章右键商人，确认即可招募。普通狼人最多带一名随从；通过 Alpha 试炼后，名额随技能提高，最多三名。狩猎中和阵亡的随从也占名额。 | Become a werewolf and learn Pack Oath, then right-click the merchant with a Werewolf Badge and confirm recruitment. Capacity is one before the Alpha Trial. Afterward, higher skill ranks allow up to three companions. Hunting and fallen companions still occupy their slots. |
| 跟随与守护 | 跟随只负责陪您行动，即使被打也不会还手。怪物不会主动锁定跟随中的随从，但误伤和环境伤害仍会生效。危险地区建议优先用守护，让随从帮您战斗。休息会让他留在原地等待。守护仍排除苦力怕和友方等受保护目标。 | Follow keeps your companion with you without fighting back, even when attacked. Monsters won't actively target him in this mode, though stray attacks and hazards can still hurt. Use Guard in dangerous areas for combat support. Rest makes him wait in place. Guard still avoids creepers and protected allies. |
| 九格背包 | 右键自己的随从进入设置，再点背包；潜行右键可以直接打开。随从需要存活、在同一维度且离您不超过八格。物品只能手动放入，他不会捡地上的东西。狩猎带回的物品装不下时会掉在身边。 | Right-click your companion for controls, then choose Inventory, or sneak-right-click to open it directly. He must be alive, in the same dimension and within eight blocks. Add items manually; he doesn't collect ground drops. Hunt loot that doesn't fit drops beside him when he returns. |
| 形象与称呼 | 在预览中拖动旋转、滚轮缩放，选好皮肤后点应用。名字最多三十二个字符，留空可恢复原名。模型样式由服务器世界设置决定。总控中的称呼设置会应用给您的全部随从，最多十六个字符。随从说的话只有您能看到。 | Drag the preview to rotate and use the wheel to zoom. Apply a skin when you're happy with it. Names allow up to 32 characters; leave blank to restore the original. The server world chooses the model family. The overview's address setting applies to all your companions and allows up to 16 characters. Only you receive their dialogue. |
| 狩猎与召回 | 狩猎会离队 1.5–3 个游戏日，期间只能召回。奖励在归来时按实际时长和阶数结算。提前叫回也能结算，但立即召回没有奖励，而且仍需休整三个游戏日才能再次狩猎。正常召回可以跨维度和未加载区块。 | Hunts last 1.5–3 Minecraft days. Recall is the only available order while away. Loot is drawn on return based on elapsed time and tier. Early recall still settles rewards, but immediate recall gives none and still starts the three-day cooldown. Recall works across dimensions and unloaded chunks. |
| 升级 | 新随从从零阶开始，依次花二、四、八颗月厄珍珠升到一、二、三阶，最高阶数受狼群誓约限制。升级会提高生命、攻击、减伤、恢复和狩猎抽取次数；它不会立刻回满生命，也不会单独提高稀有物品的抽中概率。 | Recruits start at tier zero. Spend two, four and eight Moonbane Pearls to reach tiers one, two and three, within the Pack Oath limit. Upgrades improve health, attack, damage reduction, recovery and hunt roll count. They don't instantly heal to full or change any item's individual drop probability. |
| 复活与设置 | 随从阵亡后，名字、皮肤、阶数和背包都会保留。准备一枚狼人徽章，在管理页点复活，就能让他满血回到身边。没有安全落脚点时不会扣徽章。默认按 C 打开总控，Esc 返回；服务器关闭随从功能时，档案和物品会保留，狩猎计时暂停。 | Fallen companions keep their names, skins, tiers and inventory. Spend a Werewolf Badge in management to revive one beside you at full health. No Badge is charged without a safe return spot. C opens the overview by default and Escape goes back. When a server disables companions, records and items remain and hunt timers pause. |

## 实装前再确认的范围

- 是否采用英文默认 boss，还是继续使用当前 my lord 或其他称呼。
- 是否将“休息”按钮改名为“等待”；这里只改提案，没有改状态枚举或现有按钮。
- 文案不新增危险检测、天气检测或物资承诺。若未来需要条件对白，再单独定义触发逻辑。
- 启用此稿时，应逐键核对中英文占位符、上下文、对白节流与仅主人可见规则；本轮没有执行该语言包替换。
