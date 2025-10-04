package com.github.manu585.manusgroups.spi;

import io.papermc.paper.chat.ChatRenderer;

public interface ChatFormatService {
    /**
     * @return Renderer used by the async Chat event
     */
    ChatRenderer renderer();
}
