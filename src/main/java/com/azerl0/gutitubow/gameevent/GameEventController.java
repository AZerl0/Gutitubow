package com.azerl0.gutitubow.gameevent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class GameEventController {
    private @Nullable GameEvent currentEvent;
    private @Nullable GameEvent nextEvent;

    public @Nullable GameEvent getCurrentEvent() {
        return currentEvent;
    }

    public boolean hasCurrentEvent() {
        return currentEvent != null;
    }

    public @Nullable GameEvent getNextEvent() {
        return nextEvent;
    }

    public boolean hasNextEvent() {
        return getNextEvent() != null;
    }

    public void setNextEvent(GameEventType nextEvent) {
        this.nextEvent = nextEvent.create();
    }

    public void activateNextEvent() {
        deactivateCurrentEvent();
        currentEvent = null;
        if (getNextEvent() != null) {
            currentEvent = getNextEvent();
            if (currentEvent.canActivate()) {
                currentEvent.activate();
                GameEventType eventType = currentEvent.getEventType();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Title title = Title.title(
                            Component.text("⚠　イベント発生　⚠"),
                            eventType.getAbout(),
                            Title.Times.times(
                                    java.time.Duration.ofMillis(100),
                                    java.time.Duration.ofSeconds(1),
                                    java.time.Duration.ofMillis(500)
                            )
                    );
                    player.showTitle(title);
                    player.sendMessage(
                            Component.text("[!] イベント発生 [!]").color(NamedTextColor.DARK_RED)
                                    .append(eventType.getName().color(NamedTextColor.RED))
                                    .append(eventType.getLore()).color(NamedTextColor.WHITE)
                    );
                    player.playSound(player, Sound.BLOCK_END_PORTAL_SPAWN, 0.7f, 1.0f);
                }
            }
        }
    }

    public void deactivateCurrentEvent() {
        if (currentEvent != null) {
            currentEvent.deactivate();
            currentEvent = null;
        }
    }
}
