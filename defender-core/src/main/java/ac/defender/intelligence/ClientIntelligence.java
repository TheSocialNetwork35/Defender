// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.intelligence;
import ac.defender.risk.*;
import java.time.Instant;
import java.util.*;
/** Session-local passive classification; no arbitrary mod enumeration and no identity tracking. */
public final class ClientIntelligence {
    public record Match(String signal, String label, Category category, double confidence) {}
    private volatile List<Signature> signatures;
    public ClientIntelligence(List<Signature> signatures) { this.signatures = List.copyOf(signatures); }
    public void replace(List<Signature> signatures) { this.signatures = List.copyOf(signatures); }
    public List<Match> classify(Signature.Source source, String value) {
        if (value == null || value.length() > 128) return List.of();
        String normalized = value.toLowerCase(Locale.ROOT);
        // Only the explicitly known Velocity brand suffix is normalized.
        if (source == Signature.Source.BRAND && normalized.endsWith(" (velocity)"))
            normalized = normalized.substring(0, normalized.length() - 11);
        List<Match> result = new ArrayList<>();
        for (Signature s : signatures) if (s.source() == source && s.value().equals(normalized))
            result.add(new Match(s.id(), s.label(), s.category(), s.confidence()));
        return List.copyOf(result);
    }
    public static final class Session {
        private final UUID id = UUID.randomUUID();
        private final Deque<Evidence> observations = new ArrayDeque<>();
        private final Set<String> seen = new HashSet<>();
        private String brand = "unknown";
        private String loader = "unknown";
        public UUID id() { return id; }
        public synchronized String brand() { return brand; }
        public synchronized String loader() { return loader; }
        public synchronized void label(Match m) {
            if (m.category() == Category.MOD_LOADER) loader = m.label();
            if (m.category() == Category.CLIENT) brand = m.label();
        }
        public synchronized boolean add(Evidence e) {
            Evidence last = observations.peekLast();
            if (last != null && last.check().equals(e.check()) && last.signal().equals(e.signal())
                    && e.timestamp().toEpochMilli() - last.timestamp().toEpochMilli() < 1000) return false;
            if (observations.size() >= 128) observations.removeFirst();
            observations.addLast(e); return true;
        }
        public synchronized boolean first(String key) {
            if (seen.size() >= 256) return false;
            return seen.add(key);
        }
        public synchronized List<Evidence> snapshot(Instant now) {
            observations.removeIf(e -> e.timestamp().isBefore(now.minusSeconds(1800)));
            return List.copyOf(observations);
        }
    }
}
