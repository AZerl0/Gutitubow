package com.azerl0.gutitubow.data.tags.block;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GlassBlocks {
    public static final Tag<Material> GLASS_BLOCKS = new Tag<>() {
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
            return Set.of(
                    Material.GLASS,
                    Material.WHITE_STAINED_GLASS,
                    Material.LIGHT_GRAY_STAINED_GLASS,
                    Material.GRAY_STAINED_GLASS,
                    Material.BLACK_STAINED_GLASS,
                    Material.BROWN_STAINED_GLASS,
                    Material.RED_STAINED_GLASS,
                    Material.ORANGE_STAINED_GLASS,
                    Material.YELLOW_STAINED_GLASS,
                    Material.LIME_STAINED_GLASS,
                    Material.GREEN_STAINED_GLASS,
                    Material.CYAN_STAINED_GLASS,
                    Material.LIGHT_BLUE_STAINED_GLASS,
                    Material.BLUE_STAINED_GLASS,
                    Material.PURPLE_STAINED_GLASS,
                    Material.MAGENTA_STAINED_GLASS,
                    Material.PINK_STAINED_GLASS,
                    Material.TINTED_GLASS
            );
        }
    };
}
