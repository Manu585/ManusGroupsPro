package com.github.manu585.manusgroups.permissions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PermissionExpander {
    private PermissionExpander() {}

    public static Map<String, Boolean> expand(final Map<String, Boolean> rawNodes, final Collection<String> registeredPermissions) {
        if (rawNodes == null || rawNodes.isEmpty()) return Collections.emptyMap();

        Map<String, Boolean> positives = new LinkedHashMap<>();
        Map<String, Boolean> negatives = new LinkedHashMap<>();

        for (Map.Entry<String, Boolean> entry : rawNodes.entrySet()) {
            (entry.getValue() ? positives : negatives).put(entry.getKey(), entry.getValue());
        }

        Map<String, Boolean> out = new LinkedHashMap<>();

        // Expand positives
        for (Map.Entry<String, Boolean> entry : positives.entrySet()) {
            expandOne(entry.getKey(), true, registeredPermissions, out);
        }

        // Expand negatives
        for (Map.Entry<String, Boolean> entry : negatives.entrySet()) {
            expandOne(entry.getKey(), false, registeredPermissions, out);
        }

        return out;
    }

    private static void expandOne(final String key, final boolean value, final Collection<String> registered, final Map<String, Boolean> out) {
        if ("*".equals(key)) {
            for (String permission : registered) {
                out.put(permission, value);
            }
            return;
        }

        if (key.endsWith(".*")) {
            final String prefix = key.substring(0, key.length() - 2);
            final String prefixDot = prefix + ".";
            for (String permission : registered) {
                if (permission.equals(prefix) || permission.startsWith(prefixDot)) {
                    out.put(permission, value);
                }
            }
            return;
        }

        // Exact node
        out.put(key, value);
    }
}
