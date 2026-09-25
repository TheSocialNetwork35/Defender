// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.firewall;
import java.util.HashSet;
import java.util.Set;
/** Repeated multi-target attack bursts are a review signal, not a KillAura verdict. */
public final class CombatWindow {
    private final Set<Integer> targets = new HashSet<>();
    private long start = -1;
    private int bursts;
    public synchronized boolean attack(int entityId, long nanos, int ping, double tps) {
        if (ping < 0 || ping > 150 || !Double.isFinite(tps) || tps < 19 || tps > 20) {
            targets.clear(); bursts = 0; start = nanos; return false;
        }
        boolean signal = false;
        if (start < 0 || nanos < start) start = nanos;
        if (nanos - start >= 1_000_000_000L) {
            bursts = nanos - start <= 2_000_000_000L && targets.size() >= 8 ? bursts + 1 : 0;
            signal = bursts >= 3;
            targets.clear(); start = nanos;
        }
        if (targets.size() < 32) targets.add(entityId);
        return signal;
    }
}
