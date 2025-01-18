package fr.kirosnn.ultraTools.commands.subcommands;

import fr.kirosnn.dAPI.commands.SubCommand;
import fr.kirosnn.ultraTools.UltraTools;
import fr.kirosnn.ultraTools.utils.items.ItemsBuilder;
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

public class GiveItemSubCommand implements SubCommand {
    private final UltraTools plugin;

    public GiveItemSubCommand(UltraTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission("tools.giveitem")) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "tools.no-permission",
                    "&d&lᴛᴏᴏʟѕ • &7You don't have permission to use this command.",
                    null
            ));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.giveitem.usage",
                    "&d&lᴛᴏᴏʟѕ • &7Usage: &d/tools giveitem <player> <item-name>",
                    null
            ));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.giveitem.invalid-player",
                    "&d&lᴛᴏᴏʟѕ • &7The specified player is not online or does not exist.",
                    null
            ));
            return true;
        }

        String itemName = args[2];
        File itemFile = new File(plugin.getDataFolder(), "items/" + itemName + ".yml");

        if (!itemFile.exists()) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.giveitem.invalid-item",
                    "&d&lᴛᴏᴏʟѕ • &7The specified item does not exist in the 'items' folder.",
                    null
            ));
            return true;
        }

        ItemStack item = ItemsBuilder.buildItemFromYaml(itemFile, plugin);
        if (item == null) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.giveitem.error",
                    "&d&lᴛᴏᴏʟѕ • &7An error occurred while creating the item.",
                    null
            ));
            return true;
        }

        target.getInventory().addItem(item);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{player}", target.getName());
        placeholders.put("{item}", itemName);

        sender.sendMessage(plugin.getLangFile().getTranslated(
                "commands.giveitem.success",
                "&d&lᴛᴏᴏʟѕ • &7The item &d{item} &7was successfully given to &d{player}&7.",
                placeholders
        ));
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission("tools.giveitem");
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
                File[] itemFiles = itemsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
                if (itemFiles != null) {
                    return List.of(itemFiles).stream()
                            .filter(file -> {
                                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                                return config.contains("item");
                            })
                            .map(file -> file.getName().replace(".yml", ""))
                            .toList();
                }
            }
        }
        return Collections.emptyList();
    }
}
