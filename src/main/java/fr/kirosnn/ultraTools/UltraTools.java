package fr.kirosnn.ultraTools;

import fr.kirosnn.dAPI.utils.LoggerUtils;
import fr.kirosnn.dAPI.utils.YamlFile;
import fr.kirosnn.ultraTools.commands.ToolsCommand;
import fr.kirosnn.ultraTools.listeners.UpgraderListener;
import fr.kirosnn.ultraTools.utils.loaders.LoadItems;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltraTools extends JavaPlugin {

    private YamlFile configFile;
    private YamlFile langFile;
    private YamlFile exampleSwordFile;
    private YamlFile examplePickaxeFile;
    private YamlFile exampleIngotFile;
    private YamlFile exampleUpgraderFile;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        this.configFile = new YamlFile(this, null, "config.yml");
        this.langFile = new YamlFile(this, null, "lang.yml");
        this.exampleSwordFile = new YamlFile(this, "items", "examplesword.yml");
        this.examplePickaxeFile = new YamlFile(this, "items", "examplepickaxe.yml");
        this.exampleIngotFile = new YamlFile(this, "items", "exampleingot.yml");
        this.exampleUpgraderFile = new YamlFile(this, "items", "exampleupgrader.yml");

        LoadItems loader = new LoadItems(getDataFolder(), new LoggerUtils(this), this);
        loader.loadItems();

        getServer().getPluginManager().registerEvents(new UpgraderListener(this), this);

        this.getCommand("tools").setExecutor(new ToolsCommand(this));
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
        }
    }

    public YamlFile getConfigFile() {
        return configFile;
    }

    public YamlFile getLangFile() {
        return langFile;
    }

    public YamlFile getExampleSwordFile() {
        return exampleSwordFile;
    }

    public YamlFile getExamplePickaxeFile() {
        return examplePickaxeFile;
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public YamlFile getExampleIngotFile() {
        return exampleIngotFile;
    }

    public YamlFile getExampleUpgraderFile() {
        return exampleUpgraderFile;
    }
}
