package com.azerl0.gutitubow.pdc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.nio.ByteBuffer;
import java.util.UUID;

@NullMarked
public class LocationDataType implements PersistentDataType<byte[], Location> {

    public static final LocationDataType INSTANCE = new LocationDataType();

    private LocationDataType() {}

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<Location> getComplexType() {
        return Location.class;
    }

    @Override
    public byte[] toPrimitive(
            Location complex,
            PersistentDataAdapterContext context
    ) {
        UUID worldUUID = complex.getWorld().getUID();

        ByteBuffer bb = ByteBuffer.allocate(
                Long.BYTES * 2 // World UUID
                        + Double.BYTES * 3 // x, y, z
                        + Float.BYTES * 2 // yaw, pitch
        );

        // World UUID
        bb.putLong(worldUUID.getMostSignificantBits());
        bb.putLong(worldUUID.getLeastSignificantBits());

        // Position
        bb.putDouble(complex.getX());
        bb.putDouble(complex.getY());
        bb.putDouble(complex.getZ());

        // Rotation
        bb.putFloat(complex.getYaw());
        bb.putFloat(complex.getPitch());

        return bb.array();
    }

    @Override
    public Location fromPrimitive(
            byte[] primitive,
            PersistentDataAdapterContext context
    ) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);

        // World UUID
        long worldMost = bb.getLong();
        long worldLeast = bb.getLong();

        UUID worldUUID = new UUID(worldMost, worldLeast);

        World world = Bukkit.getWorld(worldUUID);

        if (world == null) {
            throw new IllegalStateException(
                    "World not found: " + worldUUID
            );
        }

        // Position
        double x = bb.getDouble();
        double y = bb.getDouble();
        double z = bb.getDouble();

        // Rotation
        float yaw = bb.getFloat();
        float pitch = bb.getFloat();

        return new Location(
                world,
                x,
                y,
                z,
                yaw,
                pitch
        );
    }
}