package com.azerl0.gutitubow.profiler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ProfileListener implements Listener {
    private final ProfileManager profileManager;

    public ProfileListener(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Profile profile = profileManager.getProfile(event.getPlayer());
        profile.updateName();
        profile.updateSkin();
    }
}
