package com.github.manu585.manusgroups.service;

import com.github.manu585.manusgroups.service.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;


public class MessageService {
    // Shared MiniMessage instance
    private static final MiniMessage MM = MiniMessage.miniMessage();

    // Active language configuration file. Volatile so background reloads are visible to renderers without locking
    private volatile YamlConfiguration lang;

    public MessageService(final YamlConfiguration lang) {
        this.lang = lang;
    }

    /**
     * Replace the active language configuration
     * @param newLang new language yml
     */
    public void reload(final YamlConfiguration newLang) {
        this.lang = newLang;
    }

    /**
     * Resolve a message by key, parse it with MiniMessage and return it as an Component
     * @param key The language key to render
     * @param resolvers optional MiniMessage TagResolvers to supply placeholders
     * @return The rendered Component
     */
    public final @NotNull Component render(final String key, final TagResolver... resolvers) {
        String raw = lang.getString(key);
        if (raw == null) {
            raw = "<red>Missing lang key:</red> <gray>" + key + "</gray>";
        }

        final TagResolver combined = (resolvers == null || resolvers.length == 0) ? TagResolver.empty() : TagResolver.resolver(resolvers);
        return MM.deserialize(raw, combined);
    }

    /**
     * Overload that accepts {@link Msg} helpers and renders a message by key
     * @param key The language key to render
     * @param placeholders zero or more {@link Msg} placeholders to resolve in the message
     * @return the rendered Component
     */
    public final @NotNull Component formatToComponent(String key, Msg... placeholders) {
        final TagResolver[] tags = (placeholders == null || placeholders.length == 0) ? new TagResolver[0] : Arrays.stream(placeholders).map(Msg::toTag).toArray(TagResolver[]::new);
        return render(key, tags);
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(render(key));
    }

    public void send(CommandSender sender, String key, Msg... placeholders) {
        sender.sendMessage(formatToComponent(key, placeholders));
    }

    public MiniMessage mm() {
        return MM;
    }
}
