# Feature status

Synthetic tests are not real client compatibility or measured false-positive rates.

Feature | Implemented | Tested | False-positive risk | Notes
--- | --- | --- | --- | ---
Grim 26.3 upstream integration | Yes, inherited | Build + upstream unit tests; gameplay pending | Unknown until client matrix | a7378b39f; do not attribute port to Defender
Passive ClientIntelligence | Yes, new | Synthetic brand/category fixtures | Low as information; spoofable | No behavioral-risk contribution
Plugin/custom channels | Yes, new | Decoder bounds/UTF-8/fuzz fixtures | Low as information; partial visibility | No arbitrary mod inventory; registered-channel hints only
Reloadable signatures | Yes, new | Schema/tag/duplicate/atomic registry tests | Operator signature quality | Exact matching; bounded confidence; no commands
Risk/confidence engine | Yes, new | Dedup/decay/lag/TPS/identity separation tests | Uncalibrated cross-check correlation | Heuristic scores; no enforcement
Admin commands | Yes, new | Compilation; runtime validation recorded separately | Low; permission/UX tests pending | Online current-session history only
Structured incident logging | Yes, new | Escaping/retention/shutdown tests | Privacy/availability review pending | Opt-in; bounded JSONL; random session IDs
Payload firewall diagnostics | Yes, new | Malformed lengths/UTF-8/5000 seeded fuzz inputs | Malformed does not imply cheating | Observation only, no new kicks
Sustained movement packet bursts | Yes, new diagnostic | Normal/lag/batch/sustained fixtures | High if misused as cheat verdict | UNKNOWN; cannot raise risk
Multiple-target combat sequences | Yes, new diagnostic | Normal/lag/sustained target fixtures | High with custom mechanics | UNKNOWN; not proof of KillAura
KillAura / Reach | Grim checks inherited; diagnostic extension | Build; real combat pending | Depends on version/attributes/lag | No new guaranteed detector
Aim / rotation anomalies | Inherited only | Build; real client pending | Potential accessibility/custom-mod effects | No independent new aim model
Timer / Speed / Fly / NoFall | Grim inherited; timing diagnostic extension | Build; real movement pending | 26.3 mechanics and proxy/timing unknown | Simulation remains upstream
Scaffold / FastBreak / FastPlace | Grim inherited | Build; real block interactions pending | Custom blocks/items/latency | No independent new detector
InventoryMove | No dedicated new detector | Not tested | High from server-side GUI ambiguity | Do not infer visible client inventory state
Elytra / vehicles | Grim simulation inherited | Build; flight/vehicle matrix pending | High until 26.3 field tests | No new flight model
FakeLag / Blink | Diagnostic timing only plus inherited checks | Synthetic timing tests | High from network batching | No definitive Blink classification
Impossible interactions / packet manipulation | Grim checks + new bounded payload diagnostics | Build + decoder fixtures | Custom mechanics and protocol translation | Review upstream check context
Freecam / ESP | Inherited interaction checks only | Not field-tested | High for indirect patterns | Pure ESP is not detectably proven
Active translation/keybind probes | Lifecycle primitive only; no wire transport | Double-check/replay/timeout/protected unit tests | Unknown; version/resource-pack sensitive | checkclient returns UNSUPPORTED
defender-client companion | No; optional future module | Nonce primitive only tested | Self-report spoofing unavoidable | No mod IDs/hashes currently collected
SQL / Discord | Inherited optional facilities | No new integration test | Privacy/configuration concerns | Not Defender-specific implementations; defaults off
Folia | Adapter source retained; loading disabled in alpha.2 | Runtime pending | Regional TPS unavailable | Requires region-thread validation before enabling
Fabric server | No distributable in selected branch | Not built or tested | Unknown | Do not list on Modrinth
Geyser / Floodgate | Inherited Java-check exemption | No actual Bedrock runtime test | Proxy/exemption configuration | Never claim Bedrock movement validation
