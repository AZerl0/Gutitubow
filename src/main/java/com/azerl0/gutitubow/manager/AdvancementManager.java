package com.azerl0.gutitubow.manager;

import com.azerl0.gutitubow.ability.AbilityType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementManager {
    private final JavaPlugin plugin;

    public AdvancementManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void grantAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
        AdvancementProgress progress = player.getAdvancementProgress(advancement);

        for (String criteria : progress.getRemainingCriteria()) {
            progress.awardCriteria(criteria);
        }
    }

    public void revokeAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
        AdvancementProgress progress = player.getAdvancementProgress(advancement);

        for (String criteria : progress.getRemainingCriteria()) {
            progress.revokeCriteria(criteria);
        }
    }

    public @Nullable Advancement getAbilityTypeAdvancement(@NotNull AbilityType abilityType) {
        NamespacedKey advancementKey = NamespacedKey.fromString("ability/type/" + abilityType.toString().toLowerCase(), plugin);
        if (advancementKey == null) return null;
        return Bukkit.getAdvancement(advancementKey);
    }
}
