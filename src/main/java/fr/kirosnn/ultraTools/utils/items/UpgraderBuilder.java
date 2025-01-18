package fr.kirosnn.ultraTools.utils.items;

import fr.kirosnn.ultraTools.utils.recipes.RecipeBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UpgraderBuilder {

    public static @Nullable ItemStack buildUpgraderFromYaml(File upgraderFile, JavaPlugin plugin) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(upgraderFile);

            if (config.contains("item")) {
                return null;
            }

            ConfigurationSection itemSection = config.getConfigurationSection("upgrader");
            if (itemSection == null) {
                logWarning("Section 'upgrader' not found in " + upgraderFile.getName());
                return null;
            }

            Material material = getMaterial(itemSection.getString("material"), upgraderFile);
            if (material == null) {
                return null;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                logWarning("Failed to retrieve ItemMeta for material: " + material.name());
                return null;
            }

            configureDisplayName(meta, itemSection);
            configureLore(meta, itemSection);
            configureCustomModelData(meta, itemSection);
            configureHiddenEnchantments(meta, itemSection, plugin);

            item.setItemMeta(meta);

            RecipeBuilder recipeBuilder = new RecipeBuilder();
            RecipeBuilder.registerRecipe(item, itemSection, plugin);

            return item;
        } catch (Exception e) {
            logSevere("Error creating item from " + upgraderFile.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private static @Nullable Material getMaterial(String materialName, File upgraderFile) {
        if (materialName == null || Material.getMaterial(materialName.toUpperCase()) == null) {
            logWarning("'material' is invalid or missing in " + upgraderFile.getName());
            return null;
        }
        return Material.valueOf(materialName.toUpperCase());
    }

    private static void configureDisplayName(ItemMeta meta, @NotNull ConfigurationSection itemSection) {
        if (itemSection.contains("display_name")) {
            String displayName = itemSection.getString("display_name");
            if (displayName != null) {
                Component nameComponent = parseMixedText(displayName);
                String legacyName = LegacyComponentSerializer.legacySection().serialize(nameComponent);
                meta.setDisplayName(legacyName);
            }
        }
    }

    private static void configureLore(ItemMeta meta, @NotNull ConfigurationSection itemSection) {
        if (itemSection.contains("lore")) {
            List<String> loreStrings = itemSection.getStringList("lore");
            List<String> convertedLore = new ArrayList<>();
            for (String loreLine : loreStrings) {
                Component loreComponent = parseMixedText(loreLine);
                String legacyLoreLine = LegacyComponentSerializer.legacySection().serialize(loreComponent);
                convertedLore.add(legacyLoreLine);
            }
            meta.setLore(convertedLore);
        }
    }

    private static @NotNull Component parseMixedText(String text) {
        String miniMessageConverted = convertLegacyColorsToMiniMessage(text);

        return MiniMessage.miniMessage().deserialize(miniMessageConverted);
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
                .replace("&n", "<underline>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>")
                .replace("&A", "<green>")
                .replace("&B", "<aqua>")
                .replace("&C", "<red>")
                .replace("&D", "<light_purple>")
                .replace("&E", "<yellow>")
                .replace("&F", "<white>")
                .replace("&K", "<obfuscated>")
                .replace("&L", "<bold>")
                .replace("&M", "<strikethrough>")
                .replace("&N", "<underline>")
                .replace("&O", "<italic>")
                .replace("&R", "<reset>");
    }

    private static void configureCustomModelData(ItemMeta meta, @NotNull ConfigurationSection itemSection) {
        if (itemSection.contains("custom-model-data")) {
            int customModelData = itemSection.getInt("custom-model-data");
            meta.setCustomModelData(customModelData);
        }
    }

    private static void configureHiddenEnchantments(ItemMeta meta, @NotNull ConfigurationSection itemSection, JavaPlugin plugin) {
        if (itemSection.contains("enchantments")) {
            ConfigurationSection enchantmentsSection = itemSection.getConfigurationSection("enchantments");
            if (enchantmentsSection != null) {
                PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                for (String enchantName : enchantmentsSection.getKeys(false)) {
                    try {
                        int level = enchantmentsSection.getInt(enchantName);
                        NamespacedKey key = new NamespacedKey(plugin, "enchantment_" + enchantName.toLowerCase());
                        dataContainer.set(key, PersistentDataType.INTEGER, level);
                    } catch (Exception e) {
                        logWarning("Error storing enchantment: " + enchantName + " - " + e.getMessage());
                    }
                }
            }
        }
    }

    private static void logWarning(String message) {
        Bukkit.getLogger().warning("[UltraTools] " + message);
    }

    private static void logSevere(String message) {
        Bukkit.getLogger().severe("[UltraTools] " + message);
    }

    private static void logInfo(String message) {
        Bukkit.getLogger().info("[UltraTools] " + message);
    }
}
