package com.azerl0.gutitubow.gameevent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;

public abstract class GameEvent {
    private boolean activated = false;

    public @NotNull
    abstract GameEventType getEventType();

    public boolean isActivated() {
        return activated;
    }

    public abstract boolean canActivate();

    public final void activate(boolean playEffect) {
        if (activated) return;
        activated = true;
        onActivate();
        if (playEffect) playActivateEffect(getEventType());
    }

    public final void activate() {
        activate(true);
    }

    protected abstract void onActivate();

    public final void deactivate(boolean playEffect) {
        activated = false;
        onDeactivated();
        if (playEffect) playDeactivateEffect();
    }

    public final void deactivate() {
        deactivate(true);
    }

    public static void playActivateEffect(GameEventType eventType) {
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
                    Component.text("[!] イベント発生 [!]\n").color(NamedTextColor.DARK_RED)
                            .append(eventType.getName().color(NamedTextColor.RED)).append(Component.text("\n"))
                            .append(eventType.getLore()).color(NamedTextColor.WHITE)
            );
            player.playSound(player, Sound.BLOCK_END_PORTAL_SPAWN, 0.7f, 1.0f);
        }
    }

    public static void playDeactivateEffect() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Title title = Title.title(
                    Component.empty(),
                    Component.text("⚠　世界の異変が収まった　⚠"),
                    Title.Times.times(
                            java.time.Duration.ofMillis(100),
                            java.time.Duration.ofSeconds(1),
                            java.time.Duration.ofMillis(500)
                    )
            );
            player.showTitle(title);
        }
    }

    protected abstract void onDeactivated();

    protected void onArrowHit(ProjectileHitEvent event) {}

    protected void onShoot(EntityShootBowEvent event) {}
}
