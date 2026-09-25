// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.risk;
import java.time.*;
import java.util.*;
/** Fingerprints cannot increase behavioral risk. Repetition of one check cannot yield HIGH. */
public final class RiskEngine {
    public enum Level { LOW, MEDIUM, HIGH, VERY_HIGH }
    public record Assessment(Level level, double behavioralScore, double identificationConfidence, int independentChecks) {}
    public Assessment assess(Collection<Evidence> observations, Instant now) {
        Map<String, Double> checks = new HashMap<>();
        double identity = 0;
        for (Evidence e : observations) {
            long age = Duration.between(e.timestamp(), now).toMillis();
            if (age < 0 || age > 300_000) continue;
            if (e.category() != Category.BEHAVIORAL_CHEAT) {
                if (e.category() == Category.CLIENT || e.category() == Category.MOD_LOADER || e.category() == Category.KNOWN_MOD_SIGNAL)
                    identity = Math.max(identity, Math.min(.6, e.confidence()));
                continue;
            }
            // Unknown health, lag and overloaded servers cannot increase the risk score.
            if (e.ping() < 0 || e.ping() > 250 || e.tps() < 18) continue;
            double score = e.confidence() * Math.exp(-age / 120_000.0) * 35;
            checks.merge(e.check(), score, Math::max);
        }
        double score = Math.min(100, checks.values().stream().mapToDouble(Double::doubleValue).sum());
        Level level = score >= 85 && checks.size() >= 3 ? Level.VERY_HIGH
                : score >= 60 && checks.size() >= 2 ? Level.HIGH : score >= 25 ? Level.MEDIUM : Level.LOW;
        return new Assessment(level, score, identity, checks.size());
    }
}
