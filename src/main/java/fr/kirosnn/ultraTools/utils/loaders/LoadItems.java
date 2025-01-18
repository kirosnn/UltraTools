package fr.kirosnn.ultraTools.utils.loaders;

import fr.kirosnn.dAPI.utils.LoggerUtils;
import fr.kirosnn.ultraTools.UltraTools;
import fr.kirosnn.ultraTools.utils.items.ItemsBuilder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class LoadItems {

    private final File itemsFolder;
    private final LoggerUtils loggerUtils;
    private final UltraTools plugin;

    public LoadItems(File dataFolder, LoggerUtils loggerUtils, UltraTools plugin) {
        this.itemsFolder = new File(dataFolder, "items");
        this.loggerUtils = loggerUtils;
        this.plugin = plugin;

        if (!this.itemsFolder.exists()) {
            this.itemsFolder.mkdirs();
        }
    }

    public void loadItems() {
        int itemCount = 0;
        int upgraderCount = 0;

        File[] yamlFiles = itemsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (yamlFiles != null) {
            for (File file : yamlFiles) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                if (config.contains("upgrader")) {
                    upgraderCount++;
                    loggerUtils.infoPlugin("Upgrader detected: " + file.getName());
                    continue;
                }

                ItemStack item = ItemsBuilder.buildItemFromYaml(file, plugin);
                if (item != null) {
                    itemCount++;
                    loggerUtils.infoPlugin("Item loaded successfully: " + file.getName());
                } else {
                    loggerUtils.infoPlugin("Failed to load item: " + file.getName());
                }
            }
        }

        if (itemCount > 0) {
            loggerUtils.infoPlugin(itemCount + " items loaded from the 'items' folder.");
        } else {
            loggerUtils.infoPlugin("No items detected from the 'items' folder.");
        }

        if (upgraderCount > 0) {
            loggerUtils.infoPlugin(upgraderCount + " upgraders detected in the 'items' folder.");
        }
    }
}
