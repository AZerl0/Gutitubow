package com.azerl0.gutitubow.manager;

import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import com.azerl0.gutitubow.map.MapRepairManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;

public class GlassBreakManager {
    MapRepairManager mapRepairManager;

    public GlassBreakManager(MapRepairManager mapRepairManager) {
        this.mapRepairManager = mapRepairManager;
    }

    public boolean breakConnectedGlass(Block block, int limit) {
        Material type = block.getType();

        if (!GutitubowBlockTags.BREAKABLE_BY_GUTITUBOW_ARROW.contains(type)) return false;

        mapRepairManager.savedBrokeBlock(block);
        block.breakNaturally(true);
        for (Item item : block.getLocation().getNearbyEntitiesByType(Item.class, 2)) {
            if (item.getTicksLived() == 0) item.remove();
        }

        if (limit <= 0) return true;
        this.breakConnectedGlass(block.getRelative(1, 0, 0), limit - 1);
        this.breakConnectedGlass(block.getRelative(0, 1, 0), limit - 1);
        this.breakConnectedGlass(block.getRelative(0, 0, 1), limit - 1);
        this.breakConnectedGlass(block.getRelative(-1, 0, 0), limit - 1);
        this.breakConnectedGlass(block.getRelative(0, -1, 0), limit - 1);
        this.breakConnectedGlass(block.getRelative(0, 0, -1), limit - 1);
        return true;
    }
}
