package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerKillEvent;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PredationAbility extends Ability {
    public PredationAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.PREDATION;
    }

    @Override
    public void reset() {
        super.reset();
        Player player = getPlayer();
        if (player == null) return;
        double maxHealth = context.game().isStarted() ? 6 : 20;
        Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(maxHealth);
    }

    @Override
    public void onKill(GamePlayerKillEvent event) {
        predationPlayer(event);
    }

    private void predationPlayer(GamePlayerKillEvent event) {
        Player player = getPlayer();
        if (player == null) return;

        event.setKillMessage(Component.text(event.getTarget().getName() + "を捕食した").color(NamedTextColor.RED));
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
        Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(maxHealth + 2);
        GamePlayer target = context.game().getGamePlayer(event.getTarget());
        int potionAmount = target.getPotionAmount();
        if (potionAmount > 0) {
            player.setHealth(Math.min(
                    player.getHealth() + potionAmount * 2,
                    Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue()
            ));
            Bukkit.getScheduler().runTaskLater(context.gutitubow(), () ->
                    player.sendMessage(Component.text(event.getTarget().getName() + "から体力を得た。").color(NamedTextColor.RED)), 1L);
        }
    }
}
