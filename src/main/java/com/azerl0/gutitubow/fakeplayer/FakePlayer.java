package com.azerl0.gutitubow.fakeplayer;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class FakePlayer {
    private final JavaPlugin plugin;
    private final String name;
    private final Location spawnLocation;
    private final NPC npc;

    public FakePlayer(JavaPlugin plugin, final String name, final Location spawnLocation) {
        this.plugin = plugin;
        this.name = name;
        this.spawnLocation = spawnLocation;
        this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name + UUID.randomUUID());
        npc.getOrAddTrait(SkinTrait.class).setSkinName(name);
        npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);
        npc.data().set(NPC.Metadata.SHOULD_SAVE, false);
        npc.spawn(spawnLocation);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "npc select " + npc.getId());
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "npc skinlayers --cape true");
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "npc skinlayers --cape false");
        }, 2L);
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