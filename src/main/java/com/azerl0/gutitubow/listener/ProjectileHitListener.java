package com.azerl0.gutitubow.listener;


import com.azerl0.gutitubow.ability.AbilityManager;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import com.azerl0.gutitubow.manager.GlassBreakManager;
import com.azerl0.gutitubow.util.ArrowUtil;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
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

    public ProjectileHitListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.breakManager = new GlassBreakManager();
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getHitBlock();
        Arrow arrow = (Arrow) event.getEntity();
        Player shooter = (Player) arrow.getShooter();

        if (shooter == null) return;

        String ability = AbilityManager.getAbilityId(shooter);


        if (block != null && event.getEntity() instanceof Arrow) {

            if (block.getType() == Material.SLIME_BLOCK) {
                if (!(ability != null && ability.equals("control"))) {
                    ArrowUtil.RicochetEvent(plugin, event);
                }

            } else if (GutitubowBlockTags.BREAKABLE_BY_GUTITUBOW_ARROW.contains(block.getType())) {
                if (breakManager.breakConnectedGlass(block, 64)) {

                    Bukkit.getScheduler().runTask(plugin, () -> NBT.modify(event.getEntity(), nbt -> {
                        nbt.setBoolean("inGround", false);
                    }));

                    if (ability != null && ability.equals("control")) {
                        event.getEntity().remove();
                    }
                }
            }


        }

    }

}
