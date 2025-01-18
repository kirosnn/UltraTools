package fr.kirosnn.ultraTools.utils.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;

public class RecipeBuilder {

    public static void registerRecipe(ItemStack item, @NotNull ConfigurationSection itemSection, JavaPlugin plugin) {
        if (!itemSection.getBoolean("craftable", false)) {
            return;
        }

        if (!itemSection.contains("recipe")) {
            logWarning("No recipe defined for item: " + itemSection.getString("name"));
            return;
        }

        String itemName = itemSection.getString("name", "unknown_item").toLowerCase();
        NamespacedKey recipeKey = new NamespacedKey(plugin, itemName);

        plugin.getServer().removeRecipe(recipeKey);

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, item);

        List<String> recipeLines = itemSection.getStringList("recipe");
        if (recipeLines.size() != 3) {
            logWarning("Invalid recipe format for item: " + itemName + ". Expected exactly 3 rows, got: " + recipeLines.size());
            return;
        }

        recipe.shape("ABC", "DEF", "GHI");

        char[] slots = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
        int slotIndex = 0;

        for (String line : recipeLines) {
            String[] parts = line.split("\\|");
            if (parts.length != 3) {
                logWarning("Crafting rows should contain exactly 3 columns for item: " + itemName + ", but got: " + parts.length);
                return;
            }

            for (String part : parts) {
                Material material = RecipeBuilder.getMaterial(part);
                if (material != null && material != Material.AIR) {
                    recipe.setIngredient(slots[slotIndex], material);
                }
                slotIndex++;
            }
        }

        plugin.getServer().addRecipe(recipe);
    }


    public static Material getMaterial(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("AIR")) {
            return Material.AIR;
        }
        return Material.matchMaterial(name.toUpperCase());
    }

    private static void logWarning(String message) {
        Bukkit.getLogger().warning("[UltraTools] " + message);
    }

    private static void logSevere(String message) {
        Bukkit.getLogger().log(Level.SEVERE, "[UltraTools] " + message);
    }

    private static void logInfo(String message) {
        Bukkit.getLogger().info("[UltraTools] " + message);
    }
}