package com.azerl0.gutitubow.entity.carepackage;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.entity.GutitubowEntity;
import com.azerl0.gutitubow.entity.GutitubowEntityManager;
import com.azerl0.gutitubow.entity.GutitubowEntityType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.packet.EntityGlowManager;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.util.EntityUtil;
import com.azerl0.gutitubow.util.MathUtil;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;

public class CarePackage extends GutitubowEntity {
    private Chicken core;
    private Interaction interaction;
    private BlockDisplay display;
    private boolean isFake;
    private final Set<UUID> used_players = new HashSet<>();
    private final Set<GameTeam> used_teams = new HashSet<>();
    private final Set<Chicken> chickens = new HashSet<>();
    private int remainedPotionCount;
    private int fallingTick;
    private boolean isFalling;
    private boolean isLanded = false;

    public CarePackage(GutitubowEntityManager gutitubowEntityManager, Location location) {
        super(gutitubowEntityManager, location);
    }

    @Override
    public @NotNull GutitubowEntityType getType() {
        return GutitubowEntityType.CAREPACKAGE;
    }

    @Override
    public @NotNull Location getLocation() {
        return core.getLocation();
    }

    @Override
    protected void summon(Location location) {
        location.setRotation(0.0f, 0.0f);
        World world = location.getWorld();

        display = world.spawn(location, BlockDisplay.class, (BlockDisplay display) -> {
            display.setBlock(Material.CHEST.createBlockData());
            display.setGlowing(true);
            EntityUtil.setGlow(display, NamedTextColor.YELLOW);
            Vector3f translation = new Vector3f(-0.5f, -0.1f, -0.5f);
            Transformation transformation = display.getTransformation();
            Transformation newTransformation = new Transformation(
                    translation,
                    transformation.getLeftRotation(),
                    transformation.getScale(),
                    transformation.getRightRotation()
            );
            display.setTransformation(newTransformation);
        });

        interaction = world.spawn(location, Interaction.class, (Interaction i) -> {
            i.setInteractionWidth(0.8f);
            i.setInteractionHeight(0.8f);
            i.setCustomNameVisible(true);
            i.customName(Component.text("ケアパッケージ"));

        });

        core = world.spawn(location, Chicken.class);
        core.setInvisible(true);
        core.setInvulnerable(true);
        core.setCollidable(false);
        core.getAttribute(Attribute.SCALE).setBaseValue(0);
        core.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0);
        core.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
        core.setSilent(true);
        core.addPassenger(display);
        core.addPassenger(interaction);

        for (int i = 0; i < 10; i++) {
            double randomRadius = 0.8;
            Random random = new Random();
            double randomX = random.nextDouble(-randomRadius, randomRadius);
            double randomY = random.nextDouble(-randomRadius, randomRadius);
            Chicken chicken = world.spawn(location.clone().add(randomX, 4.5, randomY), Chicken.class, (Chicken c) -> {
                Objects.requireNonNull(c.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0);
                c.setLeashHolder(core);
                c.setVariant(Chicken.Variant.TEMPERATE);
                c.setInvulnerable(true);
            });
            chickens.add(chicken);
        }
        isFake = false;
        remainedPotionCount = 3;
        setDisplayName(this.getRemainedPotionDisplay());
        Bukkit.getScheduler().runTaskLater(Gutitubow.get(), this::onMove, 3L);
    }

    @Override
    public void teleport(Location location) {
        Location oldLocation = getLocation().clone();
        core.teleport(location);

        display.setRotation(0.0f, 0.0f);

        for (Chicken chicken : chickens) {
            Vector offset = chicken.getLocation().toVector().subtract(oldLocation.toVector());
            chicken.teleport(location.clone().add(offset));
            chicken.setLeashHolder(core);
        }
    }

    @Override
    public Set<UUID> getPartEntitiesUUID() {
        Set<UUID> uuidSet = new HashSet<>();
        uuidSet.add(core.getUniqueId());
        uuidSet.add(core.getUniqueId());
        uuidSet.add(interaction.getUniqueId());
        uuidSet.add(display.getUniqueId());
        for (Chicken chicken : chickens) {
            uuidSet.add(chicken.getUniqueId());
        }
        return uuidSet;
    }

    @Override
    protected void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getUniqueId().equals(interaction.getUniqueId())) {
            onInteract(event.getPlayer());
        }
    }

    @Override
    protected void onEntityMove(EntityMoveEvent event) {
        if (event.getEntity().getUniqueId().equals(core.getUniqueId())) onMove();
    }

    @Override
    protected void onEntityDrop(EntityDropItemEvent event) {
        if (event.getEntity() instanceof Chicken &&
                event.getItemDrop().getItemStack().getType() == Material.EGG) {
            event.setCancelled(true);
        }
    }

    private void setDisplayName(Component displayName) {
        interaction.customName(displayName);
    }

    private Component getRemainedPotionDisplay() {
        return Component.text("ケアパッケージ[" + remainedPotionCount + "]").color(NamedTextColor.GREEN);
    }

    public boolean isFake() {
        return isFake;
    }

    public void setFake(boolean isFake) {
        this.isFake = isFake;
    }

    protected void onMove() {
        location = core.getLocation();
        if (isFalling) {
            if (core.isOnGround()) {
                this.onLand();
                isFalling = false;
            } else {
                this.onFall();
            }
        } else {
            if (core.getFallDistance() > 0.0) {
                isFalling = true;
            }
        }
    }

    private void onFall() {
        fallingTick++;
        if (!isLanded && fallingTick % 60 == 0) {
            spawnFirework();
        }
    }

    private void onLand() {
        removeBalloonEntities();
        spawnPoofParticle();
        if (!isLanded) spawnFirework();
        isLanded = true;

    }

    private void spawnFirework() {
        Firework firework = core.getWorld().spawn(core.getLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BALL_LARGE).build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);

    }

    private void spawnPoofParticle() {

        Location center = getLocation();
        int speed = 1;

        for (int i = 0; i < 15; i++) {
            double x = Math.random() * 2 - 1;
            double y = Math.random();
            double z = Math.random() * 2 - 1;
            Vector dir = new Vector(x, y, z).normalize().multiply(0.5);

            center.getWorld().spawnParticle(
                    Particle.POOF,
                    getLocation().clone().add(0, 0.5, 0),
                    0,
                    dir.getX(), dir.getY(), dir.getZ(),
                    speed
            );
        }
    }

    public void onRemove() {
        removeBalloonEntities();
        interaction.remove();
        display.remove();
        core.remove();
    }

    public void onInteract(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (isFake) {
            for (Player nearPlayer : location.getNearbyPlayers(3)) {
                if (EntityUtil.isInvalidHitEntity(nearPlayer)) continue;

                GamePlayer damageGamePlayer = Gutitubow.get().getGame().getGamePlayer(nearPlayer);
                DamageSource damageSource = DamageSource.builder(GutitubowDamageType.FAKE_CAREPACKAGE)
                        .damageLocation(MathUtil.rayTraceEntityHit(location, nearPlayer))
                        .distance(location.distance(nearPlayer.getLocation()))
                        .build();
                damageGamePlayer.damage(damageSource, 1);

                Vector knockback = nearPlayer.getLocation().toVector()
                        .subtract(location.clone().add(0.0, -0.2, 0.0).toVector())
                        .normalize()
                        .multiply(1.5);

                nearPlayer.setVelocity(knockback);

            }
            World world = location.getWorld();
            world.spawnParticle(
                    Particle.EXPLOSION,
                    location,
                    1,
                    0, 0, 0,
                    0,
                    null,
                    true
            );
            world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
            remove();
        } else {
            Game game = Gutitubow.get().getGame();
            GutitubowGameMode gameMode = game.getGameSetting().getGameMode();
            if (gameMode == GutitubowGameMode.SOLO) {
                if (used_players.contains(player.getUniqueId())) return;
                givePotion(player);
                used_players.add(player.getUniqueId());

            } else if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                GameTeam team = game.getTeamManager().getPlayerTeam(player);
                if (team == null || used_teams.contains(team)) return;
                for (GamePlayer teamGamePlayer : team.getPlayers()) {
                    Player teamPlayer = teamGamePlayer.getPlayer();
                    if (teamPlayer == null || !teamPlayer.isOnline()) return;
                    givePotion(teamPlayer);
                }
                used_teams.add(team);

                if (!team.getDeadPlayers().isEmpty()) {
                    for (GamePlayer aliveGamePlayer : team.getAlivePlayers()) {
                        aliveGamePlayer.giveRespawnBeacon();
                    }
                }
            }

            remainedPotionCount--;
            if (remainedPotionCount <= 0) {
                remove();
                return;
            }
            setDisplayName(this.getRemainedPotionDisplay());
        }
    }

    private void givePotion(Player player) {
        player.give(GutitubowItem.RECOVERY_POTION);
        player.sendMessage(
                Component.text("\n[!] ").color(NamedTextColor.RED)
                        .append(Component.text("ケアパッケージから[").color(NamedTextColor.WHITE)
                                .append(Component.text("回復ポーション").color(NamedTextColor.LIGHT_PURPLE))
                                .append(Component.text("]を入手した!\n").color(NamedTextColor.WHITE))
                        ));
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        EntityGlowManager glowManager = Gutitubow.get().getGutitubowManagers().getGlowManager();
        glowManager.removeGlow(player, display);
    }


    public void removeBalloonEntities() {
        for (Chicken chicken : chickens) {
            chicken.remove();
        }
    }

    public boolean isFalling() {
        return isFalling;
    }
}
