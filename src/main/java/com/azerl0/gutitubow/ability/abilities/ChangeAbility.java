package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.event.gameplayer.*;
import com.azerl0.gutitubow.util.ItemUtil;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ChangeAbility extends Ability {
    private @Nullable Ability currentAbility;
    private BukkitTask changeAbilityTask;

    public ChangeAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.CHANGE;
    }

    @Override
    public void init(Player player) {
        super.init(player);

        changeAbilityTask = new BukkitRunnable() {
            @Override
            public void run() {
                changeAbility();
            }
        }.runTaskTimer(context.plugin(), 3 * 20L, 60 * 20L);

    }

    @Override
    public void reset() {
        if (currentAbility != null) currentAbility.reset();
        changeAbilityTask.cancel();
        Player player = getPlayer();
        if (player == null) return;
        player.getInventory().setItem(13, GutitubowItem.INVALID_SLOT_BLACK);
    }

    public void changeAbility() {
        Player player = getPlayer();
        if (player == null) return;

        if (currentAbility != null) currentAbility.reset();
        Set<AbilityType> disabledAbilities = new HashSet<>(context.game().getGameSetting().getDisabledAbilities());
        disabledAbilities.add(AbilityType.CHANGE);
        AbilitySelector abilitySelector = new AbilitySelector(disabledAbilities);
        AbilityType nextAbilityType = abilitySelector.nextAbility();
        currentAbility = context.game().getAbilityManager().createAbility(nextAbilityType);
        currentAbility.init(player);

        Title title = Title.title(
                Component.empty(),
                Component.text("♻ 「").append(nextAbilityType.getName().append(Component.text("」に変化した ♻"))).color(NamedTextColor.LIGHT_PURPLE),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);
        World world = player.getWorld();
        Location location = player.getLocation();
        world.playSound(location, Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
        Random random = new Random();
        for (int i = 0; i < 15; i++) {
            world.spawnParticle(
                    Particle.ENTITY_EFFECT,
                    location,
                    1,
                    0.75, 0.75, 0.75,
                    0,
                    Color.fromRGB(random.nextInt(0, 16777216))
            );
        }

        ItemStack displayItem = nextAbilityType.getDisplayItem(player);
        ItemUtil.setCantMove(displayItem, true);
        ItemUtil.setCantDrop(displayItem, true);
        player.getInventory().setItem(22, displayItem);
    }

    @Override
    public void onShoot(EntityShootBowEvent event) {
        if (currentAbility != null) currentAbility.onShoot(event);
    }

    @Override
    public void onArrowHit(ProjectileHitEvent event) {
        if (currentAbility != null) currentAbility.onArrowHit(event);
    }

    @Override
    public void onAnyArrowHit(ProjectileHitEvent event) {
        if (currentAbility != null) currentAbility.onAnyArrowHit(event);
    }

    @Override
    public void onLeftClick(PlayerFixLeftClickEvent event) {
        if (currentAbility != null) currentAbility.onLeftClick(event);
    }

    @Override
    public void onPlayerRightClickBow(PlayerInteractEvent event) {
        if (currentAbility != null) currentAbility.onPlayerRightClickBow(event);
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event) {
        if (currentAbility != null) currentAbility.onPlayerLeftClickBow(event);
    }

    @Override
    public void onMove(PlayerMoveEvent event) {
        if (currentAbility != null) currentAbility.onMove(event);
    }

    @Override
    public void onSneaking(GamePlayerSneakingEvent event) {
        if (currentAbility != null) currentAbility.onSneaking(event);
    }

    @Override
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (currentAbility != null) currentAbility.onToggleSneak(event);
    }

    @Override
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        if (currentAbility != null) currentAbility.onToggleSprint(event);
    }

    @Override
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (currentAbility != null) currentAbility.onToggleFlight(event);
    }

    @Override
    public void onJump(PlayerJumpEvent event) {
        if (currentAbility != null) currentAbility.onJump(event);
    }

    @Override
    public void onPressJumpKey(PlayerInputEvent event) {
        if (currentAbility != null) currentAbility.onPressJumpKey(event);
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (currentAbility != null) currentAbility.onInteract(event);
    }

    @Override
    public void onAnyPlayerDamage(GamePlayerDamageEvent event) {
        if (currentAbility != null) currentAbility.onAnyPlayerDamage(event);
    }

    @Override
    public void onDamage(GamePlayerDamageEvent event) {
        if (currentAbility != null) currentAbility.onDamage(event);
    }

    @Override
    public void onDealDamage(GamePlayerDamageEvent event) {
        if (currentAbility != null) currentAbility.onDealDamage(event);
    }

    @Override
    public void onKill(GamePlayerKillEvent event) {
        if (currentAbility != null) currentAbility.onKill(event);
    }

    @Override
    public void onDeath(GamePlayerDeathEvent event) {
        if (currentAbility != null) currentAbility.onDeath(event);
    }
}
