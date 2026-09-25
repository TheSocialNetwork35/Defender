# Active client-probe research — 2026-09-25

These are primary project/author sources, not proof of current detection reliability. No external implementation code was copied.

- Wurst official wiki: https://wurst.wiki/sign_translation_vulnerability — describes sign text resolving translations/keybinds and leaking them in sign edits; documents Wurst's mitigation since 7.41.1. Its recommended mitigation defeats identifying Wurst by ordinary key resolution.
- Meteor issue 6185: https://github.com/MeteorDevelopment/meteor-client/issues/6185 — closed as not planned. The report describes differences in fallback/translation sanitization for versions up to 1.21.11. It does not establish behavior on 26.3 and includes anecdotal reports; not a compatibility guarantee.
- CheckHacks source/project: https://github.com/branduzzo/CheckHacks — public server-side implementation discusses double checks, empty replies, key-plus-letter ambiguity, protected results and Bedrock exclusions. These are design risks requiring independent tests; its broad marketing claims are not adopted.
- Privacy mitigation source: https://github.com/aurickk/OpSec — illustrates that spoofing and protection are intentional client features, not evidence that a particular cheat is present.

| Target | Current decision | Reason |
|---|---|---|
| Meteor | No active signature enabled | Version-specific mitigation; no 26.3 client trace |
| Wurst | No active signature enabled | Documented mitigation; protected response cannot prove installation |
| LiquidBounce | Unvalidated / disabled | No reproducible current 26.3 positive and negative fixtures |
| Aristois | Unvalidated / disabled | Same requirement |
| Impact | Unvalidated / disabled | Same requirement |
| Baritone | Unvalidated / disabled | Mod signal is not itself behavioral cheating; missing current fixtures |
| BleachHack | Unvalidated / disabled | Missing current fixtures |
| Meteor derivatives | Unvalidated / disabled | Shared keys cannot distinguish parent from derivative |

The transport-independent ProbeSession models PENDING, DOUBLE_CHECK, SIGNAL, NO_SIGNAL, TIMEOUT, BLOCKED, PROTECTED_OR_SPOOFED and UNSUPPORTED. Confirmation requires a fresh challenge and matching exact result, with valid control. Unsolicited/replayed responses are ignored. Timeout/protection are not positive detections. This does not implement a sign packet transport.

A future transport must use client-only temporary block/sign packets, snapshot and restore client-visible state on every completion/disconnect/teleport/error path, bind responses to connection + world + position + challenge, serialize scans, allow at most one outstanding probe and use explicit version capability gates. Avoid probing language/keybind values unrelated to game integrity. Never change actual world blocks. Do not enable until real clients verify invisible UI behavior and the false-positive matrix, including resource packs and protection mods. Sign GUI traffic is not guaranteed invisible on all clients. No unsupported signature should be guessed merely to populate a feature list.
