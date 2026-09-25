# Defender

**Unofficial, experimental GPL-3.0 fork of [GrimAC](https://github.com/GrimAnticheat/Grim). Not endorsed by the Grim team.**

Defender adds passive client intelligence, separated identity/behavior risk scoring, bounded incident evidence, diagnostic packet/combat monitoring and admin inspection to Grim. This is an engineering alpha, not a production-ready anticheat release. No detection is a guarantee.

The 26.3 port originates from Grim's `ver/26.3` branch, pinned in [docs/UPSTREAM_COMMIT.txt](docs/UPSTREAM_COMMIT.txt). Build and test results, including limitations, are in [release/TEST_RESULTS.md](release/TEST_RESULTS.md). Real Vanilla, Fabric and Lunar gameplay and false-positive testing are still required. Fabric is not built in this branch. Do not advertise full 26.3 platform/client compatibility from compilation alone.

## Own modules

- `defender-core`: independent Java 17-compatible library, partitioned into intelligence, risk, firewall, probe, logging and companion packages.
- `common/src/main/java/ac/defender/platform`: lifecycle, PacketEvents and Cloud command adapters; no Bukkit world operations on packet threads.
- `client-signatures.yml`: bounded safe YAML, exact brand/channel matching, atomic reload. Brands and channels are spoofable.
- Risk aggregation: per-check deduplication, decay, healthy-server gating; identity hints never increase behavioral risk. No Defender autobans.
- Session evidence: 128 entries, 30-minute retention, optional asynchronous JSONL, seven daily files, hard size/queue caps, visible loss counters. No cross-session tracking by Defender.
- Diagnostic sustained movement bursts and repeated multiple-target attacks. These remain UNKNOWN review hints because legitimate server mechanics, proxies and batching can resemble cheats.
- Double-check probe state machine and nonce handshake primitives are unit-tested. Active translation transport and the optional `defender-client` mod are **not shipped**.

## Build and installation

Use JDK 21 for `./gradlew build`. The wrapper downloads Gradle; dependencies come from upstream Maven repositories. Run Minecraft 26.3 on its required Java runtime (upstream run task uses Java 25). The Bukkit shaded artifact includes PacketEvents. Install it on an isolated Paper test server; remove the original Grim plugin first. Back up configuration/worlds. Defender uses its own plugin data folder; Grim checks and commands remain present.

`plugins/Defender/defender/defender.yml` controls Defender logging and diagnostics. `client-signatures.yml` beside it is reloadable with `/defender reload`. The original Grim configuration remains separately configurable. A normal server cannot reliably enumerate arbitrary installed mods. Pure ESP is not detectable through these modules.

## Commands

All Defender commands require `defender.admin` (operator by default):

- `/defender inspect <player>`: compact client, protocol, risk and observation counts.
- `/defender client <player>`: passive identity overview.
- `/defender evidence <player>` and `/defender history <player>`: last ten observations from the current session, including health context. Not persistent cross-session history.
- `/defender checkclient <player>`: explicitly reports UNSUPPORTED for active probes; it does not claim a clean client.
- `/defender reload`: validates and replaces Defender configuration/signatures; leaves old configuration on validation failure. `/grim reload` controls upstream settings.

## Attribution and publication

See [upstream README and credits](docs/UPSTREAM_README.md), [LICENSE](LICENSE), [attribution](release/ATTRIBUTION.md), [exact source changes](release/SOURCE_CHANGES.md), [privacy](release/PRIVACY.md), [feature matrix](release/FEATURE_MATRIX.md), and [manual test checklist](release/MANUAL_TESTS.md).

Codex generated the new Defender code, tests and release prose. This is disclosed; no AI visual assets were generated. Modrinth acceptance has not been established. The new fork-specific contribution is primarily AI-generated, so the current Modrinth AI rules are a publication blocker requiring review; disclosure alone does not resolve that rule. No Modrinth upload has been made.

## Alpha.2 runtime targets

One shared JAR targets Paper 1.21.11/26.2/26.3 and Purpur 1.21.11/26.2 for experimental use. Exact startup/reload tests and hashes: [platform report](release/PLATFORM_TESTS.md). Gameplay and false-positive testing remain outstanding. Folia loading is disabled pending regional validation; no Fabric artifact. Set exact Modrinth game versions manually; the declared minimum API is 1.21.11.
