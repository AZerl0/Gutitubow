package com.azerl0.gutitubow.profiler;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Profile {
    final UUID playerUUD;
    private final Map<ProfileChangeReason, Component> names = new HashMap<>();
    private final Map<ProfileChangeReason, SkinData> skins = new HashMap<>();
    private final SkinDataManager skinDataManager;

    public Profile(Player player, SkinDataManager skinDataManager) {
        playerUUD = player.getUniqueId();
        this.skinDataManager = skinDataManager;
    }

    public void setSkin(@NotNull SkinData skinData, @NotNull ProfileChangeReason reason) {
        ProfileChangeReason topLevelReason = getTopLevelSkinChangeReason();
        skins.put(reason, skinData);
        if (topLevelReason == null || reason.getLevel() >= topLevelReason.getLevel()) updateSkin();
    }

    public void resetSkin(ProfileChangeReason reason) {
        ProfileChangeReason topLevelReason = getTopLevelSkinChangeReason();
        skins.remove(reason);
        if (topLevelReason == null || reason.getLevel() >= topLevelReason.getLevel()) updateSkin();
    }

    public void setName(Component displayName, ProfileChangeReason reason) {
        ProfileChangeReason topLevelReason = getTopLevelSkinChangeReason();
        names.put(reason, displayName);
        if (topLevelReason == null || reason.getLevel() >= topLevelReason.getLevel()) updateName();
    }

    public void resetName(ProfileChangeReason reason) {
        ProfileChangeReason topLevelReason = getTopLevelSkinChangeReason();
        skins.remove(reason);
        if (topLevelReason == null || reason.getLevel() >= topLevelReason.getLevel()) updateName();
    }

    protected void updateName() {
        Player player = Bukkit.getPlayer(playerUUD);
        if (player == null) return;

        Component name = getTopLevelName();
        if (name == null) name = Component.text(player.getName());

        player.displayName(name);
        player.playerListName(name);
        player.customName(name);
        PlayerProfile profile = player.getPlayerProfile();
        player.setPlayerProfile(profile);

    }

    private @Nullable Component getTopLevelName() {
        ProfileChangeReason reason = getTopLevelNameChangeReason();
        return names.getOrDefault(reason, null);
    }

    private @Nullable ProfileChangeReason getTopLevelNameChangeReason() {
        ProfileChangeReason result = null;
        for (ProfileChangeReason reason : names.keySet()) {
            if (result == null || reason.getLevel() > result.getLevel()) result = reason;
        }
        return result;
    }

    protected void updateSkin() {
        Player player = Bukkit.getPlayer(playerUUD);
        if (player == null) return;


        SkinData skinData = getTopLevelSkinData();
        if (skinData == null) skinData = skinDataManager.getPlayerSkinData(player);
        if (skinData == null) return;

        final PlayerProfile playerProfile = player.getPlayerProfile();
        playerProfile.setProperties(skinData.getTextureProperty());
        player.setPlayerProfile(playerProfile);
    }

    private @Nullable ProfileChangeReason getTopLevelSkinChangeReason() {
        ProfileChangeReason result = null;
        for (ProfileChangeReason reason : skins.keySet()) {
            if (result == null || reason.getLevel() > result.getLevel()) result = reason;
        }
        return result;
    }

    private @Nullable SkinData getTopLevelSkinData() {
        ProfileChangeReason reason = getTopLevelSkinChangeReason();
        return skins.getOrDefault(reason, null);
    }
}
