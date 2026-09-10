package com.azerl0.gutitubow.util;

import com.azerl0.gutitubow.Gutitubow;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdvancementUtil {

    public static void grantAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
        AdvancementProgress progress = player.getAdvancementProgress(advancement);

        for (String criteria : progress.getRemainingCriteria()) {
            progress.awardCriteria(criteria);
        }
    }

    public static void grantAdvancement(Player player, String advancementPath) {
        NamespacedKey key = NamespacedKey.fromString(advancementPath);
        if (key == null) return;
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) return;
        grantAdvancement(player, advancement);
    }

    public static void revokeAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
        AdvancementProgress progress = player.getAdvancementProgress(advancement);

        for (String criteria : progress.getRemainingCriteria()) {
            progress.revokeCriteria(criteria);
        }
    }

    public static void revokeAdvancement(Player player, String advancementPath) {
        NamespacedKey key = NamespacedKey.fromString(advancementPath);
        if (key == null) return;
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) return;
        revokeAdvancement(player, advancement);
    }


}