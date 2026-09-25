# Security

This alpha is not audited. Do not treat signals, a clean report, or a future client self-report as proof. Defender never runs commands from signatures and adds no automatic punishments. Upstream Grim enforcement is independent and must be configured and tested by operators.

Signatures use SnakeYAML SafeConstructor, duplicate-key rejection, no collection aliases, a 64 KiB codepoint budget, 256-row limit, bounded fields and exact matches; no executable tags or regexes. Payload decoding is bounded and strict UTF-8. Session history, diagnostic target sets, nonce registry and asynchronous logging queue have explicit limits. UI uses plain text components.

Send reproducible vulnerabilities to the repository owner using GitHub private vulnerability reporting if enabled; otherwise request a private reporting channel without posting exploit details. No dedicated security response SLA exists. Do not send private player logs publicly.

Remaining review areas: snapshot dependency supply chain, platform lifecycle/races, upstream enforcement, untested Folia/Fabric behavior and adversarial clients. Negative detection results never establish cleanliness. A fully controlled client can emulate any optional companion handshake and fabricate mod/hash reports.
