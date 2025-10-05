package com.github.manu585.manusgroups.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;


public class MessageService {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private volatile YamlConfiguration lang;

    public MessageService(final YamlConfiguration lang) {
        this.lang = lang;
    }

    public void reload(YamlConfiguration newLang) {
        this.lang = newLang;
    }

    public Component render(String key, TagResolver... resolvers) {
        String raw = lang.getString(key);
        if (raw == null) {
            raw = "<red>Missing lang key:</red> <gray>" + key + "</gray>";
        }

        TagResolver combined = (resolvers == null || resolvers.length == 0) ? TagResolver.empty() : TagResolver.resolver(resolvers);
        return MM.deserialize(raw, combined);
    }

    public Component formatToComponent(String key, Msg... placeholders) {
        TagResolver[] tags = (placeholders == null || placeholders.length == 0) ? new TagResolver[0] : Arrays.stream(placeholders).map(Msg::toTag).toArray(TagResolver[]::new);
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
