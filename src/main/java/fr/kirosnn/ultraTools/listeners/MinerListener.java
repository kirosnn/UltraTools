package fr.kirosnn.ultraTools.listeners;

import fr.kirosnn.ultraTools.UltraTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MinerListener implements Listener {

    private final UltraTools plugin;
    private final File configFolder;

    public MinerListener(@NotNull UltraTools plugin, File configFolder) {
        this.plugin = plugin;
        this.configFolder = new File(plugin.getDataFolder(), "items");
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent e) {
        Player player = (Player) e.getPlayer();
        Block block = (Block) e.getBlock();
        
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
}
