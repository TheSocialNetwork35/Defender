# Alpha.2 runtime verification — 2026-09-25

Artifact: `defender-bukkit-0.1.0-alpha.2.jar`

SHA256: `f4f7483c483d072bd81ad490fc428c6b69dc79e4ba89a7c4cdec0b18ff9fdc0e`

| Platform | Minecraft | Server build | Java | Startup / reload / shutdown |
|---|---|---|---|---|
| Paper | 1.21.11 | 132 | 21 | Passed |
| Paper | 26.2 | 129 | 25 | Passed |
| Paper | 26.3 | 41 | 25 | Passed |
| Purpur | 1.21.11 | 2568 | 21 | Passed |
| Purpur | 26.2 | 2633 | 25 | Passed |

All five final runs used the exact distributed artifact (code revision 9ffdee6), reached server Done, enabled Defender, returned its version, reloaded Defender configuration/signatures, rejected an invalid player selector, disabled Defender and exited with status 0. No ERROR/Exception log entries in final runs. Existing JVM/native-library warnings are not plugin failures. Preliminary 1.21.11 runs used the redundant command-line EULA flag, which logged agreement notices at ERROR level; final runs used eula.txt only.

The test servers were isolated on loopback ports 25630–25634 with small flat worlds and fresh Defender configurations in the initial run. Final exact-artifact runs reused those temporary test worlds/configurations. Java 21.0.12.1+1 for 1.21.11; Java 25.0.4.1+1 for 26.x; macOS ARM64. Server download URLs/build IDs/checksums and logs are under platform-tests-alpha.2. Paper downloads were checked against official SHA256 values; Purpur downloads against its published MD5, with SHA256 additionally recorded.

No player joined. Movement, combat, inventories, latency, proxies, false positives and actual client compatibility were NOT tested here. Follow MANUAL_TESTS.md before production use. Versions between tested points are not implicitly validated. Folia is deliberately disabled, Fabric is not built, and Bukkit/Spigot were not separately tested. Purpur 26.3 was not tested. One JAR covers the listed combinations; renaming it per platform would not add compatibility.

Build: `JAVA_HOME=<JDK21> ./gradlew --offline --no-daemon build -Prelease=true` succeeded; 47 JUnit tests, zero failures/errors/skips. Source ZIP and dependency source archives accompany the artifact. Original Grim license/credits remain; this update changes release metadata, not detection algorithms.

For an experimental Modrinth upload after the publication checklist: Paper entry selects 1.21.11, 26.2, 26.3; a separate Purpur entry selects 1.21.11, 26.2. Both use the same JAR. This avoids implying Purpur 26.3 from a combined list. Use Alpha and explicitly disclose the smoke-test-only scope. Automatic upload detection cannot express this matrix.

Modrinth rules rechecked on 2026-09-25: https://modrinth.com/legal/rules (modified August 13, 2026). Existing derivative/AI disclosures and publication gates remain applicable. No Modrinth upload was performed.
