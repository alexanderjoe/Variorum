package dev.alexanderdiaz.variorum.command.util;

import dev.alexanderdiaz.variorum.Variorum;
import dev.alexanderdiaz.variorum.command.*;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class VariorumCommandGraph extends CommandGraph<Variorum> {
    public VariorumCommandGraph(Variorum plugin) throws Exception {
        super(plugin);
    }

    @Override
    protected void registerCommands() {
        register(new TestCommand(plugin));
        register(new TeamCommands(plugin));
        register(new ChatCommands(plugin));
        register(new GameStateCommands(plugin));
        register(new RotationCommands(plugin));
    }

    @Override
    protected MinecraftHelp<CommandSender> createHelp() {
        return null;
    }

    @Override
    protected void setupInjectors() {}

    @Override
    protected void setupParsers() {}
}
