package fr.kirosnn.ultraTools.commands.subcommands;

import fr.kirosnn.dAPI.commands.SubCommand;
import fr.kirosnn.ultraTools.UltraTools;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ReloadSubCommand implements SubCommand {
    private final UltraTools plugin;

    public ReloadSubCommand(UltraTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            plugin.getConfigFile().reload();
            plugin.getLangFile().reload();

            File itemsFolder = new File(plugin.getDataFolder(), "items");
            if (!itemsFolder.exists()) {
                itemsFolder.mkdirs();
            }

            File[] itemFiles = itemsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (itemFiles != null) {
                for (File itemFile : itemFiles) {
                    YamlConfiguration.loadConfiguration(itemFile);
                }
            }

            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.reload.success",
                    "&aLa configuration, les fichiers de langue et les items ont été rechargés avec succès !",
                    null
            ));
        } catch (Exception e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("error", e.getMessage());

            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.reload.error",
                    "&cUne erreur est survenue lors du rechargement : {error}",
                    placeholders
            ));
        }
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission("tools.reload");
    }
}
