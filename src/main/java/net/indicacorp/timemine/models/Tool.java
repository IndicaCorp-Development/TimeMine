package net.indicacorp.timemine.models;

import net.indicacorp.timemine.TimeMine;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;

public class Tool {
    private Material tool;
    private static FileConfiguration config;
    private static HashMap<String, List<?>> mineables = null;

    public Tool(Material tool) {
        this.tool = tool;
        config = TimeMine.getInstance().getConfig();
        loadFromConfig();
    }

    private static void loadFromConfig() {
        ConfigurationSection section = config.getConfigurationSection("mineables");
        if (section == null) return;
        mineables = new HashMap<>();
        for (String e : section.getKeys(false)) {
            mineables.put(e, config.getList("mineables." + e));
        }
    }

    public boolean canMine(Material block) {
        String toolEnum = tool.toString();
        if (!mineables.containsKey(toolEnum)) return false;
        List<?> mineableEnums = mineables.get(toolEnum);
        return mineableEnums.contains(block.toString());
    }
}

