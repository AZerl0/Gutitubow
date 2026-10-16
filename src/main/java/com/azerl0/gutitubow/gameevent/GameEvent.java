package com.azerl0.gutitubow.gameevent;

import com.azerl0.gutitubow.event.gameplayer.GamePlayerDeathEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerKillEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerShootEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class GameEvent {
    protected final GameEventContext context;
    private boolean activated = false;

    public GameEvent(GameEventContext context) {
        this.context = context;
    }

    public @NotNull
    abstract GameEventType getEventType();

    public boolean isActivated() {
        return activated;
    }

    public boolean canActivate() {
        return true;
    }

    protected boolean overridePlayerEffect() {
        return false;
    }

    public final void activate(boolean playEffect) {
        if (activated) return;
        activated = true;
        if (playEffect && !overridePlayerEffect()) playActivateEffect(getEventType());
        onActivate();
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
        playActivateEffect(
                eventType.getAbout(),
                eventType.getName(),
                eventType.getLore()
        );
    }

    public static void playActivateEffect(
            final @NotNull Component about,
            final @NotNull Component name,
            final @NotNull List<Component> lore

    ) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Title title = Title.title(
                    Component.text("⚠　イベント発生　⚠"),
                    about,
                    Title.Times.times(
                            java.time.Duration.ofMillis(100),
                            java.time.Duration.ofSeconds(1),
                            java.time.Duration.ofMillis(500)
                    )
            );
            player.showTitle(title);
            Component message = Component.text("[!] イベント発生 [!]\n").color(NamedTextColor.DARK_RED);
            message = message.append(name.color(NamedTextColor.RED).append(Component.text("\n")));
            for (Component loreComponent : lore) {
                message = message.append(loreComponent.color(NamedTextColor.WHITE).append(Component.text("\n")));
            }
            player.sendMessage(message);
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

    protected void onAnyPlayerKill(GamePlayerKillEvent event) {}

    protected void onAnyPlayerDeath(GamePlayerDeathEvent event) {}

    protected void onGamePlayerShoot(GamePlayerShootEvent event) {}
}
