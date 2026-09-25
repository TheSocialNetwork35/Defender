# Verification record — 2026-09-25

## Build

`./gradlew build` succeeded for the selected upstream base and for Defender. The final distributable is built with `./gradlew --offline --no-daemon build :bukkit:defenderDependencyInventory -Prelease=true` using Gradle 9.4.1 and Temurin JDK 21.0.12.1+1 on macOS ARM64. Offline mode used the resolved Maven cache; dependency hashes and fetched source artifacts are supplied. `mavenLocalOverride=false` in the final source. A fresh online dependency download was not independently validated in a clean machine.

macOS offloaded files in the original Documents workspace and produced read timeouts/corrupt Gradle cache failures. A materialized source copy under `/tmp/defender-build-workspace` was used for the final build. Those environment failures were not reported as successful tests. The source and final artifacts are copied back to the workspace and committed publicly.

Upstream warnings remain: deprecated APIs, `sun.misc.Unsafe`, and one unresolved Javadoc reference. No compilation failure remains in the successful build. Fabric is excluded in the selected upstream settings, so a successful root build does not mean Fabric was built.

## Automated tests

47 JUnit tests: 30 Defender tests, 17 inherited Grim tests. Zero failures/errors/skips in the successful runs. XML reports are included in `test-results/`.

Defender coverage: passive Vanilla/Fabric/Lunar/NeoForge/Quilt classification fixtures; Velocity suffix; multiple protocol metadata values; identity/behavior separation; deduplication/decay; high ping/low TPS/unknown health; strict UTF-8/length/channel bounds and 5,000 seeded random payload cases; protected/timeout/blocked/double-check/replay states; bounded history; nonce binding/expiry/single use; normal/batched/sustained packet windows; combat target windows; JSON escaping, retention and shutdown.

These are synthetic/model tests. They do not run the named Minecraft clients, simulate an actual proxy, or measure a production false-positive rate.

## Real Paper smoke test

- Paper 26.3 build 41, commit `a15fed9`, API `26.3.build.41-alpha`.
- Download SHA256: `2b77166ee61886a9bc9ab33dc9e4847fa3538b36d9ba6e5f2fa7ed90973aa748`.
- Java: Temurin 25.0.4.1+1, macOS ARM64.
- PacketEvents reports `2.14.0+8ca0c18b7-SNAPSHOT`, loads `V_26_3/23` block mappings.
- Binds loopback only (`127.0.0.1:25579`); no real players joined.
- Defender loads/enables; reaches server Done; commands registered.
- `/defender reload` succeeds; deliberately invalid schema is rejected; restored valid schema reloads successfully.
- Unknown player selectors fail cleanly; `version Defender` shows fork identity and upstream contributors.
- Clean server stop disables Defender and terminates PacketEvents without plugin exceptions.
- Initial flat-world test configuration emitted a server world-generator warning (`No key layers`), unrelated to Defender. OSHI/Unsafe platform warnings also appeared. Do not call the entire log warning-free.

This validates startup/lifecycle and a subset of console commands only. It does not validate movement prediction, game interactions, checks under player load, active probes or client compatibility.

## Not run / incomplete

All real client gameplay, high-latency network, degraded-TPS gameplay, proxy chains, Geyser/Floodgate, Folia regions and Fabric server tests. Active translation transport and deployable companion do not exist in this alpha. See MANUAL_TESTS.md and FEATURE_MATRIX.md.

GPL/source/attribution audit: original license preserved byte-for-byte, upstream history/credits retained, modified-file notices and exact provenance supplied, resolved dependency sources and license materials packaged. This is not a legal certification; the publication checklist calls for final version-specific review.

Modrinth rules were retrieved on 2026-09-25 (page modified August 13, 2026). Derivative/AI disclosures are drafted. No Modrinth project or version was uploaded; primarily AI-authored new functionality remains an eligibility concern requiring review.

The final committed build was smoke-tested again with fresh plugin configuration; the inherited datastore correctly remained disabled. Git metadata was reconstructed from the pinned upstream checkout in a persistent local cache after macOS blocked mmap of cloud-managed Git objects. No upstream history was rewritten.
