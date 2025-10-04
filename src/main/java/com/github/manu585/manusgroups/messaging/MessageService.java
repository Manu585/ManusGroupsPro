package com.github.manu585.manusgroups.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageService {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private volatile YamlConfiguration lang;

    public MessageService(final YamlConfiguration lang) {
        this.lang = lang;
    }

    public void reload(YamlConfiguration newLang) {
        this.lang = newLang;
    }

    public Component render(String key, TagResolver... placeholders) {
        String raw = lang.getString(key);
        if (raw == null) {
            raw = "<red>Missing lang key:</red> <gray>" + key + "</gray>";
        }
        return MM.deserialize(raw, placeholders == null ? TagResolver.empty() : TagResolver.resolver(placeholders));
    }

    public void send(CommandSender sender, String key, TagResolver... placeholders) {
        sender.sendMessage(render(key, placeholders));
    }
}
