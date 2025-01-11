package dev.alexanderdiaz.variorum.module.chat.channel;

import dev.alexanderdiaz.variorum.Variorum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Collection;

public class GlobalChannel implements ChatChannel {
    @Override
    public String getName() {
        return "Global";
    }

    @Override
    public void sendMessage(Player sender, String message) {
        Component formatted = Component.text()
                .append(Component.text("[G] ", NamedTextColor.WHITE))
                .append(sender.displayName())
                .append(Component.text(": "))
                .append(Component.text(message))
                .build();

        getRecipients(sender).forEach(player -> player.sendMessage(formatted));
        Variorum.get().getServer().getConsoleSender().sendMessage(formatted);
    }

    @Override
    public Collection<? extends Player> getRecipients(Player sender) {
        return Variorum.get().getServer().getOnlinePlayers();
    }
}
