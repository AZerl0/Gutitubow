package com.azerl0.gutitubow.debug;

import org.bukkit.plugin.java.JavaPlugin;

public class DebugManager {
    private final JavaPlugin plugin;
    private boolean quick_phase;
    private boolean no_start_teleport;
    private boolean disable_check_game_over;
    private boolean disable_solo_mode_pin;

    public DebugManager(JavaPlugin plugin) {
        this.plugin = plugin;
        quick_phase = false;
        no_start_teleport = false;
        disable_check_game_over = false;
        disable_solo_mode_pin = true;
    }

    public boolean isQuickPhase() {
        return quick_phase;
    }

    public void setQuickPhase(boolean quick_phase) {
        this.quick_phase = quick_phase;
    }

    public boolean isNoStartTeleport() {
        return no_start_teleport;
    }

    public void setNoStartTeleport(boolean no_start_teleport) {
        this.no_start_teleport = no_start_teleport;
    }

    public boolean isDisableCheckGameOver() {
        return disable_check_game_over;
    }

    public void setDisableCheckGameOver(boolean disable_check_game_over) {
        this.disable_check_game_over = disable_check_game_over;
    }

    public boolean isDisableSoloModePin() {
        return disable_solo_mode_pin;
    }

    public void setDisableSoloModePin(boolean disable_solo_mode_pin) {
        this.disable_solo_mode_pin = disable_solo_mode_pin;
    }
}
