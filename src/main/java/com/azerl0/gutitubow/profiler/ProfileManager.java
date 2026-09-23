package com.azerl0.gutitubow.profiler;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileManager {
    private final Map<UUID, Profile> playerProfiles = new HashMap<>();
    private final SkinDataManager skinDataManager;

    public ProfileManager(SkinDataManager skinDataManager) {
        this.skinDataManager = skinDataManager;
    }

    public Profile getProfile(Player player) {
        if (playerProfiles.containsKey(player.getUniqueId())) {
            return playerProfiles.get(player.getUniqueId());
        } else {
            Profile profile = new Profile(player, skinDataManager);
            playerProfiles.put(player.getUniqueId(), profile);
            return profile;
        }
    }
}
