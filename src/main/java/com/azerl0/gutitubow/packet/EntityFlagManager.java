package com.azerl0.gutitubow.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityFlagManager {

    private final Map<UUID, Map<UUID, Byte>> clientSideFlags = new HashMap<>();

    public void addFlag(Player viewer, Entity target, byte flag) {
        Map<UUID, Byte> map = clientSideFlags.computeIfAbsent(
                viewer.getUniqueId(),
                k -> new HashMap<>()
        );

        UUID entityUUID = target.getUniqueId();
        byte current = map.getOrDefault(entityUUID, (byte) 0);
        byte newFlags = (byte) (current | flag);

        map.put(entityUUID, newFlags);
        sendFlagPacket(viewer, target, newFlags);
    }

    public void removeFlag(Player viewer, Entity target, byte flag) {
        Map<UUID, Byte> map = clientSideFlags.computeIfAbsent(
                viewer.getUniqueId(),
                k -> new HashMap<>()
        );

        UUID entityUUID = target.getUniqueId();
        byte current = map.getOrDefault(entityUUID, (byte) 0);
        byte newFlags = (byte) (current & ~flag);

        if (newFlags == 0) {
            map.remove(entityUUID);
        } else {
            map.put(entityUUID, newFlags);
        }
        sendFlagPacket(viewer, target, newFlags);
    }

    public boolean hasFlag(Player viewer, Entity target, byte flag) {
        Map<UUID, Byte> map = clientSideFlags.get(viewer.getUniqueId());
        if (map == null) return false;

        byte current = map.getOrDefault(target.getUniqueId(), (byte) 0);
        return (current & flag) != 0;
    }

    public byte getFlag(Player viewer, UUID targetUUID) {
        Map<UUID, Byte> map = clientSideFlags.get(viewer.getUniqueId());
        if (map == null) return (byte) 0;

        return map.getOrDefault(targetUUID, (byte) 0);
    }

    public Byte getFlag(Player viewer, Entity target) {
        Map<UUID, Byte> map = clientSideFlags.get(viewer.getUniqueId());
        if (map == null) return (byte) 0;

        return map.getOrDefault(target.getUniqueId(), (byte) 0);
    }

    public void removeViewer(Player viewer) {
        clientSideFlags.remove(viewer.getUniqueId());
    }

    public void removeEntity(Entity entity) {
        UUID entityUUID = entity.getUniqueId();
        for (Map<UUID, Byte> map : clientSideFlags.values()) {
            map.remove(entityUUID);
        }
    }

    public void resendFlags(Player viewer) {
        Map<UUID, Byte> map = clientSideFlags.get(viewer.getUniqueId());
        if (map == null) return;

        for (Map.Entry<UUID, Byte> entry : map.entrySet()) {
            UUID entityUUID = entry.getKey();
            byte flags = entry.getValue();

            Entity target = findEntityByUUID(viewer, entityUUID);
            if (target != null) {
                sendFlagPacket(viewer, target, flags);
            }
        }
    }

    public void resendFlag(Player viewer, UUID target) {
        Entity targetEntity = Bukkit.getEntity(target);
        if (targetEntity == null) return;
        sendFlagPacket(viewer, targetEntity, getFlag(viewer, target));
    }

    public void resendFlag(Player viewer, Entity target) {
        sendFlagPacket(viewer, target, getFlag(viewer, target));
    }


    @Nullable
    private Entity findEntityByUUID(Player viewer, UUID entityUUID) {
        return viewer.getWorld().getEntity(entityUUID);
    }

    @Nullable
    public Entity findEntityById(Player viewer, int entityId) {
        for (Entity entity : viewer.getWorld().getEntities()) {
            if (entity.getEntityId() == entityId) return entity;
        }
        return null;
    }

    public void sendFlagPacket(Player viewer, Entity target, byte flags) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                target.getEntityId(),
                List.of(new EntityData<>(0, EntityDataTypes.BYTE, flags))
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }
}

