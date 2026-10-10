package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerOnAbilityErasureEvent;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.MathUtil;
import com.azerl0.gutitubow.util.PlayerUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ErasureAbility extends Ability {
    private final Key ERASING_EFFECT_KEY = Key.key("gutitubow:erasing");
    private final Key ERASURED_EFFECT_KEY = Key.key("gutitubow:erasured");
    private final int MAX_CHARGE = 20 * 20;
    private int charge = MAX_CHARGE / 2;
    private boolean erasureActivate = false;
    private final Set<Player> lookingPlayers = new HashSet<>();
    private final Set<Player> oldLookingPlayers = new HashSet<>();


    public ErasureAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.ERASURE;
    }

    @Override
    protected void onReset() {
        resetErasure();
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event){
        if (erasureActivate) {
            deactivateErasure();
        } else {
            Player player = event.getPlayer();
            if (hasCompassCooldown()) {
                PlayerUtil.failEffectWithMessage(player, Component.text("クールダウン中です！").color(NamedTextColor.RED));
                return;
            }
            if (charge < 30) {
                PlayerUtil.failEffectWithMessage(player, Component.text("発動には抹消チャージが1.5秒以上必要です！").color(NamedTextColor.RED));
                return;
            }
            if (isErasing()) {
                onErasureEffect();
                return;
            }
            activateErasing();
        }
    }

    @Override
    public void onAbilityErasure(GamePlayerOnAbilityErasureEvent event) {
        if (erasureActivate) deactivateErasure();
    }

    @Override
    public void onTick() {
        Player player = getPlayer();
        if (player == null) return;

        if (erasureActivate) {
            if (!isDebugMode()) charge--;
            if (charge <= 0) {
                deactivateErasure();
                charge = 0;
            } else {
                oldLookingPlayers.clear();
                oldLookingPlayers.addAll(lookingPlayers);
                if (!isErasing()) {
                    lookingPlayers.clear();
                    for (Player nearPlayer : player.getWorld().getNearbyPlayers(player.getLocation(), 50)) {
                        if (nearPlayer.getGameMode() == GameMode.SPECTATOR || nearPlayer.getGameMode() == GameMode.CREATIVE) continue;
                        if (context.game().isTeammate(player, nearPlayer)) continue;
                        if (!MathUtil.isInView(player, nearPlayer.getLocation(), 100, 80)) continue;
                        if (!PlayerUtil.canSee(player, nearPlayer)) continue;
                        if (nearPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
                        if (!player.canSee(nearPlayer)) continue;

                        if (!oldLookingPlayers.contains(nearPlayer)) {
                            context.glowManager().setGlowColor(player, nearPlayer, NamedTextColor.GRAY);
                            context.glowManager().addGlow(player, nearPlayer);
                        }
                        GamePlayer lookingGamePlayer = context.game().getGamePlayer(nearPlayer);
                        lookingGamePlayer.setRemainedErasureTick(3);
                        nearPlayer.postEffects().add(ERASURED_EFFECT_KEY);
                        lookingPlayers.add(nearPlayer);
                        oldLookingPlayers.remove(nearPlayer);
                    }

                }
                resetGlow(oldLookingPlayers);
            }
        } else {
            if (player.getWorld().getGameTime() % 6 == 0) {
                charge++;
                charge = Math.min(charge, MAX_CHARGE);
            }
        }

        double chargeSecondsText = Math.floor(((double) charge / 20) * 10) / 10;
        player.sendActionBar(Component.text("抹消チャージ" + chargeSecondsText + "秒"));
    }

    private void activateErasing() {
        Player player = getPlayer();
        if (player == null) return;
        erasureActivate = true;
        player.playSound(player, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 2.0f);
        player.postEffects().add(ERASING_EFFECT_KEY);
        Title title = Title.title(
                Component.empty(),
                Component.text("抹消発動中").color(NamedTextColor.GRAY),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);

        for (int i = 0; i < 15; i++) {
            double x = Math.random() * 2 - 1;
            double y = Math.random() * 2 - 1;
            double z = Math.random() * 2 - 1;
            Vector dir = new Vector(x, y, z).normalize().multiply(0.7);

            Particle.DUST_COLOR_TRANSITION.builder()
                    .location(player.getEyeLocation())
                    .count(1)
                    .offset(dir.getX(), dir.getY(), dir.getZ())
                    .speed(1.0)
                    .colorTransition(Color.fromRGB(11206669), Color.fromRGB(15073383))
                    .spawn();

        }
    }

    private void deactivateErasure() {
        Player player = getPlayer();
        if (player == null) return;

        resetErasure();
        Title title = Title.title(
                Component.empty(),
                Component.text("抹消解除").color(NamedTextColor.GRAY),
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofSeconds(1),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);
        player.playSound(player, Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 1.5f);
        setCompassCooldown(isDebugMode() ? 20 : 5 * 20);
        startCompassCooldownTask();
    }

    private void resetErasure() {
        Player player = getPlayer();
        if (player == null) return;

        erasureActivate = false;
        player.postEffects().remove(ERASING_EFFECT_KEY);
        resetGlow(lookingPlayers);
    }

    private void resetGlow(Set<Player> resetPlayers) {
        Player player = getPlayer();
        if (player == null) return;

        for (Player looking : resetPlayers) {
            context.glowManager().setGlowColor(player, looking, NamedTextColor.WHITE);
            context.glowManager().removeGlow(player, looking);
            looking.postEffects().remove(ERASURED_EFFECT_KEY);
        }
    }
}
