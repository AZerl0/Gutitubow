package com.azerl0.gutitubow.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
}
