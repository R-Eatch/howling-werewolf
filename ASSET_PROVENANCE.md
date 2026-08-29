# Howling Werewolf asset provenance

Copyright © 2026 R_Eatch.

Original art assets are governed by [LICENSE-ASSETS.md](LICENSE-ASSETS.md)
(`CC-BY-SA-4.0`), except for the official logo and project branding identified there.
Original source code is governed by [LICENSE.md](LICENSE.md) (`MPL-2.0`). Third-party
materials retain their own terms.

## Runtime assets

The runtime textures, non-branding icons, and visual model geometry under
`src/main/resources/assets/howlingwerewolf/` were created for Howling Werewolf. The
corresponding Java model classes are source code and are covered by `MPL-2.0` rather than
the art license.

- Normal Werewolf and Beast textures use original 128×128 atlases designed for this
  project's independent player-model geometry.
- Feral Werewolf artwork is a project-authored variation of the Mod's own Werewolf art.
- The quadruped form uses an original 64×32 texture with locally defined
  vanilla-compatible box geometry. No third-party Mod model or texture is bundled.
- Hunter, Alpha Werewolf, Silver, Wolfsbane, potion, ritual altar, Moonbane Pearl, badge,
  spawn-egg, and interface assets were created specifically for this project.
- Runtime code references Minecraft's installed `WOLF_HURT` sound event for Feral
  Werewolves. No Minecraft audio file is copied into this repository or the release JAR.

## Inspiration and independence

Howling Werewolf was inspired by several outstanding werewolf Mods created by the
Minecraft community. Inspiration is limited to the broad werewolf-progression genre and
thematic ideas. The project's implementation, text, models, textures, and gameplay systems
were independently developed; it is not a port, fork, official continuation, or official
remake of another Mod.

## Third-party materials

Minecraft, Minecraft Forge, Gradle Wrapper components, mappings, and other third-party
materials remain subject to their respective owners' licenses and terms. Forge licensing
and credits supplied with the development kit are preserved in `LICENSE-FORGE.txt` and
`CREDITS.txt`.
