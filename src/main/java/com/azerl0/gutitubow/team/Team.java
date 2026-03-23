package com.azerl0.gutitubow.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Team {
    private final Component teamName;
    private final ItemStack teamIconItem;
    private final NamedTextColor teamColor;
    private final List<UUID> players = new ArrayList<>();

    public Team(Component teamName, ItemStack teamIconItem, NamedTextColor teamColor) {
        this.teamName = teamName;
        this.teamIconItem = teamIconItem;
        this.teamColor = teamColor;
    }

    public Component getTeamName() {
        return teamName;
    }

    public ItemStack getTeamIconItem() {
        return teamIconItem;
    }

    public NamedTextColor getTeamColor() {
        return teamColor;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void clear() {
        players.clear();
    }

    public void addPlayer(Player player) {
        if (players.contains(player.getUniqueId())) return;
        players.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player.getUniqueId());
    }

    @Nullable
    public Player getJumpMaster() {
        if (players.isEmpty()) return null;
        return Bukkit.getPlayer(players.getFirst());
    }
}
