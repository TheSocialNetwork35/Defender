// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.companion;
import java.security.*;
import java.time.*;
import java.util.*;
/** Optional self-report handshake primitive; possession of a nonce does NOT attest a clean client. */
public final class ChallengeRegistry {
    private record Pending(byte[] nonce, Instant expires) {}
    private final Map<UUID, Pending> pending = new HashMap<>();
    private final SecureRandom random = new SecureRandom();
    public synchronized byte[] issue(UUID session, Instant now) {
        pending.values().removeIf(p -> !now.isBefore(p.expires()));
        if (pending.size() >= 1024 && !pending.containsKey(session)) throw new IllegalStateException("challenge capacity");
        byte[] nonce = new byte[32]; random.nextBytes(nonce);
        pending.put(session, new Pending(nonce.clone(), now.plusSeconds(15))); return nonce;
    }
    public synchronized boolean consume(UUID session, byte[] nonce, Instant now) {
        Pending p = pending.remove(session);
        return p != null && now.isBefore(p.expires()) && MessageDigest.isEqual(p.nonce(), nonce);
    }
    public synchronized void disconnect(UUID session) { pending.remove(session); }
}
