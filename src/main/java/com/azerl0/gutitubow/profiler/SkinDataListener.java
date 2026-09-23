package com.azerl0.gutitubow.profiler;

import com.azerl0.gutitubow.event.player.PlayerFirstJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkinDataListener implements Listener {
    private final SkinDataManager skinDataManager;

    public SkinDataListener(SkinDataManager skinDataManager) {
        this.skinDataManager = skinDataManager;
    }

    @EventHandler
    public void onFirstJoin(PlayerFirstJoinEvent event) {
        skinDataManager.cashPlayerSkinData(event.getPlayer());
    }
}
