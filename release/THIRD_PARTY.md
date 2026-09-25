# Third-party dependency audit

`DEPENDENCIES.tsv` lists exact runtime coordinates, filenames and SHA-256 digests from Gradle resolution. `third-party-notices/` retains embedded licenses/notices under separate coordinate directories, avoiding collisions during shading. `NOTICE_AUDIT.tsv` identifies artifacts without embedded notices; absence of an embedded notice is not proof of a permissive license.

`DEPENDENCY_SOURCES.txt` records source artifact availability; collected sources are in `dependency-sources/`. Missing sources or licenses require resolution before binary publication. The root/source release preserves Grim's GPL text and in-file credits. The server JAR embeds the root license, attribution and collected notices under `META-INF/defender/`.

Important upstream projects:

- GrimAC: https://github.com/GrimAnticheat/Grim — GPL-3.0. GrimAPI/internal modules: https://github.com/GrimAnticheat/GrimAPI — the retrieved GrimAPI LICENSE is MIT; preserve its authorship and license text. All resolved API/internal source artifacts are included.
- PacketEvents: https://github.com/retrooper/packetevents — GPL-3.0; use exact snapshot sources matching the inventory, not an assumed latest branch.
- Cloud: https://github.com/Incendo/cloud — MIT.
- Adventure: https://github.com/KyoriPowered/adventure — MIT.
- SnakeYAML: https://bitbucket.org/snakeyaml/snakeyaml — Apache-2.0.
- fastutil: https://github.com/vigna/fastutil — Apache-2.0.
- HikariCP: https://github.com/brettwooldridge/HikariCP — Apache-2.0.
- SLF4J: https://github.com/qos-ch/slf4j — MIT.
- Configuralize: https://github.com/Scarsz/Configuralize — inspect bundled artifact/POM and preserved notices.

The Maven POMs, source trees and exact embedded notices remain the authoritative version-specific license records. This file is an engineering audit aid, not a claim of legal certification. Shaded dependencies and moving snapshots must be checked with the exact release artifacts. Gradle/Java build tooling and Minecraft/Paper server binaries are not bundled in the distributable.

Reproduce inventory with `./gradlew :bukkit:defenderDependencyInventory`; collect source artifacts with `./gradlew :bukkit:defenderDependencySources`; extract notices with `python3 tools/collect_notices.py`; then rebuild so the shaded JAR contains those notices. Publish corresponding source with equal access alongside any public binary.
