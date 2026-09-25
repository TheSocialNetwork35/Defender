# Defender

**An unofficial GPL-3.0 fork of [GrimAC](https://github.com/GrimAnticheat/Grim). Not endorsed by the Grim team.**

Defender is an experimental server anticheat fork that adds contextual evidence review and passive client intelligence to Grim's predictive checks. It helps administrators distinguish a spoofable client hint from a behavioral observation. This alpha is not production-ready.

## Features

Reloadable brand/channel signatures, separate identity and behavior confidence, bounded session history, optional structured local incident logging, permission-controlled inspection, and observational packet/combat diagnostics. Defender's own modules do not automatically ban players.

## Minecraft 26.3 support

This alpha integrates Grim's `ver/26.3` source branch and builds against PacketEvents 2.14.0-SNAPSHOT. See the repository's TEST_RESULTS.md for the exact build and smoke-test scope. Full 26.3 gameplay, clean-client false-positive and proxy compatibility validation remain incomplete. Compilation is not a claim of complete support.

## Platforms

A Bukkit/Paper-targeted shaded JAR is built. The inherited Folia adapter remains in source but requires its own runtime and region-thread tests. Fabric projects are disabled by this upstream branch; no Fabric server artifact or compatibility claim is included. Normal clients do not need a Defender mod.

## Client intelligence

`minecraft:brand` and known custom-payload/channel registrations supply weak, spoofable hints. Categories distinguish CLIENT, MOD_LOADER, KNOWN_MOD_SIGNAL, BEHAVIORAL_CHEAT, UNKNOWN and PROTECTED_OR_SPOOFED. Recognizing Fabric or a client brand does not mean cheating.

The server cannot reliably enumerate arbitrary installed mods. Active translation/keybind detection is not shipped: public mitigations and missing 26.3 client validation make it unsuitable for an enabled claim. `/defender checkclient` explicitly reports this limitation. Meteor, Wurst, LiquidBounce, Aristois, Impact, Baritone, BleachHack and derivatives are not advertised as actively detected. The optional companion mod is not part of this alpha; any future client self-report can be spoofed.

## Anti-cheat features

Grim remains responsible for the inherited prediction, range, packet-order, timing, block-interaction and related checks. Defender adds sustained movement-packet burst and repeated multi-target attack observations for human review. These observations remain UNKNOWN, since network batching and custom mechanics can look suspicious. No new claim of reliable Aim/InventoryMove or pure ESP detection is made. Server-observable impossible interactions are distinct from visual ESP.

## Installation

1. Use an isolated test server and back up its configuration/worlds.
2. Remove the original Grim JAR before installing the Defender Bukkit shaded JAR; do not run both.
3. Use the Java runtime required by your exact server build (26.3 testing targets Java 25).
4. Start the server and inspect logs. PacketEvents is bundled in this artifact.
5. Complete the clean-client and gameplay tests before deploying to players.

## Commands

Permission: `defender.admin` (operators by default).

- `/defender inspect <player>` — client guess, loader, protocol, confidence, risk and observation counts.
- `/defender client <player>` — passive client summary.
- `/defender evidence <player>` — recent evidence with protocol/ping/TPS context.
- `/defender history <player>` — current-session history, not an offline database lookup.
- `/defender checkclient <player>` — reports unsupported active probes; does not report a clean client.
- `/defender reload` — validates and reloads Defender settings/signatures. Use `/grim reload` separately for inherited settings.

## Configuration

Defender files are under `plugins/Defender/defender/`. `client-signatures.yml` supports exact brand/channel signatures without recompilation. `defender.yml` controls diagnostic monitoring and optional `incident-log`; logging defaults off. Invalid configuration is rejected before replacing the active registry.

## Compatibility and limitations

Real Vanilla, Fabric-without-cheats, Lunar, high-latency, low-TPS, multiple-protocol, proxy, Geyser/Floodgate and Folia gameplay tests remain required. Bedrock handling relies on Grim's Java-check exemptions. Unknown health suppresses behavioral-risk scoring; Folia currently supplies unknown TPS. Fingerprints do not increase behavior risk, and high risk is still not proof.

Session evidence is capped and expires; optional local JSONL has queue, retention and size limits. No hardware/IP fingerprints or external Defender telemetry are generated. Inherited optional Discord/history/upload facilities have separate privacy implications; consult PRIVACY.md before enabling them.

## False positives

All confidence weights are heuristic and uncalibrated against a published real-client workload. Review health, server mechanics and multiple independent observations. No detector is described as completely certain, and a lack of findings does not establish a clean client.

## Credits and disclosures

**Upstream: [GrimAC](https://github.com/GrimAnticheat/Grim), DefineOutside, the Grim team and its contributors.** Grim's 26.3 work is credited upstream. PacketEvents and the other libraries retain their notices. Full source, attribution and file-level provenance accompany the fork.

Codex generated the new Defender code, tests and this description. Contains derivative content: YES. Contains AI-generated code/text: YES. No AI images or runtime AI services are used. Current Modrinth AI and substantial-fork eligibility has not been established; this text is a draft, not evidence of approval or readiness to publish.
