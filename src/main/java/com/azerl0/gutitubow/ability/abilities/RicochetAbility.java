package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RicochetAbility implements Ability {

    private final JavaPlugin plugin;

    public RicochetAbility(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "ricochet";
    }

    public void onArrowLaunch(EntityShootBowEvent event) {

        NBT.modify(event.getProjectile(), nbt -> {
            nbt.setBoolean("Glowing", true);
        });
    }

}
