package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDamageEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerSneakingEvent;
import com.azerl0.gutitubow.util.ComponentUtil;
import com.azerl0.gutitubow.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class InvincibleAbility extends Ability {
    private int toInvincibleTick = 0;
    private int invincibleCharge = 0;
    private final int TO_INVINCIBLE_TICK = 20;
    private final int MAX_INVINCIBLE_CHARGE = 10 * 20;
    private boolean canInvincible = true;
    private boolean isInvincible = false;
    private BukkitTask updateTask;

    public InvincibleAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return null;
    }

    @Override
    public void reset() {
        super.reset();
        resetInvincible();
        if (updateTask != null) updateTask.cancel();
        updateTask = null;
    }

    @Override
    public void init(Player player) {
        super.init(player);

        if (updateTask != null && !updateTask.isCancelled()) return;

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(context.gutitubow(), 0L, 1L);

    }

    @Override
    public void onAnyArrowHit(ProjectileHitEvent event) {
        if (event.getHitEntity() != null && event.getHitEntity().getUniqueId() == playerUUID && isInvincible) {
            block(event.getEntity().getLocation());
            event.getEntity().remove();
        }
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            if (isInvincible) {
                resetInvincible();
            }
        } else {
            toInvincibleTick = 0;
            canInvincible = true;
        }
    }

    @Override
    public void onSneaking(GamePlayerSneakingEvent event) {
        Player player = getPlayer();
        if (player == null || !canInvincible) return;
        Material mainHand = player.getInventory().getItemInMainHand().getType();
        if (mainHand == Material.COMPASS || mainHand == Material.BEACON) {
            toInvincibleTick = 0;
            return;
        }
        if (!isInvincible) {

            toInvincibleTick++;
            if (toInvincibleTick < TO_INVINCIBLE_TICK) {
                Component subtitle = ComponentUtil.getGaugeComponent(
                        Component.text("無敵化中... ").color(NamedTextColor.GRAY),
                        toInvincibleTick,
                        TO_INVINCIBLE_TICK,
                        NamedTextColor.BLACK,
                        NamedTextColor.GRAY
                );
                Title title = Title.title(
                        Component.empty(),
                        subtitle,
                        Title.Times.times(
                                java.time.Duration.ofMillis(0),
                                java.time.Duration.ofSeconds(1),
                                java.time.Duration.ofMillis(500)
                        ));
                player.showTitle(title);

            } else {
                if (toInvincibleTick == TO_INVINCIBLE_TICK) activeInvincible(player);
            }
        }
    }

    @Override
    public void onDamage(GamePlayerDamageEvent event) {
        GutitubowDamageType damageType = event.getDamageSource().getDamageType();
        Set<GutitubowDamageType> canBlockDamage = Set.of(
                GutitubowDamageType.ARROW_SHOT,
                GutitubowDamageType.BOMB_BLAST,
                GutitubowDamageType.RICOCHET_BLAST,
                GutitubowDamageType.SNIPE
        );
        if (isInvincible && canBlockDamage.contains(damageType)) {
            Location location = event.getDamageSource().getDamageLocation();
            if (location != null) block(event.getDamageSource().getDamageLocation());
            event.setCancelled(true);
        }
    }

    private void block(Location location) {
        World world = location.getWorld();
        BlockData blockData = Material.BARRIER.createBlockData();
        world.spawnParticle(Particle.BLOCK_MARKER, location, 1, blockData);
        world.playSound(location, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 2.0f);
    }

    private void activeInvincible(Player player) {
        isInvincible = true;
        canInvincible = false;
        PlayerUtil.playTotemAnimation(player);
        EntityEquipment equipment = player.getEquipment();
        equipment.setHelmet(ItemStack.of(Material.NETHERITE_HELMET));
        equipment.setChestplate(ItemStack.of(Material.NETHERITE_CHESTPLATE));
        equipment.setLeggings(ItemStack.of(Material.NETHERITE_LEGGINGS));
        equipment.setBoots(ItemStack.of(Material.NETHERITE_BOOTS));
        setBindingCurse(EquipmentSlot.HEAD);
        setBindingCurse(EquipmentSlot.CHEST);
        setBindingCurse(EquipmentSlot.LEGS);
        setBindingCurse(EquipmentSlot.FEET);
    }

    private void setBindingCurse(EquipmentSlot equipmentSlot) {
        Player player = getPlayer();
        if (player == null) return;
        ItemStack itemStack = player.getEquipment().getItem(equipmentSlot);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setEnchantmentGlintOverride(false);
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        itemStack.setItemMeta(meta);
        player.getEquipment().setItem(equipmentSlot, itemStack);
    }

    private void resetInvincible() {
        canInvincible = false;
        isInvincible = false;
        toInvincibleTick = 0;
        Player player = getPlayer();
        if (player == null) return;

        EntityEquipment equipment = player.getEquipment();
        equipment.setHelmet(ItemStack.of(Material.AIR));
        equipment.setChestplate(ItemStack.of(Material.AIR));
        equipment.setLeggings(ItemStack.of(Material.AIR));
        equipment.setBoots(ItemStack.of(Material.AIR));
    }

    private void update() {
        Player player = getPlayer();
        if (player == null) return;
        if (isInvincible) {
            if (player.getInventory().getItemInOffHand().getType() != Material.PAPER) invincibleCharge--;
            if (invincibleCharge <= 0) {
                resetInvincible();
                invincibleCharge = 0;
            }
        } else {
            if (player.getWorld().getGameTime() % 4 == 0) {
                invincibleCharge++;
                invincibleCharge = Math.min(invincibleCharge, MAX_INVINCIBLE_CHARGE);
            }
        }

        double chargeSecondsText = Math.floor(((double) invincibleCharge / 20) * 10) / 10;
        player.sendActionBar(Component.text("無敵チャージ" + chargeSecondsText + "秒"));

    }
}
