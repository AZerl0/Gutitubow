package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.AdvancementUtil;
import com.azerl0.gutitubow.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AbilityManager {

    private final Map<UUID, Ability> abilities = new HashMap<>();
    private final AbilityContext context;
    private final BukkitTask updateTask;

    public AbilityManager(AbilityContext context) {
        this.context = context;
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(context.gutitubow(), 0L, 1L);
    }

    @Nullable
    public Ability getAbility(Player player) {
        return abilities.get(player.getUniqueId());
    }

    @Nullable
    public Ability getAbility(UUID playerUUID) {
        return abilities.get(playerUUID);
    }

    @Nullable
    public Ability getAbility(GamePlayer gamePlayer) {
        return abilities.get(gamePlayer.getPlayerUUID());
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

        AdvancementUtil.grantAdvancement(player, "gutitubow:ability/root");
        AdvancementUtil.grantAdvancement(player, "gutitubow:ability/type/" + type.toString().toLowerCase());

    }

    public void setDisplayItem(Player player, AbilityType type, int index) {
        ItemStack displayItem = type.getDisplayItem(player);
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

    public Ability createAbility(AbilityType abilityType) {
        return AbilityRegistry.create(abilityType, context);
    }

    private void update() {
        for (Ability ability : new HashSet<>(abilities.values())) {
            ability.tick();
        }
    }
}