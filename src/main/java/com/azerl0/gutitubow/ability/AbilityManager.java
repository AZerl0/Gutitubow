package com.azerl0.gutitubow.ability;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {

    private final Map<UUID, Ability> abilities = new HashMap<>();
    private final AbilityContext context;

    public AbilityManager(AbilityContext context) {
        this.context = context;
    }

    @Nullable
    public Ability getAbility(Player player) {
        return abilities.get(player.getUniqueId());
    }

    public void setAbility(Player player, AbilityType type) {

        Ability old = abilities.remove(player.getUniqueId());
        if (old != null) old.reset();

        Ability ability = AbilityRegistry.create(type, context);
        if (ability == null) return;

        ability.init(player);
        abilities.put(player.getUniqueId(), ability);
        player.getInventory().setItem(13, type.getDisplayItem());
    }

    public void resetAbility(Player player) {
        Ability ability = abilities.remove(player.getUniqueId());
        if (ability != null) ability.reset();
        player.getInventory().setItem(13, ItemStack.of(Material.AIR));
    }
}