package com.azerl0.gutitubow.data.tags.block;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GlassPanes {
    public static final Tag<Material> GLASS_PANES = new Tag<>() {

        @Override
        public @NotNull NamespacedKey getKey() {
            return new NamespacedKey("GUTITUBOW", "GLASS_BLOCKS");
        }

        @Override
        public boolean isTagged(@NotNull Material item) {
            return false;
        }

        @Override
        public @NotNull Set<Material> getValues() {
            return Set.of(
                    Material.GLASS_PANE,
                    Material.WHITE_STAINED_GLASS_PANE,
                    Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                    Material.GRAY_STAINED_GLASS_PANE,
                    Material.BLACK_STAINED_GLASS_PANE,
                    Material.BROWN_STAINED_GLASS_PANE,
                    Material.RED_STAINED_GLASS_PANE,
                    Material.ORANGE_STAINED_GLASS_PANE,
                    Material.YELLOW_STAINED_GLASS_PANE,
                    Material.LIME_STAINED_GLASS_PANE,
                    Material.GREEN_STAINED_GLASS_PANE,
                    Material.CYAN_STAINED_GLASS_PANE,
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    Material.BLUE_STAINED_GLASS_PANE,
                    Material.PURPLE_STAINED_GLASS_PANE,
                    Material.MAGENTA_STAINED_GLASS_PANE,
                    Material.PINK_STAINED_GLASS_PANE
            );
        }
    };
}
