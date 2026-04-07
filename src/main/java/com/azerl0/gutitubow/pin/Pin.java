package com.azerl0.gutitubow.pin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Pin {
    private final UUID ownerUUID;
    private final Component ownerName;
    private final int entityId = SpigotReflectionUtil.generateEntityId();
    private final int subEntityId = SpigotReflectionUtil.generateEntityId();
    private final UUID uuid = UUID.randomUUID();
    private final UUID subUuid = UUID.randomUUID();
    private final Set<UUID> viewers = new HashSet<>();
    private final org.bukkit.Location location;
    private final BukkitTask updateTask;
    private final PinManager pinManager;
    private int despawnCount = 10 * 20;

    public Pin(JavaPlugin plugin, PinManager pinManager, org.bukkit.Location location, UUID ownerUUID, Component ownerName) {
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.location = location;
        this.pinManager = pinManager;

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updatePin();
            }
        }.runTaskTimer(plugin, 0L, 1L);

    }

    public @NotNull Set<UUID> getViewers() {
        return viewers;
    }

    public @NotNull Location getLocation() {
        return location;
    }

    public void addViewer(@NotNull Player player) {
        viewers.add(player.getUniqueId());
        spawn(player);
    }

    public void removeViewer(@NotNull Player player) {
        viewers.remove(player.getUniqueId());
        sendRemovePacket(player);
    }

    public void updatePin() {
        despawnCount--;
        if (despawnCount <= 0) {
            pinManager.removePin(ownerUUID);
            return;
        }
        for (UUID uuid : viewers) {
            Player viewer = Bukkit.getPlayer(uuid);
            if (viewer == null) continue;
            updatePin(viewer);
        }
    }

    public void updatePin(@NotNull Player viewer) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);

        double distance = viewer.getLocation().distance(location);
        float scale = Math.max((float) (distance / 5), 3.0f);
        scale = Math.min(scale, 17.0f);

        List<EntityData<?>> defaultData = List.of(
                new EntityData<>(10, EntityDataTypes.INT, 1), // teleport_duration
                new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(scale, scale, scale)), // Scale
                new EntityData<>(15, EntityDataTypes.BYTE, (byte) 3), // Billboard Constraints: CENTER = 3
                new EntityData<>(16, EntityDataTypes.INT, (15 << 4) | (15 << 20)), // Brightness
                new EntityData<>(17, EntityDataTypes.FLOAT, 10f), // View range
                new EntityData<>(25, EntityDataTypes.INT, 0), // Background color
                new EntityData<>(27, EntityDataTypes.BYTE, (byte) 2) // Is see through
        );

        user.sendPacket(new WrapperPlayServerEntityMetadata(
                this.entityId,
                defaultData
        ));
        user.sendPacket(new WrapperPlayServerEntityMetadata(
                this.subEntityId,
                defaultData
        ));


        Component component = Component.text("距離: " + Math.floor(distance * 10) / 10 + "m\n").color(NamedTextColor.WHITE)
                .append(Component.text("(").append(ownerName).append(Component.text(")")).color(NamedTextColor.WHITE));

        WrapperPlayServerEntityMetadata mainEntityPacket = new WrapperPlayServerEntityMetadata(
                this.entityId,
                List.of(
                        new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, component) // Text
                )
        );
        user.sendPacket(mainEntityPacket);
        Vector worldUp = new Vector(0, 1, 0);

        Vector dir = viewer.getLocation().getDirection();
        Vector rightVec = dir.getCrossProduct(worldUp).normalize();
        Vector upVec    = rightVec.getCrossProduct(dir).normalize();

        Location mainEntityLocation = this.location.clone().add(
                viewer.getLocation().getDirection().multiply(-1).add(upVec.multiply(scale / 1.5))
        );
        WrapperPlayServerEntityTeleport mainEntityTeleportPacket = new WrapperPlayServerEntityTeleport(entityId, SpigotConversionUtil.fromBukkitLocation(mainEntityLocation), false);
        user.sendPacket(mainEntityTeleportPacket);



        float subEntityScale = scale * 3;
        Component subComponent = Component.text("❖").color(NamedTextColor.RED);
        WrapperPlayServerEntityMetadata subEntityPacket = new WrapperPlayServerEntityMetadata(
                this.subEntityId,
                List.of(
                        new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(subEntityScale, subEntityScale, subEntityScale)), // Scale
                        new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, subComponent) // Text
                )
        );
        user.sendPacket(subEntityPacket);

    }

    public void spawn(Player viewer) {
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                this.entityId,
                this.uuid,
                EntityTypes.TEXT_DISPLAY,
                SpigotConversionUtil.fromBukkitLocation(this.location),
                SpigotConversionUtil.fromBukkitLocation(this.location).getYaw(), // Head yaw
                0, // No additional data
                null // We won't specify any initial velocity
        );

        WrapperPlayServerSpawnEntity subEntityPacket = new WrapperPlayServerSpawnEntity(
                this.subEntityId,
                this.subUuid,
                EntityTypes.TEXT_DISPLAY,
                SpigotConversionUtil.fromBukkitLocation(this.location),
                SpigotConversionUtil.fromBukkitLocation(this.location).getYaw(),
                0,
                null
        );


        User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
        user.sendPacket(packet);
        user.sendPacket(subEntityPacket);
    }

    private void sendRemovePacket(@NotNull Player player) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        user.sendPacket(new WrapperPlayServerDestroyEntities(this.entityId));
        user.sendPacket(new WrapperPlayServerDestroyEntities(this.subEntityId));
    }

    protected void remove() {
        for (UUID uuid : viewers) {
            Player viewer = Bukkit.getPlayer(uuid);
            if (viewer == null) continue;
            sendRemovePacket(viewer);
        }
        updateTask.cancel();
    }
}
