package com.github.manu585.manusgroups.service.spi;

import io.papermc.paper.chat.ChatRenderer;

public interface ChatFormatService {
    /**
     * @return Renderer used by the async Chat event
     */
    ChatRenderer renderer();

    void updateFormat(String newFormat);
}
