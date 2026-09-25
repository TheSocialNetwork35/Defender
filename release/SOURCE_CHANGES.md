# Source changes and provenance

Base: GrimAC `ver/26.3`, commit `a7378b39f2caa5fd5638a10220624fc14ae62be6`.
All files at that revision are enumerated with original Git blob IDs in `docs/UPSTREAM_FILES.tsv`. `SOURCE_PROVENANCE.tsv` records unchanged, modified, moved and added files against that exact tree. Generate it with `python3 tools/provenance.py`; it excludes its own recursive hash. Full inherited Git history and credits are retained.

## New Defender code

- `defender-core/src/main/java/ac/defender/intelligence`: signatures, safe YAML loader, passive classification and bounded session model.
- `.../risk`: category/evidence types and decaying health-gated aggregation with strict separation from fingerprints.
- `.../firewall`: strict brand/channel decoding, diagnostic packet and combat windows.
- `.../logging`: optional bounded asynchronous JSONL writer with session pseudonyms, retention/caps/loss counters.
- `.../probe`: double-check lifecycle only; no active sign packet transport.
- `.../companion`: nonce primitive only; no deployable companion mod.
- `defender-core/src/test`: automated model, decoder, logging and adversarial-input tests.
- `common/src/main/java/ac/defender/platform`: lifecycle, PacketEvents intake, accepted Grim-flag bridge and six Defender commands.
- `common/src/main/resources/{defender,client-signatures}.yml`: defaults and editable passive signatures.

## Deliberate upstream bridge changes

- `Check.java`: accepted flags feed bounded observations; upstream enforcement remains separate.
- `CloudCommandService.java`: registers Defender commands.
- `InitManager.java`: starts/stops Defender lifecycle.
- `PlayerDataManager.java`: opens Defender sessions on join and discards them on disconnect; late packets cannot recreate closed sessions.
- `PacketEventsInit.java`: disables embedded PacketEvents bStats.
- `GrimACBukkitLoaderPlugin.java`: no longer starts upstream bStats metrics.
- All shipped `config/*.yml`: automatic upstream updates and fingerprint-only legacy Forge disconnection default to false; original license headers preserved.
- `UpdateChecker.java`: missing config key also defaults to no update requests. `BaseConfigManager.java` defaults the legacy brand-only Forge block to false.
- `PacketOrderH.java`: upstream unused import removed by upstream's build-time formatter; no behavior change.

## Build/documentation

Root/common/Bukkit Gradle files integrate the library, alpha version, distinct plugin identity, admin permission and dependency audit tasks. `gradle.properties` disables local Maven overrides for reproducibility. `settings.gradle.kts` includes defender-core; Fabric remains disabled as inherited from the 26.3 branch. License/notice bundling and release audit tools are supplied.

Original README and publishing workflows are preserved under `docs/`; publishing workflows are removed from active `.github/workflows` to prevent unintended upstream publication. A new read-only build/test workflow is provided. Release description, compatibility audit, manual matrix, disclosures, privacy/security and publication gate are new. No visual assets are generated.

The 26.3 code changes between upstream `2.0` and `ver/26.3` are upstream work. Use `git diff 8eb5f2809591c891deb4958bb2927844871e0600 a7378b39f2caa5fd5638a10220624fc14ae62be6` to inspect those separately from Defender's additions.

Defender also disables the inherited database master toggle in all shipped locales by default; operators can explicitly enable upstream persistence. Both upstream and PacketEvents bStats startup are disabled. Existing operator configuration is not silently overwritten.
