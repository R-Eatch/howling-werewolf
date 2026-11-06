# Make your own werewolf skin

For **Howling Werewolf 1.3.3 · Minecraft 26.1.2 NeoForge**. No extra mod is required. [简体中文](CUSTOM_SKINS-zh.md)

## Start in the game

1. Open the werewolf menu (default **K**) → **Skins**, choose **Classic / Modern** in the upper-right corner, then select **Create Template**. A new resource-pack folder opens with the selected set's three textures, Blockbench painting templates and this guide. Classic uses Adrian; Modern uses the selected built-in coat, or Ashen when exporting from a custom coat. Repeated exports create separate folders and never overwrite your artwork.
2. Open a `.bbmodel` from `blockbench` in [Blockbench](https://www.blockbench.net/), switch to **Paint**, and draw the coat, markings and eyes. Keep the geometry, UV layout and transparent areas. You can start with just one form.
3. **Export PNG** from the texture panel and replace the matching file below. **Saving the `.bbmodel` alone does not update the game texture.** Unpainted forms keep the supplied coat.
4. Return to **Skins → Resource Packs**, enable the new **My Wolf** pack, and select Done. Select the same Classic / Modern set and use the skin page arrows to find **My Wolf**, preview all three forms and select **Apply**.
5. After editing an enabled pack, press **F3 + T** to reload textures and skin entries. No restart is needed.

| Painting template | PNG path inside the resource pack | Base size |
|---|---|---|
| `blockbench/werewolf.bbmodel` | `assets/mywolf/textures/entity/werewolf.png` | 128 × 128 |
| `blockbench/quadruped.bbmodel` | `assets/mywolf/textures/entity/quadruped_werewolf.png` | 64 × 32 |
| `blockbench/beast.bbmodel` | `assets/mywolf/textures/entity/beast.png` | 128 × 128 |

The table above lists **Classic** sizes. **Modern** uses a **128 × 128** UV base for Werewolf and Quadruped Wolf, and **256 × 256** for Beast. Its supplied PNGs are 512 × 512, 512 × 512 and 1024 × 1024 respectively. Exported PNGs match their model.

In-game exports replace `mywolf` with a unique `wolfskin_…` namespace to keep creations separate. Use the paths in your actual folder. Edit `name`, `description` and `author` in `assets/<your namespace>/werewolf_skins/custom.json`. The `"model": "classic"` or `"model": "modern"` field selects the model set; omitting it defaults to Classic. Edit `description` in `pack.mcmeta` to change the pack's description; its title in the resource-pack list comes from the folder or ZIP filename.

## Sharing and troubleshooting

- Zip the pack with `pack.mcmeta` and `assets` directly at the ZIP root. Friends put it in `resourcepacks` and enable it with version 1.2.2 of this mod. A server can also distribute it through Minecraft's standard server resource-pack feature.
- Multiplayer synchronizes the model set and skin ID; it does not upload artwork. A viewer missing the pack sees Adrian for Classic or Ashen for Modern. Your selected ID remains saved and works again when the pack is enabled. Keep the namespace folder and JSON filename stable after sharing: together they define the skin ID.
- Regular 64 × 64 human player skins cannot be used directly. Supply all three PNGs at the base sizes above or an exact **2×, 3× or 4×** scale, with each file at most **1 MiB**.
- Skin JSON is limited to 16 KiB: name and author at most 48 characters each, description at most 160. The `howlingwerewolf` namespace is reserved. Invalid entries are skipped; see `logs/latest.log` for the reason.
- Custom packs' `blockbench/*.bbmodel` files remain painting and UV references. PNGs and skin JSON add coats; geometry/animation edits in that directory are not loaded. Modern's own projects are read by a separate loader from `assets/howlingwerewolf/models/entity/modern/` inside the mod. The supplied projects have no keyframes; existing actions are retargeted in code.

The source repository also includes `examples/custom-skin-pack`; copy that whole folder to `resourcepacks` to start manually.

Classic template artwork is by R_Eatch under [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). When sharing adaptations, credit the original artist, identify changes and retain the same license. See [the asset license](LICENSE-ASSETS.md).

Modern Werewolf, Quadruped Wolf, and Beast models and their nine skins per model
were designed, created, and shared by **thewolfulti**. These assets and their
painting templates follow the same **CC BY-SA 4.0** license. Credit thewolfulti when
sharing adaptations, identify your changes, and retain the same license.
