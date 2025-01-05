package dev.alexanderdiaz.variorum.command;

import dev.alexanderdiaz.variorum.Variorum;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;

public class TestCommand {
    private final Variorum plugin;

    public TestCommand(Variorum plugin) {
        this.plugin = plugin;
    }

    @Command("pong")
    @CommandDescription("Command to return \"ping\"")
    public void pong(
            final CommandSender sender
            ) {

        sender.sendMessage("Pong!");
    }
}
