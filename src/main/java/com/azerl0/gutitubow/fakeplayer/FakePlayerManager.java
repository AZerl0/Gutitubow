package com.azerl0.gutitubow.fakeplayer;

import org.bukkit.Location;

public class FakePlayerManager {

    public FakePlayer createFakePlayer(final String name, final Location spawnLocation) {
        return new FakePlayer(name, spawnLocation);
    }
}
