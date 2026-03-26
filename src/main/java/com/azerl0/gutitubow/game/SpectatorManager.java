package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.gui.guis.SpectatorsGUI;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpectatorManager {
    private Set<UUID> spectators = new HashSet<>();
    private SpectatorsGUI spectatorsGUI;

    public SpectatorManager() {
        this.spectatorsGUI = new SpectatorsGUI(this);
    }

    public Set<UUID> getSpectators() {
        return spectators;
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player.getUniqueId());
    }

    public void clear() {
        spectators.clear();
    }

    public void addPlayer(Player player) {
        spectators.add(player.getUniqueId());
        spectatorsGUI.updateGUI();
    }

    public void removePlayer(Player player) {
        spectators.remove(player.getUniqueId());
        spectatorsGUI.updateGUI();
    }

    public SpectatorsGUI getSpectatorsGUI() {
        return spectatorsGUI;
    }
}
