package net.enelson.sopachievements.util;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class ValueMatcher {

    private final boolean any;
    private final boolean negated;
    private final Set<String> values;

    private ValueMatcher(boolean any, boolean negated, Set<String> values) {
        this.any = any;
        this.negated = negated;
        this.values = values;
    }

    public static ValueMatcher parse(String raw) {
        if (raw == null || raw.trim().isEmpty() || "any".equalsIgnoreCase(raw.trim())) {
            return new ValueMatcher(true, false, new LinkedHashSet<String>());
        }

        String normalized = raw.trim();
        boolean negated = normalized.startsWith("!");
        if (negated) {
            normalized = normalized.substring(1);
        }

        Set<String> parsed = new LinkedHashSet<String>();
        for (String token : normalized.split(",")) {
            String value = token.trim();
            if (!value.isEmpty()) {
                parsed.add(value.toUpperCase(Locale.ROOT));
            }
        }
        if (parsed.isEmpty()) {
            return new ValueMatcher(true, false, new LinkedHashSet<String>());
        }
        return new ValueMatcher(false, negated, parsed);
    }

    public boolean matches(String value) {
        if (value == null) {
            return false;
        }
        if (any) {
            return true;
        }
        boolean contains = values.contains(value.trim().toUpperCase(Locale.ROOT));
        return negated ? !contains : contains;
    }
}
