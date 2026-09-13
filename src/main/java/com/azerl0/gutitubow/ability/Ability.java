package com.azerl0.gutitubow.ability;


import com.azerl0.gutitubow.event.gameplayer.*;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Ability {
    protected UUID playerUUID;
    protected final AbilityContext context;
    protected BukkitTask cooldownTask;

    public Ability(AbilityContext context) {
        this.context = context;
    }

    @NotNull
    public abstract AbilityType getType();

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public void init(Player player) {
        this.playerUUID = player.getUniqueId();
    }

    public void reset() {
        Player player = getPlayer();
        if (player != null) {
            getPlayer().sendActionBar(Component.empty());
            setCompassCooldown(0);
            player.setAbsorptionAmount(0);
        }
        if (cooldownTask != null) cooldownTask.cancel();
        cooldownTask = null;
    }

    public boolean hasCompassCooldown() {
        Player player = getPlayer();
        if (player == null) return false;
        return player.hasCooldown(Material.COMPASS);
    }

    public int getCompassCooldown() {
        Player player = getPlayer();
        if (player == null) return 0;
        return player.getCooldown(Material.COMPASS);
    }

    public void setCompassCooldown(int tick) {
        Player player = getPlayer();
        if (player == null) return;
        player.setCooldown(Material.COMPASS, tick);
    }

    public void sendActionBarCompassCooldown() {
        Player player = getPlayer();
        if (player == null) return;
        double cooldown = getCompassCooldown();
        double cooldownSec = Math.floor((cooldown / 20) * 10) / 10;
        Component message = Component.text(" - 能力クールタイム中 " + cooldownSec + "秒 - ").color(NamedTextColor.RED);
        player.sendActionBar(message);
    }

    public void startCompassCooldownTask() {
        if (cooldownTask != null && !cooldownTask.isCancelled()) return;

        cooldownTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (player == null) {
                    this.cancel();
                    return;
                }
                if (player.getCooldown(Material.COMPASS) == 0) {
                    this.cancel();
                    player.sendActionBar(Component.text(" - 能力使用可能 - ").color(NamedTextColor.GREEN));
                    cooldownTask = null;
                    return;
                }
                sendActionBarCompassCooldown();
            }
        }.runTaskTimer(context.gutitubow(), 0L, 1L);
    }

    public boolean isDebugMode() {
        Player player = getPlayer();
        if (player == null) return false;
        Material handItem = player.getInventory().getItemInOffHand().getType();
        return handItem == Material.PAPER;
    }

    /*
     * 能力に関する発火を追加した際には、
     * 「変化」、「複製」で、発火のOverrideを追加すること
     */

    public void onShoot(EntityShootBowEvent event) {}

    public void onArrowHit(ProjectileHitEvent event) {}

    public void onAnyArrowHit(ProjectileHitEvent event) {}

    public void onLeftClick(PlayerFixLeftClickEvent event) {}

    public void onPlayerRightClickBow(PlayerInteractEvent event) {}

    public void onPlayerLeftClickBow(PlayerInteractEvent event) {}

    public void onMove(PlayerMoveEvent event) {}

    public void onSneaking(GamePlayerSneakingEvent event) {}

    public void onToggleSneak(PlayerToggleSneakEvent event) {}

    public void onToggleSprint(PlayerToggleSprintEvent event) {}

    public void onToggleFlight(PlayerToggleFlightEvent event) {}

    public void onJump(PlayerJumpEvent event) {}

    public void onPressJumpKey(PlayerInputEvent event) {}

    public void onInteract(PlayerInteractEvent event) {}

    public void onAttack(EntityDamageByEntityEvent event) {}

    public void onAnyPlayerDamage(GamePlayerDamageEvent event) {}

    public void onDamage(GamePlayerDamageEvent event) {}

    public void onDealDamage(GamePlayerDamageEvent event) {}

    public void onKill(GamePlayerKillEvent event) {}

    public void onDeath(GamePlayerDeathEvent event) {}

    public void onClick(InventoryClickEvent event) {}

    public void onWin(GamePlayerWinEvent event) {}
}