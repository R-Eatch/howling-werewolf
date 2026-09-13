# Make your own werewolf skin

For **Howling Werewolf 1.1.3 · Minecraft 1.21.1 NeoForge**. No extra mod is required. [简体中文](CUSTOM_SKINS-zh.md)

## Start in the game

1. Open the werewolf menu (default **K**) → **Skins** → **Create Template**. A new resource-pack folder opens with three Adrian textures, Blockbench painting templates and this guide. Repeated exports create separate folders and never overwrite your artwork.
2. Open a `.bbmodel` from `blockbench` in [Blockbench](https://www.blockbench.net/), switch to **Paint**, and draw the coat, markings and eyes. Keep the geometry, UV layout and transparent areas. You can start with just one form.
3. **Export PNG** from the texture panel and replace the matching file below. **Saving the `.bbmodel` alone does not update the game texture.** Unpainted forms keep the supplied Adrian texture.
4. Return to **Skins → Resource Packs**, enable the new **My Wolf** pack, and select Done. Use the skin page arrows to find **My Wolf**, preview all three forms and select **Apply**.
5. After editing an enabled pack, press **F3 + T** to reload textures and skin entries. No restart is needed.

| Painting template | PNG path inside the resource pack | Base size |
|---|---|---|
| `blockbench/werewolf.bbmodel` | `assets/mywolf/textures/entity/werewolf.png` | 128 × 128 |
| `blockbench/quadruped.bbmodel` | `assets/mywolf/textures/entity/quadruped_werewolf.png` | 64 × 32 |
| `blockbench/beast.bbmodel` | `assets/mywolf/textures/entity/beast.png` | 128 × 128 |

In-game exports replace `mywolf` with a unique `wolfskin_…` namespace to keep creations separate. Use the paths in your actual folder. Edit `name`, `description` and `author` in `assets/<your namespace>/werewolf_skins/custom.json`. Edit `description` in `pack.mcmeta` to change the pack's description; its title in the resource-pack list comes from the folder or ZIP filename.

## Sharing and troubleshooting

- Zip the pack with `pack.mcmeta` and `assets` directly at the ZIP root. Friends put it in `resourcepacks` and enable it with version 1.1.3 of this mod. A server can also distribute it through Minecraft's standard server resource-pack feature.
- Multiplayer synchronizes only the skin ID; it does not upload artwork. A viewer missing the pack sees Adrian. Your selected ID remains saved and works again when the pack is enabled. Keep the namespace folder and JSON filename stable after sharing: together they define the skin ID.
- Regular 64 × 64 human player skins cannot be used directly. Supply all three PNGs at the base sizes above or an exact **2×, 3× or 4×** scale, with each file at most **1 MiB**.
- Skin JSON is limited to 16 KiB: name and author at most 48 characters each, description at most 160. The `howlingwerewolf` namespace is reserved. Invalid entries are skipped; see `logs/latest.log` for the reason.
- `.bbmodel` is a painting and UV reference template in this version. Runtime skins use PNG and JSON. Changes to bones, geometry or animations are not imported.

The source repository also includes `examples/custom-skin-pack`; copy that whole folder to `resourcepacks` to start manually.

Template artwork is by R_Eatch under [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). When sharing adaptations, credit the original artist, identify changes and retain the same license. See [the asset license](LICENSE-ASSETS.md).
