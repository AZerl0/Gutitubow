package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import com.azerl0.gutitubow.util.ArrowUtil;
import de.tr7zw.nbtapi.NBT;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

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
        if (CitizensAPI.getNPCRegistry().isNPC(player)) return;
        GamePlayer gamePlayer = game.getGamePlayer(player);
        gamePlayer.updatePlayerBoard();
    }

    @EventHandler
    public void interactBlock(PlayerInteractEvent event) {
        if (game.isStarted() && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Material block = event.getClickedBlock().getType();
            Set<Material> cancelBlocks = new HashSet<>(Set.of(
                    Material.CHEST,
                    Material.ENDER_CHEST,
                    Material.TRAPPED_CHEST,
                    Material.BARREL,
                    Material.FURNACE,
                    Material.BREWING_STAND,
                    Material.ENCHANTING_TABLE,
                    Material.FLOWER_POT,
                    Material.SMOKER,
                    Material.BLAST_FURNACE,
                    Material.ANVIL,
                    Material.CHIPPED_ANVIL,
                    Material.DAMAGED_ANVIL,
                    Material.DROPPER,
                    Material.DISPENSER,
                    Material.FLETCHING_TABLE,
                    Material.SMITHING_TABLE,
                    Material.CRAFTER,
                    Material.CRAFTING_TABLE,
                    Material.CARTOGRAPHY_TABLE,
                    Material.STONECUTTER,
                    Material.BEACON,
                    Material.DECORATED_POT,
                    Material.CHISELED_BOOKSHELF

            ));
            if (
                    Tag.COPPER_CHESTS.isTagged(block) ||
                    Tag.SHULKER_BOXES.isTagged(block) ||
                    Tag.FLOWER_POTS.isTagged(block) ||
                    Tag.BEDS.isTagged(block) ||
                    Tag.WOODEN_SHELVES.isTagged(block) ||
                    cancelBlocks.contains(block)
                    ) {
                event.setCancelled(true);
            }
        }
    }
}
