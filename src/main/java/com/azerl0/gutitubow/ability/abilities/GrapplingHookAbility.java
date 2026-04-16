package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.ability.data.GrapplingHoolAbilityStateType;
import com.azerl0.gutitubow.game.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GrapplingHookAbility extends Ability {
    private @Nullable Chicken leashChicken;
    private @Nullable Arrow grapplingArrow;
    private GrapplingHoolAbilityStateType state = GrapplingHoolAbilityStateType.NONE;
    private BukkitTask updateTask;

    public GrapplingHookAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.GRAPPLINGHOOK;
    }

    @Override
    public void init(Player player) {
        super.init(player);

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    @Override
    public void reset() {
        super.reset();

        if (leashChicken != null) leashChicken.remove();
        if (grapplingArrow != null) grapplingArrow.remove();
        updateTask.cancel();
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        if (!hasCompassCooldown()) {
            if (state == GrapplingHoolAbilityStateType.READY) {
                if (grapplingArrow != null) {
                    grapplingMovement(grapplingArrow.getLocation());
                    state = GrapplingHoolAbilityStateType.NONE;
                    if (grapplingArrow != null) grapplingArrow.remove();
                    if (leashChicken != null) leashChicken.remove();
                    setCompassCooldown(3 * 20);
                    startCompassCooldownTask();
                }

            } else if (state == GrapplingHoolAbilityStateType.NONE) {

                GamePlayer gamePlayer = context.game().getGamePlayer(playerUUID);
                if (gamePlayer != null && gamePlayer.getArrowAmount() > 0) {
                    shootGrapplingHookArrow();
                    gamePlayer.removeArrow(1);
                    setCompassCooldown(10);
                    startCompassCooldownTask();
                }
            }
        }
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity().getScoreboardTags().contains("gutitubow.grapplingHookArrow")) {
            if (event.getHitEntity() != null) {
                event.setCancelled(true);
            } else if (event.getHitBlock() != null) {
                state = GrapplingHoolAbilityStateType.READY;
                Player player = getPlayer();
                if (player == null) return;
                player.playSound(player, Sound.BLOCK_IRON_DOOR_CLOSE, 0.75f, 2.0f);
            }
        }
    }

    private void grapplingMovement(Location location) {
        Player player = getPlayer();
        if (player == null) return;

        double distance = player.getLocation().clone().distance(location.clone());
        Vector sub = location.clone().subtract(player.getLocation()).toVector();
        double v = Math.max(0.75 , Math.min(distance * 2 , 5));
        Vector velocity = sub.normalize().multiply(v);
        if (velocity.getY() > 3.5) velocity.setY(3.5);

        player.setVelocity(velocity);

        PotionEffect levitation = new PotionEffect(PotionEffectType.LEVITATION, 6, 20, true, false, false);
        player.addPotionEffect(levitation);

        World world = player.getWorld();
        Location soundLocation = player.getLocation();
        world.playSound(soundLocation, Sound.ENTITY_GOAT_LONG_JUMP, 1.5f, 1.2f);
        world.playSound(soundLocation, Sound.ENTITY_ZOMBIE_INFECT, 1.5f, 2.0f);
        world.playSound(soundLocation, Sound.ENTITY_WITCH_THROW, 1.5f, 1.0f);
    }

    private void shootGrapplingHookArrow() {
        Player player = getPlayer();
        if (player == null) return;

        state = GrapplingHoolAbilityStateType.SHOOTING;
        Vector velocity = player.getLocation().getDirection().multiply(4);
        Arrow arrow = player.getWorld().spawn(player.getEyeLocation(), Arrow.class);
        arrow.setShooter(player);
        arrow.setVelocity(velocity);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        ArrowUtil.setFixRotation(arrow, velocity);
        arrow.addScoreboardTag("gutitubow.grapplingHookArrow");
        if (leashChicken != null) leashChicken.remove();
        leashChicken = player.getWorld().spawn(arrow.getLocation(), Chicken.class);
        leashChicken.addScoreboardTag("gutitubow.cancelUnleash");
        leashChicken.setCollidable(false);
        leashChicken.setSilent(true);
        leashChicken.setAI(false);
        leashChicken.setInvulnerable(true);
        leashChicken.setInvisible(true);
        Objects.requireNonNull(leashChicken.getAttribute(Attribute.SCALE)).setBaseValue(0.0);
        leashChicken.setLeashHolder(player);
        arrow.addPassenger(leashChicken);
        grapplingArrow = arrow;

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1.0f, 2.0f);
    }

    private void update() {
        if (leashChicken != null) {
            if (leashChicken.isInsideVehicle()) {
                if (getPlayer() != null) leashChicken.setLeashHolder(getPlayer());
            }
        }
        if (grapplingArrow != null && leashChicken != null) grapplingArrow.addPassenger(leashChicken);
    }
}
