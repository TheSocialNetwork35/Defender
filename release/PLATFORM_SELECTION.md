# Platform selection — 2026-09-25

Paper is the primary target; Purpur is the secondary target. Both use the same Bukkit-family plugin JAR. The bStats reporting-server sample returned Paper 131418, Purpur 17212 and Spigot 12250. This is not download count or a complete market survey; subsequent requests returned HTTP 403. Minecraft-version popularity could not be reliably fetched, so 1.21.11 and 26.2 are pragmatic recent stable targets; 26.3 retains the user's explicit target. No download increase is promised.

Sources: https://bstats.org/global/bukkit and https://bstats.org/api/v1/plugins/1/charts/serverSoftware/data

Folia loading is disabled pending region-thread tests. Fabric requires a separate port/build. Spigot is not advertised based on Paper tests alone. See PLATFORM_TESTS.md for exact runtime evidence.
