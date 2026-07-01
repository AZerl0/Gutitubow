package com.azerl0.gutitubow.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityFlagManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Map<UUID, Byte>> clientSideFlags = new HashMap<>();
    private final BukkitTask resendTask;

    public EntityFlagManager(JavaPlugin plugin) {
        this.plugin = plugin;


        resendTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player onliePlayer : Bukkit.getOnlinePlayers()) {
                    resendFlags(onliePlayer);
                }
            }
        }.runTaskTimer(plugin, 15L, 1L);
    }

    public void addFlag(Player viewer, Entity target, byte flag) {
        Map<UUID, Byte> map = clientSideFlags.computeIfAbsent(
                viewer.getUniqueId(),
                k -> new HashMap<>()
        );

        UUID entityUUID = target.getUniqueId();
        byte current = map.getOrDefault(entityUUID, getCurrentFlag(target));
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
        byte current = map.getOrDefault(entityUUID, getCurrentFlag(target));
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

        byte current = map.getOrDefault(target.getUniqueId(), getCurrentFlag(target));
        return (current & flag) != 0;
    }

    public byte getFlag(Player viewer, UUID targetUUID) {
        Map<UUID, Byte> map = clientSideFlags.get(viewer.getUniqueId());
        byte flag = 0;

        Entity entity = Bukkit.getEntity(targetUUID);
        if (entity != null) {
            flag = getCurrentFlag(entity);
        }
        if (map != null && map.containsKey(targetUUID)) {
            flag = map.get(targetUUID);
        }

        return flag;
    }

    public @NotNull Byte getFlag(Player viewer, Entity target) {
        Map<UUID, Byte> map = clientSideFlags.get(viewer.getUniqueId());
        if (map == null) return (byte) 0;

        return map.getOrDefault(target.getUniqueId(), getCurrentFlag(target));
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

    public byte getCurrentFlag(Entity entity) {
        byte flags = (byte) 0;
        if (entity.isSneaking()) flags = (byte) (flags | 0x02);

        if (entity instanceof Player player && player.isSprinting() ) flags = (byte) (flags | 0x08);

        if (entity instanceof LivingEntity livingEntity && livingEntity.isSwimming()) flags = (byte) (flags | 0x10);

        if (entity instanceof LivingEntity livingEntity && livingEntity.hasPotionEffect(PotionEffectType.INVISIBILITY)) flags = (byte) (flags | 0x32);

        if (entity.isGlowing()) flags = (byte) (flags | 0x40);

        if (entity instanceof LivingEntity livingEntity && livingEntity.isGliding()) flags = (byte) (flags | 0x128);

        return flags;
    }
}

