package com.azerl0.gutitubow.game;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum GamePhase {
    UNPLAYED(-1),
    STARTING(-1),
    PHASE0(0),
    PHASE1(1),
    PHASE1_SHRINKING(1),
    PHASE2(2),
    PHASE2_SHRINKING(2),
    PHASE3(3),
    PHASE3_SHRINKING(3),
    PHASE4(4),
    PHASE4_SHRINKING(4),
    PHASE5(5),
    PHASE5_SHRINKING(5),
    PRE_FINAL_PHASE(6),
    FINAL_PHASE(6);

    private final int phaseNumber;

    GamePhase(int phaseNumber) {
        this.phaseNumber = phaseNumber;
    }

    public int getPhaseNumber() {
        return phaseNumber;
    }

    public boolean isShrinking() {
        List<GamePhase> shrinkingPhases = List.of(PHASE1_SHRINKING, PHASE2_SHRINKING, PHASE3_SHRINKING, PHASE4_SHRINKING, PHASE5_SHRINKING);
        return shrinkingPhases.contains(this);
    }

    public boolean isPlaying() {
        List<GamePhase> shrinkingPhases = List.of(PHASE1, PHASE2, PHASE3, PHASE4, PHASE5);
        return shrinkingPhases.contains(this);
    }

    public @Nullable GamePhase getNextPhase() {
        GamePhase[] values = values();
        int nextIndex = ordinal() + 1;

        return nextIndex < values.length ? values[nextIndex] : null;
    }
}
