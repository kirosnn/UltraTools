package fr.kirosnn.ultraTools.commands;

import fr.kirosnn.dAPI.commands.CommandBase;
import fr.kirosnn.ultraTools.UltraTools;
import fr.kirosnn.ultraTools.commands.subcommands.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ToolsCommand extends CommandBase {
    private final UltraTools plugin;

    public ToolsCommand(UltraTools plugin) {
        this.plugin = plugin;


        this.registerSubCommand("giveitem", new GiveItemSubCommand(plugin));
        this.registerSubCommand("iteminfo", new ItemInfoSubCommand(plugin));
        this.registerSubCommand("giveupgrader", new GiveUpgraderSubCommand(plugin));

        this.setNoPermissionMessage(plugin.getLangFile().getTranslated(
                "tools.no-permission",
                "&cVous n'avez pas la permission d'utiliser cette commande.",
                Collections.emptyMap()
        ));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, String[] args) {
        sender.sendMessage(plugin.getLangFile().getTranslated(
                "tools.usage",
                "&eUtilisation : /birthday <reload>",
                null
        ));
        return true;
    }
}