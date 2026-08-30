# Howling Werewolf asset provenance

Copyright © 2026 R_Eatch.

Original art assets are governed by `LICENSE-ASSETS.md` (`CC-BY-SA-4.0`), except for the
official logo and project branding identified there. Original source code is governed by
`LICENSE.md` (`MPL-2.0`). Third-party materials retain their own terms.

This file records the provenance policy for the public release branch.

## 1.0.2 owner-updated assets

- `textures/entity/hunter.png`
  - Updated by the project owner through the textured Forge 1.0.2 Blockbench handoff, then copied byte-for-byte to this port's runtime texture.
- `textures/entity/quadruped_werewolf.png`
  - Updated by the project owner through the aligned Forge 1.0.2 Blockbench handoff, then copied byte-for-byte to this port's runtime texture. The accepted 1.0.1 geometry remains unchanged.

## 0.3.2 current assets

- `assets/howlingwerewolf/textures/entity/werewolf.png`
  - Original UV atlas for Howling Werewolf's independent `ModelPart` werewolf geometry.
  - Repainted from scratch in the 0.3.2 debug/model pass.
  - Palette and visual direction were guided by the user-supplied werewolf reference: grey-brown upper fur, cream muzzle/chest/forearms/lower legs, amber eyes and dark claws.
  - No pixels or UV islands were copied from Howling Moon or Howling Moon Rising.
- `assets/howlingwerewolf/textures/entity/beast.png`
  - Original UV atlas for the independent Beast geometry.
  - Repainted for the current integer-aligned UV layout; no third-party model texture is used.
- `assets/howlingwerewolf/textures/item/wolfsbane_potion.png`
  - Original 16x16 Minecraft-style potion icon drawn for Howling Werewolf.
- `assets/howlingwerewolf/textures/item/werewolf_potion.png`
  - Original 16x16 Minecraft-style potion icon drawn for Howling Werewolf.
- `assets/howlingwerewolf/textures/item/silver_ingot.png`
  - Original pixel icon created from scratch for Howling Werewolf.
- `assets/howlingwerewolf/textures/item/silver_sword.png`
  - Original pixel icon created from scratch for Howling Werewolf.
- `assets/howlingwerewolf/textures/item/moonbane_pearl.png`
  - Project-specific Moonbane Pearl artwork.
- `assets/howlingwerewolf/textures/block/wolfsbane_flower.png`
  - Project-specific wolfsbane artwork.

## Audio / models

- No third-party werewolf howl audio is distributed. Transformation audio uses Minecraft sound events at runtime.
- Normal werewolf and Beast model geometry, UV definitions and animations are implemented directly in the Howling Werewolf source tree and do not require GeckoLib.

## Release policy

Before public releases, third-party assets should be added only when their source and redistribution license are recorded here. Development references may guide palette, theme or general subject matter, but final distributed assets must be original or have a documented redistribution license.

## 0.6.4 current assets

- `src/main/resources/logo.png`
  - Original project-specific icon generated with the built-in OpenAI image-generation workflow
    for the 0.6.3 “月厄狼人” identity, then deterministically reduced to 128×128.
  - The 0.6.2 dark crescent/claw icon was used only as a style-density reference. The new emblem
    instead centers a Moonbane Pearl marked by three crimson claw gouges between angular wolf-fang
    shapes and does not reuse
    the old composition.
  - The generated master and 512×512 release image live under `design/branding/`; the exact old
    runtime icon is preserved as `design/branding/logo-0.6.2-original.png`.
- Werewolf hurt audio
  - No Minecraft audio file is copied or redistributed. Runtime code references Minecraft's
    built-in `SoundEvents.WOLF_HURT` only for wild Feral Werewolves, so clients use the legally
    installed game asset. The Alpha and transformed players retain their ordinary hurt sounds.
- `client/QuadrupedWerewolfModel.java`
  - Contains this project's local vanilla-compatible 64×32 box-layer definition and player-driven
    animation. The compiled model class is packaged in the JAR; no Mojang source or model file is
    bundled as a separate asset.

## 0.6.2 current assets

- `textures/entity/hunter.png`
  - Preserves the project-owner-supplied 0.6.1 head and torso artwork from
    `art-backup/hunter_wolf/hunter.png`.
  - `tools/generate_hunter_0_6_2_texture.py` deterministically completes the missing independent
    left/right arm and leg UV islands in the existing brown/green/metal palette. The new pixels
    include visible hands, cuffs, seams, knee reinforcement, boots, soles, and all four limb overlays.
  - Hunter rendering continues to use the complete non-slim player-skin layout; the repair is in
    the texture itself and does not mirror one model limb onto the other.
- `design/blockbench/hunter_0.6.2-player-skin-model.bbmodel`
  - Generated locally from the matching runtime geometry and embeds the completed 0.6.2 texture.
- `textures/entity/quadruped_werewolf.png` and
  `design/blockbench/quadruped_wolf_0.6.1-vanilla-wolf-rig.bbmodel`
  - The runtime texture remains unchanged from the project-owner-supplied 0.6.1 artwork. In 0.8.0,
    the editable model's tail root was centered to match the compiled model; the existing `(9,18)`
    UV island and all runtime texture bytes remain unchanged.

## 0.8.0 model handoff maintenance

- `design/blockbench/quadruped_wolf_0.6.1-vanilla-wolf-rig.bbmodel`
  - Tail geometry was centered from X `-2..0` to `-1..1` and its root moved to X `0`, matching
    `QuadrupedWerewolfModel.java`. No texture generation or repaint was performed.
- Blockbench texture paths
  - Hunter and quadruped editable models and their generators now use project-relative paths rather
    than a developer-machine absolute path. Embedded texture data and runtime textures were not
    replaced by this portability cleanup.

## 0.6.0 current assets

- `textures/entity/werewolf_entity.png`
  - Derived deterministically from this project's original `werewolf.png` with sparse black-fur
    pixels and dark-red healed scratches. `tools/generate_texture_masks.ps1` reproduces the exact
    runtime atlas without moving UV islands or painting transparent space.
- `design/texture-masks/`
  - Project-generated painting bases, UV coverage masks, and a constrained feral-werewolf edit
    mask for the biped werewolf, quadruped player wolf, and Hunter atlases. These are authoring
    aids and are not loaded at runtime.

## 0.5.0 current assets

- `design/concepts/0.5.0-hunter-altars-alpha-concept.png`
  - AI-generated development concept sheet made for this project in the `stylized-concept`
    workflow. It established the charcoal/brown Hunter silhouette, stone-and-wolfsbane
    altar palette, gold/diamond central-altar accents, and the Alpha's warm brown/crimson
    visual direction. It is a design reference and is not loaded by the game.
- `textures/entity/quadruped_werewolf.png`, `hunter.png`, and `alpha_werewolf.png`
  - Original deterministic pixel atlases generated by `tools/generate_0_5_assets.py`.
    They follow Minecraft-compatible UV layouts without copying third-party mod pixels.
    The quadruped uses Minecraft's built-in Wolf model geometry at runtime.
- `textures/block/ritual_altar_side.png`, `ritual_altar_top.png`,
  `central_ritual_altar_side.png`, and `central_ritual_altar_top.png`
  - Original deterministic 16x16 textures generated by the same project script. Their
    visible stone brick, wolfsbane-purple, gold, and diamond cues correspond to recipes.


## 0.3.4 current assets

- `textures/entity/werewolf.png`
  - Repainted again in the 0.3.4 normal-model refinement pass.
  - Goal: softer fur transitions, sharper wolf ears / snout identity, and support for the new pelvis / snout_bridge UVs.
- `textures/entity/beast.png`
  - Retained from the 0.3.2 debug/model pass except for script regeneration compatibility.


## 0.3.5 current assets

- `textures/entity/werewolf.png` and `textures/entity/beast.png`
  - Repainted together in the 0.3.5 model-only pass.
  - Goal: shared coyote-inspired fur identity, stronger color separation, and a less rabbit-like / more wolf-like head read.


## 0.3.7 current assets

The current entity textures and ear fine-tuning were supplied via the user-edited 0.3.7 Blockbench handoff and merged without changing the project's original animation/gameplay implementation.

- `textures/entity/werewolf.png` and `textures/entity/beast.png`
  - Repainted in a warmer 0.3.0-inspired brown palette.
  - Chest, abdomen, forearms, lower legs and tail are intentionally more separated now.
- `WerewolfPlayerModel.java` / `BeastPlayerModel.java`
  - Head presentation simplified: smaller cube ears and a clearer protruding muzzle volume.
