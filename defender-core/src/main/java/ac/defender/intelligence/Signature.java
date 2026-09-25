// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.intelligence;
import ac.defender.risk.Category;
import java.util.Locale;
import java.util.Objects;
public record Signature(String id, Source source, String value, String label, Category category, double confidence) {
    public enum Source { BRAND, CHANNEL }
    public Signature {
        Objects.requireNonNull(source); Objects.requireNonNull(category);
        if (id == null || !id.matches("[a-z0-9_.-]{1,64}") || value == null || value.isBlank() || value.length() > 128
                || label == null || label.length() > 64 || category == Category.BEHAVIORAL_CHEAT
                || !Double.isFinite(confidence) || confidence < 0 || confidence > .6) throw new IllegalArgumentException("Invalid signature");
        value = value.toLowerCase(Locale.ROOT);
    }
}
