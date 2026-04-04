package com.azerl0.gutitubow.carepackage;

import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GamePlayer;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.packet.EntityGlowManager;
import com.azerl0.gutitubow.team.GameTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CarePackage {
    private final Game game;
    private final EntityGlowManager glowManager;
    private final UUID uuid;
    private final UUID displayEntityUUID;
    private final Set<UUID> used_players = new HashSet<>();
    private final Set<GameTeam> used_teams = new HashSet<>();
    private final NamespacedKey isFallingKey;
    private final NamespacedKey fallingTickKey;
    private final NamespacedKey remainedPotionKey;

    public CarePackage(UUID uniqueId, UUID displayEntityUUID, Game game, EntityGlowManager glowManager, NamespacedKey isFallingKey, NamespacedKey fallingTickKey, NamespacedKey remainedPotionKey) {
        this.game = game;
        this.glowManager = glowManager;
        this.uuid = uniqueId;
        this.displayEntityUUID = displayEntityUUID;
        this.isFallingKey = isFallingKey;
        this.fallingTickKey = fallingTickKey;
        this.remainedPotionKey = remainedPotionKey;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    private int getFallingTick() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return 0;
        PersistentDataContainer pdc = coreEntity.getPersistentDataContainer();
        return pdc.getOrDefault(fallingTickKey, PersistentDataType.INTEGER, 0);
    }

    private int getRemainedPotion() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return 0;
        return coreEntity.getPersistentDataContainer().getOrDefault(remainedPotionKey, PersistentDataType.INTEGER, 0);
    }

    public void setRemainedPotion(int count) {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;

        coreEntity.getPersistentDataContainer().set(remainedPotionKey, PersistentDataType.INTEGER, count);
        this.setDisplayName(this.getRemainedPotionDisplay());
    }

    private void setDisplayName(Component displayName) {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;
        for (Entity entity : coreEntity.getPassengers()) {
            if (entity instanceof Interaction) {
                entity.customName(displayName);
            }
        }
    }

    private Component getRemainedPotionDisplay() {
        return Component.text("ケアパッケージ[" + this.getRemainedPotion() + "]").color(NamedTextColor.GREEN);
    }

    private void setFallingTick(int tick) {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;
        PersistentDataContainer pdc = coreEntity.getPersistentDataContainer();
        pdc.set(fallingTickKey, PersistentDataType.INTEGER, tick);
    }


    @Nullable
    public Entity getCoreEntity() {
        return Bukkit.getServer().getEntity(this.uuid);
    }

    public Entity getDisplayEntity() {
        return Bukkit.getServer().getEntity(this.displayEntityUUID);
    }

    public void onMove() {
        Entity entity = this.getCoreEntity();
        if (entity == null) return;
        boolean isFalling = entity.getPersistentDataContainer().getOrDefault(isFallingKey, PersistentDataType.BOOLEAN, true);
        if (isFalling) {
            if (entity.isOnGround()) {
                this.onLand();
                entity.getPersistentDataContainer().set(isFallingKey, PersistentDataType.BOOLEAN, false);
            } else {
                this.onFall();
            }
        } else {
            if (entity.getFallDistance() > 0.0) {
                entity.getPersistentDataContainer().set(isFallingKey, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    private void onFall() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;

        int fallingTick = this.getFallingTick();
        this.setFallingTick(fallingTick + 1);
        if (fallingTick % 60 == 0) {
            spawnFirework();
        }
    }

    private void onLand() {
        removeBalloonEntities();
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;
        spawnPoofParticle();
        spawnFirework();

    }

    private void spawnFirework() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;
        Firework firework = coreEntity.getWorld().spawn(coreEntity.getLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BALL_LARGE).build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);

    }

    private void spawnPoofParticle() {
        Entity coreEntity = this.getCoreEntity();
        if (coreEntity == null) return;

        Location center = coreEntity.getLocation();
        int speed = 1;

        for (int i = 0; i < 15; i++) {
            double x = Math.random() * 2 - 1;
            double y = Math.random();
            double z = Math.random() * 2 - 1;
            Vector dir = new Vector(x, y, z).normalize().multiply(0.5);

            center.getWorld().spawnParticle(
                    Particle.POOF,
                    coreEntity.getLocation().clone().add(0, 0.5, 0),
                    0,
                    dir.getX(), dir.getY(), dir.getZ(),
                    speed
            );
        }
    }

    public void remove() {
        Entity coreEntity = getCoreEntity();

        if (coreEntity == null) return;

        this.removeBalloonEntities();

        for (Entity passenger : coreEntity.getPassengers()) {
            passenger.remove();
        }
        coreEntity.remove();
    }

    public void onInteract(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        GutitubowGameMode gameMode = game.getGameSetting().getGameMode();
        if (gameMode == GutitubowGameMode.SOLO) {
            if (used_players.contains(player.getUniqueId())) return;
            givePotion(player);
            used_players.add(player.getUniqueId());

        } else if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
            GameTeam team = game.getTeamManager().getPlayerTeam(player);
            if (team == null || used_teams.contains(team)) return;
            for (UUID uuid : team.getPlayers()) {
                Player teamPlayer = Bukkit.getPlayer(uuid);
                if (teamPlayer == null || !teamPlayer.isOnline()) return;
                givePotion(teamPlayer);
            }
            used_teams.add(team);

            if (!team.getDeadPlayers().isEmpty()) {
                for (UUID uuid : team.getAlivePlayers()) {
                    GamePlayer gamePlayer = game.getGamePlayer(uuid);
                    if (gamePlayer == null) continue;
                    gamePlayer.giveRespawnBeacon();
                }
            }
        }

        this.setRemainedPotion(getRemainedPotion() - 1);
        if (this.getRemainedPotion() <= 0) {
            this.remove();
            return;
        }
        setDisplayName(this.getRemainedPotionDisplay());
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
        glowManager.removeGlow(player, getDisplayEntity());
    }


    public void removeBalloonEntities() {
        Entity coreEntity = getCoreEntity();
        if (coreEntity == null) return;

        for (Entity entity : coreEntity.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof LivingEntity living) {
                if (living.isLeashed()) {
                    if (living.getLeashHolder().equals(coreEntity)) {
                        living.remove();
                    }
                }
            }
        }
    }

}
