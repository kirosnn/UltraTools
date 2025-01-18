package fr.kirosnn.ultraTools.commands.subcommands;

import fr.kirosnn.dAPI.commands.SubCommand;
import fr.kirosnn.ultraTools.UltraTools;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class HelpSubCommand implements SubCommand {

    private final UltraTools plugin;

    public HelpSubCommand(UltraTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, String[] args) {
        Map<String, String> placeholders = new HashMap<>();

        sender.sendMessage(plugin.getLangFile().getTranslated(
                "commands.help.header",
                "&6--- &eAvailable Birthday Commands &6---",
                placeholders
        ));

        placeholders.put("%command%", "/tools reload");
        placeholders.put("%description%", "Reloads the plugin configuration and language files.");
        sender.sendMessage(plugin.getLangFile().getTranslated(
                "commands.help.list",
                "&7- &e%command%: %description%",
                placeholders
        ));

        placeholders.put("%command%", "/tools help");
        placeholders.put("%description%", "Displays this help menu.");
        sender.sendMessage(plugin.getLangFile().getTranslated(
                "commands.help.list",
                "&7- &e%command%: %description%",
                placeholders
        ));

        sender.sendMessage(plugin.getLangFile().getTranslated(
                "commands.help.footer",
                "&6--- &eEnd of Help &6---",
                new HashMap<>()
        ));

        return true;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission("tools.help");
    }

    @Override
    public java.util.List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}