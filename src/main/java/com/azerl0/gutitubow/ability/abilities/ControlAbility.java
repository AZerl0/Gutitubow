package com.azerl0.gutitubow.ability.abilities;


import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.entity.controlarrow.ControlArrow;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerOnAbilityErasureEvent;
import com.azerl0.gutitubow.event.gutitubowentity.GutitubowEntityRemoveEvent;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.MathUtil;
import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;

import java.util.HashSet;
import java.util.Set;

public class ControlAbility extends Ability {
    private final Set<ControlArrow> controlArrowSet = new HashSet<>();

    public ControlAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public final @NotNull AbilityType getType() {
        return AbilityType.CONTROL;
    }

    @Override
    public void onShoot(EntityShootBowEvent event) {
        if (isErasing()) {
            return;
        }
        ControlArrow controlArrow = ControlArrow.spawnFromShootEvent(event, context.gutitubow().getGutitubowEntityManager());
        controlArrowSet.add(controlArrow);
        event.getProjectile().remove();
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
        if (event.getGutitubowEntity() instanceof ControlArrow controlArrow) {
            controlArrowSet.remove(controlArrow);
        }
    }

    @Override
    public void onAbilityErasure(GamePlayerOnAbilityErasureEvent event) {
        for (ControlArrow controlArrow : controlArrowSet) {
            controlArrow.setControlling(false);
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
        for (ControlArrow controlArrow : controlArrowSet) {
            controlArrow.setControlling(false);
            Vector newVelocity = returnLocation.toVector().subtract(controlArrow.getLocation().toVector()).normalize().multiply(3);
            double distance = controlArrow.getLocation().distance(returnLocation);

            if (distance < 48) {
                Arrow arrow = controlArrow.getArrow();

                arrow.setVelocity(newVelocity);
                ArrowUtil.setFixRotation(arrow, newVelocity);

                NBT.modify(arrow, nbt -> {
                    nbt.setBoolean("inGround", false);
                });

                arrow.addScoreboardTag("gutitubow:control_arrow/collecting");
            } else {
                controlArrow.remove();
            }
        }
    }
}
