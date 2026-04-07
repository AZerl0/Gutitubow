package com.azerl0.gutitubow.data.tags.block;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class BreakableByGutitubowArrow {
    public static final Set<Material> BREAKABLE_BY_GUTITUBOW_ARROW;
    static {
        Set<Material> blocks = new HashSet<>();
        blocks.addAll(GlassBlocks.GLASS_BLOCKS);
        blocks.addAll(GlassPanes.GLASS_PANES);
        BREAKABLE_BY_GUTITUBOW_ARROW = blocks;
    }
}
