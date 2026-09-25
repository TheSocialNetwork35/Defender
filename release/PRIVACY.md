# Privacy

Defender stores technical signals in memory using a random per-connection session identifier. It uses the existing authenticated player UUID transiently to associate an online player with a session, and removes that map entry on disconnect. It does not persist player names, account UUIDs, IP addresses, hardware identifiers or raw unknown payloads. Known brands/channels are normalized to predefined labels. It does not enumerate arbitrary mods, read files from clients, query hardware or transmit telemetry to third-party servers.

Each session holds at most 128 observations. Entries expire after 30 minutes when queried; the entire session is removed on disconnect. The five-minute risk window is independent. Passive fingerprints are spoofable.

Local JSONL is OFF by default (`incident-log: false`). If enabled, it includes random session ID, signal/category/confidence/time/check/evidence/protocol/ping/TPS/client label. Retention is seven UTC dates with a 16 MiB/day cap and a 2048-entry async queue; pruning runs on the first logged event of each day. Stopped servers retain existing files until logging resumes or the operator deletes them. Logs are accessible to the server operator; use suitable filesystem permissions and explain collection to players. Timestamps and sessions can still be linkable using other server records; do not call this guaranteed anonymization.

The inherited Grim history database, console logs, alerts, optional Discord webhooks and diagnostic upload commands are separate facilities. They may contain player names, UUIDs and verbose details; consult and configure the inherited settings. Defender disables the inherited automatic update check and brand-based legacy Forge disconnect by default. Discord is off in the supplied defaults. Enabling operator-selected webhooks transmits to that endpoint. No Defender remote endpoint exists.

No companion mod is shipped in this alpha. Any future voluntary mod list/hash reporting must be disclosed and explicitly enabled; a nonce does not prove that a client is honest.

Defender also disables the inherited database master toggle in all shipped locales by default; operators can explicitly enable upstream persistence. Both upstream and PacketEvents bStats startup are disabled. Existing operator configuration is not silently overwritten.
