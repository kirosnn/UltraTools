package fr.kirosnn.ultraTools.listeners;

import fr.kirosnn.ultraTools.UltraTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

public class UpgraderListener implements Listener {

    private final UltraTools plugin;
    private final File configFolder;
    private final boolean enchantmentsConflictCheck;
    private final boolean sumExistingEnchantments;

    public UpgraderListener(@NotNull UltraTools plugin) {
        this.plugin = plugin;
        this.configFolder = new File(plugin.getDataFolder(), "items");
        this.enchantmentsConflictCheck = plugin.getConfig().getBoolean("enchantments-conflict-check", false);
        this.sumExistingEnchantments = plugin.getConfig().getBoolean("sum-existing-enchantments", false);
    }

    private static @NotNull Component parseMixedText(String text) {
        return MiniMessage.miniMessage().deserialize(convertLegacyColorsToMiniMessage(text));
    }

    private static @NotNull String convertLegacyColorsToMiniMessage(@NotNull String text) {
        return text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
    }

    private static void logWarning(String message) {
        Bukkit.getLogger().warning("[UltraTools] " + message);
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getBottomInventory();
        ItemStack itemOnCursor = e.getCursor();
        ItemStack itemInteractedWith = e.getCurrentItem();

        if (!e.getAction().equals(InventoryAction.SWAP_WITH_CURSOR) || !e.getClick().isRightClick()) {
            return;
        }

        if (!(inventory.getType().equals(InventoryType.PLAYER) || e.getInventory().getType().equals(InventoryType.CRAFTING))) {
            return;
        }

        YamlConfiguration upgraderConfig = getUpgraderConfig(itemOnCursor);
        if (upgraderConfig == null) {
            return;
        }

        if (!isApplicable(itemInteractedWith, upgraderConfig)) {
            player.sendMessage(plugin.getLangFile().getTranslated(
                    "upgrade.not-applicable",
                    "&d&lᴛᴏᴏʟѕ • &7This item is not compatible with this upgrader.",
                    null
            ));
            e.setCancelled(true);
            return;
        }

        if (applyUpgrader(itemInteractedWith, upgraderConfig)) {
            player.sendMessage(plugin.getLangFile().getTranslated(
                    "upgrade.success",
                    "&d&lᴛᴏᴏʟѕ • &7Enchantments have been successfully applied!",
                    null
            ));
            itemOnCursor.setAmount(itemOnCursor.getAmount() - 1);
            e.setCancelled(true);
        } else {
            player.sendMessage(plugin.getLangFile().getTranslated(
                    "upgrade.error",
                    "&d&lᴛᴏᴏʟѕ • &7An error occurred while using the upgrader.",
                    null
            ));
        }
    }

    private YamlConfiguration getUpgraderConfig(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return null;
        }

        String displayName = meta.getDisplayName();

        File[] files = configFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            logWarning("No YAML files found in the 'items' folder.");
            return null;
        }

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String configName = config.getString("upgrader.display_name");

            if (configName != null) {
                Component parsedComponent = parseMixedText(configName);
                String legacyConfigName = LegacyComponentSerializer.legacySection().serialize(parsedComponent);

                if (displayName.equalsIgnoreCase(legacyConfigName)) {
                    return config;
                }
            }
        }

        return null;
    }

    private boolean isApplicable(ItemStack targetItem, YamlConfiguration config) {
        if (targetItem == null || targetItem.getType() == Material.AIR) {
            return false;
        }

        List<String> applicableItems = config.getStringList("upgrader.applicable-items");
        return applicableItems.contains(targetItem.getType().name());
    }

    private boolean applyUpgrader(ItemStack targetItem, @NotNull YamlConfiguration config) {
        if (!config.contains("upgrader.enchantments")) {
            logWarning("No enchantments found in the configuration.");
            return false;
        }

        Map<String, Object> enchantments = config.getConfigurationSection("upgrader.enchantments").getValues(false);

        for (Map.Entry<String, Object> entry : enchantments.entrySet()) {
            String enchantName = entry.getKey().toUpperCase();
            int level;

            try {
                level = Integer.parseInt(entry.getValue().toString());
            } catch (NumberFormatException e) {
                logWarning("Invalid enchantment level for: " + enchantName);
                continue;
            }

            Enchantment enchantment = Enchantment.getByName(enchantName);
            if (enchantment != null) {
                if (enchantmentsConflictCheck && targetItem.containsEnchantment(enchantment)) {
                    logWarning("Conflict detected: " + enchantName + " cannot be applied.");
                    return false;
                }

                if (sumExistingEnchantments && targetItem.containsEnchantment(enchantment)) {
                    level += targetItem.getEnchantmentLevel(enchantment);
                }

                targetItem.addUnsafeEnchantment(enchantment, level);
            } else {
                logWarning("Enchantment not found: " + enchantName);
            }
        }

        return true;
    }
}