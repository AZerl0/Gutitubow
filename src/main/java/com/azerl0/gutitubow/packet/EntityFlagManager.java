package com.azerl0.gutitubow.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityFlagManager {
    private final Map<UUID, Byte> clientSideFlags = new HashMap<>();

    public void addFlag(Player viewer, Entity target, byte flag) {
        byte current = clientSideFlags.getOrDefault(target.getUniqueId(), (byte) 0);
        byte newFlags = (byte)(current | flag);
        clientSideFlags.put(target.getUniqueId(), newFlags);
        sendFlagPacket(viewer, target, newFlags);
    }

    public void removeFlag(Player viewer, Entity target, byte flag) {
        byte current = clientSideFlags.getOrDefault(target.getUniqueId(), (byte) 0);
        byte newFlags = (byte)(current & ~flag);
        clientSideFlags.put(target.getUniqueId(), newFlags);
        sendFlagPacket(viewer, target, newFlags);
    }

    public boolean hasFlag(UUID uuid, byte flag) {
        byte current = clientSideFlags.getOrDefault(uuid, (byte) 0);
        return (current & 0x40) != 0;
    }

    public void remove(UUID uuid) {
        clientSideFlags.remove(uuid);
    }

    private void sendFlagPacket(Player viewer, Entity target, byte flags) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                target.getEntityId(),
                List.of(new EntityData<>(0, EntityDataTypes.BYTE, flags))
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }
}

