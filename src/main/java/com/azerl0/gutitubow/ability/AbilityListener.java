package com.azerl0.gutitubow.ability;

import com.azerl0.gutitubow.event.gameplayer.*;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.Material;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class AbilityListener implements Listener {
    private final AbilityManager abilityManager;

    public AbilityListener(AbilityManager abilityManager) {
        this.abilityManager = abilityManager;
    }


    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Ability ability = abilityManager.getAbility(player);
        if (ability != null) {
            ability.onShoot(event);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onMove(event);
    }

    @EventHandler
    public void onSneaking(GamePlayerSneakingEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onSneaking(event);
    }

    @EventHandler
    public void onToggleSneaking(PlayerToggleSneakEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onToggleSneak(event);
    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onToggleSprint(event);
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onToggleFlight(event);
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onJump(event);
    }

    @EventHandler
    public void onPressJumpKey(PlayerInputEvent event) {
        if (!event.getInput().isJump()) return;
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onPressJumpKey(event);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        for (Ability ability : abilityManager.getValues()) {
            ability.onAnyArrowHit(event);
        }

        if (!(event.getEntity().getShooter() instanceof Player player)) return;

        Ability ability = abilityManager.getAbility(player);
        if (ability != null) ability.onArrowHit(event);
    }

    @EventHandler
    public void onPlayerRightClickBow(PlayerInteractEvent event) {
        if (!(event.getItem() != null &&
                event.getItem().getType() == Material.BOW)) return;

        Action action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;

        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onPlayerRightClickBow(event);

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Ability ability = abilityManager.getAbility(player);
        if (ability != null) ability.onInteract(event);
    }

    @EventHandler
    public void onDamage(GamePlayerDamageEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onDamage(event);
        Player attacker = event.getDamageSource().getDirectPlayer();
        if (attacker != null) {
            ability = abilityManager.getAbility(attacker);
            if (ability != null) ability.onDealDamage(event);
        }
    }

    @EventHandler
    public void onKill(GamePlayerKillEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onKill(event);
    }

    @EventHandler
    public void onDeath(GamePlayerDeathEvent event) {
        Ability ability = abilityManager.getAbility(event.getPlayer());
        if (ability != null) ability.onDeath(event);
    }

    @EventHandler
    public void onLeftClick(PlayerFixLeftClickEvent event) {
        Ability ability = abilityManager.getAbility(event.getEvent().getPlayer());
        if (ability != null) ability.onLeftClick(event);

        ItemStack itemStack = event.getEvent().getItem();
        if (!(itemStack != null &&
                itemStack.getType() == Material.BOW)) return;

        Action action = event.getEvent().getAction();
        if (!(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) return;

        if(ability != null) ability.onPlayerLeftClickBow(event.getEvent());
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() == EntityType.HAPPY_GHAST) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() == EntityType.HAPPY_GHAST) {
            if (event.getDamageSource().getDamageType() == DamageType.PLAYER_ATTACK) {
                event.setCancelled(true);
            }
        }
    }
}
