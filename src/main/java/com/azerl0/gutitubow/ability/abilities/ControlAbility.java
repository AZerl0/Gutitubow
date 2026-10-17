package com.azerl0.gutitubow.ability.abilities;


import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.entity.advanceprojectile.AdvanceProjectile;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerOnAbilityErasureEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerShootEvent;
import com.azerl0.gutitubow.event.gutitubowentity.GutitubowEntityRemoveEvent;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.MathUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;

import java.util.HashSet;
import java.util.Set;

public class ControlAbility extends Ability {
    private final Set<AdvanceProjectile> advanceProjectiles = new HashSet<>();

    public ControlAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public final @NotNull AbilityType getType() {
        return AbilityType.CONTROL;
    }

    @Override
    public void onGamePlayerShoot(GamePlayerShootEvent event) {
        if (event.isCancelled()) return;
        if (isErasing()) {
            onErasureEffect();
            return;
        }
        if (event.getProjectile() == null) return;

        Player player = event.getPlayer();

        AdvanceProjectile advanceProjectile = getAdvanceProjectile(event.getProjectile());
        advanceProjectile.setControlling(true);
        advanceProjectile.setControlSpeed(getDefaultControlSpeed(event.getForce()));
        advanceProjectile.setControlEffect(true);
        player.playSound(player, Sound.ENTITY_EVOKER_CAST_SPELL, 0.5f, 1.0f);
        advanceProjectiles.add(advanceProjectile);
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        Arrow arrow = (Arrow) event.getEntity();
        Player shooter = (Player) arrow.getShooter();

        if (shooter == null) return;

        if (event.getHitEntity() == shooter) {
            GamePlayer gamePlayer = context.game().getGamePlayer(shooter);
            if (gamePlayer.getArrowAmount() < gamePlayer.getMaxArrowStackSize()) gamePlayer.addArrow(1);
            event.setCancelled(true);
            event.getEntity().remove();
        }

        if (event.getHitBlock() != null) {
            arrow.setLifetimeTicks(1000);
            boolean collecting = arrow.getScoreboardTags().contains("gutitubow:control_arrow/collecting");
            if (collecting) arrow.remove();
        }
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        int cooldown = event.getPlayer().getCooldown(event.getPlayer().getInventory().getItemInMainHand());
        if (cooldown != 0) return;
        if (isErasing()) {
            onErasureEffect();
            return;
        }
        collectArrow(event.getPlayer());
    }

    @Override
    public void onGutitubowEntityRemove(GutitubowEntityRemoveEvent event) {
        if (event.getGutitubowEntity() instanceof AdvanceProjectile advanceProjectile) {
            advanceProjectiles.remove(advanceProjectile);
        }
    }

    @Override
    public void onAbilityErasure(GamePlayerOnAbilityErasureEvent event) {
        for (AdvanceProjectile advanceProjectile : advanceProjectiles) {
            advanceProjectile.setControlling(false);
        }
    }

    private void collectArrow(Player player) {
        player.setCooldown(player.getInventory().getItemInMainHand(), 20);

        Title title = Title.title(
                Component.empty(),
                Component.text("周囲の矢を回収します").color(NamedTextColor.LIGHT_PURPLE),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);
        player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.0f);

        Location returnLocation = player.getLocation().toVector().midpoint(player.getEyeLocation().toVector()).toLocation(player.getWorld());
        for (AdvanceProjectile advanceProjectile : advanceProjectiles) {
            advanceProjectile.setControlling(false);
            Vector newVelocity = returnLocation.toVector().subtract(advanceProjectile.getLocation().toVector()).normalize().multiply(3);
            double distance = advanceProjectile.getLocation().distance(returnLocation);

            if (distance < 48 && advanceProjectile.getProjectile() instanceof Arrow arrow) {

                arrow.setVelocity(newVelocity);
                ArrowUtil.setFixRotation(arrow, newVelocity);


                arrow.addScoreboardTag("gutitubow:control_arrow/collecting");
            } else {
                advanceProjectile.remove();
            }
        }
    }

    public static float getDefaultControlSpeed(float force) {
        return (float) MathUtil.getLerp(force, 0.0, 1.0, 0.4, 1.5);
    }
}
