package com.azerl0.gutitubow.data.damage;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageSource {
    private final GutitubowDamageType damageType;
    private final Player directPlayer;
    private final @Nullable Location damageLocation;
    private final double distance;

    public DamageSource(GutitubowDamageType damageType, Player directPlayer, @Nullable Location damageLocation, double distance) {
        this.damageType = damageType;
        this.directPlayer = directPlayer;
        this.damageLocation = damageLocation;
        this.distance = distance;
    }

    public static class DamageSourceBuilder {
        private GutitubowDamageType damageType;
        private Player directPlayer;
        private Location damageLocation;
        private double distance;

        DamageSourceBuilder() {}

        public DamageSourceBuilder damageType(final GutitubowDamageType damageType) {
            this.damageType = damageType;
            return this;
        }

        public DamageSourceBuilder directPlayer(final Player directPlayer) {
            this.directPlayer = directPlayer;
            return this;
        }

        public DamageSourceBuilder damageLocation(final Location damageLocation) {
            this.damageLocation = damageLocation;
            return this;
        }

        public DamageSourceBuilder distance(final double distance) {
            this.distance = distance;
            return this;
        }

        public DamageSource build() {
            return new DamageSource(damageType, directPlayer, damageLocation, distance);
        }

    }

    public static DamageSourceBuilder builder() {
        return new DamageSourceBuilder();
    }

    public @NotNull GutitubowDamageType getDamageType() {
        return damageType;
    }

    public @Nullable Location getDamageLocation() {
        return damageLocation;
    }

    public double getDistance() {
        return distance;
    }

    public @Nullable Player getDirectPlayer() {
        return directPlayer;
    }
}
