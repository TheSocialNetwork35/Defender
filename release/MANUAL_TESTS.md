# Required manual tests before publication

Record tester/date, exact JAR SHA256, server build, Java build, PacketEvents resolved SHA, client/version/loader/mods, protocol ID, proxy chain, ping distribution, TPS and observed evidence. Mark each case PASS/FAIL with reproducible steps. All rows below are currently NOT RUN with real gameplay clients.

| Environment | Required cases | Expected Defender behavior |
|---|---|---|
| Vanilla 26.3 | At least 60 minutes survival/creative/adventure/spectator; sprint, jump, sneak, swim, climb, knockback, teleport, respawn, dimension changes | No identity-only risk; review every upstream violation |
| Fabric 26.3 without cheats | Fabric loader alone, then Sodium/UI/accessibility mods and resource packs | Loader identification only; no cheating inference |
| Lunar | Exact current build on supported protocol, normal gameplay | Unknown or weak client guess only; no ban |
| Forge/NeoForge/Quilt/LabyMod/Badlion | Supported game versions, exact branding/channel traces | Loader/client categories remain separate; unknown variants remain unknown |
| High latency | 150/300/600 ms; jitter, packet loss/retransmission, TCP batching | Degraded health suppresses risk; no Blink verdict |
| Low TPS | 20/18/15/10 TPS, recovery, tick-rate-changing commands | Below threshold/unknown suppresses risk; diagnostic timing stays UNKNOWN |
| Versions | 26.3 plus each actually advertised older protocol through ViaVersion/ViaBackwards | Version-correct interactions; no inferred compatibility from enum presence |
| Proxies | Direct, Velocity modern forwarding, Bungee, reconnect/server switches | One session per connection; no stale history; Velocity suffix normalization only |
| Geyser/Floodgate | Direct and behind proxy; UUID/exemption paths | Bedrock excluded from Java Defender observations; no claim to check Bedrock movement |
| Folia | Multiple regions, moving across regions, teleports, disconnect while commands execute | No illegal thread access; TPS unknown prevents risk escalation |
| Bukkit/Paper | Separate actual server builds, reload/restart, duplicate-plugin refusal/operator guidance | Commands work, listener lifecycle clean, no duplicate Grim installation |
| Admin permissions | Non-op, op, explicit allow/deny, console, offline/external/exempt player | No access without defender.admin; no crash; explicit unknown states |
| Configuration | Valid/invalid YAML, duplicate keys, oversized file, tags, reload under traffic | Invalid reload preserves previous config; no arbitrary execution |
| Logging | Disk full/read-only, queue overflow, size cap, midnight, seven-day cleanup, shutdown | Bounded resources; visible dropped/error counters; no Netty blocking |
| Controlled cheat tests | KillAura/reach/timer/speed/fly/nofall/scaffold/fastbreak/fastplace/elytra/Blink/invalid packets | Capture server-observable behavior; distinguish inherited checks from new diagnostics |
| Inventories/interactions | Chests, horses, boats, crafting, containers, item use, range attributes, custom server items | No fixed-distance assumptions; legitimate mechanics preserved |
| 26.3 content | All new blocks/states/collision shapes, entity interpolation, fluids, attributes and packets | Compare actual client movement traces against upstream prediction |
| Vehicles/elytra | Boats/minecarts/mounts, passenger transitions, rockets, landing, water, portals | No legitimate repeated violations; compare multiple latency conditions |
| Freecam/ESP | Remote interactions vs normal line-of-sight, occluded entities, permitted spectator behavior | Never label pure ESP as proven; only review observable interactions |

Translation/keybind transport: NOT IMPLEMENTED. Before future enablement, test each exact target client/version and clean control in multiple languages, modified keybinds, server/client resource packs, privacy/protection mods and proxies. Verify double checking, replay rejection, timeout and cleanup without world changes or visible UI disruption.

Optional companion: NOT SHIPPED. Future testing must cover explicit opt-in, disclosure, maximum list size, payload/hash allowlist, nonce replay/expiry/session binding, malformed messages, disconnect cleanup and spoofed self-reports. Spoofable attestation is not proof of a clean client.

Release acceptance: no unexplained clean-client flags in the agreed recorded workload, no client-signal penalties, no leaks/unbounded state, reproducible builds and no unsupported version/platform promises. Define and publish workload size and measured false-positive rate; do not claim zero false positives from a small sample.
