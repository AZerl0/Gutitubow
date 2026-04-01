package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.util.ComponentUtil;
import com.azerl0.gutitubow.util.MathUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;


public class JumpAbility extends Ability {
    private BukkitTask task = null;
    private int chargeTick;
    private final int maxChargeTick = (2 * 20) * 10; // 一つのゲージのチャージに2秒かかりそのゲージが10個ある

    public JumpAbility(AbilityContext context) {
        super(context);
        this.chargeTick = 0;
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.JUMP;
    }

    @Override
    public void init(Player player) {
        this.playerUUID = player.getUniqueId();
        player.setAllowFlight(true);
        chargeTick = 0;
        if (task != null && !task.isCancelled()) return;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                update(player);
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    public void update(Player player) {
        chargeTick++;
        if (chargeTick > maxChargeTick) chargeTick = maxChargeTick;
        if (((Entity) player).isOnGround() && chargeTick >= maxChargeTick / 10) player.setAllowFlight(true);

        Component message = ComponentUtil.getGaugeComponent(
                Component.text("§f跳躍チャージ: "),
                chargeTick,
                maxChargeTick,
                NamedTextColor.GREEN,
                NamedTextColor.WHITE
        );
        player.sendActionBar(message);
    }

    @Override
    public void reset() {
        getPlayer().setAllowFlight(false);
        getPlayer().sendActionBar(Component.empty());
        if (task != null) task.cancel();
        task = null;
    }

    @Override
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (!event.isFlying()) return;
        if (chargeTick < maxChargeTick / 10) {
            event.setCancelled(true);
            return;
        }

        Vector velocity = player.getLocation().clone().getDirection();
        if (velocity.getY() < 0.4) velocity.setY(0.4);
        player.setVelocity(velocity.multiply(MathUtil.getLerp(chargeTick, 0, maxChargeTick, 0.1, 2.5)));
        World world = player.getWorld();
        Location location = player.getLocation();
        player.playSound(location, Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        world.spawnParticle(Particle.POOF, location, 30, 1.0, 0.25, 1.0, 0);
        world.spawnParticle(Particle.SWEEP_ATTACK, location, 1);
        player.setFlying(false);
        player.setAllowFlight(false);
        event.setCancelled(true);
        chargeTick = 0;
    }
}
