package com.github.manu585.manusgroups.service.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PermissionExpander {
    private PermissionExpander() {}

    public static Map<String, Boolean> expand(final Map<String, Boolean> raw, final List<String> registered) {
        final Map<String, Boolean> out = new LinkedHashMap<>();

        for (final Map.Entry<String, Boolean> entry : raw.entrySet()) {
            final String node = entry.getKey();
            final boolean value = entry.getValue();

            // Global star -> all registered nodes
            if ("*".equals(node)) {
                for (String r : registered) {
                    out.put(r, value);
                }
                continue;
            }

            // Prefix star -> "prefix" and everything starting with "prefix."
            if (node.endsWith(".*")) {
                final String prefix = node.substring(0, node.length() - 2);
                for (String r : registered) {
                    if (r.equals(prefix) || r.startsWith(prefix + ".")) {
                        out.put(r, value);
                    }
                }
                continue;
            }

            out.put(node, value);
        }
        return out;
    }
}
