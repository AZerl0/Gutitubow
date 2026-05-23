package com.azerl0.gutitubow.gameevent;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDeathEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerKillEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Collection;
import java.util.function.Consumer;

public class GameEventListener implements Listener {

    private Collection<GameEvent> getEvents() {
        return Gutitubow.get().getGame().getGameSetting().getGameEventController().getEvents();
    }

    private void eventAction(Consumer<GameEvent> action) {
        getEvents().forEach( gameEvent -> {
            if (gameEvent != null && gameEvent.isActivated()) {
                action.accept(gameEvent);
            }
        });
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        eventAction(gameEvent -> gameEvent.onArrowHit(event));
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        eventAction(gameEvent -> gameEvent.onShoot(event));
    }

    @EventHandler
    public void onAnyPlayerKill(GamePlayerKillEvent event) {
        eventAction(gameEvent -> gameEvent.onAnyPlayerKill(event));
    }

    @EventHandler
    public void onAnyPlayerDeath(GamePlayerDeathEvent event) {
        eventAction(gameEvent -> gameEvent.onAnyPlayerDeath(event));
    }
}
