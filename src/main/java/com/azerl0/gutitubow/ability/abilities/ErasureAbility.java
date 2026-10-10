package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.gameplayer.GamePlayer;
import com.azerl0.gutitubow.util.MathUtil;
import com.azerl0.gutitubow.util.PlayerUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ErasureAbility extends Ability {
    private final Key postEffectKey = Key.key("gutitubow:erasing");
    private int charge = 0;
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
        resetGlow(lookingPlayers);
    }

    @Override
    public void onPlayerLeftClickBow(PlayerInteractEvent event){
        if (erasureActivate) {
            resetErasing();
        } else {
            activateErasing();
        }
    }

    @Override
    public void onTick() {
        Player player = getPlayer();
        if (player == null) return;

        if (erasureActivate) {
            if (!isDebugMode()) charge++;
            if (charge <= 0) {
                resetErasing();
                charge = 0;
            } else {
                oldLookingPlayers.clear();
                oldLookingPlayers.addAll(lookingPlayers);
                if (!isErasing()) {
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
                        lookingGamePlayer.setRemainedErasureTick(1);
                        nearPlayer.postEffects().add(postEffectKey);
                        lookingPlayers.add(nearPlayer);
                        oldLookingPlayers.remove(nearPlayer);
                    }

                }
                resetGlow(oldLookingPlayers);
            }
        } else {
            if (player.getWorld().getGameTime() % 6 == 0) {
                charge++;
                int MAX_CHARGE = 7 * 20;
                charge = Math.min(charge, MAX_CHARGE);
            }
        }

        double chargeSecondsText = Math.floor(((double) charge / 20) * 10) / 10;
        player.sendActionBar(Component.text("抹消チャージ" + chargeSecondsText + "秒"));
    }

    private void activateErasing() {
        Player player = getPlayer();
        if (player == null) return;
        player.playSound(player, Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 1.5f);
        erasureActivate = true;
    }

    private void resetErasing() {
        erasureActivate = false;
    }

    private void resetGlow(Set<Player> resetPlayers) {
        Player player = getPlayer();
        if (player == null) return;

        for (Player looking : resetPlayers) {
            context.glowManager().setGlowColor(player, looking, NamedTextColor.WHITE);
            context.glowManager().removeGlow(player, looking);
            looking.postEffects().remove(postEffectKey);
        }
    }
}
