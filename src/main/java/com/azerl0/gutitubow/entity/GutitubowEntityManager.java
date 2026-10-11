package com.azerl0.gutitubow.entity;

import com.azerl0.gutitubow.Gutitubow;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;

public class GutitubowEntityManager {

    public Map<UUID, GutitubowEntity> gutitubowEntities = new HashMap<>();
    public Map<UUID, UUID> gutitubowEntityCash = new HashMap<>();
    private BukkitTask updateTask;

    public GutitubowEntityManager() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(Gutitubow.get(), 0L, 1L);
    }

    private void tick() {
        for (GutitubowEntity gutitubowEntity : getGutitubowEntities()) {
            gutitubowEntity.onTick();
        }
    }

    public Optional<GutitubowEntity> getGutitubowEntity(Entity entity) {
        return getGutitubowEntityFromEntityUUID(entity.getUniqueId());
    }

    public @NotNull Optional<GutitubowEntity> getGutitubowEntityFromEntityUUID(UUID uuid) {
        UUID gutitubowEntityUUID = gutitubowEntityCash.get(uuid);
        if (gutitubowEntityUUID == null) return Optional.empty();

        GutitubowEntity gutitubowEntity = gutitubowEntities.get(gutitubowEntityUUID);
        return Optional.ofNullable(gutitubowEntity);
    }


    public void onEntityRemove(Entity entity) {
        gutitubowEntityCash.remove(entity.getUniqueId());
    }

    public  <T extends GutitubowEntity> T spawnGutitubowEntity(
            final @NotNull Location location,
            final @NotNull Class<T> clazz,
            final @Nullable Consumer<T> initializer
    ) {
        try {
            Constructor<T> constructor =
                    clazz.getConstructor(
                            GutitubowEntityManager.class,
                            Location.class
                    );

            T gutitubowEntity = constructor.newInstance(this, location);

            gutitubowEntity.summon(location);

            UUID gutitubowEntityUUID = gutitubowEntity.getUniqueId();

            gutitubowEntities.put(gutitubowEntityUUID, gutitubowEntity);
            for (UUID partEntityUUID : gutitubowEntity.getPartEntitiesUUID()) {
                gutitubowEntityCash.put(partEntityUUID, gutitubowEntityUUID);
            }

            if (initializer != null) {
                initializer.accept(gutitubowEntity);
            }


            return gutitubowEntity;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Failed to create GutitubowEntity: " + clazz.getName(),
                    e
            );
        }
    }

    public <T extends GutitubowEntity> T spawnGutitubowEntity(
            final @NotNull Location location,
            final @NotNull Class<T> clazz
    ) {
        return spawnGutitubowEntity(location, clazz, _ -> {});
    }

    public Collection<? extends GutitubowEntity> getGutitubowEntities() {
        return new ArrayList<>(gutitubowEntities.values());
    }

    public void onGutitubowEntityRemove(UUID gutitubowEntityUUID) {
        gutitubowEntities.remove(gutitubowEntityUUID);
    }

    public void removeAllGutitubowEntities() {
        for (GutitubowEntity gutitubowEntity : getGutitubowEntities()) {
            gutitubowEntity.remove();
        }
    }
}
