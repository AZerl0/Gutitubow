package com.azerl0.gutitubow.ability;

import java.util.*;

public class AbilityRegistry {

    static private final Map<String, Ability> abilities = new HashMap<>();

    static public void register(Ability ability) {
        abilities.put(ability.getType().toString(), ability);
    }

    static public Ability get(String id) {
        return abilities.get(id);
    }

    static public Set<String> getAllIds() {
        return abilities.keySet();
    }


}