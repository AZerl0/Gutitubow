package com.azerl0.gutitubow.manager;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import com.azerl0.gutitubow.gamemap.MapRepairManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public class GlassBreakManager {
    MapRepairManager mapRepairManager;

    public GlassBreakManager(MapRepairManager mapRepairManager) {
        this.mapRepairManager = mapRepairManager;
    }

    public boolean breakConnectedGlass(Block block, int limit) {
        Material type = block.getType();

        if (!GutitubowBlockTags.BREAKABLE_BY_GUTITUBOW_ARROW.isTagged(type)) return false;

        mapRepairManager.savedBrokeBlock(block);
        block.breakNaturally(true);
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.0f);

        if (limit <= 0) return true;
        Bukkit.getScheduler().runTaskLater(Gutitubow.get(), () -> {
            this.breakConnectedGlass(block.getRelative(1, 0, 0), limit - 1);
            this.breakConnectedGlass(block.getRelative(0, 1, 0), limit - 1);
            this.breakConnectedGlass(block.getRelative(0, 0, 1), limit - 1);
            this.breakConnectedGlass(block.getRelative(-1, 0, 0), limit - 1);
            this.breakConnectedGlass(block.getRelative(0, -1, 0), limit - 1);
            this.breakConnectedGlass(block.getRelative(0, 0, -1), limit - 1);
        }, 1L);
        return true;
    }
}
