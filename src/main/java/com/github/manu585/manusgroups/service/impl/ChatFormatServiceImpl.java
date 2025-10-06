package com.github.manu585.manusgroups.service.impl;

import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.spi.ChatFormatService;
import com.github.manu585.manusgroups.service.spi.PrefixService;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.concurrent.atomic.AtomicReference;

public class ChatFormatServiceImpl implements ChatFormatService {
    private final AtomicReference<String> chatFormat = new AtomicReference<>();
    private final ChatRenderer renderer;

    public ChatFormatServiceImpl(final PrefixService prefixService, final String initialFormat) {
        this.chatFormat.set(initialFormat);

        renderer = (source, sourceDisplayName, message, viewer) -> {
            final Component prefix = prefixService.cachedPrefix(source.getUniqueId());

            String format = chatFormat.get();
            if (format == null || format.isBlank()) {
                format = "<prefix> <name>: <message>";
            }

            return MessageService.mm().deserialize(
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
