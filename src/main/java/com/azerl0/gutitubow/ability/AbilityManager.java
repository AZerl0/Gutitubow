package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    public Set<UUID> getKeySet() {
        return abilities.keySet();
    }

    public Collection<Ability> getValues() {
        return abilities.values();
    }

    public void setAbility(Player player, AbilityType type) {

        Ability old = abilities.remove(player.getUniqueId());
        if (old != null) old.reset();

        Ability ability = AbilityRegistry.create(type, context);
        if (ability == null) return;

        ability.init(player);
        abilities.put(player.getUniqueId(), ability);
        setDisplayItem(player, type, 13);
    }

    public void setDisplayItem(Player player, AbilityType type, int index) {
        ItemStack displayItem = type.getDisplayItem();
        ItemUtil.setCantMove(displayItem, true);
        ItemUtil.setCantDrop(displayItem, true);
        player.getInventory().setItem(index, displayItem);
    }

    public void resetAbility(Player player) {
        Ability ability = abilities.remove(player.getUniqueId());
        if (ability != null) ability.reset();
        player.getInventory().setItem(13, ItemStack.of(Material.AIR));
    }

    public void allResetAbilities() {
        for (Ability ability : abilities.values()) {
            ability.reset();
        }
    }
}