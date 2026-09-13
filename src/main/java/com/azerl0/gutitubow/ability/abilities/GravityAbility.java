package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.entity.grabityhole.GravityHole;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class GravityAbility extends Ability {
    private GravityHole gravityHole;

    public GravityAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.GRAVITY;
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        if (!hasCompassCooldown()) {
            launchGravityHole();
            setCompassCooldown(3 * 20);
            startCompassCooldownTask();
        } else  {
            activateGravityHole();
        }
    }

    private void launchGravityHole() {
        Player player = getPlayer();
        if (player == null) return;
        gravityHole = context.gutitubow().getGutitubowEntityManager().spawnGutitubowEntity(player.getEyeLocation(), GravityHole.class, g -> {
            g.setDirection(player.getLocation().getDirection().clone().normalize());
            g.setOwnerUUID(playerUUID);
        });
        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
    }

    private void activateGravityHole() {
        if (gravityHole != null && !gravityHole.isRemoved() && gravityHole.isSmall()) {
            gravityHole.growUp();
        }
    }
}
