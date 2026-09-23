package com.azerl0.gutitubow.profiler;

import com.destroystokyo.paper.profile.ProfileProperty;

import java.util.Collection;

public class SkinData {
    public final Collection<ProfileProperty> textureProperty;

    public SkinData(Collection<ProfileProperty> textureProperty) {
        this.textureProperty = textureProperty;
    }

    public Collection<ProfileProperty> getTextureProperty() {
        return textureProperty;
    }
}
