package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

public class RicochetAbility implements Ability {

    @Override
    public String getId() {
        return "ricochet";
    }

//    @EventHandler
//    void onShootBow(EntityShootBowEvent event) {
////        NamespacedKey key = new NamespacedKey(plugin, "ability_arrow");
//
//        Entity arrow = event.getProjectile();
//
//    }

}
