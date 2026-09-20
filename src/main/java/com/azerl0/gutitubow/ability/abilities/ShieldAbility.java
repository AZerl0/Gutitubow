package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.shield.Shield;
import com.azerl0.gutitubow.event.gutitubowentity.shield.ShieldLowerEvent;
import com.azerl0.gutitubow.event.gutitubowentity.shield.ShieldRaisingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShieldAbility extends Ability {
    private Shield shield;

    public ShieldAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.SHIELD;
    }

    @Override
    protected void onInit(Player player) {
        shield = getEntityManager().spawnGutitubowEntity(player.getLocation(), Shield.class);
        shield.setOwnerUUID(playerUUID);
    }

    @Override
    protected void onReset() {
        shield.remove();
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = getPlayer();
        if (player == null) return;

        if (event.isSneaking()) {
            if (!shield.isBlocking() && shield.getHealth() > 0 && player.getGameMode() != GameMode.SPECTATOR&& !hasCompassCooldown()) shield.raise();
        } else {
            if (shield.isBlocking()) shield.lower();
        }
    }

    @Override
    public void onShieldRaising(ShieldRaisingEvent event) {
        Player player = getPlayer();
        if (player == null) return;
        player.setCooldown(Material.BOW, 200);
        setCompassCooldown(200);
    }

    @Override
    public void onShieldLower(ShieldLowerEvent event) {
        Player player = getPlayer();
        if (player == null) return;
        player.setCooldown(Material.BOW, 20);
        setCompassCooldown(20);
        startCompassCooldownTask();
    }

    @Override
    public void onAnyArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        Entity hitEntity = event.getHitEntity();
        if (hitEntity == null) return;
        Optional<GutitubowEntity> optional = getEntityManager().getGutitubowEntity(hitEntity);
        if (optional.isEmpty()) return;
        if (optional.get() instanceof Shield hitShield) {
            if (hitShield != shield) return;
            Entity shooter = (Entity) event.getEntity().getShooter();
            if (shooter != null && context.game().isTeammate(playerUUID, shooter.getUniqueId())) {
                event.setCancelled(true);
                return;
            }

            Shield.blockEffect(event.getEntity().getLocation());
            shield.damage();
            event.getEntity().remove();
        }
    }

    @Override
    protected void onTick() {
        Player player = getPlayer();
        if (player == null) return;
        if (shield.isBlocking()) setCompassCooldown(200);
        Component healthComponent = Component.empty();
        for (int i = 0; i < 5; i++) {
            healthComponent = healthComponent.append(Component.text(i < shield.getHealth() ? "♥" : "-").color(NamedTextColor.RED));
        }
        player.sendActionBar(Component.text("SHIELD: ").append(healthComponent));
    }

    private GutitubowEntityManager getEntityManager() {
        return context.gutitubow().getGutitubowEntityManager();
    }
}
