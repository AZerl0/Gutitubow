package com.azerl0.gutitubow.manager;

import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class GlassBreakManager {

    public boolean breakConnectedGlass(Block block, int limit) {
        Material type = block.getType();

        if (!GutitubowBlockTags.BREAKABLE_BY_GUTITUBOW_ARROW.contains(type)) return false;


        block.breakNaturally(true);

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
