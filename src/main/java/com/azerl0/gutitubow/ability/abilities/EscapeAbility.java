package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.damage.GutitubowDamageType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerPreDamageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;


public class EscapeAbility extends Ability {
    private BukkitTask escapeTask = null;

    public EscapeAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.ESCAPE;
    }

    @Override
    public void onInit(Player player) {
        resetEscape();
    }

    @Override
    public void onReset() {
        resetEscape();
    }

    @Override
    public void onDamage(GamePlayerPreDamageEvent event) {
        GutitubowDamageType type = event.getDamageSource().getDamageType();
        if (!(type == GutitubowDamageType.ARROW_SHOT ||
                type == GutitubowDamageType.BOMB_BLAST ||
                type == GutitubowDamageType.RICOCHET_BLAST)) return;
        if (!getPlayer().isSneaking()) return;

        escape();

    }

    private void escape() {
        if (escapeTask != null && !escapeTask.isCancelled()) return;
        Location location = getPlayer().getLocation();
        World world = getPlayer().getWorld();

        ItemStack elytra = ItemStack.of(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        meta.setUnbreakable(true);
        elytra.setItemMeta(meta);
        getPlayer().getInventory().setChestplate(elytra);

        getPlayer().setVelocity(new Vector(0.0, 4.0, 0.0));

        Title title = Title.title(
                Component.text("⚠").color(NamedTextColor.YELLOW)
                        .append(Component.text(" 緊急脱出 ").color(NamedTextColor.RED)
                                .append(Component.text("⚠").color(NamedTextColor.YELLOW)
                                )),
                Component.empty(),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        getPlayer().showTitle(title);
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.0f);

        world.spawnParticle(
                Particle.EXPLOSION,
                location, 1);


        escapeTask = new BukkitRunnable() {
            private boolean startedGliding = false;

            @Override
            public void run() {
                if (getPlayer().isGliding()) {
                    startedGliding = true;
                    return;
                }
                if (startedGliding) {
                    resetEscape();
                }
                getPlayer().setGliding(true);
            }
        }.runTaskTimer(context.gutitubow(), 1L, 1L);
    }


    private void resetEscape() {
        getPlayer().getInventory().setChestplate(ItemStack.of(Material.AIR));
        if (escapeTask != null) escapeTask.cancel();
        escapeTask = null;
    }
}
