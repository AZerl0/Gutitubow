package com.azerl0.gutitubow.ability;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {

    static final Map<UUID, String> playerAbilities = new HashMap<>();

    @Nullable
    static public String getAbilityId(Player player) {
        return playerAbilities.get(player.getUniqueId());
    }

    @Nullable
    static public Ability getAbility(Player player) {
        return AbilityRegistry.get(getAbilityId(player));
    }

    @Nullable
    static public AbilityType getAbilityType(Player player) {
        String id = getAbilityId(player);
        if (id == null) return null;
        return AbilityType.valueOf(id.toUpperCase());
    }

    static public void setAbility(Player player, String id) {
        playerAbilities.put(player.getUniqueId(), id);
    }

    static public void resetAbility(Player player) {
        playerAbilities.put(player.getUniqueId(), "None");
    }
}