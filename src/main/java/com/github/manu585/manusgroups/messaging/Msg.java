package com.github.manu585.manusgroups.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Helper to build MiniMessage placeholder in a typesafe way
 */
public record Msg(String key, Tag tag) {

    /**
     * Insert a plain string that is parsed by MiniMessage
     */
    public static Msg str(String key, String value) {
        return new Msg(key, Tag.preProcessParsed(value));
    }

    /**
     * Insert a prebuilt Adventure Component
     */
    public static Msg comp(String key, Component value) {
        return new Msg(key, Tag.inserting(value));
    }

    /**
     * "(permanent)" suffix placeholder.
     */
    public static Msg permanent(boolean isPermanent) {
        return isPermanent
                ? new Msg("permanent", Tag.preProcessParsed(" <gray>(permanent)</gray>"))
                : new Msg("permanent", Tag.preProcessParsed(""));
    }

    /**
     * Convert to a named TagResolve MiniMessage understands
     */
    public TagResolver toTag() {
        return TagResolver.resolver(key, tag);
    }
}
