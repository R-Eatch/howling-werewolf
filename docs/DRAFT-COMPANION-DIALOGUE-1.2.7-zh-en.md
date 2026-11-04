# 随从对白修订稿：1.2.7（中英，待审阅）

状态：**仅供审阅，尚未同步到游戏**。游戏里的对白仍是下表“现行”一栏。你确认后，我再把定稿写入语言文件。

## 这版的写法

目标是稍微现代一点，同时留住中世纪的代入感。

- **句子变短。** 一句话说一件事，读起来像在开口说话。
- **用词变直白。** “此身”“旨意”“誓死”这类书面腔减少，换成“这条命”“遵命”“我陪您打”。
- **世界观词汇留下。** 月亮、爪子、狼群、誓言、猎物、兄弟都还在，所以听起来仍是月下的狼人，不是现代同事。
- **敬称保留。** 中文继续用“您”和可自定义的称呼（默认“主人”），英文继续用 `my lord`。这次没有采用“收到”“老大”“boss”这类现代口语，这是和上一版 [现代口吻草稿](DRAFT-COMPANION-DIALOGUE-MODERN-zh-en.md) 最大的区别。
- **兄弟之间更随意。** 商人对玩家以“兄弟”相称，比随从对主人的语气松一些。

`%s` 是玩家设置的称呼，位置可以调整，但每句的占位符数量不变。所有翻译键沿用现有名称。

## 商人

| 键 | 现行中文 | 修订中文 | Current English | Revised English |
|---|---|---|---|---|
| `greeting` | 我的狼人兄弟，你好，叫我来什么事情？ | 月安，兄弟。唤我前来，有什么要紧事？ | Greetings, my werewolf brother. What brings you to call on me? | Well met, brother. What brings you to call on me? |
| `stay_notice` | 兄弟，月路催我前行，我只能在此停留十分钟；若有所需，便趁此时相商。 | 兄弟，我只能留十分钟，月路还在等我。要什么，趁早开口。 | Brother, the moonlit road calls. I can stay for only ten minutes; let us speak while there is time. | I can stay but ten minutes, brother; the moonlit road is waiting. If you need anything, speak up. |
| `traded` | 交易愉快 | 公平交易。愿你猎运亨通。 | A pleasure doing business. | A fair trade. Good hunting to you. |
| `farewell.0` | 兄弟，月路将尽，改日再会。 | 月路还长，兄弟，我该动身了。 | Brother, the moonlit path calls. Until we meet again. | The moonlit road is long, brother. Time I was off. |
| `farewell.1` | 兄弟，今日暂且作别，有事再唤我前来。 | 今天就到这里。兄弟，有事再唤我。 | Our business is done for now, brother. Call when you need me. | That is all for today, brother. Call me when you need me. |

## 招募与重要时刻

| 键 | 现行中文 | 修订中文 | Current English | Revised English |
|---|---|---|---|---|
| `recruited` | 谨遵%s的旨意，我将誓死跟随于您。 | %s，从今往后我随您同行。这是我的誓言。 | As %s commands, I swear to follow you unto death. | From this day I walk with you, %s. That is my oath. |
| `command` | %s，谨遵您的旨意。 | 遵命，%s。 | As you command, %s. | As you say, %s. |
| `hunting` | %s，谨遵您的旨意，我将为您狩猎日与月。 | 遵命，%s。我这就出发，等我带猎物回来。 | As you command, %s. I shall hunt the sun and moon for you. | As you say, %s. I will be back with the kill. |
| `recalled` | %s，我已回到您的身边。 | %s，我回来了，就在您身边。 | I am at your side, %s. | I am back at your side, %s. |
| `returned` | %s，我携狩猎所得归来。 | %s，猎物都带回来了。 | %s, I return with the spoils of the hunt. | %s, I am back, and the hunt was good. |
| `revived` | 感谢您赐予我第二次生命，%s。 | 多谢您，%s。这条命，还是您的。 | Thank you for granting me another life, %s. | My thanks, %s. This life is yours to command. |

## 指令回应

| 键 | 现行中文 | 修订中文 | Current English | Revised English |
|---|---|---|---|---|
| `follow.0` | %s，我会紧随您的脚步。 | %s，我跟在您身后。 | %s, I shall follow in your footsteps. | I am right behind you, %s. |
| `follow.1` | 请前行，%s，我就在您身后。 | 您带路，%s，我跟得上。 | Lead on, %s. I am right behind you. | Lead on, %s. I will keep pace. |
| `follow.2` | 以月为誓，%s，我将与您同行。 | 走吧，%s。月亮照着这条路。 | By the moon, %s, I shall walk with you. | Let us go, %s. The moon lights our road. |
| `guard.0` | %s，您的敌人亦是我的猎物。 | %s，敌人交给我。 | %s, your enemies are my quarry. | Leave your enemies to me, %s. |
| `guard.1` | 利爪已备，%s，我会守住您的身侧。 | 爪子磨好了，%s，我守在您身边。 | My claws are ready, %s. I shall guard your side. | My claws are sharp, %s. I am at your side. |
| `guard.2` | 只要此身尚存，%s便不会孤身迎敌。 | 有我在，%s，谁也别想近您的身。 | While I draw breath, %s, you shall not stand alone. | While I stand, %s, no one reaches you. |
| `rest.0` | 谨遵您的旨意，%s，我将在此等候。 | 遵命，%s。我就在这里等您。 | As you command, %s. I shall wait here. | As you say, %s. I will wait here. |
| `rest.1` | %s，我将在这片月光下等待您的召唤。 | 我在这月光下等您回来，%s。 | %s, I shall await your call beneath this moon. | I will wait beneath this moon, %s. |
| `rest.2` | %s，我留守此处，静候您的下一道命令。 | 我留下看着这里，%s。需要时唤我一声。 | I shall stay, %s. Give the word when you need me. | I will hold this spot, %s. Call when you need me. |
| `sort` | %s，我会将这些箱中的物品归整妥当。 | 遵命，%s。这些箱子交给我。 | %s, I shall put these supplies in order. | As you say, %s. Leave these chests to me. |
| `sorted` | %s，这批物资已整理妥当。 | %s，都归置好了。 | %s, these supplies have been put in order. | %s, everything is in its place. |

## 背包、升级与外观

| 键 | 现行中文 | 修订中文 | Current English | Revised English |
|---|---|---|---|---|
| `inventory.0` | %s，我所携之物皆听您调遣。 | %s，我带的东西都在这里，您随意。 | %s, all that I carry is yours to command. | All I carry is here, %s. Help yourself. |
| `inventory.1` | 所得皆在此处，%s，请取您所需。 | 东西都在，%s，需要什么就拿。 | The spoils are here, %s. Take what you need. | It is all here, %s. Take what you need. |
| `upgrade.0` | 月光磨砺了我的利爪，%s，誓约始终如一。 | 月光又磨利了我的爪子，%s。 | The moon strengthens my claws, %s. My oath endures. | The moon has sharpened my claws, %s. |
| `upgrade.1` | 珍珠之力已入此身，%s，我定不负所托。 | 这珍珠的力量不小，%s。您放心看我的。 | I feel the strength of the pearl, %s. I shall serve you well. | There is real power in that pearl, %s. Watch what I can do now. |
| `renamed.0` | %s，我将铭记您赐予的名字。 | 好名字，%s。我记下了。 | %s, I shall bear the name you have bestowed. | A good name, %s. I will wear it well. |
| `renamed.1` | 谨遵您的意愿，%s，此名将见证我的誓约。 | 从今往后我就用这个名字，%s。 | As you wish, %s. This name shall witness my oath. | That name is mine now, %s. |
| `reskinned.0` | 毛色虽易，誓约不改，%s。 | 毛色换了，%s，誓言照旧。 | A changed coat, %s, yet the same oath. | A new coat, %s. Same oath. |
| `reskinned.1` | 谨遵您的旨意，%s，我已整装待命。 | 如何，%s？这身毛色还合您心意吧。 | As you command, %s. I am ready to follow. | How does it look, %s? I rather like it. |

## 战斗

| 键 | 现行中文 | 修订中文 | Current English | Revised English |
|---|---|---|---|---|
| `combat.0` | %s，请退到我身后。 | %s，站到我身后！ | %s, stand behind me. | Get behind me, %s! |
| `combat.1` | %s，这一战我与您并肩。 | %s，这一仗我陪您打。 | You shall not face this foe alone, %s. | We fight this one together, %s. |
| `combat.2` | %s，我将以利爪回应来犯之敌。 | 来得好，%s，让我的爪子招呼它们。 | %s, my claws shall answer this threat. | Let them come, %s. My claws are ready. |
| `wounded.0` | %s，伤势虽重，誓约未断。 | %s，我伤得不轻，还撑得住。 | %s, my wounds run deep. My oath remains unbroken. | %s, I am hurt, but I can still fight. |
| `wounded.1` | %s，我尚能站立，请容我片刻休整。 | 容我缓一口气，%s。 | I still stand, %s. Allow me a moment to recover. | Give me a moment to catch my breath, %s. |
| `victory.0` | %s，威胁已除。 | 敌人倒下了，%s。 | The threat is gone, %s. | The foe is down, %s. |
| `victory.1` | %s，前路已清。 | 前路清了，%s，继续走吧。 | %s, the path ahead is clear. | The way is clear, %s. Let us move on. |
| `fallen.0` | %s……愿月光护佑您的前路。 | %s……愿月光照着您回家的路。 | %s... may the moon watch over you. | %s... may the moon light your way home. |
| `fallen.1` | %s，恕我暂退……此身虽陨，誓约仍在。 | 对不住，%s……我先走一步，誓言还在。 | Forgive me, %s... my oath outlives this body. | Forgive me, %s... my oath outlives me. |

## 闲聊

| 键 | 现行中文 | 修订中文 | Current English | Revised English |
|---|---|---|---|---|
| `idle_follow.0` | %s，前路虽长，我会与您同行。 | 路还长，%s，我陪您走完。 | The road is long, %s. I shall keep pace with you. | It is a long road, %s. I will walk it with you. |
| `idle_follow.1` | 风中尚有远方的气息，%s，我们不会迷失。 | 风里有远方的气味，%s。闻着就让人想赶路。 | The wind carries distant scents, %s. We shall not lose our way. | The wind smells of far places, %s. It makes a wolf want to run. |
| `idle_follow.2` | %s，纵在日光之下，我的誓约也未曾改变。 | 太阳底下也一样，%s，我就在近旁。 | %s, even beneath the sun, my oath remains. | Sun or moon, %s, I stay close. |
| `idle_guard.0` | %s，四下暂且平静，我仍在警戒。 | 眼下还算安静，%s。我继续盯着。 | All is quiet for now, %s. I remain watchful. | Quiet for now, %s. I am still watching. |
| `idle_guard.1` | %s，我听着风声，不会让来敌悄然靠近。 | 风声我听着呢，%s，您放心。 | %s, I hear the wind. No foe shall pass unseen. | I am listening to the wind, %s. Rest easy. |
| `idle_guard.2` | 狼群自会守望彼此，%s，请安心。 | 狼群护着自己人，%s。 | The pack guards its own, %s. Rest easy. | The pack looks after its own, %s. |
| `idle_rest.0` | %s，我会在等候时磨利爪锋。 | 趁这会儿磨磨爪子，%s。您叫我就来。 | %s, I shall tend my claws while I await your word. | I will sharpen my claws while I wait, %s. |
| `idle_rest.1` | 此处尚且安宁，%s，您呼唤时我便启程。 | 这里挺安稳，%s。您一唤我就动身。 | The ground is still, %s. I shall be ready when you call. | It is peaceful here, %s. Call and I will come. |
| `idle_rest.2` | %s，待片刻休整，我们再赴月下长路。 | 歇一歇也好，%s。等月亮升起，我们再上路。 | A brief rest, %s, and we shall walk beneath the moon again. | A short rest, %s, and we will walk under the moon again. |
| `idle_sort` | %s，这里的物资由我照看。 | %s，这些家当我看着呢。 | %s, I am keeping watch over these supplies. | I am watching over the stores, %s. |

## 可选：给新整理流程加几句

现在随从会真的走到箱子前干活，下面这几句能让过程更有人味。它们需要新增翻译键和少量代码，所以单独列出，你可以挑着要。

| 建议键 | 触发时机 | 中文 | English |
|---|---|---|---|
| `sort.1`、`sort.2` | 开始整理（和现有 `sort` 随机轮换） | 这些箱子乱得有点年头了，%s。交给我吧。／我去翻翻这些箱子，%s。 | These chests have seen better days, %s. Leave them to me. / I will go through these chests, %s. |
| `sort_unreachable` | 有容器走不到时（一轮最多说一次） | %s，有个箱子我够不着，先跳过了。 | One chest is out of my reach, %s. I have left it be. |
| `sort_idle_found` | 留守时发现新物品要归置 | %s，又有新东西了，我去收拾。 | New goods, %s. I will see to them. |

## 需要你定的三件事

1. **整体语气够不够？** 如果想再现代一点，可以把“遵命”换成“好的”，把“爪子磨好了”换成更口语的说法；想更古雅，就退回部分“谨遵”。
2. **“还合您心意吧”“来得好”这类带点个性的句子**要不要保留？它们让随从更有性格，也稍微离开了恭敬的基调。
3. **可选的新台词**要不要加？
