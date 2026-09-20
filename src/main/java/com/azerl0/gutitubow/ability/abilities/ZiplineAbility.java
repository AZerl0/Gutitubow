package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.ability.zipline.Zipline;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ZiplineAbility extends Ability {
    private Zipline zipline;
    private @Nullable Chicken leashChicken;


    public ZiplineAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.ZIPLINE;
    }

    @Override
    protected void onReset() {
        if (zipline != null) context.ziplineManager().removeZipline(zipline);
        if (leashChicken != null) leashChicken.remove();
        updateTask.cancel();
    }

    @Override
    public void onShoot(EntityShootBowEvent event) {
        Player player = getPlayer();
        if (player == null) return;
        if (!hasCompassCooldown() && player.isSneaking()) {

            event.getProjectile().addScoreboardTag("gutitubow.ziplineArrow");
            if (leashChicken != null) leashChicken.remove();
            leashChicken = player.getWorld().spawn(event.getProjectile().getLocation(), Chicken.class);
            leashChicken.addScoreboardTag("gutitubow.cancelUnleash");
            leashChicken.setCollidable(false);
            leashChicken.setSilent(true);
            leashChicken.setAI(false);
            leashChicken.setInvulnerable(true);
            leashChicken.setInvisible(true);
            Objects.requireNonNull(leashChicken.getAttribute(Attribute.SCALE)).setBaseValue(0.0);
            leashChicken.setLeashHolder(player);
            event.getProjectile().addPassenger(leashChicken);
            setCompassCooldown(30 * 20);
            startCompassCooldownTask();
        }
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity().getScoreboardTags().contains("gutitubow.ziplineArrow") && event.getHitBlock() != null && getPlayer() != null) {
            if (zipline != null) context.ziplineManager().removeZipline(zipline);

            Location start = getPlayer().getLocation();
            Location end = event.getEntity().getLocation().add(0.0, 0.25, 0.0);
            // https://bugs.mojang.com/browse/MC-112474
            Vector offsetDir = event.getEntity().getLocation().getDirection();
            offsetDir.setZ(-offsetDir.getZ());
            end.add(offsetDir.multiply(0.1));
            zipline = context.ziplineManager().createZipline(start, end);
            if (leashChicken != null) leashChicken.remove();

        }
    }

    @Override
    protected void onTick() {
        if (leashChicken != null) {
            if (leashChicken.isInsideVehicle()) {
                if (getPlayer() != null) leashChicken.setLeashHolder(getPlayer());
            }
        }
    }
}
