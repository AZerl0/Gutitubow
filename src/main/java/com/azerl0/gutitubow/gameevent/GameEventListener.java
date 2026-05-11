package com.azerl0.gutitubow.gameevent;

import com.azerl0.gutitubow.Gutitubow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class GameEventListener implements Listener {

    private GameEvent getCurrentEvent() {
        return Gutitubow.get().getGame().getGameSetting().getGameEventController().getCurrentEvent();
    }


    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (getCurrentEvent() != null) {
            getCurrentEvent().onArrowHit(event);
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (getCurrentEvent() != null) {
            getCurrentEvent().onShoot(event);
        }
    }
}
