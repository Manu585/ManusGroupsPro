package com.github.manu585.manusgroups.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class Msg {
    private Msg() {}

    public static TagResolver comp(String name, Component value) {
        return TagResolver.resolver(name, Tag.inserting(value == null ? Component.empty() : value));
    }

    public static TagResolver str(String name, String value) {
        return comp(name, Component.text(value == null ? "" : value));
    }

    public static TagResolver permanent(boolean isPermanent) {
        return str("permanent", isPermanent ? " (permanent)" : "");
    }
}
