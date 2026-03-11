package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import com.azerl0.gutitubow.util.ArrowUtil;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class GameListener implements Listener {
    private final JavaPlugin plugin;
    private final Game game;

    public GameListener(JavaPlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @EventHandler
    public void onLootDrop(BlockDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = game.getGamePlayer(player);
        if (game.isStarted()) {
            player.setGameMode(GameMode.SPECTATOR);
            PlayerInventory inventory = player.getInventory();
            inventory.setChestplate(ItemStack.of(Material.AIR));
        }
        gamePlayer.updatePlayerBoard();
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (event.isCancelled()) return;

        Block block = event.getHitBlock();
        AbilityType abilityType = null;

        if (arrow.getShooter() instanceof Player shooter) {

            Ability ability = game.getGamePlayer(shooter).getAbility();
            if (ability != null) abilityType = ability.getType();
        }

        if (block != null && event.getEntity() instanceof Arrow) {
            ((Arrow) event.getEntity()).setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

            if (block.getType() == Material.SLIME_BLOCK) {
                if (abilityType != AbilityType.CONTROL) {
                    ArrowUtil.RicochetEvent(plugin, event);
                }

            } else if (GutitubowBlockTags.BREAKABLE_BY_GUTITUBOW_ARROW.contains(block.getType())) {
                if (game.getGlassBreakManager().breakConnectedGlass(block, 64)) {

                    Bukkit.getScheduler().runTask(plugin, () -> NBT.modify(event.getEntity(), nbt -> {
                        nbt.setBoolean("inGround", false);
                    }));

                    if (abilityType != AbilityType.RICOCHET) {
                        event.getEntity().remove();
                    }
                }
            }


        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = game.getGamePlayer(player);
        gamePlayer.updatePlayerBoard();
    }
}
