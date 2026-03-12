package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class TeleportAbility extends Ability {
    private Location savedLocation;
    private BukkitTask cooldownTask = null;

    public TeleportAbility(AbilityContext context) {
        super(context);
    }


    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.TELEPORT;
    }

    @Override
    public void init(Player player) {
        player.setCooldown(Material.COMPASS, 0);
    }

    @Override
    public void reset() {
        player.setCooldown(Material.COMPASS, 0);
    }


    @Override
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && item.getType() == Material.COMPASS) onRightClickCompass(player);
        }
    }

    private void onRightClickCompass(Player player) {
        if (player.getCooldown(Material.COMPASS) != 0) return;

        if (savedLocation == null) {
            savedLocation = player.getLocation();
            player.sendMessage(Component.text("設定しました！").color(NamedTextColor.RED));
            compassCooldownTask(player, 3 * 20);
        } else {
            if (savedLocation.getWorld().getWorldBorder().isInside(savedLocation)) {
                player.teleport(savedLocation);
                savedLocation = null;
                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 1.0f);
                compassCooldownTask(player, 10 * 20);

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
                    nearPlayer.addPotionEffect(effect);
                    nearPlayer.showTitle(title);
                }
            } else {
                savedLocation = null;
                player.sendMessage(Component.text("設定先がエリア外のためテレポートをキャンセルしました").color(NamedTextColor.RED));
                compassCooldownTask(player, 3 * 20);
            }
        }
    }

    private void compassCooldownTask(Player player, int cooldownTick) {
        player.setCooldown(Material.COMPASS, cooldownTick);
        if (cooldownTask != null && !cooldownTask.isCancelled()) return;

        cooldownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getCooldown(Material.COMPASS) == 0) {
                    this.cancel();
                    player.sendActionBar(Component.text(" - 能力使用可能 - ").color(NamedTextColor.GREEN));
                    cooldownTask = null;
                    return;
                }
                compassCooldownTaskUpdate(player);
            }
        }.runTaskTimer(context.plugin(), 0L, 1L);
    }

    private void compassCooldownTaskUpdate(Player player) {
        double cooldown = player.getCooldown(Material.COMPASS);
        double cooldownSec = Math.floor((cooldown / 20) * 10) / 10;
        Component message = Component.text(" - 能力クールタイム中 " + cooldownSec + "秒 - ").color(NamedTextColor.RED);
        player.sendActionBar(message);
    }
}
