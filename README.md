# Howling Werewolf — NeoForge 1.21.1

<p align="center">
  <img src="src/main/resources/logo.png" alt="Howling Werewolf logo" width="128">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.248%2B-E04E14?style=flat-square" alt="NeoForge 21.1.248 or later">
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square" alt="Java 21">
  <a href="LICENSE.md"><img src="https://img.shields.io/badge/code-MPL--2.0-blue?style=flat-square" alt="Code license: MPL-2.0"></a>
  <a href="LICENSE-ASSETS.md"><img src="https://img.shields.io/badge/assets-CC_BY--SA_4.0-lightgrey?style=flat-square" alt="Asset license: CC BY-SA 4.0"></a>
</p>

<p align="center"><a href="README-zh.md">简体中文</a></p>

> **Development warning:** The NeoForge edition for Minecraft 1.21.1 is still under development and testing. Back up your worlds before playing.

This branch contains the in-development NeoForge edition of Howling Werewolf for Minecraft 1.21.1. You can download the source and build the `1.0.6-beta` version yourself. The maintained Forge 1.20.1 source is available on the [`1.20.1-forge`](https://github.com/R-Eatch/howling-werewolf/tree/1.20.1-forge) branch.

Howling Werewolf is an independently developed transformation and progression Mod built around infection, lunar transformations, two progression systems, three werewolf forms, Hunters and Silver, Wolfsbane, and the Alpha Trial.

## Requirements

| Component | Version |
|---|---|
| Minecraft Java Edition | 1.21.1 |
| Mod loader | NeoForge 21.1.248 or later compatible 21.1.x release |
| Java | 21 |
| Howling Werewolf | 1.0.6-beta |

## Build from source

Download or clone this branch, then use the Gradle wrapper with a Java 21 toolchain:

```powershell
./gradlew.bat clean build
```

The distributable JAR is written to `build/libs/howlingwerewolf-1.0.6-neoforge-beta.jar`.

## Release status

The NeoForge port is still under development and testing. Back up every important world before adding or updating this beta.

## Administrator commands

Server operators and testers can consult the [administrator command guide](ADMIN_COMMANDS.md), also available in [Simplified Chinese](ADMIN_COMMANDS-zh.md).

## License

Source code is licensed under the [Mozilla Public License 2.0](LICENSE.md). Original project art is licensed separately under [CC BY-SA 4.0](LICENSE-ASSETS.md). See [ASSET_PROVENANCE.md](ASSET_PROVENANCE.md) and [CREDITS.txt](CREDITS.txt) for attribution and provenance.
