# 制作自己的狼人皮肤

适用于 **月厄狼人 1.1.2 · Minecraft 1.20.1 Forge**。无需安装额外 Mod。

## 从游戏内开始

1. 打开狼人菜单（默认 **K**）→ **皮肤** → **创建模板**。游戏会打开新建的资源包文件夹，其中包含三个形态的原棕色 Adrian 贴图、Blockbench 模板和本说明。重复点击会创建新文件夹，不会覆盖你的作品。
2. 用 [Blockbench](https://www.blockbench.net/) 打开 `blockbench` 文件夹中的 `.bbmodel`，切到 **Paint / 绘制** 模式画毛色、纹理和眼睛。请保留模型、UV 和透明区域；可以先只画一个形态。
3. 在 Blockbench 的纹理面板中**导出 PNG**，覆盖下表对应的文件。**只保存 `.bbmodel` 不会更新游戏皮肤**；未修改的形态仍使用模板中的 Adrian。
4. 返回皮肤页 → **资源包**，启用新建的 **My Wolf** 资源包并点击完成。用左侧翻页箭头找到 **My Wolf**，预览三个形态后点击 **应用**。
5. 之后修改 PNG 或皮肤说明，按 **F3 + T** 重新加载资源即可，无需重启游戏。

| 模板 | 导出的 PNG（资源包根目录下） | 默认尺寸 |
|---|---|---|
| `blockbench/werewolf.bbmodel` | `assets/mywolf/textures/entity/werewolf.png` | 128 × 128 |
| `blockbench/quadruped.bbmodel` | `assets/mywolf/textures/entity/quadruped_werewolf.png` | 64 × 32 |
| `blockbench/beast.bbmodel` | `assets/mywolf/textures/entity/beast.png` | 128 × 128 |

游戏内创建的模板会把上述 `mywolf` 自动换成唯一的 `wolfskin_…` 文件夹名，以免多个作品互相覆盖。请使用你文件夹中实际存在的路径。可以编辑 `assets/<该文件夹名>/werewolf_skins/custom.json` 中的 `name`、`description`、`author`；资源包列表名称在 `pack.mcmeta` 中修改。

## 分享与常见问题

- 将 `pack.mcmeta`、`assets` 等文件压缩到 ZIP 根目录，发给朋友放入自己的 `resourcepacks` 并启用。双方还需要安装 1.1.2 Mod。服务器也可以通过原版服务器资源包分发功能提供同一个包。
- 联机只同步皮肤 ID，不会自动上传图片。没有安装对应资源包的人会看到 Adrian；已选皮肤 ID 仍保留，重新启用包后恢复。分享后不要随意改命名空间文件夹或 `custom.json` 文件名，它们共同决定皮肤 ID。
- 普通玩家的 64 × 64 人形皮肤不能直接套用。三张 PNG 都必须存在，支持表中尺寸的 **1、2、3、4 倍等比放大**，每张不超过 **1 MiB**。
- 皮肤 JSON 不超过 16 KiB；名称和作者各最多 48 字符，介绍最多 160 字符。自定义命名空间不能使用保留的 `howlingwerewolf`。无效条目会跳过，原因见 `logs/latest.log`。
- `.bbmodel` 在此版本是方便绘画和检查 UV 的编辑模板。游戏读取 PNG 和 JSON；更换骨骼、模型形状或动画不会导入游戏。

公开模板也在源码的 `examples/custom-skin-pack` 文件夹中；手动复制整个文件夹到 `resourcepacks` 即可使用。

模板美术由 R_Eatch 创作，采用 [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)。分享基于模板绘制的作品时请署名、注明修改并保留相同许可，详见 [美术许可](LICENSE-ASSETS.md)。
