package com.azerl0.gutitubow.ability.abilities;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityContext;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.event.ability.copy.UseCopiedAbilityEvent;
import com.azerl0.gutitubow.event.gameplayer.*;
import com.azerl0.gutitubow.event.gutitubowentity.shield.ShieldBreakEvent;
import com.azerl0.gutitubow.event.gutitubowentity.shield.ShieldLowerEvent;
import com.azerl0.gutitubow.event.gutitubowentity.shield.ShieldRaiseEvent;
import com.azerl0.gutitubow.event.gutitubowentity.shield.ShieldRaisingEvent;
import com.azerl0.gutitubow.gui.guis.CopiedAbilitiesGUI;
import com.azerl0.gutitubow.util.AdvancementUtil;
import com.azerl0.gutitubow.util.ItemUtil;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CopyAbility extends Ability {
    private @Nullable Ability currentAbility;
    private int currentAbilityRemainedTick = 0;
    private final Map<UUID, Long> copiedPlayerTick = new HashMap<>();
    private final List<AbilityType> stockAbilities = new ArrayList<>();
    private final CopiedAbilitiesGUI copiedAbilitiesGUI = new CopiedAbilitiesGUI(this);

    public CopyAbility(AbilityContext context) {
        super(context);
    }

    @Override
    public @NotNull AbilityType getType() {
        return AbilityType.COPY;
    }

    @Override
    protected void onInit(Player player) {
        player.getInventory().setItem(22, OPEN_STOCKED_ABILITIES_ICON);
    }

    @Override
    protected void onReset() {
        if (currentAbility != null) currentAbility.reset();
        Player player = getPlayer();
        if (player == null) return;
        player.getInventory().setItem(22, GutitubowItem.INVALID_SLOT_BLACK);
    }

    @Override
    protected void onTick() {
        currentAbilityRemainedTick--;
        if (currentAbility != null) {
            if (currentAbilityRemainedTick <= 0) {
                Player player = getPlayer();
                if (player == null) return;

                currentAbility.reset();
                Title title = Title.title(
                        Component.empty(),
                        Component.text("複製した「").append(currentAbility.getType().getName()).append(Component.text("」が終了した")).color(NamedTextColor.RED),
                        Title.Times.times(
                                java.time.Duration.ofMillis(100),
                                java.time.Duration.ofSeconds(1),
                                java.time.Duration.ofMillis(500)
                        )
                );
                player.showTitle(title);
                player.sendMessage(Component.text("「").append(currentAbility.getType().getName()).append(Component.text("」の能力の使用が終了しました。")).color(NamedTextColor.RED));
                player.getInventory().setItem(22, OPEN_STOCKED_ABILITIES_ICON);
                currentAbility = null;
            }
        }
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event) {
        Player player = getPlayer();
        if (player == null) return;
        if (event.getEntity() instanceof Player target && player.getInventory().getItemInMainHand().getType() != Material.BOW) {
            tryCopy(target);
        }

        if (currentAbility != null) currentAbility.onAttack(event);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != event.getView().getBottomInventory()) return;
        if (event.getSlot() == 22) {
            copiedAbilitiesGUI.open(getPlayer());
            event.setCancelled(true);
        }

        if (currentAbility != null) currentAbility.onClick(event);
    }

    public void setAbility(AbilityType abilityType) {
        Player player = getPlayer();
        if (player == null) return;

        if (currentAbility != null) currentAbility.reset();
        currentAbility = context.game().getAbilityManager().createAbility(abilityType);
        currentAbility.init(player);
        currentAbilityRemainedTick = 60 * 20;
        player.sendMessage(Component.text("「").append(abilityType.getName()).append(Component.text("」の能力を1分間使用します。")).color(NamedTextColor.GREEN));
        player.playSound(player, Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0f, 1.0f);

        ItemStack displayItem = abilityType.getDisplayItem(player);
        ItemUtil.setCantMove(displayItem, true);
        ItemUtil.setCantDrop(displayItem, true);
        player.getInventory().setItem(22, displayItem);
        player.getWorld().spawnParticle(
                Particle.ENTITY_EFFECT,
                player.getLocation(),
                10,
                0.75, 0.75, 0.75,
                0,
                Color.fromRGB(255, 255, 255)
        );


    }

    private void tryCopy(Player origin) {
        Player player = getPlayer();
        if (player == null) return;

        long currentTime = origin.getWorld().getGameTime();
        long cooldown = currentTime - copiedPlayerTick.getOrDefault(origin.getUniqueId(), currentTime);
        if (copiedPlayerTick.containsKey(origin.getUniqueId()) && cooldown <= 30 * 20) {
            player.sendMessage(Component.text("この相手の能力は30秒間コピーできません。").color(NamedTextColor.RED));
            return;
        }
        Ability targetAbility = context.game().getAbilityManager().getAbility(origin);
        if (targetAbility == null) {
            player.sendMessage(Component.text("この相手は能力を持っていません。").color(NamedTextColor.RED));
        } else {
            AbilityType abilityType = targetAbility.getType();
            stockAbilities.add(abilityType);
            copiedPlayerTick.put(origin.getUniqueId(), currentTime);
            player.sendMessage(Component.text("「").append(abilityType.getName()).append(Component.text("」の能力をストックしました。")).color(NamedTextColor.GREEN));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            if (stockAbilities.size() >= 9) {
                AdvancementUtil.grantAdvancement(player, "gutitubow:ability/extend/copy_collect_many_ability");
            }
        }
    }

    private static final ItemStack OPEN_STOCKED_ABILITIES_ICON;

    static {
        ItemStack itemStack = new ItemStack(Material.STRUCTURE_VOID);

        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.text("ストック能力の使用").decoration(TextDecoration.BOLD, false).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.LIGHT_PURPLE));
        itemStack.setItemMeta(meta);
        OPEN_STOCKED_ABILITIES_ICON = itemStack.clone();
    }

    public List<AbilityType> getStockAbilities() {
        return stockAbilities;
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
    public void onAnyPlayerDamage(GamePlayerPreDamageEvent event) {
        if (currentAbility != null) currentAbility.onAnyPlayerDamage(event);
    }

    @Override
    public void onDamage(GamePlayerPreDamageEvent event) {
        if (currentAbility != null) currentAbility.onDamage(event);
    }

    @Override
    public void onDealDamage(GamePlayerPreDamageEvent event) {
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

    @Override
    public void onWin(GamePlayerWinEvent event) {
        if (currentAbility != null) currentAbility.onWin(event);
    }

    @Override
    public void onShieldRaise(ShieldRaiseEvent event) {
        if (currentAbility != null) currentAbility.onShieldRaise(event);
    }

    @Override
    public void onShieldRaising(ShieldRaisingEvent event) {
        if (currentAbility != null) currentAbility.onShieldRaising(event);
    }

    @Override
    public void onShieldLower(ShieldLowerEvent event) {
        if (currentAbility != null) currentAbility.onShieldLower(event);
    }

    @Override
    public void onShieldBreak(ShieldBreakEvent event) {
        if (currentAbility != null) currentAbility.onShieldBreak(event);
    }

    @Override
    public void onUseCopiedAbility(UseCopiedAbilityEvent event) {
        if (event.getAbilityType() == AbilityType.COPY) AdvancementUtil.grantAdvancement(event.getPlayer(), "gutitubow:ability/extend/copy_use_copied_copy");
        if (currentAbility != null) currentAbility.onUseCopiedAbility(event);
    }
}
