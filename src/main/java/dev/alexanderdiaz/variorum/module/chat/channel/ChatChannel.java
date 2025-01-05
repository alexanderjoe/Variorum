package dev.alexanderdiaz.variorum.module.chat.channel;

import org.bukkit.entity.Player;

import java.util.Collection;

// Chat channel interface
public interface ChatChannel {
    String getName();

    void sendMessage(Player sender, String message);

    Collection<? extends Player> getRecipients(Player sender);
}
