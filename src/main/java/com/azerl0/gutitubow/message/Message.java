package com.azerl0.gutitubow.message;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Message {

    public static void broadcastMessage(final @Nullable Component message) {
        if (message == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public @Nullable static Component getDamageMessage(Player player, DamageSource damageSource, Player viewer) {
        Game game = Gutitubow.get().getGame();
        boolean playerIsTeammate = game.isTeammate(player, viewer);
        double distance = damageSource.getDistance();;

        Player attacker = damageSource.getDirectPlayer();
        if (attacker != null) {

            boolean attackerIsTeammate = game.isTeammate(player, attacker);
            return Component.text("§f§l[§c§lHIT§f§l] ")
                    .append(attacker.displayName().color(attackerIsTeammate ? NamedTextColor.AQUA : NamedTextColor.WHITE))
                    .append(Component.text(" ➽ "))
                    .append(player.displayName().color(playerIsTeammate ? NamedTextColor.RED : NamedTextColor.WHITE))
                    .append(getDistanceSuffix(distance));
        }

        GutitubowDamageType damageType = damageSource.getDamageType();
        if (damageType == GutitubowDamageType.FAKE_CAREPACKAGE) {
            return Component.text("§f§l[§c§lHIT§f§l] §r§f救援物資")
                    .append(Component.text(" ➽ "))
                    .append(player.displayName().color(playerIsTeammate ? NamedTextColor.RED : NamedTextColor.WHITE))
                    .append(getDistanceSuffix(distance));
        }

        return null;
    }

    public @Nullable
    static Component getDeathMessage(Player player, DamageSource damageSource, Player viewer) {
        Game game = Gutitubow.get().getGame();
        boolean playerIsTeammate = game.isTeammate(player, viewer);
        double distance = damageSource.getDistance();

        Player attacker = damageSource.getDirectPlayer();
        if (attacker != null) {

            boolean attackerIsTeammate = game.isTeammate(player, attacker);
            return Component.text("§f§l[§4§lKILL§f§l] ")
                    .append(attacker.displayName().color(attackerIsTeammate ? NamedTextColor.AQUA : NamedTextColor.WHITE))
                    .append(Component.text(" ➽ "))
                    .append(player.displayName().color(playerIsTeammate ? NamedTextColor.AQUA : NamedTextColor.WHITE))
                    .append(getDistanceSuffix(distance));
        }

        GutitubowDamageType damageType = damageSource.getDamageType();
        Component suffix = null;
        switch (damageType) {
            case FAKE_CAREPACKAGE -> suffix = getSimpleSuffix("救援物資", true);
            case OUTSIDE_BORDER -> suffix = getSimpleSuffix("エリア外", true);
            case FAILED_LAST_STAND -> suffix = getSimpleSuffix("背水の陣失敗", true);
            case LOGOUT -> suffix = getSimpleSuffix("ログアウト", true);
        }
        if (suffix == null) return null;

        return getSimpleDeathMessage(player, playerIsTeammate).append(suffix);
    }

    private static @NotNull Component getDistanceSuffix(double distance, boolean preSpace) {
        return getSimpleSuffix(Math.floor(distance * 10) / 10 + "m", preSpace);
    }

    private static @NotNull Component getDistanceSuffix(double distance) {
        return getDistanceSuffix(distance, true);
    }

    private static @NotNull Component getSimpleDeathMessage(Player player, boolean playerIsTeammate) {
        return Component.text("§f§l[§4§lDEATH§f§l] ")
                .append(player.name().color(playerIsTeammate ? NamedTextColor.AQUA : NamedTextColor.WHITE));
    }

    private static @NotNull Component getSimpleSuffix(String text, boolean space) {
        String spaceText = space ? " " : "";
        return Component.text(spaceText + "(" + text + ")").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false);
    }
}
