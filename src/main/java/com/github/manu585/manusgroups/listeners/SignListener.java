package com.github.manu585.manusgroups.listeners;

import com.github.manu585.manusgroups.manager.SignSelectionManager;
import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.spi.GroupSignService;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

import static com.github.manu585.manusgroups.util.General.isSign;

public class SignListener implements Listener {
    private final GroupSignService signService;
    private final SignSelectionManager selectionManager;
    private final MessageService messages;

    public SignListener(GroupSignService signService, SignSelectionManager selectionManager, MessageService messages) {
        this.signService = signService;
        this.selectionManager = selectionManager;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!event.getPlayer().isSneaking()) return;

        final Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        if (!isSign(clickedBlock)) return;

        final Player player = event.getPlayer();
        final UUID target = selectionManager.consume(player.getUniqueId());
        if (target == null) return;

        final Location location = clickedBlock.getLocation();
        signService.bind(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), target).thenRun(() -> messages.send(player, "Sign.Bound"));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Block brokenBlock = event.getBlock();
        if (!isSign(brokenBlock)) return;

        Location location = brokenBlock.getLocation();
        signService.unbind(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
