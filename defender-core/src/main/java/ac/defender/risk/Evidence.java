// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.risk;
import java.time.Instant;
import java.util.Objects;
/** An observation, never proof of cheating. Unknown telemetry is represented by -1. */
public record Evidence(String signal, Category category, double confidence, Instant timestamp,
                       String check, String evidence, int protocolVersion, int ping, double tps,
                       String clientInformation) {
    public Evidence {
        Objects.requireNonNull(category); Objects.requireNonNull(timestamp);
        if (!Double.isFinite(confidence) || confidence < 0 || confidence > 1) throw new IllegalArgumentException("confidence");
        signal = clean(signal, 96); check = clean(check, 96);
        evidence = clean(evidence, 512); clientInformation = clean(clientInformation, 128);
        if (!Double.isFinite(tps) || tps < 0 || tps > 20) tps = -1;
        if (ping < 0) ping = -1;
    }
    public static String clean(String value, int limit) {
        if (value == null) return "unknown";
        StringBuilder out = new StringBuilder();
        value.codePoints().limit(limit).forEach(c -> { if (!Character.isISOControl(c)) out.appendCodePoint(c); });
        return out.toString();
    }
}
