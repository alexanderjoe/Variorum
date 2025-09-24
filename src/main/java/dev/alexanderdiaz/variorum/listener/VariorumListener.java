package dev.alexanderdiaz.variorum.listener;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.match.MatchOpenEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class VariorumListener implements Listener {
  @EventHandler
  public void onMatchOpenEvent(MatchOpenEvent event) {
    Variorum.get().getLogger().info("Match Opened: " + event.getMatch().getMap().getName());
  }

  @EventHandler
  public void playerLoginEvent(PlayerLoginEvent event) {
    if (Variorum.getMatch() == null) {
      event.disallow(
          PlayerLoginEvent.Result.KICK_OTHER,
          Component.text("The match is still loading!\n Try again later.")
              .color(NamedTextColor.RED));
    }
  }
}
