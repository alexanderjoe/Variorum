package dev.alexanderdiaz.variorum.module.chat.channel;

import java.util.Collection;
import org.bukkit.entity.Player;

public interface ChatChannel {
  String getName();

  void sendMessage(Player sender, String message);

  Collection<? extends Player> getRecipients(Player sender);
}
