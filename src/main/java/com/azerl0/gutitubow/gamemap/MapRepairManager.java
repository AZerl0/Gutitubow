package com.azerl0.gutitubow.gamemap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;

public class MapRepairManager {
    private final Map<Location, BlockData> brokeBlocks = new HashMap<>();
    private final Map<Location, BlockData> placedBlocks = new HashMap<>();

    public MapRepairManager() {
    }

    public void reset() {
        brokeBlocks.clear();
        placedBlocks.clear();
    }

    public void savedBrokeBlock(Block block) {
        if (brokeBlocks.containsKey(block.getLocation())) return;
        brokeBlocks.put(block.getLocation(), block.getBlockData());
    }

    public void repairBrokeBlocks() {
        for (Map.Entry<Location, BlockData> entry : brokeBlocks.entrySet()) {
            Location location = entry.getKey();
            BlockData blockData = entry.getValue();
            location.getWorld().setBlockData(location, blockData);
        }
    }

    public void savedPlacedBlock(Block block) {
        if (placedBlocks.containsKey(block.getLocation())) return;
        if (brokeBlocks.containsKey(block.getLocation())) return;
        placedBlocks.put(block.getLocation(), block.getBlockData());
    }

    public void repairPlacedBlocks() {
        for (Map.Entry<Location, BlockData> entry : placedBlocks.entrySet()) {
            Location location = entry.getKey();
            BlockData blockData = entry.getValue();
            location.getWorld().setBlockData(location, blockData);
        }
    }
}
