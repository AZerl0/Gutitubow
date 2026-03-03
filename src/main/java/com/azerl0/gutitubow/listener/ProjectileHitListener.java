package com.azerl0.gutitubow.listener;


import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import com.azerl0.gutitubow.manager.GlassBreakManager;
import com.azerl0.gutitubow.util.ArrowUtil;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Arrow;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;


public class ProjectileHitListener implements Listener {

    private final JavaPlugin plugin;
    private final GlassBreakManager breakManager;
    private final AbilityManager abilityManager;

    public ProjectileHitListener(JavaPlugin plugin, AbilityManager abilityManager) {
        this.plugin = plugin;
        this.breakManager = new GlassBreakManager();
        this.abilityManager = abilityManager;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (event.isCancelled()) return;

        Block block = event.getHitBlock();
        AbilityType ability = null;

        if (arrow.getShooter() instanceof Player shooter) {
            ability = abilityManager.getAbilityType(shooter);

        }


        if (block != null && event.getEntity() instanceof Arrow) {
            ((Arrow) event.getEntity()).setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

            if (block.getType() == Material.SLIME_BLOCK) {
                if (ability != AbilityType.CONTROL) {
                    ArrowUtil.RicochetEvent(plugin, event);
                }

            } else if (GutitubowBlockTags.BREAKABLE_BY_GUTITUBOW_ARROW.contains(block.getType())) {
                if (breakManager.breakConnectedGlass(block, 64)) {

                    Bukkit.getScheduler().runTask(plugin, () -> NBT.modify(event.getEntity(), nbt -> {
                        nbt.setBoolean("inGround", false);
                    }));

                    if (ability != AbilityType.RICOCHET) {
                        event.getEntity().remove();
                    }
                }
            }


        }

    }

}
