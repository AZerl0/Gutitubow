package com.azerl0.gutitubow.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.plugin.Plugin;

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
}
