package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerKillEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerPreDamageEvent;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.AdvancementUtil;
import com.azerl0.gutitubow.util.MathUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SmashAbility extends Ability {
    private final Map<UUID, CountdownDisplay> playerCountdowns = new HashMap<>();


    public SmashAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.SMASH;
    }

    @Override
    protected void onReset() {
        for (CountdownDisplay countdownDisplay : new HashSet<>(playerCountdowns.values())) {
            countdownDisplay.remove();
        }
    }

    @Override
    public void onAnyPlayerDamage(GamePlayerPreDamageEvent event) {
        if (event.isCancelled()) return;

        Player hitPlayer = event.getPlayer();
        if (context.game().isTeammate(playerUUID, hitPlayer.getUniqueId())) return;
        if (!isPinch(hitPlayer, -event.getAmount())) return;

        Player player = getPlayer();
        if (player != null && player.getGameMode() == GameMode.SPECTATOR) return;

        if (!playerCountdowns.containsKey(hitPlayer.getUniqueId())) {
            CountdownDisplay countdownDisplay = new CountdownDisplay(hitPlayer);
            playerCountdowns.put(hitPlayer.getUniqueId(), countdownDisplay);
        }
    }

    @Override
    public void onShoot(EntityShootBowEvent event) {
        if (event.isCancelled()) return;

        boolean shoot = false;
        for (UUID uuid : new HashSet<>(playerCountdowns.keySet())) {
            CountdownDisplay countdownDisplay = playerCountdowns.get(uuid);
            if (countdownDisplay.canShoot()) {
                smash(countdownDisplay.getTarget());
                playerCountdowns.remove(uuid);
                shoot = true;
            }
        }
        if (shoot) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onKill(GamePlayerKillEvent event) {
        if (event.getDamageSource().getDamageType() == GutitubowDamageType.SMASH) AdvancementUtil.grantAdvancement(event.getPlayer(), "gutitubow");
    }

    private void smash(Player target) {
        Player player = getPlayer();
        if (player == null) return;

        if (target.getGameMode() == GameMode.SPECTATOR) return;

        DamageSource damageSource = DamageSource.builder(GutitubowDamageType.SMASH)
                .directPlayer(player)
                .damageLocation(target.getEyeLocation())
                .distance(player.getLocation().distance(target.getLocation()))
                .build();
        GamePlayer gamePlayer = context.game().getGamePlayer(target);
        gamePlayer.damage(damageSource, 255);
        spawnParticleLine(
                target.getWorld(),
                player.getEyeLocation(),
                target.getEyeLocation(),
                0.1
        );
        if (playerCountdowns.containsKey(target.getUniqueId())) {
            CountdownDisplay countdownDisplay = playerCountdowns.get(target.getUniqueId());
            countdownDisplay.remove();
            playerCountdowns.remove(target.getUniqueId());
        }
    }

    private void spawnParticleLine(
            World world,
            Location start,
            Location end,
            double spacing
    ) {

        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();

        direction.normalize();

        int particles = (int) (distance / spacing);

        for (int i = 0; i <= particles; i++) {
            Vector offset = direction.clone().multiply(spacing * i);
            Location point = start.clone().add(offset);

            world.spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    point,
                    1,
                    0, 0, 0,
                    0,
                    null,
                    true
            );
        }
    }

    private boolean isPinch(Player target, double healthOffset) {
        double health = target.getHealth() + target.getAbsorptionAmount() + healthOffset;
        return  health <= 2.0;
    }

    private boolean isPinch(Player target) {
        return isPinch(target, 0.0);
    }

    private boolean isCanThrough(Material material) {
        if (Tag.FENCES.isTagged(material)) return true;
        if (Tag.FENCE_GATES.isTagged(material)) return true;
        if (Tag.CHAINS.isTagged(material)) return true;
        if (GutitubowBlockTags.GLASS_BLOCKS.isTagged(material)) return true;
        if (GutitubowBlockTags.GLASS_PANES.isTagged(material)) return true;
        return false;
    }

    private boolean canSee(Player player, Player target) {
        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector direction = target.getEyeLocation().toVector().subtract(eye.toVector());
        double distance = eye.distance(target.getEyeLocation());

        RayTraceResult blockHit = world.rayTraceBlocks(
                eye,
                direction,
                distance,
                FluidCollisionMode.NEVER,
                true,
                block -> !isCanThrough(block.getType())
        );

        boolean canSeeEye = blockHit == null;

        direction = target.getLocation().toVector().subtract(eye.toVector());
        distance = eye.distance(target.getLocation());


        blockHit = world.rayTraceBlocks(
                eye,
                direction,
                distance,
                FluidCollisionMode.NEVER,
                true,
                block -> !isCanThrough(block.getType())
        );


        boolean canSeeFoot = blockHit == null;

        return canSeeEye || canSeeFoot;
    }

    private class CountdownDisplay {
        private final int MAX_LOOKING_TICK = 3 * 20;
        private final TextDisplay textDisplay;
        private final BukkitTask updateTask;
        private final Player target;
        private int lookingTick = 0;

        private CountdownDisplay(Player target) {

            this.target = target;
            playerCountdowns.put(target.getUniqueId(), this);
            textDisplay = target.getWorld().spawn(target.getEyeLocation(), TextDisplay.class);
            target.addPassenger(textDisplay);
            textDisplay.setSeeThrough(true);
            textDisplay.setBackgroundColor(Color.fromARGB(0));
            textDisplay.setViewRange(10f);
            textDisplay.setBrightness(new Display.Brightness(15, 15));
            textDisplay.setBillboard(Display.Billboard.CENTER);

            updateTask = new BukkitRunnable() {
                @Override
                public void run() {
                    updateCountdownDisplay();
                }
            }.runTaskTimer(context.gutitubow(), 0L, 1L);
        }

        private Player getTarget() {
            return target;
        }

        private boolean canShoot() {
            return lookingTick >= MAX_LOOKING_TICK;
        }

        private void remove() {
            playerCountdowns.remove(target.getUniqueId());
            updateTask.cancel();

            textDisplay.remove();
        }

        private void updateCountdownDisplay() {
            Player player = getPlayer();
            if (player == null) {
                lookingTick = 0;
                return;
            }

            if (target == null || !target.isOnline() || !isPinch(target) || player.getGameMode() == GameMode.SPECTATOR || target.getGameMode() == GameMode.SPECTATOR) {
                this.remove();
                return;
            }
            if (MathUtil.isInView(player, target.getLocation(), 100, 80) && canSee(player, target) && !isErasing()) {
                lookingTick++;
                if (lookingTick == 1) {
                    target.playSound(target, Sound.ENTITY_WOLF_GROWL, 1.0f, 1.0f);
                    Title title = Title.title(
                            Component.empty(),
                            Component.text("狙われている....").color(NamedTextColor.RED),
                            Title.Times.times(
                                    java.time.Duration.ofMillis(100),
                                    java.time.Duration.ofSeconds(2),
                                    java.time.Duration.ofMillis(500)
                            )
                    );
                    target.showTitle(title);

                }
            } else {
                lookingTick -= 10;
                if (lookingTick < 0) lookingTick = 0;
            }

            // 距離から表示scaleの取得
            double distance = getPlayer().getLocation().distance(target.getLocation());
            float scale = (float) Math.max(distance / 5, 3.0f);
            scale = Math.min(scale, 16.0f);

            // パーセンテージの取得
            double per = Math.floor((double) lookingTick / MAX_LOOKING_TICK * 100);
            Component text = Component.text(per + "%").color(NamedTextColor.RED);
            if (lookingTick >= MAX_LOOKING_TICK) {
                text = Component.text("☠").color(NamedTextColor.RED);
                scale *= 2.25f;
            }
            if (lookingTick <= 0) text = Component.empty();


            WrapperPlayServerEntityMetadata textPacket = new WrapperPlayServerEntityMetadata(
                    textDisplay.getEntityId(),
                    List.of(
                            new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(scale, scale, scale)), // Scale
                            new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, text) // Text
                    )
            );

            User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            user.sendPacket(textPacket);
        }
    }
}
