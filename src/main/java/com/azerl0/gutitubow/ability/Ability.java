package com.azerl0.gutitubow.ability;


import com.azerl0.gutitubow.event.gameplayer.GamePlayerDamageEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerSneakingEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Ability {
    protected UUID playerUUID;
    protected final AbilityContext context;

    public Ability(AbilityContext context) {
        this.context = context;
    }

    @NotNull
    public abstract AbilityType getType();

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public void init(Player player) {
        this.playerUUID = player.getUniqueId();
    }

    public void reset() {
        getPlayer().sendActionBar(Component.empty());
        setCompassCooldown(0);
    }

    public boolean hasCompassCooldown() {
        Player player = getPlayer();
        if (player == null) return false;
        return player.hasCooldown(Material.COMPASS);
    }

    public int getCompassCooldown() {
        return getPlayer().getCooldown(Material.COMPASS);
    }

    public void setCompassCooldown(int tick) {
        getPlayer().setCooldown(Material.COMPASS, tick);
    }

    public void sendActionBarCompassCooldown() {
        double cooldown = getCompassCooldown();
        double cooldownSec = Math.floor((cooldown / 20) * 10) / 10;
        Component message = Component.text(" - 能力クールタイム中 " + cooldownSec + "秒 - ").color(NamedTextColor.RED);
        getPlayer().sendActionBar(message);
    }

    public void onArrowLaunch(EntityShootBowEvent event) {}

    public void onArrowHit(ProjectileHitEvent event) {}

    public void onAnyArrowHit(ProjectileHitEvent event) {}

    public void onPlayerRightClickBow(PlayerInteractEvent event) {}

    public void onPlayerLeftClickBow(PlayerInteractEvent event) {}

    public void onMove(PlayerMoveEvent event) {}

    public void onSneaking(GamePlayerSneakingEvent event) {}

    public void onToggleSneaking(PlayerToggleSneakEvent event) {}

    public void onToggleSprint(PlayerToggleSprintEvent event) {}

    public void onToggleFlight(PlayerToggleFlightEvent event) {}

    public void onJump(PlayerJumpEvent event) {}

    public void onPressJumpKey(PlayerInputEvent event) {}

    public void onInteract(PlayerInteractEvent event) {}

    public void onDamage(GamePlayerDamageEvent event) {}

    public void onDealDamage(GamePlayerDamageEvent event) {}

}