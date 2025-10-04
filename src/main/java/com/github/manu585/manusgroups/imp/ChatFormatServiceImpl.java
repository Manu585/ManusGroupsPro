package com.github.manu585.manusgroups.imp;

import com.github.manu585.manusgroups.spi.ChatFormatService;
import com.github.manu585.manusgroups.spi.PrefixService;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ChatFormatServiceImpl implements ChatFormatService {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final PrefixService prefixService;
    private final String chatFormat;

    public ChatFormatServiceImpl(final PrefixService prefixService, final String chatFormat) {
        this.prefixService = prefixService;
        this.chatFormat = chatFormat;
    }

    @Override
    public ChatRenderer renderer() {
        return (source, sourceDisplayName, message, viewer) -> {
            Component prefix = prefixService.cachedPrefix(source.getUniqueId());
            return MM.deserialize(
                    chatFormat,
                    TagResolver.builder()
                            .tag("prefix", Tag.inserting(prefix))
                            .tag("name", Tag.inserting(source.name()))
                            .tag("message", Tag.inserting(message))
                            .build()
            );
        };
    }
}
