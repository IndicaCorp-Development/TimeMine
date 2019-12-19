package net.indicacorp.timemine.models;

import net.indicacorp.timemine.TimeMine;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tool {
    private TimeMine plugin;
    private Material tool;
    private HashMap<Material, ArrayList<Material>> mineables;
    private FileConfiguration config;

    public Tool(Material material) {
        plugin = TimeMine.getInstance();
        tool = material;
        config = plugin.getConfig();
        loadFromConfig();
    }

    private void loadFromConfig() {
        List<String> mineables = config.getStringList("timemine.mineables");
//        for (String e : mineables) {
//
//        }
//        System.out.println(mineables);
    }

    public boolean canMine(Material material) {
        HashMap<String, Material> validTools = new HashMap<String, Material>();
        return true;
    }
}

