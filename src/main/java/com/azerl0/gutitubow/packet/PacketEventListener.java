package com.azerl0.gutitubow.packet;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;
import java.util.UUID;

import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.SPAWN_ENTITY;
import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.SPAWN_LIVING_ENTITY;
import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server.SPAWN_PLAYER;

@NullMarked
public class PacketEventListener implements PacketListener {
    private final JavaPlugin plugin;
    private final EntityFlagManager flagManager;


    public PacketEventListener(JavaPlugin plugin, EntityFlagManager flagManager) {
        this.plugin = plugin;
        this.flagManager = flagManager;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {

        if (event.getPacketType() != SPAWN_ENTITY &&
                event.getPacketType() != SPAWN_LIVING_ENTITY &&
                event.getPacketType() != SPAWN_PLAYER) {
            return;
        }

        Player viewer = event.getPlayer();

        int entityId = getEntityId(event);
        if (entityId == -1) return;

        Bukkit.getScheduler().runTask(plugin, () -> {

            Entity entity = findEntityById(viewer, entityId);
            if (entity == null) return;
            if (entity instanceof ArmorStand) return;

            flagManager.resendFlag(viewer, getEntityUUID(event));
        });
    }

    private int getEntityId(PacketSendEvent event) {
        switch (event.getPacketType()) {
            case SPAWN_PLAYER -> {
                return new WrapperPlayServerSpawnPlayer(event).getEntityId();
            }
            case SPAWN_LIVING_ENTITY -> {
                return new WrapperPlayServerSpawnLivingEntity(event).getEntityId();
            }
            case SPAWN_ENTITY -> {
                return new WrapperPlayServerSpawnEntity(event).getEntityId();
            }
            default -> {
                return -1;
            }
        }
    }

    @Nullable
    private UUID getEntityUUID(PacketSendEvent event) {
        switch (event.getPacketType()) {
            case SPAWN_PLAYER -> {
                return new WrapperPlayServerSpawnPlayer(event).getUUID();
            }
            case SPAWN_LIVING_ENTITY -> {
                return new WrapperPlayServerSpawnLivingEntity(event).getEntityUUID();
            }
            case SPAWN_ENTITY -> {
                Optional<UUID> entityUUID = new WrapperPlayServerSpawnEntity(event).getUUID();
                if (entityUUID.isPresent()) return entityUUID.get();
            }
            default -> {
                return null;
            }
        }
        return null;
    }


    @Nullable
    private Entity findEntityById(Player viewer, int entityId) {
        for (Entity entity : viewer.getWorld().getEntities()) {
            if (entity.getEntityId() == entityId) return entity;
        }
        return null;
    }
}
