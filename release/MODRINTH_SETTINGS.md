# Publication settings (draft; blocked)

Project type: plugin. Name: Defender. Status: draft. Release channel: alpha.
License: GPL-3.0. Source: https://github.com/TheSocialNetwork35/Defender
Issues: https://github.com/TheSocialNetwork35/Defender/issues
Client side: unsupported/not required. Server side: required.
Loaders for the experimental alpha: Paper and Purpur only, subject to PLATFORM_TESTS.md. Folia is disabled; Bukkit/Spigot are not separately validated. No Fabric artifact.
Experimental runtime test targets: Paper 1.21.11, 26.2, 26.3; Purpur 1.21.11, 26.2. These are startup/reload tests only, not completed gameplay validation. Do not infer Purpur 26.3 from the combined loader/version selection. Use separate Modrinth version entries for Paper (1.21.11, 26.2, 26.3) and Purpur (1.21.11, 26.2) if publishing after the checklist. Both use the same JAR. Correct auto-detected fields manually; plugin.yml cannot encode this matrix.
PacketEvents is shaded in the supplied Bukkit build; do not additionally require its standalone plugin for that artifact. For any future lite build, declare a required compatible PacketEvents dependency on the version page. Declare other selected optional integrations accurately.
Derivative disclosure: YES, GrimAC. AI code/text: YES. AI images: none; upload none. Runtime AI: NO.
Do not publish until PUBLISH_CHECKLIST.md is complete, including Modrinth AI/substantive-fork eligibility.
