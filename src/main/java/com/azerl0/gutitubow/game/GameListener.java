package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import com.azerl0.gutitubow.event.gameplayer.*;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.message.Message;
import com.azerl0.gutitubow.pdc.LocationDataType;
import com.azerl0.gutitubow.util.AdvancementUtil;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.EntityUtil;
import com.azerl0.gutitubow.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;

public class GameListener implements Listener {
    private final Gutitubow gutitubow;
    private final Game game;
    private final NamespacedKey spawnLocationKey;

    public GameListener(Gutitubow gutitubow, Game game) {
        this.gutitubow = gutitubow;
        this.game = game;
        spawnLocationKey = new NamespacedKey(gutitubow, "spawn_location");
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
            game.showBossBar(player);
        }
        gamePlayer.updatePlayerBoard();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = game.getGamePlayer(player);
        if (game.isStarted() && game.getAlivePlayers().contains(gamePlayer)) {
            DamageSource damageSource = DamageSource.builder(GutitubowDamageType.LOGOUT).build();
            game.kill(gamePlayer, damageSource);
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (event.isCancelled()) return;

        Entity hitEntity = event.getHitEntity();

        if (hitEntity == null) {
            // hit block
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
                        ArrowUtil.RicochetEvent(gutitubow, event);
                    }

                } else if (GutitubowBlockTags.BREAKABLE_BY_GUTITUBOW_ARROW.isTagged(block.getType())) {
                    if (game.getGlassBreakManager().breakConnectedGlass(block, 64)) {

                        if (abilityType != AbilityType.RICOCHET) {
                            event.getEntity().remove();
                        }
                    }
                }
            }
        } else {
            // Hit Entity

            Entity shooter = (Entity) event.getEntity().getShooter();

            if (!(event.getEntity() instanceof AbstractArrow)) return;
            if (event.getEntity() instanceof SpectralArrow) return;
            if (shooter == null) return;

            // myself cancel
            if (shooter.getUniqueId().equals(hitEntity.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
            if (EntityUtil.isInvalidHitEntity(hitEntity)) return;

            if (hitEntity instanceof Player hitPlayer) {
                if (!(shooter instanceof Player shooterPlayer)) return;

                if (game.getTeamManager().isTeammate(shooterPlayer, hitPlayer)) {
                    event.setCancelled(true);
                    return;
                }

                Location projectileSpawnLocation = arrow.getPersistentDataContainer().getOrDefault(spawnLocationKey, LocationDataType.INSTANCE, arrow.getLocation());

                double distance = projectileSpawnLocation.distance(hitEntity.getLocation());

                DamageSource damageSource = DamageSource.builder(GutitubowDamageType.ARROW_SHOT)
                        .directPlayer(shooterPlayer)
                        .damageLocation(event.getEntity().getLocation())
                        .distance(distance)
                        .build();
                game.getGamePlayer(hitPlayer).damage(damageSource, 2);

                event.setCancelled(true);
                event.getEntity().remove();

            } else {
                if (hitEntity instanceof HappyGhast hitHappyGhast) {
                    event.getEntity().remove();

                    hitHappyGhast.damage(2);
                    event.setCancelled(true);
                    return;
                } else if (hitEntity instanceof Mannequin) {
                    return;
                }
                if (!(hitEntity instanceof ArmorStand armorStand && armorStand.isMarkedInvulnerable())) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        event.getProjectile().getPersistentDataContainer().set(spawnLocationKey, LocationDataType.INSTANCE, event.getProjectile().getLocation());
    }

    @EventHandler
    public void onPlayerDamage(GamePlayerDamageEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Component damageMessage = Message.getDamageMessage(event.getPlayer(), event.getDamageSource(), player);
            if (damageMessage != null) player.sendMessage(damageMessage);
        }
        if (event.getDamageSource().getDamageType() == GutitubowDamageType.ARROW_SHOT) {
            double distance = event.getDamageSource().getDistance();
            Player attacker = event.getDamageSource().getDirectPlayer();

            if (attacker != null) {
                if (distance >= 75) {
                    AdvancementUtil.grantAdvancement(attacker, "gutitubow:gutitubow/shoot_75m");
                    if (distance >= 100) AdvancementUtil.grantAdvancement(attacker, "gutitubow:/gutitubow/shoot_100m");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(GamePlayerDeathEvent event) {
        if (event.isCancelled()) return;
        Player deathPlayer = event.getGamePlayer().getPlayer();
        if (deathPlayer == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Component damageMessage = Message.getDeathMessage(deathPlayer, event.getDamageSource(), player);
            if (damageMessage != null) player.sendMessage(damageMessage);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
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

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player inteactPlayer) {
            Player player = event.getPlayer();
            if (player.getGameMode() != GameMode.SPECTATOR) return;
            GamePlayer interactGamePlayer = game.getGamePlayer(inteactPlayer);
            Ability ability = gutitubow.getGutitubowManagers().getAbilityManager().getAbility(inteactPlayer);
            Component abilityName = ability == null ? Component.text("なし") : ability.getType().getName();
            Component message = inteactPlayer.displayName().color(NamedTextColor.GREEN)
                    .append(Component.text("\n能力：").color(NamedTextColor.GOLD).append(abilityName))
                    .append(Component.text("\n残り体力：").color(NamedTextColor.GOLD).append(PlayerUtil.getHealthComponent(inteactPlayer)))
                    .append(Component.text("\nキル数：").color(NamedTextColor.GOLD).append(Component.text(interactGamePlayer.getTotalKill()).color(NamedTextColor.GREEN)))
                    .append(Component.text(", ヒット数：").color(NamedTextColor.GOLD).append(Component.text(interactGamePlayer.getTotalHit()).color(NamedTextColor.GREEN)))
                    .append(Component.text("\nポーション数：").color(NamedTextColor.GOLD).append(Component.text(interactGamePlayer.getPotionAmount() + "個").color(NamedTextColor.GREEN)));
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onWin(GamePlayerWinEvent event) {
        AdvancementUtil.grantAdvancement(event.getPlayer(), "gutitubow:gutitubow/win");
        GamePlayer gamePlayer = game.getGamePlayer(event.getPlayer());
        if (gamePlayer.getTotalKill() == 0) AdvancementUtil.grantAdvancement(event.getPlayer(), "gutitubow:gutitubow/win");
    }
}
