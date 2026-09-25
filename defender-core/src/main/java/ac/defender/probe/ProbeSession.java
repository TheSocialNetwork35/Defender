// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.probe;
import java.time.*;
import java.util.*;
/** Transport-independent double-check lifecycle. No sign packets are sent by this class. */
public final class ProbeSession {
    public enum State { PENDING, DOUBLE_CHECK, SIGNAL, NO_SIGNAL, TIMEOUT, BLOCKED, PROTECTED_OR_SPOOFED, UNSUPPORTED }
    private State state = State.PENDING;
    private UUID challenge = UUID.randomUUID();
    private final Instant deadline;
    private String candidate;
    public ProbeSession(Instant now, Duration timeout) {
        if (timeout.isNegative() || timeout.isZero() || timeout.compareTo(Duration.ofMinutes(1)) > 0) throw new IllegalArgumentException("timeout");
        deadline = now.plus(timeout);
    }
    public synchronized UUID challenge() { return challenge; }
    public synchronized State state(Instant now) {
        if ((state == State.PENDING || state == State.DOUBLE_CHECK) && !now.isBefore(deadline)) state = State.TIMEOUT;
        return state;
    }
    public synchronized State answer(UUID nonce, String exactKnownMatch, boolean controlValid, Instant now) {
        state(now);
        if (state != State.PENDING && state != State.DOUBLE_CHECK) return state;
        if (!challenge.equals(nonce)) return state; // unsolicited/replayed reply
        if (!controlValid) return state = State.PROTECTED_OR_SPOOFED;
        if (exactKnownMatch == null || exactKnownMatch.isBlank()) return state = State.NO_SIGNAL;
        if (candidate == null) { candidate = exactKnownMatch; challenge = UUID.randomUUID(); return state = State.DOUBLE_CHECK; }
        return state = candidate.equals(exactKnownMatch) ? State.SIGNAL : State.PROTECTED_OR_SPOOFED;
    }
    public synchronized void block() { if (state == State.PENDING || state == State.DOUBLE_CHECK) state = State.BLOCKED; }
}
