package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.team.GameTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class TeleportAbility extends Ability {
    private Location savedLocation;

    public TeleportAbility(AbilityContext context) {
        super(context);
    }


    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.TELEPORT;
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item != null && item.getType() == Material.COMPASS) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                onRightClickCompass();
            } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                onLeftClickCompass();
            }
        }
    }

    private void onRightClickCompass() {
        Player player = getPlayer();
        if (player == null) return;
        if (hasCompassCooldown()) return;
        if (isErasing()) {
            onErasureEffect();
            return;
        }

        if (savedLocation == null) {
            savedLocation = player.getLocation();
            player.sendMessage(Component.text("設定しました！").color(NamedTextColor.RED));
            setCompassCooldown(3 * 20);
            startCompassCooldownTask();
        } else {
            if (savedLocation.getWorld().getWorldBorder().isInside(savedLocation)) {
                teleport(savedLocation);
            } else {
                savedLocation = null;
                player.sendMessage(Component.text("設定先がエリア外のためテレポートをキャンセルしました").color(NamedTextColor.RED));
                setCompassCooldown(3 * 20);
                startCompassCooldownTask();
            }
        }
    }

    private void onLeftClickCompass() {
        Player player = getPlayer();
        if (player == null) return;
        if (isErasing()) {
            onErasureEffect();
            return;
        }

        GutitubowGameMode gameMode = context.game().getGameSetting().getGameMode();
        if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
            GameTeam team = context.game().getGameTeam(player);
            if (team == null) return;
            List<UUID> otherAlivePlayers = team.getAlivePlayersWithout(player.getUniqueId());
            if (!team.getPlayersWithout(player.getUniqueId()).isEmpty() && otherAlivePlayers.isEmpty()) {
                player.sendMessage(Component.text("チームメイトが全員ダウンしています！").color(NamedTextColor.RED));
                return;
            } else {
                for (UUID otherAlivePlayerUUID : otherAlivePlayers) {
                    Player otherAlivePlayer = Bukkit.getPlayer(otherAlivePlayerUUID);
                    if (otherAlivePlayer == null || !otherAlivePlayer.isOnline()) continue;
                    teleport(otherAlivePlayer.getLocation());
                    return;
                }
                // 空じゃないのにここまで通った場合は
                // チームメイトがオフラインになっている
                player.sendMessage(Component.text("チームメイトがオフラインです！").color(NamedTextColor.RED));
            }
        }
    }

    private void teleport(Location location) {
        Player player = getPlayer();
        Location oldPlayerLocation = player.getEyeLocation().clone();
        World world = player.getWorld();


        player.teleport(location);
        savedLocation = null;
        world.playSound(oldPlayerLocation, Sound.ENTITY_PLAYER_TELEPORT, 0.5f, 1.0f);
        world.spawnParticle(Particle.ENCHANT, oldPlayerLocation, 7, 0.2, 0.2, 0.2);
        world.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 1.0f);
        setCompassCooldown(10 * 20);
        startCompassCooldownTask();

        PotionEffect effect = new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 0, true, false, true);
        Title title = Title.title(
                Component.empty(),
                Component.text("付近の空間に歪みができた！").color(NamedTextColor.LIGHT_PURPLE),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(3),
                        java.time.Duration.ofMillis(500)
                )
        );
        for (Player nearPlayer : player.getWorld().getNearbyPlayers(player.getLocation(), 5)) {
            if (nearPlayer.getUniqueId() == player.getUniqueId()) continue;
            if (context.game().isTeammate(player, nearPlayer)) continue;
            nearPlayer.addPotionEffect(effect);
            nearPlayer.showTitle(title);
        }
    }
}
