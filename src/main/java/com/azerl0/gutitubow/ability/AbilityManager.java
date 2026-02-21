package com.azerl0.gutitubow.ability;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {

    private final Map<UUID, String> playerAbilities = new HashMap<>();

    public String getAbilityId(Player player) {
        return playerAbilities.get(player.getUniqueId());
    }

    public void setAbility(Player player, String id) {
        playerAbilities.put(player.getUniqueId(), id);
    }

    public void resetAbility(Player player) {
        playerAbilities.put(player.getUniqueId(), "None");
    }
}