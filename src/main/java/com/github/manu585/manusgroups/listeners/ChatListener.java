package com.github.manu585.manusgroups.listeners;

import com.github.manu585.manusgroups.spi.ChatFormatService;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private final ChatFormatService chatFormatService;

    public ChatListener(final ChatFormatService chatFormatService) {
        this.chatFormatService = chatFormatService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent event) {
        event.renderer(chatFormatService.renderer());
    }
}
