package com.azerl0.gutitubow.ability;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class AbilityRegistry {

    private static final Map<AbilityType, Function<AbilityContext, Ability>> abilities = new HashMap<>();

    public static void register(AbilityType type, Function<AbilityContext, Ability> factory) {
        abilities.put(type, factory);
    }

    @Nullable
    public static Ability create(AbilityType type, AbilityContext context) {
        Function<AbilityContext, Ability> factory = abilities.get(type);
        if (factory == null) return null;
        return factory.apply(context);
    }

    public static Set<AbilityType> getTypes() {
        return abilities.keySet();
    }

    public static int getSize() {
        return abilities.size();
    }

    @Nullable
    public static AbilityType getType(String type) {
        AbilityType abilityType = AbilityType.valueOf(type.toUpperCase());
        if (abilities.containsKey(abilityType)) {
            return abilityType;
        } else {
            return null;
        }
    }
}