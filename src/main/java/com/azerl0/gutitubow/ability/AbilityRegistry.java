package com.azerl0.gutitubow.ability;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AbilityRegistry {

    static private final Map<String, Ability> abilities = new HashMap<>();

    static public void register(Ability ability) {
        abilities.put(ability.getType().toString().toLowerCase(), ability);
    }

    @Nullable
    static public Ability get(AbilityType type) {
        if (abilities.containsKey(type.toString().toLowerCase())) return abilities.get(type.toString().toLowerCase());
        return null;
    }

    static public Set<String> getAllIds() {
        return abilities.keySet();
    }


}