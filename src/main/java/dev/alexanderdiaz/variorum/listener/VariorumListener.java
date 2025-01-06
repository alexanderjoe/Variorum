package dev.alexanderdiaz.variorum.listener;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.match.MatchOpenEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VariorumListener implements Listener {
    @EventHandler
    public void onMatchOpenEvent(MatchOpenEvent event) {
        Variorum.get().getLogger().info("Match Opened: " + event.getMatch().toString());
    }
}