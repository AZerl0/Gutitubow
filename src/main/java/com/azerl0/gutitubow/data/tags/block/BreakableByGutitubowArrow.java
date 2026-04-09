package com.azerl0.gutitubow.data.tags.block;

import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BreakableByGutitubowArrow {
    public static final Tag<Material> BREAKABLE_BY_GUTITUBOW_ARROW = new Tag<>() {

        @Override
        public @NotNull NamespacedKey getKey() {
            return new NamespacedKey("GUTITUBOW", "GLASS_BLOCKS");
        }

        @Override
        public boolean isTagged(@NotNull Material item) {
            return getValues().contains(item);
        }

        @Override
        public @NotNull Set<Material> getValues() {
            Set<Material> items = new HashSet<>();
            items.addAll(GutitubowBlockTags.GLASS_BLOCKS.getValues());
            items.addAll(GlassPanes.GLASS_PANES.getValues());
            return items;
        }
    };
}
