package com.azerl0.gutitubow.gameplayer;

import com.azerl0.gutitubow.data.damage.DamageSource;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerCreatePinEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerPreCreatePinEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerSneakingEvent;
import com.azerl0.gutitubow.game.Game;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.pin.Pin;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.util.EntityUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class GamePlayerListener implements Listener {
    private final Game game;

    public GamePlayerListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        GamePlayer gamePlayer = game.getGamePlayer(event.getPlayer());
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        GamePlayer gamePlayer = this.game.getGamePlayer(player);
        gamePlayer.removeArrow(1);
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;

        Entity shooter = (Entity) event.getEntity().getShooter();
        Entity tar = event.getHitEntity();

        if (!(event.getEntity() instanceof AbstractArrow)) return;
        if (event.getEntity() instanceof SpectralArrow) return;
        if (shooter == null || tar == null) return;

        if (shooter.getUniqueId().equals(tar.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (EntityUtil.isInvalidHitEntity(tar)) return;

        if (!(tar instanceof Player tarPlayer)) {
            if (tar instanceof HappyGhast) {
                event.getEntity().remove();
                ((HappyGhast) tar).damage(2);
                event.setCancelled(true);
                return;
            }
            if (!(tar instanceof ArmorStand armorStand && armorStand.isInvulnerable())) event.setCancelled(true);
            return;
        } else if (CitizensAPI.getNPCRegistry().isNPC(tarPlayer)) {
            event.setCancelled(true);
            return;
        }
        if (!(shooter instanceof Player shooterPlayer)) return;
        if (game.getTeamManager().isTeammate(shooterPlayer, tarPlayer)) {
            event.setCancelled(true);
            return;
        }

        double distance = shooter.getLocation().distance(tar.getLocation());

        DamageSource damageSource = DamageSource.builder()
                .damageType(GutitubowDamageType.ARROW_SHOT)
                .directPlayer(shooterPlayer)
                .damageLocation(event.getEntity().getLocation())
                .distance(distance)
                .build();
        this.game.getGamePlayer(tarPlayer).damage(tarPlayer, 2, damageSource, true);

        event.setCancelled(true);
        event.getEntity().remove();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getDamage() == 0.0) return;

        DamageType type = event.getDamageSource().getDamageType();
        if (type == DamageType.OUTSIDE_BORDER) {
            this.game.getGamePlayer(player).borderOutside();
            event.setCancelled(true);
            return;
        }
        if (type == DamageType.GENERIC) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        GamePlayer gamePlayer = game.getGamePlayer(event.getPlayer());
        gamePlayer.onToggleSneak(event);
    }

    @EventHandler
    public void onSneaking(GamePlayerSneakingEvent event) {
        GamePlayer gamePlayer = game.getGamePlayer(event.getPlayer());
        gamePlayer.onSneaking(event);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        event.setCancelled(true);
        player.setFoodLevel(20);
        player.setSaturation(20f);
    }

    @EventHandler
    public void onChangePotionEffect(EntityPotionEffectEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        GamePlayer gamePlayer = game.getGamePlayer(player);

        if (event.getModifiedType() == PotionEffectType.BLINDNESS) gamePlayer.setFlashParticleTask();
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        GamePlayer gamePlayer = game.getGamePlayer(event.getPlayer());
        gamePlayer.onDropPotion(event);
    }

    @EventHandler
    public void onRide(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) return;
        if (game.isGamePlayer(player)) {
            ItemStack chestplate = player.getInventory().getChestplate();
            if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
                player.getInventory().setChestplate(ItemStack.of(Material.AIR));
            }
        }
    }


    @EventHandler
    public void onPreCreatePin(GamePlayerPreCreatePinEvent event) {
        if (game.getDebugManager().isDisableSoloModePin() && game.getGameSetting().getGameMode() == GutitubowGameMode.SOLO) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatePin(GamePlayerCreatePinEvent event) {
        GutitubowGameMode gameMode = game.getGameSetting().getGameMode();
        if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
            GameTeam team = game.getGameTeam(event.getPlayer());
            if (team == null) return;
            Pin pin = event.getPin();

            for (UUID uuid : team.getPlayersWithout(event.getPlayer().getUniqueId())) {
                Player teammate = Bukkit.getPlayer(uuid);
                if (teammate == null) return;

                double distance = event.getPin().getLocation().distance(teammate.getLocation());
                teammate.sendMessage(Component.text("[!] ").append(event.getPlayer().displayName()).append(Component.text("が場所を指定した！ (" + Math.floor(distance * 10) / 10 + "m)")).color(NamedTextColor.GREEN));
                teammate.playSound(teammate, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                pin.addViewer(teammate);
            }
        }
    }
}
