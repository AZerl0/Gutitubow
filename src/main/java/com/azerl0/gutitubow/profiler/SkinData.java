package com.azerl0.gutitubow.profiler;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SkinData {
    public final Collection<ProfileProperty> textureProperty;

    public SkinData(@NotNull Collection<ProfileProperty> textureProperty) {
        this.textureProperty = textureProperty;
    }

    public @NotNull Collection<ProfileProperty> getTextureProperty() {
        return textureProperty;
    }
}
