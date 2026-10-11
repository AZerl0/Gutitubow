package com.azerl0.gutitubow.gameplayer;

import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerCreatePinEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDeathEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerPreCreatePinEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerSneakingEvent;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.pin.Pin;
import com.azerl0.gutitubow.team.GameTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;

public class GamePlayerListener implements Listener {
    private final Game game;

    public GamePlayerListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        game.getGamePlayer(event.getPlayer());
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        GamePlayer gamePlayer = this.game.getGamePlayer(player);
        gamePlayer.removeArrow(1);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        if (event.getDamage() == 0.0) return;

        DamageType type = event.getDamageSource().getDamageType();
        if (type == DamageType.GENERIC) return;
        event.setCancelled(true);
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
    public void onDrop(PlayerDropItemEvent event) {
        GamePlayer gamePlayer = game.getGamePlayer(event.getPlayer());
        gamePlayer.onDropPotion(event);
    }

    @EventHandler
    public void onRide(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) return;
        if (game.isThisGamePlaying(player)) {
            ItemStack chestplate = player.getInventory().getChestplate();
            if (chestplate.getType() == Material.ELYTRA) {
                player.getInventory().setChestplate(ItemStack.of(Material.AIR));
            }
        }
    }


    @EventHandler
    public void onPreCreatePin(GamePlayerPreCreatePinEvent event) {
        if (game.getGutitubowManagers().getDebugManager().isDisableSoloModePin() && game.getGameSetting().getGameMode() == GutitubowGameMode.SOLO) {
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

            for (GamePlayer gamePlayer : team.getPlayersWithout(event.getPlayer().getUniqueId())) {
                Player teammate = gamePlayer.getPlayer();
                if (teammate == null) return;

                double distance = event.getPin().getLocation().distance(teammate.getLocation());
                teammate.sendMessage(Component.text("[!] ").append(event.getPlayer().displayName()).append(Component.text("が場所を指定した！ (" + Math.floor(distance * 10) / 10 + "m)")).color(NamedTextColor.GREEN));
                teammate.playSound(teammate, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                pin.addViewer(teammate);
            }
        }
    }

    @EventHandler
    public void onDeath(GamePlayerDeathEvent event) {
        event.getGamePlayer().onDeath();
    }
}
