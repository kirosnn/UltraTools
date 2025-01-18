package fr.kirosnn.ultraTools.commands.subcommands;

import fr.kirosnn.dAPI.commands.SubCommand;
import fr.kirosnn.ultraTools.UltraTools;
import fr.kirosnn.ultraTools.utils.items.UpgraderBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiveUpgraderSubCommand implements SubCommand {
    private final UltraTools plugin;

    public GiveUpgraderSubCommand(UltraTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission("tools.giveupgrader")) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "tools.no-permission",
                    "&d&lᴛᴏᴏʟѕ • &7You don't have permission to use this command.",
                    null
            ));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.giveupgrader.usage",
                    "&d&lᴛᴏᴏʟѕ • &7Usage: &d/tools giveupgrader <player> <upgrader-name>",
                    null
            ));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.giveupgrader.invalid-player",
                    "&d&lᴛᴏᴏʟѕ • &7The specified player is not online or does not exist.",
                    null
            ));
            return true;
        }

        String upgraderName = args[2];
        File upgraderFile = new File(plugin.getDataFolder(), "items/" + upgraderName + ".yml");

        if (!upgraderFile.exists()) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.giveupgrader.invalid-upgrader",
                    "&d&lᴛᴏᴏʟѕ • &7The specified upgrader does not exist in the 'items' folder.",
                    null
            ));
            return true;
        }

        ItemStack upgrader = UpgraderBuilder.buildUpgraderFromYaml(upgraderFile, plugin);
        if (upgrader == null) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.giveupgrader.error",
                    "&d&lᴛᴏᴏʟѕ • &7An error occurred while creating the upgrader.",
                    null
            ));
            return true;
        }

        target.getInventory().addItem(upgrader);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{player}", target.getName());
        placeholders.put("{upgrader}", upgraderName);

        sender.sendMessage(plugin.getLangFile().getTranslated(
                "commands.giveupgrader.success",
                "&d&lᴛᴏᴏʟѕ • &7The upgrader &d{upgrader} &7was successfully given to &d{player}&7.",
                placeholders
        ));
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission("tools.giveupgrader");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String @NotNull [] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        } else if (args.length == 2) {
            File itemsFolder = new File(plugin.getDataFolder(), "items");
            if (itemsFolder.exists()) {
                File[] upgraderFiles = itemsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
                if (upgraderFiles != null) {
                    return List.of(upgraderFiles).stream()
                            .filter(file -> {
                                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                                return config.contains("upgrader");
                            })
                            .map(file -> file.getName().replace(".yml", ""))
                            .toList();
                }
            }
        }
        return Collections.emptyList();
    }
}