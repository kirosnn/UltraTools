package fr.kirosnn.ultraTools.utils.items;

import fr.kirosnn.ultraTools.utils.recipes.RecipeBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class ItemsBuilder {

    private static final Set<String> loadingItems = new HashSet<>();

    public static @Nullable ItemStack buildItemFromYaml(File yamlFile, JavaPlugin plugin) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(yamlFile);

            if (config.contains("upgrader")) {
                return null;
            }

            ConfigurationSection itemSection = config.getConfigurationSection("item");
            if (itemSection == null) {
                logWarning("Section 'item' not found in " + yamlFile.getName());
                return null;
            }

            Material material = getMaterial(itemSection.getString("material"), yamlFile);
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
            configureEnchantments(meta, itemSection);
            configureAttributes(meta, itemSection);

            item.setItemMeta(meta);

            RecipeBuilder recipeBuilder = new RecipeBuilder();
            RecipeBuilder.registerRecipe(item, itemSection, plugin);

            return item;
        } catch (Exception e) {
            logSevere("Error creating item from " + yamlFile.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private static @Nullable Material getMaterial(String materialName, File yamlFile) {
        if (materialName == null || Material.getMaterial(materialName.toUpperCase()) == null) {
            logWarning("'material' is invalid or missing in " + yamlFile.getName());
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

    private static void configureEnchantments(ItemMeta meta, @NotNull ConfigurationSection itemSection) {
        if (itemSection.contains("enchants")) {
            ConfigurationSection enchantsSection = itemSection.getConfigurationSection("enchants");
            if (enchantsSection != null) {
                for (String enchantKey : enchantsSection.getKeys(false)) {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantKey.toLowerCase()));
                    if (enchantment != null) {
                        int level = enchantsSection.getInt(enchantKey);
                        meta.addEnchant(enchantment, level, true);
                    } else {
                        logWarning("Invalid enchantment: " + enchantKey);
                    }
                }
            }
        }
    }

    private static void configureAttributes(ItemMeta meta, @NotNull ConfigurationSection itemSection) {
        if (itemSection.contains("attributes")) {
            ConfigurationSection attributesSection = itemSection.getConfigurationSection("attributes");
            if (attributesSection != null) {
                for (String attributeKey : attributesSection.getKeys(false)) {
                    Attribute attribute = Attribute.valueOf(attributeKey.toUpperCase());
                    ConfigurationSection attributeData = attributesSection.getConfigurationSection(attributeKey);

                    if (attribute != null && attributeData != null) {
                        double amount = attributeData.getDouble("amount");
                        String slotName = attributeData.getString("slot", "HAND").toUpperCase();
                        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(attributeData.getString("operation", "ADD_NUMBER").toUpperCase());

                        AttributeModifier modifier = new AttributeModifier(
                                UUID.randomUUID(),
                                attributeKey,
                                amount,
                                operation,
                                EquipmentSlot.valueOf(slotName)
                        );
                        meta.addAttributeModifier(attribute, modifier);
                    } else {
                        logWarning("Invalid attribute or data: " + attributeKey);
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