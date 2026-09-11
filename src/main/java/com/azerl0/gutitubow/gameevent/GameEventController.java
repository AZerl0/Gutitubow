package com.azerl0.gutitubow.gameevent;

import com.azerl0.gutitubow.gui.guis.GameEventGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class GameEventController {
    private final @NotNull GameEventGUI gameEventGUI = new GameEventGUI(this);
    private @Nullable GameEvent phase2Event;
    private @Nullable GameEvent phase3Event;
    private @Nullable GameEvent phase4Event;
    private @Nullable GameEvent testEvent;

    public @Nullable GameEvent getEvent(@NotNull GameEventControllerGameEventType controllerGameEventType) {
        return switch (controllerGameEventType) {
            case PHASE2 -> phase2Event;
            case PHASE3 -> phase3Event;
            case PHASE4 -> phase4Event;
            case TEST -> testEvent;
        };
    }

    public Collection<GameEvent> getEvents() {
        Collection<GameEvent> events = new HashSet<>();
        events.add(phase2Event);
        events.add(phase3Event);
        events.add(phase4Event);
        events.add(testEvent);
        return events;
    }

    public void setEvent(@NotNull GameEventControllerGameEventType controllerGameEventType, @NotNull GameEventType eventType) {
        switch (controllerGameEventType) {
            case PHASE2 -> phase2Event = eventType.create();
            case PHASE3 -> phase3Event = eventType.create();
            case PHASE4 -> phase4Event = eventType.create();
            case TEST -> testEvent = eventType.create();
        }
        gameEventGUI.updateGUI();
    }

    public void removeEvent(GameEventControllerGameEventType eventType, boolean deactivateCheck) {
        switch (eventType) {
            case PHASE2 -> {
                if (deactivateCheck && phase2Event != null && phase2Event.isActivated()) {
                    phase2Event.deactivate();
                }
                phase2Event = null;
            }
            case PHASE3 -> {
                if (deactivateCheck && phase3Event != null && phase3Event.isActivated()) {
                    phase3Event.deactivate();
                }
                phase3Event = null;
            }
            case PHASE4 -> {
                if (deactivateCheck && phase4Event != null && phase4Event.isActivated()) {
                    phase4Event.deactivate();
                }
                phase4Event = null;
            }
            case TEST -> {
                if (deactivateCheck && testEvent != null && testEvent.isActivated()) {
                    testEvent.deactivate();
                }
                testEvent = null;
            }
        }
        gameEventGUI.updateGUI();
    }

    public void removeEvent(GameEventControllerGameEventType controllerGameEventType) {
        removeEvent(controllerGameEventType, true);
    }

    public void activateEvent(GameEventControllerGameEventType controllerGameEventType) {
        GameEvent event = getEvent(controllerGameEventType);
        if (event != null) {
            event.activate(true);
        }
    }

    public void deactivateEvent(GameEventControllerGameEventType controllerGameEventType) {
        GameEvent event = getEvent(controllerGameEventType);
        if (event != null) {
            event.deactivate();
            removeEvent(controllerGameEventType, false);
        }
        GameEvent.playDeactivateEffect();
        gameEventGUI.updateGUI();
    }

    public @NotNull GameEventGUI getGameEventGUI() {
        return gameEventGUI;
    }

    public void openGameEventGUI(Player player) {
        gameEventGUI.open(player);
    }

}
