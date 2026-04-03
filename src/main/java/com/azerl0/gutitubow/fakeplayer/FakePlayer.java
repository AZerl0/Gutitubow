package com.azerl0.gutitubow.fakeplayer;

import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class FakePlayer {

    private final String name;
    private final Location spawnLocation;
    private final NPC npc;

    public FakePlayer(final String name, final Location spawnLocation) {
        this.name = name;
        this.spawnLocation = spawnLocation;
        this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.spawn(spawnLocation);
    }

    public NPC getNpc() {
        return npc;
    }

    public String getName() {
        return name;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
}