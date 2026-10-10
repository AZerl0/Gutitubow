package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.tags.GutitubowBlockTags;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerWinEvent;
import com.azerl0.gutitubow.util.AdvancementUtil;
import com.azerl0.gutitubow.util.MathUtil;
import com.azerl0.gutitubow.util.PlayerUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;

import java.util.*;

public class AppraisalAbility extends Ability {
    private final Map<Player, Integer> lookingPlayers = new HashMap<>();
    private Map<Player, Integer> oldLookingPlayers = new HashMap<>();
    private final Map<UUID, InfoDisplay> playerAppraisals = new HashMap<>();


    public AppraisalAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.APPRAISAL;
    }

    @Override
    protected void onReset() {
        for (InfoDisplay infoDisplay : new HashSet<>(playerAppraisals.values())) {
            infoDisplay.remove();
        }
    }

    @Override
    public void onTick() {
        Player player = getPlayer();
        if (player == null) return;


        if (hasCompassCooldown()) {
            lookingPlayers.clear();
            oldLookingPlayers.clear();
        } else {
            oldLookingPlayers = new HashMap<>(lookingPlayers);
            lookingPlayers.clear();
            if (isErasing()) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getGameMode() == GameMode.SPECTATOR) continue;
                    if (context.game().isTeammate(player, onlinePlayer)) continue;
                    if (!MathUtil.isInView(player, onlinePlayer.getLocation(), 100, 80)) continue;
                    if (!canSee(player, onlinePlayer)) continue;


                    putLookingPlayer(onlinePlayer, 20);
                    oldLookingPlayers.remove(onlinePlayer);
                }
            }
            for (Player lookedPlayer : oldLookingPlayers.keySet()) {

                int tick = oldLookingPlayers.get(lookedPlayer) - 1;
                if (tick <= 0) {
                    UUID lookedPlayerUUID = lookedPlayer.getUniqueId();
                    InfoDisplay infoDisplay = playerAppraisals.get(lookedPlayerUUID);
                    infoDisplay.remove();
                    playerAppraisals.remove(lookedPlayerUUID);
                    playerAppraisals.remove(lookedPlayerUUID);
                } else {
                    putLookingPlayer(lookedPlayer, tick);
                }
            }
        }
    }

    private void putLookingPlayer(Player target, int lookingTime) {
        Player player = getPlayer();
        if (player == null) return;

        lookingPlayers.put(target, lookingTime);

        InfoDisplay infoDisplay;
        if (playerAppraisals.containsKey(target.getUniqueId())) {
            infoDisplay = playerAppraisals.get(target.getUniqueId());
        } else {
            infoDisplay = new InfoDisplay(target);
            infoDisplay.spawnPacket(player, target);
            playerAppraisals.put(target.getUniqueId(), infoDisplay);
        }
        infoDisplay.update();
    }

    private boolean isCanThrough(Material material) {
        if (Tag.FENCES.isTagged(material)) return true;
        if (Tag.FENCE_GATES.isTagged(material)) return true;
        if (Tag.CHAINS.isTagged(material)) return true;
        if (GutitubowBlockTags.GLASS_BLOCKS.isTagged(material)) return true;
        if (GutitubowBlockTags.GLASS_PANES.isTagged(material)) return true;
        return false;
    }

    private boolean canSee(Player player, Player target) {
        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        Vector direction = target.getEyeLocation().toVector().subtract(eye.toVector());
        double distance = eye.distance(target.getEyeLocation());

        RayTraceResult blockHit = world.rayTraceBlocks(
                eye,
                direction,
                distance,
                FluidCollisionMode.NEVER,
                true,
                block -> !isCanThrough(block.getType())
        );

        boolean canSeeEye = blockHit == null;

        direction = target.getLocation().toVector().subtract(eye.toVector());
        distance = eye.distance(target.getLocation());


        blockHit = world.rayTraceBlocks(
                eye,
                direction,
                distance,
                FluidCollisionMode.NEVER,
                true,
                block -> !isCanThrough(block.getType())
        );


        boolean canSeeFoot = blockHit == null;

        return canSeeEye || canSeeFoot;
    }

    private class InfoDisplay {
        private final UUID targetUUID;
        private final TextDisplay textDisplay;


        private InfoDisplay(@NotNull Player target) {
            this.targetUUID = target.getUniqueId();
            textDisplay = target.getWorld().spawn(target.getLocation(), TextDisplay.class);
            target.addPassenger(textDisplay);
            textDisplay.setTransformation(new Transformation(
                    new org.joml.Vector3f(0.0f, 0.25f, 0.0f),
                    new AxisAngle4f(0.0f, 0.0f, 0.0f, 1.0f),
                    new org.joml.Vector3f(1.0f, 1.0f, 1.0f),
                    new AxisAngle4f(0.0f, 0.0f, 0.0f, 1.0f)
            ));
            textDisplay.getScoreboardTags().add("gutitubow.appraisalDisplay");
        }

        private void spawnPacket(@NotNull Player viewer, @NotNull Player target) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            float scale = 1.0f;
            List<EntityData<?>> defaultData = List.of(
                    new EntityData<>(10, EntityDataTypes.INT, 1), // teleport_duration
                    new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(scale, scale, scale)), // Scale
                    new EntityData<>(15, EntityDataTypes.BYTE, (byte) 3), // Billboard Constraints: CENTER = 3
                    new EntityData<>(16, EntityDataTypes.INT, (15 << 4) | (15 << 20)), // Brightness
                    new EntityData<>(17, EntityDataTypes.FLOAT, 10f), // View range
                    new EntityData<>(25, EntityDataTypes.INT, 0), // Background color
                    new EntityData<>(27, EntityDataTypes.BYTE, (byte) 2) // Is see through
            );


            user.sendPacket(new WrapperPlayServerEntityMetadata(
                    textDisplay.getEntityId(),
                    defaultData
            ));
        }

        private void remove() {
            textDisplay.remove();
        }

        private void update() {
            Player player = getPlayer();
            if (player == null) return;
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null) return;

            User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            Component targetAbilityName = Component.text("なし");
            Ability targetAbility = context.game().getAbilityManager().getAbility(target);
            if (targetAbility != null) targetAbilityName = targetAbility.getType().getName();
            double distance = player.getLocation().distance(target.getLocation());
            Component distanceComponent = Component.text(Math.floor(distance * 10) / 10 + "m");
            int potionCount = context.game().getGamePlayer(target).getPotionAmount();

            Component info = Component.text("鑑定情報").color(NamedTextColor.RED)
                    .append(Component.text("\n名前: ").color(NamedTextColor.RED).append(target.displayName().color(NamedTextColor.GOLD)))
                    .append(Component.text("\n能力: ").color(NamedTextColor.RED).append(targetAbilityName.color(NamedTextColor.GOLD)))
                    .append(Component.text("\n体力: ").color(NamedTextColor.RED).append(PlayerUtil.getHealthComponent(target)))
                    .append(Component.text("\n距離: ").color(NamedTextColor.RED).append(distanceComponent.color(NamedTextColor.GOLD)))
                    .append(Component.text("\nポーション: ").color(NamedTextColor.RED).append(Component.text(potionCount + "個").color(NamedTextColor.GOLD)));
            float scale = (float) Math.max(distance / 7.5, 1.0f);
            scale = Math.min(scale, 3.0f);
            WrapperPlayServerEntityMetadata mainEntityPacket = new WrapperPlayServerEntityMetadata(
                    textDisplay.getEntityId(),
                    List.of(
                            new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f(scale, scale, scale)), // Scale
                            new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, info) // Text
                    )
            );
            user.sendPacket(mainEntityPacket);
        }
    }

    @Override
    public void onWin(GamePlayerWinEvent event) {
        AdvancementUtil.grantAdvancement(event.getPlayer(), "gutitubow:ability/extend/appraisal_win");
    }
}
