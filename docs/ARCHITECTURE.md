# Architecture

`defender-core` has no Grim, Bukkit, Folia or PacketEvents dependency. Its packages are independent responsibilities: `intelligence` (safe signature registry and sessions), `risk` (typed evidence and assessment), `firewall` (bounded decoders and observation windows), `logging` (bounded asynchronous incidents), `probe` (double-check lifecycle), `companion` (one-use challenge primitive). These are logical modules in one Gradle library to keep this alpha's dependency surface small.

`common/.../ac/defender/platform` adapts PacketEvents to these modules and registers Cloud commands. The only Grim bridge edits are lifecycle start/stop, command registration, post-accepted-flag recording and disconnect cleanup. Defaults disable brand-only Forge disconnection, upstream update checking and bStats. Original checks, simulation and attribution remain in `ac.grim`.

Netty-owned data enters synchronized bounded session objects. Command reads return immutable copies. YAML reload builds a complete candidate before replacing the registry. Packet paths perform no disk writes, world access or network requests. One daemon writer owns local JSONL and never grows an unbounded queue. TPS is sampled off the packet thread, becomes unknown after three seconds, and remains unknown on Folia until a region-aware metric is available. Diagnostic packet/combat hints remain UNKNOWN and therefore cannot increase behavior risk.

Raw client payloads are discarded after strict decoding. Only predefined labels reach evidence logs. The risk engine ignores weak identification signals, uses maximum per check rather than repeated accumulation, requires multiple checks for high levels, decays observations and ignores unhealthy/unknown health samples. Check independence is heuristic; no enforcement consumes the output.

Active probe transport and a deployable companion are deliberately absent, not represented as finished modules. Their tested primitives are future interfaces, not compatibility claims. Existing Grim movement/combat enforcement is independent of Defender's observation-only behavior.
