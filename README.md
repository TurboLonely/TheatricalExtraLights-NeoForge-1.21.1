# Theatrical: Extra Lights — NeoForge 1.21.1 (Unofficial Port)

> **This is an unofficial, community-made port.** It is **not** affiliated with, endorsed by, or
> supported by the original authors. All credit for the mod itself goes to the original
> Extra Lights team — see [Credits & Attribution](#credits--attribution).
>
> **这是一个非官方移植版**，与原作者团队无隶属关系，也未获得其背书或支持。模组本体的全部
> 功劳归原作者团队所有，详见下方的署名与致谢章节。

A port of [Theatrical: Extra Lights](https://github.com/dumann089/TheatricalExtraLights) to
**Minecraft 1.21.1 / NeoForge**, built on [Architectury](https://github.com/architectury).

Extra Lights is an addon for [Theatrical](https://github.com/theatricalmod/Theatrical) that adds
extra live-event fixtures — moving heads, LED panels, PARs, lasers, water jets, pyro, and more —
controlled over DMX / Art-Net just like the base mod.

> **This is an addon.** [Theatrical](https://github.com/theatricalmod/Theatrical) is **required**.
> Keep both mods on compatible versions.

---

## About this port

This repository is **NeoForge-only**. The upstream multi-loader layout was reduced so that only the
NeoForge target is built:

| | Upstream | This port |
|---|---|---|
| Loaders | Fabric + Forge | **NeoForge only** |
| Minecraft | 1.20.1 | **1.21.1** |
| Modules | `common`, `fabric`, `forge` | `common`, `neoforge` |

Work done in this port, on top of the upstream sources:

- Removed the `fabric` and `forge` modules and reworked `settings.gradle` / `build.gradle` so only
  `common` + `neoforge` are built.
- Completed the Minecraft 1.20.1 → 1.21.1 API migration (rendering / vertex API, data components,
  `RegistryFriendlyByteBuf`-based networking, screen input handling, and more).
- Reworked the networking layer for the NeoForge payload system.
- Removed the optional Shimmer compatibility layer — the `ReloadShaderManager` mixin and its shader
  resource providers were specific to the 1.20.1 rendering stack and no longer apply on 1.21.1.

Upstream features, content, textures and assets are unchanged, except for the addition of a
built-in Simplified Chinese language file (`assets/theatricalextralights/lang/zh_cn.json`).

---

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| Mod loader | NeoForge 21.1.x (built against 21.1.251) |
| Architectury API | 13.x |
| Theatrical | 1.0.0-alpha.23 or newer (use the companion NeoForge port) |
| Java | 21 |

**Required dependencies** (neither is bundled): [Architectury API](https://www.curseforge.com/minecraft/mc-mods/architectury-api)
(NeoForge, 13.x) and [Theatrical](https://github.com/theatricalmod/Theatrical). The addon will not
load without them.

## Installation

1. Install Minecraft 1.21.1 with **NeoForge**.
2. Drop [Architectury API](https://www.curseforge.com/minecraft/mc-mods/architectury-api) (NeoForge 13.x), **Theatrical**, and this addon into your `mods` folder.
3. Launch the game.

## Downloads

Grab the latest jar from the [**Releases**](../../releases) page of this repository:

- `TheatricalExtraLights-neoforge-<version>-mc1.21.1.jar` — the addon itself.

This port ships a **built-in Simplified Chinese localization**
(`assets/theatricalextralights/lang/zh_cn.json` inside the jar). Set the game language to
*简体中文 (Simplified Chinese)* and it applies automatically — **no separate resource pack is
required**.

## Building from source

```bash
# JDK 21 required
./gradlew :neoforge:build
```

The addon compiles against the companion Theatrical NeoForge port, expected as a sibling checkout
(`../theatrical-1.21.1`) with its dev jars built (`:neoforge:build`, `:common:build`).

The mod jar is produced in `neoforge/build/libs/`. Use the plain
`TheatricalExtraLights-neoforge-<version>-mc1.21.1.jar` (not `-dev-shadow` or `-sources`) for
installation.

To run a development client:

```bash
./gradlew :neoforge:runClient
```

---

## Credits & Attribution

**Original addon — all credit belongs to the Extra Lights team:**

- Repository: [dumann089/TheatricalExtraLights](https://github.com/dumann089/TheatricalExtraLights)
- Documentation / wiki: [Theatrical: Extra Lights wiki](https://extra-light.nailec.fr/)
- Base mod: [theatricalmod/Theatrical](https://github.com/theatricalmod/Theatrical)
- Discord: [Join the Discord](https://discord.gg/7qMs5d6)

Authors:

- dumann089
- Rushmead
- J8-Diablo
- nailec

This port is maintained by [TurboLonely](https://github.com/TurboLonely). Please **do not** report
bugs in this port to the original authors — open an issue in this repository instead. For the
original addon on supported versions, use the upstream pages above.

## License

Licensed under the **MIT License**, the same license as the upstream project.

```
MIT License

Copyright (c) 2025 Stuart Pomeroy
```

See [LICENSE](LICENSE) for the full text. The original copyright notice and permission notice are
retained, as required by the license. If you redistribute this or a modified version, keep the
`LICENSE` file and this attribution intact.

---

## 中文说明

**这是一个非官方移植版**，将 [Theatrical: Extra Lights](https://github.com/dumann089/TheatricalExtraLights)
移植到 **Minecraft 1.21.1 + NeoForge**。模组本体由原作者团队开发，版权归其所有（MIT 协议），
本仓库仅为 NeoForge 1.21.1 的适配版本，与原作者团队无关。

**这是 Theatrical 的附属模组**，必须同时安装 [Theatrical](https://github.com/theatricalmod/Theatrical)
才能使用。

**环境要求：** Minecraft 1.21.1、NeoForge 21.1.x、Architectury API 13.x、Theatrical
1.0.0-alpha.23 或更高版本、Java 21。

**安装：** 安装 NeoForge 后，把 Architectury API、Theatrical 与本附属模组的 jar 一起放进
`mods` 文件夹即可。

**本移植版改动：** 移除 Fabric / Forge 模块（仅保留 NeoForge）、移除可选的 Shimmer 兼容层
（`ReloadShaderManager` 注入及其着色器资源类，仅适用于 1.20.1 渲染栈）、完成 1.21.1 API
迁移、重做网络层。除新增简体中文语言文件外，模组内容与材质资源均未改动。

**内置汉化：** 本移植版已将简体中文语言文件直接内置进模组 jar
（`assets/theatricalextralights/lang/zh_cn.json`），把游戏语言设为「简体中文」即自动生效，
无需再额外安装任何汉化资源包。

**反馈：** 本移植版的问题请在本仓库提 Issue，请勿打扰原作者。
