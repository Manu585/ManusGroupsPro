package com.github.manu585.manusgroups.permissions;

import com.github.manu585.manusgroups.service.util.PermissionExpander;
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
        assertThat(out).containsEntry("essentials.fly", true);
        assertThat(out).containsEntry("test.test", true);
        assertThat(out).containsEntry("foo.bar", true);

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

    @Test
    void globalDenyMarksAllFalse() {
        final Map<String, Boolean> raw = Map.of("*", false);
        final List<String> registered = List.of("foo.bar", "bar.foo");

        final Map<String, Boolean> out = PermissionExpander.expand(raw, registered);

        assertThat(out).containsEntry("foo.bar", false);
        assertThat(out).containsEntry("bar.foo", false);
        assertThat(out).hasSize(2);
    }

    @Test
    void unknownPrefixStartIsIgnored() {
        final Map<String, Boolean> raw = Map.of("notexisting.*", true);
        final List<String> registered = List.of("essentials.fly", "foo.bar");

        final Map<String, Boolean> out = PermissionExpander.expand(raw, registered);

        assertThat(out).isEmpty();
    }

    @Test
    void laterExplicitOverridesEarlierWildcardEvenIfOpposite() {
        final Map<String, Boolean> raw = new LinkedHashMap<>();
        raw.put("essentials.*", false); // First deny essential
        raw.put("essentials.fly", true); // Then explicitly allow fly

        final List<String> registered = List.of("essentials.fly", "essentials.god");

        final Map<String, Boolean> out = PermissionExpander.expand(raw, registered);

        assertThat(out).containsEntry("essentials.fly", true); // explicit override
        assertThat(out).containsEntry("essentials.god", false); // still denied via wildcard
        assertThat(out).hasSize(2);
    }

    @Test
    void multiplePrefixesPlusExplicitNegative() {
        final Map<String, Boolean> raw = new LinkedHashMap<>();
        raw.put("essentials.*", true);
        raw.put("groupspro.*", true);
        raw.put("essentials.god", false); // exact negative given

        final List<String> registered = List.of("essentials.god", "essentials.fly", "groupspro.admin");

        final Map<String, Boolean> out = PermissionExpander.expand(raw, registered);

        assertThat(out).containsEntry("essentials.fly", true);
        assertThat(out).containsEntry("groupspro.admin", true);
        assertThat(out).containsEntry("essentials.god", false);
        assertThat(out).hasSize(3);
    }
}
