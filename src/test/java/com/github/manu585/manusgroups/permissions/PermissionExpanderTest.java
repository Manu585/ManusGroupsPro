package com.github.manu585.manusgroups.permissions;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PermissionExpander}
 * <ul>
 *     <li>Expands "*" to all registered permissions</li>
 *     <li>Expands "prefix.*" to both "prefix" and everything under "prefix."</li>
 *     <li>Keeps exact nodes as is</li>
 *     <li>Ensures that negatives override positives for the same node</li>
 * </ul>
 */
class PermissionExpanderTest {
    @Test
    void expandsStarToAll() {
        final Map<String, Boolean> raw = Map.of("*", true);
        final List<String> registered = List.of("essentials.fly", "test.test", "foo.bar");

        final Map<String, Boolean> out = PermissionExpander.expand(raw, registered);

        // Every registered permission should be present and true
        assertThat(out).containsEntry("essentials.fly", true).containsEntry("test.test", true).containsEntry("foo.bar", true);

        // And nothing extra should appear
        assertThat(out).hasSize(3);
    }

    @Test
    void expandsPrefixStarAndOverridesNegatives() {
        final Map<String, Boolean> raw = new LinkedHashMap<>();
        raw.put("essentials.*", true);
        raw.put("essentials.fly", false);

        final List<String> registered = List.of("essentials.fly", "essentials.god", "foo.bar");

        final Map<String, Boolean> out = PermissionExpander.expand(raw, registered);

        // "essentials.*" should have granted both "essentials" and everything starting with it.
        // From our list, "essentials.god" matches, should be true
        assertThat(out).containsEntry("essentials.god", true);

        // negative permission must override the earlier positive for the same node
        assertThat(out).containsEntry("essentials.fly", false);

        // Unrelated nodes are untouched
        assertThat(out).doesNotContainKey("foo.bar");
    }

    @Test
    void passesThroughExactNodes() {
        final Map<String, Boolean> raw = Map.of("foo.bar", true);
        final List<String> registered = List.of("foo.bar");

        final Map<String, Boolean> out = PermissionExpander.expand(raw, registered);

        // The exact node passes straight through
        assertThat(out).containsEntry("foo.bar", true);
        assertThat(out).hasSize(1);
    }
}
