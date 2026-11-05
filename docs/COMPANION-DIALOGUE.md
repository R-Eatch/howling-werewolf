# 商人与随从对白 / Merchant and companion dialogue

`%s` 是玩家设置的称呼，中文默认“主人”，英文默认“my lord”。商人以“兄弟”称呼玩家，这几句不带称呼。翻译键前缀为 `dialogue.howlingwerewolf.companion.`。

`%s` is the address the player sets, “my lord” by default in English and “主人” in Chinese. The merchant calls the player “brother”, so its lines take no address. Translation keys start with `dialogue.howlingwerewolf.companion.`.

[随从指南](COMPANIONS-zh.md) · [Companion guide](COMPANIONS.md)

## 何时说话 / When lines are spoken

- 跟随、守护、休息和开始整理：切换成功时随机一句，重复下达相同指令时保持安静。 / Follow, Guard, Rest and starting a sort: one random line on a successful switch, and quiet when the same order repeats.
- 整理：一轮完成报告一次；走不到容器时一项工作最多提醒一次；一轮做完后又有新物品时提一句，五分钟内最多一次，避免漏斗持续进货时刷屏。 / Sorting: a report when a round finishes; one mention per job when a container is out of reach; a remark when new goods arrive after a finished round, at most once in five minutes, so a steady hopper cannot flood chat.
- 迎敌、重伤、胜利、阵亡：随战斗自动触发，有节流。 / Battle lines: triggered by combat, with throttling.
- 闲谈：靠近主人八格内、无目标、五秒未受伤，且主人没有睡觉、旁观或打开其他容器时才说；同一主人的所有随从共享至少一分钟的间隔。 / Idle words: only within eight blocks of the owner, with no target, five seconds unhurt, and the owner not sleeping, spectating or in another container; all of an owner's companions share a gap of at least a minute.
- 对白只发给主人本人。 / Dialogue goes only to the owner.

## 商人 / The merchant

| 键 / Key | 中文 | English |
|---|---|---|
| `greeting` | 月下相逢，兄弟别来无恙。今番唤我前来，所为何事？ | Well met beneath the moon, brother. What errand moves you to summon me? |
| `stay_notice` | 兄弟，月路催人，我只能在此盘桓十分钟；若有所需，趁此相商。 | The moonlit road grows impatient, brother. Ten minutes I may tarry; speak while the hour allows. |
| `traded` | 公平交易，各得其所。愿兄弟猎运亨通。 | A fair bargain, fairly struck. Good hunting to you, brother. |
| `farewell.0` | 月路迢迢，兄弟，容我就此别过。 | Long is the moonlit road, brother. Here I take my leave. |
| `farewell.1` | 今日之事已了，兄弟，他日有需，再唤我来。 | Our business is concluded, brother. Call upon me when next you have need. |

## 招募与重要时刻 / Oaths and turning points

| 键 / Key | 中文 | English |
|---|---|---|
| `recruited` | %s在上，此身从此追随左右，至死不渝。 | By the moon I swear it, %s: I am yours to follow, even unto death. |
| `command` | 谨遵%s之命。 | As you command, %s. |
| `hunting` | 谨遵钧命，%s。我将为您狩猎日与月，不负此行。 | As you command, %s. I shall hunt the sun and the moon for you. |
| `recalled` | %s，我已归来，侍立左右。 | I am returned to your side, %s. |
| `returned` | %s，此行不虚，猎获尽在此处。 | The hunt is done, %s, and I return laden with its spoils. |
| `revived` | 再造之恩，没齿难忘。此命仍归%s所有。 | You have given me life anew, %s. It is yours, as it ever was. |

## 指令回应 / Answering orders

| 键 / Key | 中文 | English |
|---|---|---|
| `follow.0` | %s请行，我当紧随其后。 | Go before me, %s. I shall follow in your steps. |
| `follow.1` | 前路但凭%s引领，我随后便至。 | Lead on, %s. Where you tread, I shall tread. |
| `follow.2` | 以月为誓，%s，此路我与您同行。 | By the moon, %s, this road we walk together. |
| `guard.0` | %s之敌，便是我的猎物。 | Your enemies are my quarry, %s. |
| `guard.1` | 利爪已砺，%s，我自当护您左右。 | My claws are whetted, %s. None shall come at your side. |
| `guard.2` | 此身尚存一息，便不教%s孤身迎敌。 | While breath remains in me, %s, you shall not stand alone. |
| `rest.0` | 谨遵钧命，%s，我便在此恭候。 | As you command, %s. Here I shall bide. |
| `rest.1` | %s自去，我在这片月华之下静候召唤。 | Go, %s. I shall await your call beneath this moon. |
| `rest.2` | 我且留守于此，%s，但有差遣，一唤即至。 | I shall hold this ground, %s. One word, and I come. |

## 整理 / Sorting

| 键 / Key | 中文 | English |
|---|---|---|
| `sort.0` | %s放心，这些箱中之物，容我一一归置。 | Rest easy, %s. I shall set these stores in good order. |
| `sort.1` | 这些箱笼久未清点，%s，且交与我吧。 | These chests have long wanted tending, %s. Leave them in my care. |
| `sort.2` | 遵命，%s。待我逐一开箱，清点归位。 | As you command, %s. I shall open each in turn and see all things to their place. |
| `sorted` | %s，诸般物资皆已归置停当。 | It is done, %s. All your stores lie in good order. |
| `sort_unreachable` | %s，有一处箱笼我无路可至，只得暂且搁下。 | One chest lies beyond my reach, %s. I must leave it be for now. |
| `sort_idle_found.0` | %s，又有新物入库，容我前去料理。 | New goods have come to the stores, %s. I shall see to them. |
| `sort_idle_found.1` | 箱中又添了些东西，%s，我这便去归置。 | The stores have grown again, %s. Let me put them right. |

## 背包、升级与外观 / Packs, growth and looks

| 键 / Key | 中文 | English |
|---|---|---|
| `inventory.0` | %s，我所携之物，悉听取用。 | All that I carry is yours to command, %s. |
| `inventory.1` | 所得尽在于此，%s，请自取所需。 | The spoils lie here, %s. Take what you will. |
| `upgrade.0` | 月华又砺我爪牙，%s，誓约始终如一。 | The moon has whetted my claws anew, %s. My oath holds as ever. |
| `upgrade.1` | 珍珠之力已入此身，%s，定不负所托。 | The pearl's strength runs in my veins, %s. I shall not fail your trust. |
| `renamed.0` | %s所赐之名，我当铭记于心。 | The name you bestow I shall bear with honour, %s. |
| `renamed.1` | 此名甚好，%s，从今便以此名行走月下。 | A worthy name, %s. By it shall I walk beneath the moon. |
| `reskinned.0` | 毛色虽易，誓约不改，%s。 | A changed coat, %s, yet the same oath. |
| `reskinned.1` | 如何，%s？这身毛色可还合您心意？ | How say you, %s? Does this coat please you? |

## 战斗 / Battle

| 键 / Key | 中文 | English |
|---|---|---|
| `combat.0` | %s且退，此敌交由我来！ | Stand behind me, %s! This foe is mine. |
| `combat.1` | %s莫忧，此战我与您并肩。 | You shall not face this foe alone, %s. |
| `combat.2` | 来得好！%s且看我利爪如何招呼。 | Let them come, %s! My claws shall give them answer. |
| `wounded.0` | %s，伤势虽重，誓约未断。 | My wounds run deep, %s, yet my oath stands unbroken. |
| `wounded.1` | %s，容我喘息片刻，尚可再战。 | Grant me but a moment's breath, %s. I can fight yet. |
| `victory.0` | %s，来敌已伏诛。 | The foe lies slain, %s. |
| `victory.1` | 前路已清，%s，请继续前行。 | The way lies clear, %s. Let us press on. |
| `fallen.0` | %s……愿月华长照您的归途。 | %s... may the moon light your road home. |
| `fallen.1` | 恕我先行一步，%s……此身虽陨，誓约犹在。 | Forgive me, %s... my oath outlives this flesh. |

## 闲谈 / Idle words

| 键 / Key | 中文 | English |
|---|---|---|
| `idle_follow.0` | 前路虽长，%s，我自当相伴始终。 | Long is the road, %s, and I shall walk it to its end beside you. |
| `idle_follow.1` | 风中尚有远方的气息，%s，闻之令狼神往。 | The wind bears the scent of far places, %s. It stirs a wolf's blood. |
| `idle_follow.2` | 日升月落，%s，我自相随不离。 | Sun or moon, %s, I stay at your side. |
| `idle_guard.0` | 四野暂且无事，%s，我仍在警戒。 | All is quiet for now, %s, yet I keep watch. |
| `idle_guard.1` | 我听着风声，%s，断不教来敌悄然近身。 | I listen to the wind, %s. No foe shall pass unmarked. |
| `idle_guard.2` | 狼群自会守望彼此，%s，尽可安心。 | The pack guards its own, %s. Be at ease. |
| `idle_rest.0` | 闲来无事，且磨爪锋，%s一唤我便动身。 | I shall whet my claws whilst I wait, %s. Call, and I come. |
| `idle_rest.1` | 此地尚算安宁，%s，但闻呼唤，即刻启程。 | This ground is peaceful, %s. At your word I shall set forth. |
| `idle_rest.2` | %s，待片刻休整，再赴月下长路。 | A brief rest, %s, and we walk beneath the moon once more. |
| `idle_sort` | %s放心，此间物资自有我来照看。 | Fear not, %s. These stores are in my keeping. |
