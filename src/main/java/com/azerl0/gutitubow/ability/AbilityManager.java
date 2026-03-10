package com.azerl0.gutitubow.ability;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {
    private final JavaPlugin plugin;

    public AbilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    static final Map<UUID, String> playerAbilities = new HashMap<>();

    @Nullable
    public String getAbilityId(Player player) {
        return playerAbilities.get(player.getUniqueId());
    }

    @Nullable
    public Ability getAbility(Player player) {
        AbilityType type = getAbilityType(player);
        if (type == null) return null;
        if (AbilityRegistry.get(type) == null) return null;
        return AbilityRegistry.get(type);
    }

    @Nullable
    public AbilityType getAbilityType(Player player) {
        String id = getAbilityId(player);
        if (id == null) return null;
        return AbilityType.valueOf(id.toUpperCase());
    }

    public void setAbility(Player player, AbilityType type) {
        Ability ability = getAbility(player);
        if (ability != null) ability.reset(player);
        playerAbilities.put(player.getUniqueId(), type.toString());
        Ability newAbility = getAbility(player);
        if (newAbility != null) newAbility.init(player);
    }

    public void resetAbility(Player player) {
        Ability ability = getAbility(player);
        if (ability != null) ability.reset(player);
        playerAbilities.put(player.getUniqueId(), "None");
    }
}