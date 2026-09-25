# 0.1.0-alpha.2

- Raise the declared Bukkit API floor from 1.13 to 1.21.11, matching the oldest selected modern runtime test target. This is not a list of supported Minecraft versions.
- Disable the inherited Folia support declaration until region-threaded operation has been validated.
- Test one shared Bukkit-family artifact on selected Paper and Purpur server builds; see PLATFORM_TESTS.md for exact outcomes and checksums.
- No new cheat checks or additional gameplay guarantees in this version. Previous limitations remain.

This is an experimental alpha. Startup/reload testing does not establish gameplay compatibility or false-positive rates. Set exact tested game versions and loaders manually on Modrinth; automatic detection from plugin.yml is incomplete. No Fabric artifact.
