package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.entity.advanceprojectile.AdvanceProjectile;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerShootEvent;
import com.azerl0.gutitubow.util.ArrowUtil;
import com.azerl0.gutitubow.util.PlayerUtil;
import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EchoAbility extends Ability {
    private @Nullable Mannequin mannequin;
    private final List<Flame> flames = new ArrayList<>();
    private int echoTime = 3;
    private boolean timeLeaping = false;

    public EchoAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.ECHO;
    }

    @Override
    protected void onInit(Player player) {
        createFakePlayer();
    }

    @Override
    protected void onReset() {
        removeFakePlayer();
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        Player player = getPlayer();
        if (player == null) return;
        if (hasCompassCooldown()) return;
        if (isErasing()) {
            onErasureEffect();
            return;
        }
        timeLeap();
        setCompassCooldown(10 * 20);
        startCompassCooldownTask();
    }

    @Override
    public void onGamePlayerShoot(GamePlayerShootEvent event) {
        if (event.isCancelled()) return;
        if (event.getProjectile() == null) return;
        Flame latestFlame = flames.getLast();
        if (latestFlame == null) return;

        latestFlame.setShootData(Flame.ShootData.getShootData(event.getProjectile()));
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item != null && item.getType() == Material.COMPASS) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                onRightClickCompass();
            }
        }
    }

    private void createFakePlayer() {
        Player player = getPlayer();
        if (player == null) return;

        mannequin = player.getWorld().spawn(player.getLocation(), Mannequin.class);
        mannequin.setProfile(ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
        mannequin.setMainHand(player.getMainHand());
        mannequin.setSkinParts(player.getClientOption(ClientOption.SKIN_PARTS));
        mannequin.setInvulnerable(true);

        context.scoreboardManager().addEchoMannequin(mannequin);
    }

    private void removeFakePlayer() {
        if (mannequin == null) return;
        mannequin.remove();
        context.scoreboardManager().removeEchoPlayer(mannequin);
        mannequin = null;

    }

    private void onRightClickCompass() {
        Player player = getPlayer();
        if (player == null) return;

        echoTime += 1;
        if (echoTime > 5) echoTime = 1;
    }

    @Override
    protected void onTick() {
        Player player = getPlayer();
        if (player == null) return;

        if (!timeLeaping) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                removeFakePlayer();
            } else {
                if (mannequin == null) createFakePlayer();
                flames.add(Flame.getFlame(player));
                if (flames.size() > 5 * 20) flames.removeFirst();
                int index = 5 * 20 - echoTime * 20;
                if (index >= flames.size()) index = 0;
                if (!flames.isEmpty()) updateFakePlayer(mannequin, flames.get(index));
            }
        }
        if (!hasCompassCooldown()) player.sendActionBar(Component.text("反響: " + echoTime + "秒前"));
        if (mannequin != null) {
            PotionEffect invisible = new PotionEffect(PotionEffectType.INVISIBILITY, -1, 0, true, false, true);
            mannequin.addPotionEffect(invisible);
        }

    }

    private void updateFakePlayer(@NotNull Mannequin targetFakePlayer, @NotNull Flame flame) {
        targetFakePlayer.teleport(flame.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        targetFakePlayer.setPose(flame.getPose());
            targetFakePlayer.getEquipment().setArmorContents(flame.getArmorContents());
            targetFakePlayer.getEquipment().setItemInMainHand(flame.getMainHandItem());
            targetFakePlayer.getEquipment().setItemInOffHand(flame.getOffhandItem());
            targetFakePlayer.setPose(flame.getPose(), true);
            targetFakePlayer.setSneaking(flame.getPose() == Pose.SNEAKING);
        if (flame.isHandRaised()) {
            targetFakePlayer.startUsingItem(EquipmentSlot.HAND);
        } else targetFakePlayer.clearActiveItem();

        Flame.ShootData shootData = flame.getShootData();
        if (shootData != null && !isErasing()) {
            Vector velocity = shootData.getVelocity();
            Arrow arrow = shootData.getLocation().getWorld().spawn(shootData.getLocation(), Arrow.class);
            arrow.setShooter(getPlayer());
            arrow.setVelocity(velocity);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            ArrowUtil.setFixRotation(arrow, velocity);

            AdvanceProjectile advanceProjectile = getAdvanceProjectile(arrow);
            advanceProjectile.setParticle(location -> {
                final Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(255, 240, 23), 1.0f);
                location.getWorld().spawnParticle(
                        Particle.DUST,
                        location,
                        4,
                        0.1, 0.1, 0.1,
                        0,
                        dust
                );
            });

        }
    }

    private void timeLeap() {
        Player player = getPlayer();
        if (player == null) return;

        timeLeaping = true;
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.0f, 1.0f);

        Mannequin timeLeapMannequin = player.getWorld().spawn(player.getLocation(), Mannequin.class);
        timeLeapMannequin.setCollidable(false);
        timeLeapMannequin.setInvulnerable(true);

        context.scoreboardManager().addEchoMannequin(timeLeapMannequin);

        player.setGameMode(GameMode.SPECTATOR);
        Bukkit.getScheduler().runTaskLater(context.gutitubow(), () -> {
            player.setGameMode(GameMode.ADVENTURE);
            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(player);
        }, 1L);


        player.setCooldown(player.getInventory().getItemInMainHand(), 20);
        Title title = Title.title(
                Component.empty(),
                Component.text("TIME LEAP!!!"),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(3),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                step += echoTime == 1 ? 1 : 2;

                if (step > echoTime * 20) {
                    updateFakePlayer(timeLeapMannequin, flames.getFirst());
                    flames.clear();
                    timeLeaping = false;
                    player.setGameMode(GameMode.ADVENTURE);
                    context.scoreboardManager().removeEchoPlayer(timeLeapMannequin);

                    timeLeapMannequin.remove();
                    cancel();
                    return;
                }
                int index = flames.size() - step;
                if (index >= flames.size()) index = flames.size() - 1;
                if (index < 0) index = 0;
                updateFakePlayer(timeLeapMannequin, flames.get(index));
                player.setSpectatorTarget(timeLeapMannequin);
                if (PlayerUtil.isBedrock(player)) {
                    player.teleport(timeLeapMannequin);
                }

            }
        }.runTaskTimer(context.gutitubow(), 0L, 1L);
    }

    private static class Flame {
        private final Location location;
        private final Pose pose;
        private final ItemStack[] armorContents;
        private final ItemStack mainHandItem;
        private final ItemStack offhandItem;
        private final boolean isHandRaised;
        private @Nullable EchoAbility.Flame.ShootData shootData;

        private static Flame getFlame(Player player) {
            return new Flame(
                    player.getLocation().clone(),
                    player.getPose(),
                    player.getEquipment().getArmorContents().clone(),
                    player.getEquipment().getItemInMainHand().clone(),
                    player.getEquipment().getItemInOffHand().clone(),
                    player.isHandRaised(),
                    null
            );
        }

        private Flame(
                Location location,
                Pose pose,
                ItemStack[] armorContents,
                ItemStack mainHandItem,
                ItemStack offhandItem,
                boolean isHandRaised,
                @Nullable ShootData shootData
        ) {

            this.location = location;
            this.pose = pose;
            this.armorContents = armorContents;
            this.mainHandItem = mainHandItem;
            this.offhandItem = offhandItem;
            this.isHandRaised = isHandRaised;
            this.shootData = shootData;
        }

        private Location getLocation() {
            return location;
        }

        private Pose getPose() {
            return pose;
        }

        private ItemStack[] getArmorContents() {
            return armorContents;
        }

        private ItemStack getMainHandItem() {
            return mainHandItem;
        }

        private ItemStack getOffhandItem() {
            return offhandItem;
        }

        private boolean isHandRaised() {
            return isHandRaised;
        }

        private @Nullable ShootData getShootData() {
            return shootData;
        }

        private void setShootData(@Nullable ShootData shootData) {
            this.shootData = shootData;
        }

        private static class ShootData {
            private final Location location;
            private final Vector velocity;

            private ShootData(Location location, Vector velocity) {
                this.location = location;
                this.velocity = velocity;
            }

            private static ShootData getShootData(Entity entity) {
                return new ShootData(
                        entity.getLocation(),
                        entity.getVelocity()
                );
            }

            private Location getLocation() {
                return location;
            }

            private Vector getVelocity() {
                return velocity;
            }
        }
    }

}
