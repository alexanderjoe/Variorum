package dev.alexanderdiaz.variorum.module.chat;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.match.Match;
import dev.alexanderdiaz.variorum.module.Module;
import dev.alexanderdiaz.variorum.module.chat.channel.ChatChannel;
import dev.alexanderdiaz.variorum.module.chat.channel.GlobalChannel;
import dev.alexanderdiaz.variorum.module.chat.channel.TeamChannel;
import dev.alexanderdiaz.variorum.module.team.TeamsModule;
import dev.alexanderdiaz.variorum.util.Events;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class ChatModule implements Module {
    private final Match match;
    private final Map<UUID, ChatChannel> playerChannels = new HashMap<>();
    private ChatListener listener;
    @Getter
    private final List<ChatChannel> channels = new ArrayList<>();

    public ChatModule(Match match) {
        this.match = match;
        setupDefaultChannels();
    }

    private void setupDefaultChannels() {
        channels.add(new GlobalChannel());
        match.getModule(TeamsModule.class).ifPresent(teamsModule -> {
            teamsModule.getTeams().forEach(team ->
                    channels.add(new TeamChannel(team))
            );
        });
    }

    public Optional<ChatChannel> getPlayerChannel(Player player) {
        return Optional.ofNullable(playerChannels.get(player.getUniqueId()));
    }

    public void setPlayerChannel(Player player, ChatChannel channel) {
        playerChannels.put(player.getUniqueId(), channel);
        player.sendMessage(Component.text("Chat channel set to: " + channel.getName(), NamedTextColor.YELLOW));
    }

    @Override
    public void enable() {
        this.listener = new ChatListener();
        Events.register(listener);
    }

    @Override
    public void disable() {
        if (listener != null) {
            Events.unregister(listener);
        }
        playerChannels.clear();
        channels.clear();
    }

    private class ChatListener implements Listener {
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onPlayerChat(AsyncChatEvent event) {
            Player player = event.getPlayer();
            String messageStr = ((net.kyori.adventure.text.TextComponent) event.message()).content();

            ChatChannel channel = playerChannels.getOrDefault(player.getUniqueId(),
                    channels.stream()
                            .filter(ch -> ch instanceof GlobalChannel)
                            .findFirst()
                            .orElseThrow());

            event.setCancelled(true);
            channel.sendMessage(player, messageStr);
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            playerChannels.remove(event.getPlayer().getUniqueId());
        }
    }
}