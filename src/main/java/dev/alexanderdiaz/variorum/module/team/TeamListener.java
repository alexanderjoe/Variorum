package dev.alexanderdiaz.variorum.module.team;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class TeamListener implements Listener {
    private final TeamsModule module;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }

        Optional<Team> victimTeam = module.getPlayerTeam(victim);
        Optional<Team> attackerTeam = module.getPlayerTeam(attacker);

        if (victimTeam.isPresent()
                && attackerTeam.isPresent()
                && victimTeam.get().equals(attackerTeam.get())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        module.removePlayerFromTeam(event.getPlayer());
    }
}
