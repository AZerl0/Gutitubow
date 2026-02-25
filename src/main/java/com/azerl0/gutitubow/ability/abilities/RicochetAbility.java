package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import org.bukkit.event.entity.EntityShootBowEvent;

public class RicochetAbility implements Ability {
    private final AbilityContext context;

    public RicochetAbility(AbilityContext context) {
        this.context = context;
    }

    @Override
    public String getId() {
        return "ricochet";
    }

    public void onArrowLaunch(EntityShootBowEvent event) {
        event.getProjectile().setGlowing(true);
    }

}
