package dev.alexanderdiaz.variorum.module.team;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.event.player.PlayerSpawnStartEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class SpectatorListener implements Listener {
  private final TeamsModule module;

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerSpawn(PlayerSpawnStartEvent event) {
    boolean isSpectator = event.getTeam().isEmpty();

    if (isSpectator) {
      event.getPlayer().setGameMode(GameMode.SURVIVAL);

      // run sync on the main thread -- weird bug where it wouldn't set
      Variorum.get().getServer().getScheduler().runTask(Variorum.get(), () -> {
        event.getPlayer().setAllowFlight(true);
        event.getPlayer().setFlying(true);
      });
    }
  }
}
