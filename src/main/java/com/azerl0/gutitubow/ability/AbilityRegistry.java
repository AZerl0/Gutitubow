package com.azerl0.gutitubow.ability;

import java.util.*;

public class AbilityRegistry {

    private final Map<String, Ability> abilities = new HashMap<>();

    public void register(Ability ability) {
        abilities.put(ability.getId(), ability);
    }

    public Ability get(String id) {
        return abilities.get(id);
    }

    public Set<String> getAllIds() {
        return abilities.keySet();
    }


}