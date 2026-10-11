package com.azerl0.gutitubow.profiler;

public enum ProfileChangeReason {
    DEFAULT(1),
    COMMAND(2),
    DOPPELGANGER(5),
    SKIN_EVENT(10),
    MEDUSA(20);

    private final int level;

    ProfileChangeReason(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
