# Known limitations — release blockers

- Alpha only. A build and synthetic test suite do not establish Minecraft 26.3 gameplay compatibility or low false-positive rates.
- The selected upstream disables Fabric projects. No Fabric JAR or Fabric runtime support is claimed. Bukkit/Paper/Folia share source but each needs a real runtime test.
- Latest Paper/Folia, actual Vanilla/Fabric/Lunar/Badlion clients, ViaVersion/proxies and Geyser/Floodgate need the manual matrix. Geyser exemption uses inherited detection, not a new Bedrock anticheat.
- Translation/keybind wire transport is not implemented: no active probes are sent. The core lifecycle supports double-check/timeout/blocked/protected; no listed cheat client has a validated active detection in this alpha. Resource packs, localization, privacy mods and spoofing can confound probes.
- Meteor, Wurst, LiquidBounce, Aristois, Impact, Baritone, BleachHack and Meteor derivatives are research targets, not supported active detections. Wurst documents a fix since 7.41.1; Meteor's public issue describes version-specific behavior and is not a 26.3 validation.
- Known brands/channels are exact, weak hints. Unknown/versioned brand variants are deliberately not guessed. A normal server cannot reliably read all installed mods.
- New packet/combat modules observe diagnostic patterns and do not prove Timer, Blink or KillAura. Aim, InventoryMove and ESP behavioral extensions are not independently implemented. Pure ESP cannot be established from server-only observations.
- The optional `defender-client` mod is not delivered. A tested nonce primitive is not an installed mod or attestation system.
- History commands show the current session only. Optional JSONL is not a SQL query backend. SQL/Discord facilities are inherited from Grim, not newly written Defender modules.
- Confidence numbers are heuristic weights, not measured probabilities. Per-check evidence may still be correlated across upstream checks. No auto-enforcement consumes risk levels.
- Folia's upstream TPS API reports NaN: Defender normalizes this to unknown and does not escalate behavioral risk. Regional health integration remains work.
- PacketEvents 2.14.0-SNAPSHOT and other upstream snapshots are moving dependencies; archive exact artifacts/sources and hashes for release reproducibility.
- Primarily AI-authored new functionality requires Modrinth eligibility review under the current rules. No approval or publication readiness is claimed.
