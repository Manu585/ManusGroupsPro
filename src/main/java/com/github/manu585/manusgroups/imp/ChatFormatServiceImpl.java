package com.github.manu585.manusgroups.imp;

import com.github.manu585.manusgroups.spi.ChatFormatService;
import com.github.manu585.manusgroups.spi.PrefixService;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.concurrent.atomic.AtomicReference;

public class ChatFormatServiceImpl implements ChatFormatService {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AtomicReference<String> chatFormat = new AtomicReference<>();
    private final ChatRenderer renderer;

    public ChatFormatServiceImpl(final PrefixService prefixService, final String initialFormat) {
        this.chatFormat.set(initialFormat);

        renderer = (source, sourceDisplayName, message, viewer) -> {
            Component prefix = prefixService.cachedPrefix(source.getUniqueId());

            String format = chatFormat.get();
            if (format == null || format.isBlank()) {
                format = "<prefix> <name>: <message>";
            }

            return MM.deserialize(
                    format,
                    TagResolver.builder()
                            .tag("prefix", Tag.inserting(prefix))
                            .tag("name", Tag.inserting(source.name()))
                            .tag("message", Tag.inserting(message))
                            .build()
            );
        };
    }

    @Override
    public ChatRenderer renderer() {
        return renderer;
    }

    @Override
    public void updateFormat(String newFormat) {
        this.chatFormat.set(newFormat);
    }
}
