package fr.kirosnn.ultraTools.commands.subcommands;

import fr.kirosnn.dAPI.commands.SubCommand;
import fr.kirosnn.ultraTools.UltraTools;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemInfoSubCommand implements SubCommand {

    private final UltraTools plugin;

    public ItemInfoSubCommand(UltraTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission("tools.iteminfo")) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "tools.no-permission",
                    "&d&lᴛᴏᴏʟѕ • &7You don't have permission to use this command.",
                    null
            ));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "tools.player-only",
                    "&d&lᴛᴏᴏʟѕ • &7Only players can use this command.",
                    null
            ));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.iteminfo.no-item",
                    "&d&lᴛᴏᴏʟѕ • &7You are not holding any item.",
                    null
            ));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            sender.sendMessage(plugin.getLangFile().getTranslated(
                    "commands.iteminfo.no-meta",
                    "&d&lᴛᴏᴏʟѕ • &7This item has no metadata to display.",
                    null
            ));
            return true;
        }

        Map<String, String> info = new HashMap<>();
        info.put("Material", item.getType().name());
        info.put("Amount", String.valueOf(item.getAmount()));

        if (meta.hasDisplayName()) {
            info.put("Display Name", Component.text(meta.getDisplayName()).toString());
        }
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                info.put("Lore", String.join("\n", lore));
            }
        }
        if (meta.hasEnchants()) {
            String enchants = meta.getEnchants().entrySet().stream()
                    .map(e -> e.getKey().getKey().getKey() + " " + e.getValue())
                    .collect(Collectors.joining(", "));
            info.put("Enchantments", enchants);
        }
        if (meta.hasCustomModelData()) {
            info.put("Custom Model Data", String.valueOf(meta.getCustomModelData()));
        }
        if (meta.hasAttributeModifiers()) {
            String attributes = meta.getAttributeModifiers().entries().stream()
                    .map(entry -> entry.getKey().toString() + ": " + entry.getValue())
                    .collect(Collectors.joining(", "));
            info.put("Attributes", attributes);
        }

        sender.sendMessage(plugin.getLangFile().getTranslated(
                "commands.iteminfo.header",
                "&d&lᴛᴏᴏʟѕ • &7Item Information:",
                null
        ));
        info.forEach((key, value) -> sender.sendMessage("§d" + key + "§7: " + value));
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission("tools.iteminfo");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String @NotNull [] args) {
        return List.of();
    }
}