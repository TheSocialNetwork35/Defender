// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.firewall;
/** Diagnostic burst detector. TCP batching, proxies and lag are NOT proof of Blink. */
public final class PacketWindow {
    private long start = -1;
    private int count;
    private int consecutive;
    public synchronized boolean movement(long nanos, int ping, double tps) {
        if (start < 0 || nanos < start) { start = nanos; count = 1; consecutive = 0; return false; }
        count++;
        if (nanos - start < 5_000_000_000L) return false;
        double rate = count * 1_000_000_000.0 / (nanos - start);
        start = nanos; count = 0;
        if (ping < 0 || ping > 250 || !Double.isFinite(tps) || tps < 18 || tps > 20) { consecutive = 0; return false; }
        consecutive = rate > 60 ? consecutive + 1 : 0;
        return consecutive >= 3;
    }
}
