package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.MathUtil;
import com.azerl0.gutitubow.util.PlayerUtil;
import io.papermc.paper.math.Rotation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InversionAbility extends Ability {
    private int stock = 2;
    private int restockCharge = 0;
    private @Nullable Snowball inversionBall;

    public InversionAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.INVERSION;
    }

    @Override
    protected void onTick() {
        if (stock < 2) {
            restockCharge++;
            int RESTOCK_CHARGE_TICK = 20 * 20;
            if (restockCharge >= RESTOCK_CHARGE_TICK) {
                stock++;
                restockCharge = 0;
            }
        }

        Player player = getPlayer();
        if (player == null) return;
        if (hasCompassCooldown()) return;
        Component message = Component.text("Stock: ");
        for (int i = 0; i < 2; i++) {
            message = message.append(Component.text(i < stock ? "○" : "-"));
        }
        player.sendActionBar(message);
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        if (hasCompassCooldown()) return;
        if (event.getPlayer().isSneaking()) {
            Player player = getPlayer();
            if (player == null) return;
            if (stock < 2) {
                PlayerUtil.failEffectWithMessage(player, Component.text("能力を使用するためのストックが足りません！").color(NamedTextColor.RED));
            }

            shootInversionArrow(player);
            setCompassCooldown(isDebugMode() ? 20 : 45 * 20);
            startCompassCooldownTask();
            if (!isDebugMode()) stock -= 2;
        } else {
            if (stock <= 0) return;
            tryInversion();
            setCompassCooldown(isDebugMode() ? 20 : 20 * 20);
            startCompassCooldownTask();
            if (!isDebugMode()) stock--;

        }
    }

    @Override
    public void onAnyArrowHit(ProjectileHitEvent event) {
        if (inversionBall == null) return;
        Projectile projectile = event.getEntity();
        if (!projectile.getUniqueId().equals(inversionBall.getUniqueId())) return;

        Bukkit.getScheduler().runTaskLater(context.gutitubow(), () -> {
            projectile.getWorld().playSound(projectile.getLocation(), "gutitubow:sound.finger_snap", SoundCategory.MASTER, 0.75f, 1.0f);
            inversionFlash(projectile.getLocation());
            projectile.remove();
            inversionBall = null;
        }, 15L);
    }

    private void inversionFlash(Location location) {
        World world = location.getWorld();

        for (Player nearPlayer : world.getNearbyPlayers(location, 15)) {

            Key key = Key.key("gutitubow:horizontal_inversion");
            nearPlayer.postEffects().add(key);

            Bukkit.getScheduler().runTaskLater(context.gutitubow(), () -> {
                nearPlayer.postEffects().remove(key);
                nearPlayer.playSound(nearPlayer, "gutitubow:sound.finger_snap", SoundCategory.MASTER, 0.5f, 1.0f);
            }, 70);
        }
    }

    private void tryInversion() {
        Player player = getPlayer();
        if (player == null) return;

        player.getWorld().playSound(player.getEyeLocation(), "gutitubow:sound.finger_snap", SoundCategory.MASTER, 0.75f, 1.0f);
        double r = 5;
        for (Entity nearEntity : player.getNearbyEntities(r, r, r)) {
            if (nearEntity instanceof AbstractArrow arrow && arrow.isInBlock()) continue;
            if (!PlayerUtil.canSee(player, nearEntity.getLocation())) continue;

            if (nearEntity instanceof AbstractArrow arrow) {
                Vector velocity = arrow.getVelocity().multiply(-1.0);
                arrow.setVelocity(velocity);
                ArrowUtil.setFixRotation(arrow, velocity);
            } if (nearEntity instanceof LivingEntity livingEntity) {
                Rotation rotation = livingEntity.getLocation().getRotation();
                livingEntity.setRotation(rotation.yaw() + 180.0f, -rotation.pitch());
                livingEntity.setVelocity(livingEntity.getVelocity().multiply(-1.0));
            }
        }

        for (Player nearPlayer : player.getLocation().getNearbyPlayers(10)) {
            if (!MathUtil.isInView(player, nearPlayer.getLocation(), 100, 80)) continue;
            if (!PlayerUtil.canSee(player, nearPlayer)) continue;

            Rotation rotation = nearPlayer.getLocation().getRotation();
            nearPlayer.setRotation(rotation.yaw() + 180.0f, -rotation.pitch());
            nearPlayer.setVelocity(player.getVelocity().multiply(-1.0));
        }

    }

    private void shootInversionArrow(Player player) {
        Vector velocity = player.getLocation().getDirection().multiply(3);
        inversionBall = player.getWorld().spawn(player.getEyeLocation(), Snowball.class);
        inversionBall.setShooter(player);
        inversionBall.setVelocity(velocity);
        ItemStack itemStack = ItemStack.of(Material.FIREWORK_STAR);
        inversionBall.setItem(itemStack);
    }
}
