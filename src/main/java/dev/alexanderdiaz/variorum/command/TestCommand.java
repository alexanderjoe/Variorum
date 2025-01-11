package dev.alexanderdiaz.variorum.command;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.module.objectives.Objective;
import dev.alexanderdiaz.variorum.module.objectives.ObjectivesModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;

public class TestCommand {
    private final Variorum plugin;

    public TestCommand(Variorum plugin) {
        this.plugin = plugin;
    }

    @Command("cycle")
    @CommandDescription("Command to return \"ping\"")
    public void pong(final CommandSender sender) {

        Variorum.get().getMatchManager().cycleToNextMatch();
    }

    @Command("objectives|objs")
    @CommandDescription("List the objectives available for the current match.")
    public void listObjectives(final CommandSender sender) {
        ObjectivesModule om = Variorum.getMatch().getRequiredModule(ObjectivesModule.class);

        var message = Component.text("Objectives", NamedTextColor.YELLOW).append(Component.newline());

        for (Objective o : om.getObjectives()) {
            message = message.append(Component.text("    "))
                    .append(Component.text(o.getName(), NamedTextColor.YELLOW).append(Component.newline()));
        }

        sender.sendMessage(message);
    }

    @Command("gmc")
    @CommandDescription("Creative.")
    public void gmc(CommandSender sender) {
        Player player = (Player) sender;
        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage(Component.text("Set gamemode to creative", NamedTextColor.YELLOW));
    }
}
