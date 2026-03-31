package com.azerl0.gutitubow.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerSkinChangerListener implements Listener {
    private final PlayerSkinChanger playerSkinChanger;

    public PlayerSkinChangerListener(PlayerSkinChanger playerSkinChanger) {
        this.playerSkinChanger = playerSkinChanger;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playerSkinChanger.cacheSkin(event.getPlayer());
    }
}
