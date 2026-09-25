# Minecraft 26.3 audit

Primary release reference: https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3 (retrieved 2026-09-25). Distinguish Java Edition 26.3 from the unrelated Bedrock hotfix with the same number.

Grim base: `ver/26.3` at `a7378b39f2caa5fd5638a10220624fc14ae62be6`. This fast-forwards `2.0` at `8eb5f2809591c891deb4958bb2927844871e0600`. Those 54 upstream-changed files and their history are preserved; no manual reimplementation was presented as original work.

| Area | Source reviewed / build evidence | Remaining proof |
|---|---|---|
| PacketEvents / new packets | 2.14.0-SNAPSHOT API resolves and compiles upstream 26.3 handlers | Record exact resolved SHA; test login/config/play and malformed packet behavior |
| Protocol / ViaVersion | Upstream ViaMovementTranslator and packet listener changes | Direct and proxy translated-client traces |
| Movement prediction | MovementCheckRunner, MovementTicker, EntityFluidInteraction | Replay actual 26.3 legitimate movement in all states |
| Entities | PacketEntityReplication; legacy and stepped interpolation hierarchy | Entity motion, riding and hitbox traces |
| Blocks / collisions | CollisionData, HitboxData, BlockProperties, BlockPlaceResult | New block states/shapes, liquids, placement, break timings |
| Attributes | PacketEntitySelf and compensated entity handling inherited | Reach/movement/scale/step attribute changes and server modifiers |
| Vehicles | PacketPlayerSteer and entity replication inherited | Each rideable entity; passenger transitions and latency |
| Elytra / gliding | Existing simulation retained | Rocket boost, water, landing, teleport and 26.3 changes |
| Inventories | Existing handlers retained | Every container, cursor changes and custom server mechanics |
| Interactions / combat | PacketPlayerAttack, packet ordering and range checks retained | Creative/survival/custom attributes and knockback under latency |
| Bukkit / Paper | Bukkit module compiles; actual smoke result recorded in TEST_RESULTS | Gameplay; separate implementation builds |
| Folia | Upstream scheduler adapter and descriptor retained | Region/thread tests; regional health integration |
| Fabric | Disabled by chosen upstream settings | Port, compile and run Fabric separately before any claim |

Compilation demonstrates API consistency, not physics correctness or comprehensive protocol coverage. All manual validation is listed in release/MANUAL_TESTS.md. No unsupported version was added merely by extending a version enum.
