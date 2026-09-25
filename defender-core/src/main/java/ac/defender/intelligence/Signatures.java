// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.intelligence;
import ac.defender.risk.Category;
import java.io.*;
import java.util.*;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.SafeConstructor;
/** Exact matches only: no untrusted regex, expressions, tags or executable commands. */
public final class Signatures {
    private Signatures() {}
    public static List<Signature> load(InputStream input) {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false); options.setMaxAliasesForCollections(0); options.setCodePointLimit(65536);
        Object document = new Yaml(new SafeConstructor(options)).load(input);
        if (!(document instanceof Map<?, ?> root) || !Integer.valueOf(1).equals(root.get("schema"))
                || !(root.get("signatures") instanceof List<?> rows) || rows.size() > 256) throw new IllegalArgumentException("Signature schema");
        List<Signature> result = new ArrayList<>(); Set<String> ids = new HashSet<>();
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> m)) throw new IllegalArgumentException("Signature row");
            Signature s = new Signature((String)m.get("id"), Signature.Source.valueOf((String)m.get("source")),
                    (String)m.get("value"), (String)m.get("label"), Category.valueOf((String)m.get("category")),
                    ((Number)m.get("confidence")).doubleValue());
            if (!ids.add(s.id())) throw new IllegalArgumentException("Duplicate signature id");
            result.add(s);
        }
        return List.copyOf(result);
    }
}
