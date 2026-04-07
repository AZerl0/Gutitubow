package com.azerl0.gutitubow.pin;

import com.azerl0.gutitubow.event.gameplayer.GamePlayerCreatePinEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerPreCreatePinEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PinManager {
    private final JavaPlugin plugin;
    private final Map<UUID, Pin> playerPins = new HashMap<>();

    public PinManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public @Nullable Pin getPin(@NotNull Player owner) {
        if (hasPin(owner)) {
            return playerPins.get(owner.getUniqueId());
        } else return null;
    }

    public boolean hasPin(@NotNull Player owner) {
        return playerPins.containsKey(owner.getUniqueId());
    }

    public @NotNull Pin createPin(@NotNull Player owner, Location location) {
        removePin(owner);
        Pin pin = new Pin(plugin, this, location, owner.getUniqueId(), owner.displayName());
        pin.addViewer(owner);
        playerPins.put(owner.getUniqueId(), pin);
        return pin;
    }

    public void removePin(@NotNull UUID ownerUUID) {
        Pin pin = playerPins.get(ownerUUID);
        if (pin != null) pin.remove();
        playerPins.remove(ownerUUID);
    }

    public void removePin(@NotNull Player owner) {
        removePin(owner.getUniqueId());
    }

    public void tryCreatePin(@NotNull Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        World world = player.getWorld();

        RayTraceResult rayTraceBlocks = world.rayTraceBlocks(
                eye,
                dir,
                256,
                FluidCollisionMode.NEVER,
                true
        );
        Location location = null;
        if (rayTraceBlocks != null && rayTraceBlocks.getHitBlock() != null) {
            location = rayTraceBlocks.getHitPosition().toLocation(world);
        }

        GamePlayerPreCreatePinEvent preEvent = new GamePlayerPreCreatePinEvent(player, location);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) return;

        if (location != null) {
            Pin pin = createPin(player, location);
            double distance = location.distance(player.getLocation());

            player.sendMessage(Component.text("[!] ").append(player.displayName()).append(Component.text("が場所を指定した！ (" + Math.floor(distance * 10) / 10 + "m)")).color(NamedTextColor.GREEN));
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            GamePlayerCreatePinEvent event = new GamePlayerCreatePinEvent(player, pin);
            Bukkit.getPluginManager().callEvent(event);
        } else {
            player.sendMessage(Component.text("[!] 視点の方向にブロックが存在しないか遠すぎます！").color(NamedTextColor.RED));
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }
}
