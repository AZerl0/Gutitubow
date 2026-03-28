package com.azerl0.gutitubow.data.damage;

import com.azerl0.gutitubow.event.gameplayer.GamePlayerDamageEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageSource {
    private final GutitubowDamageType damageType;
    private final @Nullable Entity directEntity;

    public DamageSource(GamePlayerDamageEvent event) {
        this.damageType = event.getDamageType();
        this.directEntity = event.getAttacker();
    }

    public DamageSource(GutitubowDamageType damageType) {
        this.damageType = damageType;
        this.directEntity = null;
    }

    public DamageSource(GutitubowDamageType damageType, @Nullable Player directEntity) {
        this.damageType = damageType;
        this.directEntity = directEntity;
    }

    public @NotNull GutitubowDamageType getDamageType() {
        return damageType;
    }

    public @Nullable Entity getDirectEntity() {
        return directEntity;
    }
}
