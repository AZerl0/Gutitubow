package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerPreDamageEvent;
import com.azerl0.gutitubow.profiler.ProfileChangeReason;
import com.azerl0.gutitubow.profiler.SkinDataManager;
import com.azerl0.gutitubow.util.MathUtil;
import com.azerl0.gutitubow.util.PlayerUtil;
import com.azerl0.gutitubow.util.PostEffectUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MedusaAbility extends Ability {
    private final Map<Player, LookingTime> lookingPlayers = new HashMap<>();
    private Map<Player, LookingTime> oldLookingPlayers = new HashMap<>();
    private final Set<Player> petrificationPlayers = new HashSet<>();


    public MedusaAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.MEDUSA;
    }

    @Override
    protected void onReset() {
        resetGlow();
        for (Player target : petrificationPlayers) {
            resetPetrification(target);
        }
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        if (isErasing()) {
            onErasureEffect();
            return;
        }
        Player player = getPlayer();
        if (player == null) return;

        if (hasCompassCooldown()) {
            player.sendMessage(Component.text("能力クールダウン中です").color(NamedTextColor.RED));
            return;
        }

        boolean petrification = false;
        for (Player nearPlayer : player.getWorld().getNearbyPlayers(player.getEyeLocation(), 50)) {
            if (nearPlayer.getGameMode() == GameMode.SPECTATOR || nearPlayer.getGameMode() == GameMode.CREATIVE) continue;
            if (context.game().isTeammate(player, nearPlayer)) continue;
            if (isLookingAtEachOther(player, nearPlayer) && canSee(player, nearPlayer)) {
                LookingTime lookingTime = lookingPlayers.get(nearPlayer);
                int tick = Math.min(lookingTime.getLookingTick() * 2, 4 * 20 + 10);
                petrificationPlayer(nearPlayer, tick);
                petrification = true;
            }
        }
        if (!petrification) {
            Title title = Title.title(
                    Component.empty(),
                    Component.text("石化可能な相手がいない(目が合っていない)").color(NamedTextColor.GRAY),
                    Title.Times.times(
                            java.time.Duration.ofMillis(0),
                            java.time.Duration.ofSeconds(1),
                            java.time.Duration.ofMillis(0)
                    )
            );
            player.showTitle(title);

            return;
        }
        resetGlow();
        setCompassCooldown(20 * 20);
        startCompassCooldownTask();
    }

    @Override
    public void onAnyPlayerDamage(GamePlayerPreDamageEvent event) {
        if (petrificationPlayers.contains(event.getPlayer())) {
            Location location = event.getDamageSource().getDamageLocation();
            if (location != null) {
                World world = event.getPlayer().getWorld();
                BlockData blockData = Material.BARRIER.createBlockData();
                world.spawnParticle(Particle.BLOCK_MARKER, location, 1, blockData);

            }
            event.setCancelled(true);
        }
    }

    private static class LookingTime {
        private int lookingTick = 0;
        private int outsideSafeTick = 0;

        private int getLookingTick() {
            return lookingTick;
        }

        private void update(boolean isOutside) {
            if (isOutside) {
                outsideSafeTick -= 1;
                if (outsideSafeTick <= 0) {
                    outsideSafeTick = 0;
                    lookingTick = 0;
                }
            } else{
                lookingTick += 4;
                outsideSafeTick = 10;
            }
        }
    }

    @Override
    protected void onTick() {
        Player player = getPlayer();
        if (player == null) return;


        if (hasCompassCooldown() || player.getGameMode() == GameMode.SPECTATOR) {
            lookingPlayers.clear();
            oldLookingPlayers.clear();
        } else {
            oldLookingPlayers = new HashMap<>(lookingPlayers);
            lookingPlayers.clear();
            if (!isErasing()) {
                for (Player nearPlayer : player.getWorld().getNearbyPlayers(player.getEyeLocation(), 50)) {
                    if (nearPlayer.getGameMode() == GameMode.SPECTATOR || nearPlayer.getGameMode() == GameMode.CREATIVE) continue;
                    if (context.game().isTeammate(player, nearPlayer)) continue;
                    if (!MathUtil.isInView(player, nearPlayer.getLocation(), 100, 80)) continue;
                    if (!canSee(player, nearPlayer)) continue;
                    if (nearPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;

                    LookingTime lookingTime;
                    if (oldLookingPlayers.containsKey(nearPlayer)) {
                        lookingTime = oldLookingPlayers.get(nearPlayer);
                    } else {
                        lookingTime = new LookingTime();
                        context.glowManager().setGlowColor(player, nearPlayer, NamedTextColor.DARK_PURPLE);
                        context.glowManager().addGlow(player, nearPlayer);
                    }

                    lookingTime.update(false);

                    putLookingPlayer(nearPlayer, lookingTime);
                    oldLookingPlayers.remove(nearPlayer);
                }
            }
            for (Player lookedPlayer : oldLookingPlayers.keySet()) {
                LookingTime lookingTime = oldLookingPlayers.get(lookedPlayer);
                lookingTime.update(true);

                int tick = lookingTime.getLookingTick();
                if (tick <= 0) {
                    context.glowManager().setGlowColor(player, lookedPlayer, NamedTextColor.WHITE);
                    context.glowManager().removeGlow(player, lookedPlayer);
                    PostEffectUtil.clearGrayEffect(player);
                } else {
                    putLookingPlayer(lookedPlayer, lookingTime);
                }
            }
        }
    }

    private void putLookingPlayer(Player target, LookingTime lookingTime) {
        Player player = getPlayer();
        if (player == null) return;

        lookingPlayers.put(target, lookingTime);
        PotionEffect slowness = new PotionEffect(PotionEffectType.SLOWNESS, 10, 0, true, true, true);
        target.addPotionEffect(slowness);

        float effectStrength;
        if (lookingTime.getLookingTick() > 20) {
            effectStrength = 1.0f;
        } else {
            effectStrength = lookingTime.getLookingTick() / 20.f;
        }
        PostEffectUtil.applyGrayEffect(target, effectStrength);


        double offset = 3;
        Random random = new Random();

        World world = target.getWorld();
        if (world.getGameTime() % 3 == 0) target.getWorld().spawnParticle(
                Particle.PORTAL,
                target.getEyeLocation().add(random.nextDouble(-offset, offset), random.nextDouble(-offset, offset), random.nextDouble(-offset, offset)),
                2,
                0.1, 0.1, 0.1,
                1f
        );

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
                true
        );

        boolean canSeeEye = blockHit == null;

        direction = target.getLocation().toVector().subtract(eye.toVector());
        distance = eye.distance(target.getLocation());


        blockHit = world.rayTraceBlocks(
                eye,
                direction,
                distance,
                FluidCollisionMode.NEVER,
                true
        );


        boolean canSeeFoot = blockHit == null;

        return canSeeEye || canSeeFoot;
    }

    private void petrificationPlayer(Player target, int tick) {
        Player player = getPlayer();
        if (player == null) return;

        Title title = Title.title(
                Component.empty(),
                Component.text(target.getName() + "を石化させた").color(NamedTextColor.GRAY),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);

        title = Title.title(
                Component.text("<●> <●>").color(NamedTextColor.DARK_RED),
                Component.text("身体が石化してしまった！").color(NamedTextColor.GRAY),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofMillis(50L * tick),
                        java.time.Duration.ofMillis(500)
                )
        );
        target.showTitle(title);
        target.playSound(target, Sound.ENTITY_VEX_DEATH, 1.0f, 1.0f);

        context.getGutitubowManagers().getProfileManager().getProfile(target).setSkin(SkinDataManager.STONE_SKIN(), ProfileChangeReason.MEDUSA);

        PotionEffect slowness = new PotionEffect(PotionEffectType.SLOWNESS, -1, 3, true, false, true);
        PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, -1, 0, true, false, true);
        target.addPotionEffect(slowness);
        target.addPotionEffect(weakness);
        Objects.requireNonNull(target.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0);
        Objects.requireNonNull(target.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0);
        target.setCooldown(Material.BOW, tick);
        target.setCooldown(Material.POTION, tick);
        target.setCooldown(Material.COMPASS, tick);
        petrificationPlayers.add(target);


        Bukkit.getScheduler().runTaskLater(context.gutitubow(), () -> {
            petrificationPlayers.remove(target);
            resetPetrification(target);
        }, tick);
    }

    private void resetPetrification(Player target) {
        if (PlayerUtil.isBedrock(target)) {
//            context.skinChanger().setRandomDefaultSkin(target);
        } else {
            context.getGutitubowManagers().getProfileManager().getProfile(target).resetSkin(ProfileChangeReason.MEDUSA);
        }
        target.removePotionEffect(PotionEffectType.SLOWNESS);
        target.removePotionEffect(PotionEffectType.WEAKNESS);
        Objects.requireNonNull(target.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
        Objects.requireNonNull(target.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0.42);
    }

    private boolean isLookingAtEachOther(Player player, Player target) {
        double view = 25;
        return MathUtil.isInView(player, target.getEyeLocation(), view, view) && MathUtil.isInView(target, player.getEyeLocation(), view * 2.5, view * 2.5);
    }

    private void resetGlow() {
        Player player = getPlayer();
        if (player == null) return;
        for (Player lookingPlayer : lookingPlayers.keySet()) {
            context.glowManager().setGlowColor(player, lookingPlayer, NamedTextColor.WHITE);
            context.glowManager().removeGlow(player, lookingPlayer);
            PostEffectUtil.clearGrayEffect(lookingPlayer);
        }
    }
}
