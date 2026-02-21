package com.azerl0.gutitubow.ability;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {

    static final Map<UUID, String> playerAbilities = new HashMap<>();

    static public String getAbilityId(Player player) {
        String ability = playerAbilities.get(player.getUniqueId());
        if (ability == null) ability = "None";
        return ability;
    }

    static public void setAbility(Player player, String id) {
        playerAbilities.put(player.getUniqueId(), id);
    }

    static public void resetAbility(Player player) {
        playerAbilities.put(player.getUniqueId(), "None");
    }
}