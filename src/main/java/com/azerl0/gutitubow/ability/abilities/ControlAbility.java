package com.azerl0.gutitubow.ability.abilities;


import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.EntityUtil;
import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;

public class ControlAbility extends Ability {
    private final NamespacedKey controllingKey;
    private final NamespacedKey collectingKey;

    public ControlAbility(AbilityContext context) {
        super(context);
        this.controllingKey = new NamespacedKey(context.plugin(), "controlling");
        this.collectingKey = new NamespacedKey(context.plugin(), "collecting");
    }

    @Override
    public final @NotNull AbilityType getType() {
        return AbilityType.CONTROL;
    }

    @Override
    public void onShoot(EntityShootBowEvent event) {
        Arrow arrow = (Arrow) event.getProjectile();

        EntityUtil.setGlow(arrow, NamedTextColor.DARK_PURPLE);
        arrow.setGlowing(true);
        arrow.setGravity(false);

        NBT.modify(arrow, nbt -> {
            nbt.setBoolean("crit", false);
        });

        startControl(event);
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
            Boolean collecting = arrow.getPersistentDataContainer().get(collectingKey, PersistentDataType.BOOLEAN);
            if (collecting == null) return;
            if (collecting) arrow.remove();
        }


    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        int cooldown = event.getPlayer().getCooldown(event.getPlayer().getInventory().getItemInMainHand());
        if (cooldown != 0) return;
        Collect(event.getPlayer());
    }

    private void startControl(EntityShootBowEvent event) {
        Arrow arrow = (Arrow) event.getProjectile();

        arrow.getPersistentDataContainer().set(controllingKey, PersistentDataType.BOOLEAN, true);
        getPlayer().playSound(getPlayer(), Sound.ENTITY_EVOKER_CAST_SPELL, 0.5f, 1.0f);

        new BukkitRunnable() {

            @Override
            public void run() {

                Boolean controlling = arrow.getPersistentDataContainer().get(controllingKey, PersistentDataType.BOOLEAN);
                boolean inGround = NBT.get(arrow, nbt -> (boolean) nbt.getBoolean("inGround"));

                if (controlling == null) controlling = true;

                if (!arrow.isValid() || inGround || !controlling) {
                    cancel();
                    return;
                }

                arrow.getWorld().spawnParticle(
                        Particle.WITCH,
                        arrow.getLocation(),
                        2,
                        0, 0, 0,
                        0
                );

                @NotNull Vector v = event.getEntity().getEyeLocation().getDirection().multiply(0.75);

                arrow.setVelocity(v);


            }

        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    private void Collect(Player player) {


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


        for (World world : Bukkit.getWorlds()) {
            for (Arrow arrow : world.getEntitiesByClass(Arrow.class)) {

                if (!(arrow.getShooter() instanceof Player shooter)) continue;
                if (!shooter.getUniqueId().equals(player.getUniqueId())) continue;

                Location returnLocaton = shooter.getLocation().toVector().midpoint(shooter.getEyeLocation().toVector()).toLocation(player.getWorld());
                Vector newVelocity = returnLocaton.toVector().subtract(arrow.getLocation().toVector()).normalize().multiply(3);
                double distance = arrow.getLocation().distance(returnLocaton);

                if (distance < 48) {

                    arrow.getPersistentDataContainer().set(controllingKey, PersistentDataType.BOOLEAN, false);
                    arrow.getPersistentDataContainer().set(collectingKey, PersistentDataType.BOOLEAN, true);

                    arrow.setVelocity(newVelocity);
                    ArrowUtil.setFixRotation(arrow, newVelocity);

                    NBT.modify(arrow, nbt -> {
                        nbt.setBoolean("inGround", false);

                    });
                } else {
                    arrow.remove();
                }
            }
        }
    }
}
