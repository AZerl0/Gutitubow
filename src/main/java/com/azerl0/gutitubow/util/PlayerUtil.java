package com.azerl0.gutitubow.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;


public class PlayerUtil {

    public static void playTotemAnimation(Player viewer, Player target) {
        WrapperPlayServerEntityStatus packet =
                new WrapperPlayServerEntityStatus(
                        target.getEntityId(),
                        (byte) 35
                );

        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }

    public static void playTotemAnimation(Player target) {
        WrapperPlayServerEntityStatus packet =
                new WrapperPlayServerEntityStatus(
                        target.getEntityId(),
                        (byte) 35
                );

        for (Player p : Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);
        }
    }

    public static boolean isBedrock(Player player) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("floodgate");
        if (plugin == null) return false;

        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            return (boolean) apiClass
                    .getMethod("isFloodgatePlayer", UUID.class)
                    .invoke(api, player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidJavaName(String name) {
        return name.matches("^[a-zA-Z0-9_]{3,16}$");
    }



    public static Component getHealthComponent(@NotNull Player player) {
        Component health = Component.empty();

        int count = (int) Math.ceil(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue() / 2);
        for (int i = 0; i < count; i++) {
            health = health.append(Component.text((i * 2 < player.getHealth()) ? "♥" : "♡").color(NamedTextColor.RED));
        }
        if (player.getAbsorptionAmount() > 0) {
            count = (int) Math.ceil(Objects.requireNonNull(player.getAttribute(Attribute.MAX_ABSORPTION)).getValue() / 2);
            for (int i = 0; i < count; i++) {
                health = health.append(Component.text((i * 2 < player.getAbsorptionAmount()) ? "♥" : "♡").color(NamedTextColor.YELLOW));
            }
        }
        return health;
    }
}
