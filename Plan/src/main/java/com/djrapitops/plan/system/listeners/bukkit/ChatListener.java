package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.PlayerProcessors;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Event Listener for AsyncPlayerChatEvents.
 *
 * @author Rsl1122
 */
public class ChatListener implements Listener {

    private final PlayerProcessors processorFactory;
    private final Processing processing;
    private final ErrorHandler errorHandler;

    @Inject
    public ChatListener(
            PlayerProcessors processorFactory,
            Processing processing,
            ErrorHandler errorHandler
    ) {
        this.processorFactory = processorFactory;
        this.processing = processing;
        this.errorHandler = errorHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnChatEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnChatEvent(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        String displayName = p.getDisplayName();
        processing.submit(processorFactory.nameProcessor(uuid, name, displayName));
    }
}