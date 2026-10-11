package com.azerl0.gutitubow.gameplayer;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.ability.AbilityType;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.damage.DamageSource;
import com.azerl0.gutitubow.damage.GutitubowDamageType;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.event.gameplayer.*;
import com.azerl0.gutitubow.game.GutitubowGameMode;
import com.azerl0.gutitubow.scoreboard.PlayerBoard;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.util.ComponentUtil;
import com.azerl0.gutitubow.util.ItemUtil;
import com.azerl0.gutitubow.util.PostEffectUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class GamePlayer {
    public final int COMPASS_TELEPORT_TICK = 10 * 20;
    public final int REVIVE_TICK = 15 * 20;

    private final Gutitubow gutitubow;
    private final @NotNull UUID playerUUID;
    private final Game game;
    private int playerArrowReloadTick;
    private int playerArrowMaxStackSize;
    private int playerTotalHit;
    private int playerTotalKill;
    private int playerBorderOutsideTick;
    private int arrowReloadProgressTick;
    private boolean firstElytraFlag;
    private final NamespacedKey gutitubowArrowItemKey;
    private final PlayerBoard playerBoard;
    private int sneakingTick = 0;
    private int revivingTick = 0;
    private int compassTeleportingTick = 0;
    private int remainedFlashTick;
    private int remainedErasureTick = 0;
    private @Nullable AbilityType doppelgangerHoldOriginAbility;


    public GamePlayer(Gutitubow gutitubow, Player player, Game game) {
        this.gutitubow = gutitubow;
        this.playerUUID = player.getUniqueId();
        this.game = game;
        playerArrowReloadTick = 100;
        playerArrowMaxStackSize = 1;
        playerTotalHit = 0;
        playerTotalKill = 0;
        playerBorderOutsideTick = 0;
        arrowReloadProgressTick = 0;
        firstElytraFlag = false;
        gutitubowArrowItemKey = new NamespacedKey(this.gutitubow, "gutitubow.arrow");
        this.playerBoard = game.getPlayerBoard(this);
        this.remainedFlashTick = 0;
    }

    @Nullable
    public Ability getAbility() {
        return gutitubow.getGutitubowManagers().getAbilityManager().getAbility(playerUUID);
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public @NotNull UUID getPlayerUUID() {
        return playerUUID;
    }

    public Game getGame() {
        return this.game;
    }

    @Nullable
    public GameTeam getTeam() {
        return game.getTeamManager().getPlayerTeam(playerUUID);
    }

    public boolean isDead() {
        return game.getDeadPlayers().contains(this);
    }

    private int getBorderOutsideTicks() {
        return this.playerBorderOutsideTick;
    }

    private void setBorderOutsideTicks(int ticks) {
        this.playerBorderOutsideTick = ticks;
    }

    private void updateBorderOutside() {
        Player player = getPlayer();
        if (player != null) {
            World world = player.getWorld();
            if (world.getWorldBorder().isInside(player.getLocation()) || getPlayer().getGameMode() != GameMode.ADVENTURE) {
                setBorderOutsideTicks(0);
                return;
            }

            int borderOutsideTick = getBorderOutsideTicks() + 1;
            setBorderOutsideTicks(borderOutsideTick);
            int borderOutsideDamageTick = 30;
            if (borderOutsideTick % borderOutsideDamageTick == 0) {
                int damageCount = 10;
                if (game.isFinalPhase()) damageCount = 2;
                if (borderOutsideTick % (damageCount * borderOutsideDamageTick) == 0) {
                    DamageSource damageSource = DamageSource.builder(GutitubowDamageType.OUTSIDE_BORDER).build();
                    damage(damageSource, 1);
                } else {
                    getPlayer().playHurtAnimation(0.0f);
                    world.playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
                }
            }
        }
    }

    public int getTotalHit() {
        return this.playerTotalHit;
    }

    public void setTotalHit(int count) {
        this.playerTotalHit = count;
    }

    public int getTotalKill() {
        return this.playerTotalKill;
    }

    public void setTotalKill(int count) {
        this.playerTotalKill = count;
    }

    public void resetTotalHit() {
        this.setTotalHit(0);
    }

    public int getArrowReloadTick() {
        return this.playerArrowReloadTick;
    }

    public void setArrowReloadTick(int tick) {
        this.playerArrowReloadTick = tick;
    }

    public void resetArrowReloadTick() {
        this.setArrowReloadTick(5 * 20);
    }

    public int getMaxArrowStackSize() {
        return this.playerArrowMaxStackSize;
    }

    public void setMaxArrowStackSize(int size) {
        this.playerArrowMaxStackSize = size;
    }

    public void resetMaxArrowStackSize() {
        this.setMaxArrowStackSize(1);
    }

    public boolean upgradeArrowReloadTick() {
        int reload = this.getArrowReloadTick();

        if (reload > 3 * 20) {
            Player player = getPlayer();
            if (player != null) {
                this.setArrowReloadTick(reload - 20);
                Component message = Component.text("\n[!]").color(NamedTextColor.GOLD)
                        .append(Component.text(" リロード時間短縮").color(NamedTextColor.AQUA)
                                .append(Component.text(" の強化を得た！\n").color(NamedTextColor.WHITE))
                        );
                player.sendMessage(message);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean upgradeMaxArrowStackSize() {
        int size = this.getMaxArrowStackSize();
        if (size < 3) {
            this.setMaxArrowStackSize(size + 1);
            Player player = getPlayer();
            if (player != null) {
                Component message = Component.text("\n[!]").color(NamedTextColor.GOLD)
                        .append(Component.text(" 矢所持数増加").color(NamedTextColor.AQUA)
                                .append(Component.text(" の強化を得た！\n").color(NamedTextColor.WHITE))
                        );
                player.sendMessage(message);
            }
            return true;
        } else {
            return false;
        }
    }

    public int getNearEmptySlot() {
        Player player = getPlayer();
        if (player == null) return -1;
        List<Integer> slots = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 27, 28, 29, 30, 31, 32, 33, 34, 35);
        for (Integer slot : slots) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null || item.getType() == Material.AIR) {
                return slot;
            }
        }
        return -1;
    }

    public boolean hasEmptySlot() {
        return getNearEmptySlot() != -1;
    }

    public int getArrowAmount() {
        Player player = getPlayer();
        if (player == null) return 0;
        int slot = getArrowItemSlot();
        if (slot != -1) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null) return 0;
            if (item.getType() == Material.ARROW) {
                return item.getAmount();
            } else {
                return 0;
            }
        }
        return 0;
    }

    public void setArrowAmount(int amount) {
        Player player = getPlayer();
        if (player == null) return;
        int slot = this.getArrowItemSlot();
        if (slot == -1) slot = this.getCanAddArrowSlot();
        if (slot != -1) getPlayer().getInventory().setItem(slot, getArrowItem(amount));
    }

    public void addArrow(int amount) {
        this.setArrowAmount(getArrowAmount() + amount);
    }

    public void removeArrow(int amount) {
        this.setArrowAmount(getArrowAmount() - amount);
    }

    private @NotNull ItemStack getArrowItem(int amount) {
        ItemStack item;
        if (amount <= 0) {
            item = new ItemStack(Material.BARRIER);
        } else {
            item = new ItemStack(Material.ARROW);
        }
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(gutitubowArrowItemKey, PersistentDataType.BOOLEAN, true);
        meta.displayName(Component.translatable("item.minecraft.arrow").decoration(TextDecoration.ITALIC, false));
        meta.setRarity(ItemRarity.COMMON);

        item.setItemMeta(meta);
        if (amount > 1) item.setAmount(amount);
        ItemUtil.setCantDrop(item, true);
        ItemUtil.setCantMove(item, true);
        return item;
    }

    private int getArrowItemSlot() {
        Player player = getPlayer();
        if (player == null) return -1;
        List<Integer> slots = List.of(8, 7, 6, 5, 4, 3, 2, 1, 0, 35, 34, 33, 32, 31, 30, 29, 28, 27);

        for (Integer slot : slots) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null && item.getItemMeta().getPersistentDataContainer().has(gutitubowArrowItemKey)) {
                return slot;
            }
        }
        return -1;
    }

    private int getCanAddArrowSlot() {
        Player player = getPlayer();
        if (player == null) return -1;
        List<Integer> slots = List.of(8, 7, 6, 5, 4, 3, 2, 1, 0, 35, 34, 33, 32, 31, 30, 29, 28, 27);

        for (Integer slot : slots) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null) {
                return slot;
            }
        }
        return -1;
    }

    public int getRemainedErasureTick() {
        return remainedErasureTick;
    }

    public void setRemainedErasureTick(int tick) {
        if (tick > 0 && remainedErasureTick <= 0) {
            GamePlayerOnAbilityErasureEvent gamePlayerOnAbilityErasureEvent = new GamePlayerOnAbilityErasureEvent(this);
            Bukkit.getPluginManager().callEvent(gamePlayerOnAbilityErasureEvent);
        }
        remainedErasureTick = tick;
    }

    public boolean isErasing() {
        return remainedErasureTick > 0;
    }

    public @Nullable AbilityType getDoppelgangerHoldOriginAbility() {
        return doppelgangerHoldOriginAbility;
    }

    public void setDoppelgangerHoldOriginAbility(@Nullable AbilityType doppelgangerHoldOriginAbility) {
        this.doppelgangerHoldOriginAbility = doppelgangerHoldOriginAbility;
    }

    protected void tick() {
        Player player = getPlayer();
        updateBorderOutside();
        updateArrow();

        if (player != null) {
            // Sneak
            if (player.isSneaking()) {
                sneakingTick++;
                GamePlayerSneakingEvent event = new GamePlayerSneakingEvent(player, sneakingTick);
                Bukkit.getPluginManager().callEvent(event);
            } else {
                sneakingTick = 0;
                compassTeleportingTick = 0;
                if (isReviving()) {
                    revivingTick = 0;
                    player.removePotionEffect(PotionEffectType.GLOWING);
                }
            }

            // Flash
            if (player.hasPotionEffect(PotionEffectType.BLINDNESS) || remainedFlashTick > 0) {
                if (remainedFlashTick > 0) {
                    remainedFlashTick--;

                    if (remainedFlashTick > 20) {
                        PostEffectUtil.applyFlashEffect(player, 1.0f);
                    } else if (remainedFlashTick == 0) {
                        PostEffectUtil.clearFlashEffect(player);
                    } else {
                        float strength = remainedFlashTick / 20.0f;
                        PostEffectUtil.applyFlashEffect(player,  strength);
                    }
                }
                player.getWorld().spawnParticle(
                        Particle.INSTANT_EFFECT,
                        player.getEyeLocation(),
                        3,
                        0.15, 0.15, 0.15,
                        0,
                        new Particle.Spell(Color.fromRGB(255, 255, 255), 1),
                        true
                );
            }

            if (remainedErasureTick > 0) {
                remainedErasureTick--;

            }
        }

        if (firstElytraFlag && player != null) {
            if (((Entity) player).isOnGround()) {
                resetFirstElytra();
                return;
            }
            if (!player.isGliding()) {
                resetFirstElytra();
                return;
            }
            if (!player.isInsideVehicle()) {
                player.setGliding(true);
            }
        }
    }

    private void updateArrow() {
        if (getArrowAmount() < getMaxArrowStackSize()) {
            arrowReloadProgressTick++;

            if (arrowReloadProgressTick >= getArrowReloadTick()) {
                arrowReloadProgressTick = 0;
                addArrow(1);
            }
        }
    }

    public void firstElytra() {
        Player player = getPlayer();
        if (player == null) return;
        ItemStack elytra = ItemStack.of(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        meta.setUnbreakable(true);
        elytra.setItemMeta(meta);
        player.getInventory().setChestplate(elytra);
        player.setGliding(true);

        // 0 tickだとgamePlayer#tick()内でonGroundが更新される前にリセットされてしまうので
        // onGroundが更新されてからフラグを立てる
        Bukkit.getScheduler().runTaskLater(gutitubow, () -> firstElytraFlag = true, 1L);
    }

    public void resetFirstElytra() {
        Player player = getPlayer();
        if (player == null) return;
        getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
        firstElytraFlag = false;
    }

    public int getPotionAmount() {
        Player player = getPlayer();
        if (player == null) return 0;
        Inventory inventory = player.getInventory();
        int amount = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) continue;
            if (itemStack.getType() == Material.POTION) amount++;
        }
        return amount;
    }

    public void updatePlayerBoard() {
        this.playerBoard.update();
    }

    public boolean isFlashing() {
        return remainedFlashTick > 0;
    }

    public void setFlashParticle(int duration) {
        remainedFlashTick = duration;
    }

    public void onDropPotion(PlayerDropItemEvent event) {
        if (game.isStarted() && event.getItemDrop().getItemStack().getType() == Material.POTION) {
            Player player = event.getPlayer();
            GutitubowGameMode gameMode = game.getGameSetting().getGameMode();
            if (!game.getPlayers().contains(this)) return;

            if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                GameTeam team = game.getGameTeam(player);
                if (team == null) {
                    event.setCancelled(true);
                    return;
                }
                List<GamePlayer> aliveTeammates = team.getAlivePlayersWithout(playerUUID);
                if (aliveTeammates.isEmpty()) {
                    if (!team.getPlayersWithout(playerUUID).isEmpty())
                        player.sendMessage(Component.text("チームメイトが全員ダウンしています！！\nダウンしているチームメイトにアイテムを渡すことはできません").color(NamedTextColor.RED));
                    event.setCancelled(true);
                    return;
                }
                for (GamePlayer gamePlayer : aliveTeammates) {
                    Player teammate = gamePlayer.getPlayer();
                    if (teammate == null) continue;
                    GamePlayer gameTeammate = getGame().getGamePlayer(teammate);
                    if (!gameTeammate.hasEmptySlot()) continue;
                    giveItem(teammate, event.getItemDrop().getItemStack());
                    event.getItemDrop().remove();
                    return;
                }
                player.sendMessage(Component.text("チームメイトのインベントリに空きがありません！！").color(NamedTextColor.RED));

            } else if (gameMode == GutitubowGameMode.SOLO) {
                event.setCancelled(true);
            }
        }
    }

    public boolean isReviving() {
        return revivingTick != 0;
    }


    public boolean isTeammate(Player target) {
        return game.isTeammate(playerUUID, target.getUniqueId());
    }

    public boolean isTeammate(UUID target) {
        return game.isTeammate(playerUUID, target);
    }

    public void onSneaking(@NotNull GamePlayerSneakingEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        Material mainHandType = mainHandItem.getType();
        if (mainHandType == Material.BEACON) {
            updateOnUseRespawnBeacon();
        } else if (mainHandType == Material.COMPASS) {
            updateOnUseCompassTeleport();
        }
    }

    public void giveRespawnBeacon() {
        Player player = getPlayer();
        if (player == null) return;
        Inventory inventory = player.getInventory();
        if (inventory.contains(Material.BEACON)) return;
        List<Integer> slots = List.of(8, 7, 6, 5, 4, 3, 2, 1, 0, 35, 34, 33, 32, 31, 30, 29, 28, 27);
        for (Integer slot : slots) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                inventory.setItem(slot, GutitubowItem.RESPAWN_BEACON);
                return;
            }
        }

        for (Integer slot : slots) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && itemStack.getType() == Material.POTION) {
                player.dropItem(slot);
                inventory.setItem(slot, GutitubowItem.RESPAWN_BEACON);
            }
        }
    }


    private void updateOnUseRespawnBeacon() {
        GameTeam team = getTeam();
        if (team == null || team.getDeadPlayers().isEmpty()) return;
        Player player = getPlayer();
        if (player == null) return;

        revivingTick++;
        if (revivingTick == 1) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getUniqueId() == playerUUID) continue;
                Title title = Title.title(
                        Component.empty(),
                        Component.text("[!] " + player.getName() + "が味方の蘇生を開始した！").color(NamedTextColor.GREEN),
                        Title.Times.times(
                                java.time.Duration.ofMillis(500),
                                java.time.Duration.ofSeconds(3),
                                java.time.Duration.ofMillis(500)
                        )
                );
                onlinePlayer.showTitle(title);
                PotionEffect effect = new PotionEffect(PotionEffectType.GLOWING, 15 * 20, 0, true, false, false);
                player.addPotionEffect(effect);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.0f);
            }
        } else if (revivingTick >= REVIVE_TICK) {
            for (GamePlayer gamePlayer : team.getDeadPlayers()) {
                Player deadTeammate = gamePlayer.getPlayer();
                if (deadTeammate == null) continue;
                game.revive(deadTeammate, player);
            }
            Inventory inventory = player.getInventory();
            List<Integer> slots = List.of(8, 7, 6, 5, 4, 3, 2, 1, 0, 35, 34, 33, 32, 31, 30, 29, 28, 27, -106);
            for (int slot : slots) {
                ItemStack itemStack = inventory.getItem(slot);
                if (itemStack != null && itemStack.getType() == Material.BEACON)
                    inventory.setItem(slot, ItemStack.of(Material.AIR));
            }
            revivingTick = 0;
            return;
        }
        Component gauge = ComponentUtil.getGaugeComponent(
                revivingTick,
                REVIVE_TICK,
                NamedTextColor.GREEN,
                NamedTextColor.GRAY
        );
        player.sendActionBar(Component.text("味方復活中... ").color(NamedTextColor.GREEN).append(gauge));
        for (GamePlayer gamePlayer : team.getDeadPlayers()) {
            Player deadTeammate = gamePlayer.getPlayer();
            if (deadTeammate == null || !deadTeammate.isOnline()) continue;
            Title title = Title.title(
                    Component.empty(),
                    Component.text("蘇生されています... ").color(NamedTextColor.GREEN).append(gauge),
                    Title.Times.times(
                            java.time.Duration.ofMillis(0),
                            java.time.Duration.ofSeconds(1),
                            java.time.Duration.ofMillis(0)
                    )
            );
            deadTeammate.showTitle(title);
        }
    }

    private void updateOnUseCompassTeleport() {
        GutitubowGameMode gameMode = getGame().getGameSetting().getGameMode();
        if (!(gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO)) return;
        Player player = getPlayer();
        if (player == null) return;
        compassTeleportingTick++;

        GameTeam gameTeam = getTeam();
        if (gameTeam == null) return;

        if (compassTeleportingTick >= COMPASS_TELEPORT_TICK) {
            compassTeleport();
            compassTeleportingTick = 0;
            return;
        }
        Component message = ComponentUtil.getGaugeComponent(
                Component.text("味方にテレポート中...").color(NamedTextColor.GREEN),
                compassTeleportingTick, COMPASS_TELEPORT_TICK,
                NamedTextColor.GREEN,
                NamedTextColor.GRAY
        );
        if (gameTeam.getAlivePlayersWithout(playerUUID).isEmpty()) {
            message = Component.text("テレポート可能なチームメイトがいません！").color(NamedTextColor.RED);
        }
        player.sendActionBar(message);
    }

    private void compassTeleport() {
        Player player = getPlayer();
        GameTeam gameTeam = getTeam();
        if (gameTeam == null || player == null) return;

        List<GamePlayer> aliveOtherPlayers = gameTeam.getAlivePlayersWithout(playerUUID);
        Player teleportTeammate = null;
        double teleportTeammateDistance = 999999999;
        for (GamePlayer gamePlayer : aliveOtherPlayers) {
            Player teammate = gamePlayer.getPlayer();
            if (teammate == null) continue;
            double distance = player.getLocation().distance(teammate.getLocation());
            if (distance < teleportTeammateDistance) {
                teleportTeammate = teammate;
                teleportTeammateDistance = distance;
            }
        }
        if (teleportTeammate == null) return;
        player.teleport(teleportTeammate);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 1.0f);
        player.sendMessage(Component.text("§6[!] §a味方の位置までテレポートした！"));
        teleportTeammate.sendMessage(Component.text("§6[!] §a味方がテレポートしてきた！"));
    }

    private void giveItem(Player receiver, ItemStack itemStack) {
        GamePlayer receiverGameplayer = game.getGamePlayer(receiver);
        Player player = getPlayer();
        if (player == null) return;
        if (receiverGameplayer.hasEmptySlot()) {
            double distance;
            distance = Math.floor(getPlayer().getLocation().distance(receiver.getLocation()) * 10) / 10;
            player.sendMessage(Component.text("§6[!] §a" + receiver.getName() + "にアイテムを渡した! (" + distance + "m)"));
            receiver.sendMessage(Component.text("§6[!] §a" + getPlayer().getName() + "からアイテムを受け取った! (" + distance + "m)"));
            receiver.give(itemStack);
        } else {
            player.sendMessage(Component.text("チームメイトのインベントリに空きがありません！！").color(NamedTextColor.RED));
        }
    }


    public void damage(final @NotNull DamageSource damageSource, double amount) {
        Player player = getPlayer();
        if (player == null) return;

        Player attacker = damageSource.getDirectPlayer();

        if (attacker != null) {
            GutitubowGameMode gameMode = game.getGameSetting().getGameMode();
            if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                if (game.isTeammate(player, attacker)) {
                    return;
                }
            }
        }

        Component distanceSuffix = Component.text(" §r§6(" + Math.floor(damageSource.getDistance() * 10) / 10 + "m)");
        Component hitMessage = Component.text("§cHIT §6ᐅ §f").append(player.displayName().color(NamedTextColor.WHITE)).append(distanceSuffix);

        GamePlayerPreDamageEvent event = new GamePlayerPreDamageEvent(
                player,
                amount,
                damageSource,
                hitMessage
        );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        double newAmount = event.getAmount();

        if (player.getHealth() + player.getAbsorptionAmount() <= newAmount) {
            boolean success = game.kill(this, damageSource);
            if (success) {
                onDeath();
                if (attacker != null) {
                    GamePlayer attackerGamePlayer = getGame().getGamePlayer(attacker);
                    GamePlayerKillEvent killEvent = new GamePlayerKillEvent(attacker, player, attackerGamePlayer, this, damageSource, Component.text("You killed it!").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true));
                    Bukkit.getPluginManager().callEvent(killEvent);

                    attackerGamePlayer.onKill(killEvent);
                }
            }
        } else {
            player.damage(newAmount);
            GamePlayerDamageEvent damageEvent = new GamePlayerDamageEvent(player, newAmount, damageSource, event.getHitMessage());
            Bukkit.getPluginManager().callEvent(damageEvent);
            if (attacker != null) {
                GamePlayer attackerGamePlayer = getGame().getGamePlayer(attacker);
                attackerGamePlayer.onDealDamage(damageEvent);
            }
        }
    }

    private void onDealDamage(GamePlayerDamageEvent event) {
        Player player = getPlayer();
        if (player == null) return;

        setTotalHit(getTotalHit() + 1);
        if (getTotalHit() % 2 == 0) {
            Random random = new Random();
            if (random.nextBoolean()) {
                if (!upgradeArrowReloadTick()) {
                    upgradeMaxArrowStackSize();
                }
            } else {
                if (!upgradeMaxArrowStackSize()) {
                    upgradeArrowReloadTick();
                }
            }
        }

        player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
        if (event.getHitMessage() != null) {
            Title title = Title.title(
                    Component.empty(),
                    event.getHitMessage(),
                    Title.Times.times(
                            java.time.Duration.ofMillis(100),
                            java.time.Duration.ofMillis(1250),
                            java.time.Duration.ofMillis(500)
                    )
            );
            player.showTitle(title);
        }
    }

    private void onKill(GamePlayerKillEvent event) {
        Player player = event.getPlayer();

        setTotalKill(getTotalKill() + 1);
        Component subtitle = event.getKillMessage();
        Title title = Title.title(
                Component.empty(),
                subtitle,
                Title.Times.times(
                        java.time.Duration.ofMillis(100),
                        java.time.Duration.ofMillis(1250),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);
    }

    private void onDeath() {
        Player player = getPlayer();
        if (player == null) return;

        World world = player.getWorld();
        Location location = player.getLocation();
        world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 3);
        world.strikeLightningEffect(location);
        Title title = Title.title(
                Component.text("You Died!").color(NamedTextColor.RED),
                Component.empty(),
                Title.Times.times(
                        java.time.Duration.ofMillis(500),
                        java.time.Duration.ofSeconds(3),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(title);
        for (int i = 0; i < getPotionAmount(); i++) {
            world.dropItem(location, GutitubowItem.RECOVERY_POTION);
        }
        player.getInventory().remove(Material.POTION);

        if (game.isStarted()) {
            player.setGameMode(GameMode.SPECTATOR);

            GutitubowGameMode gameMode = game.getGameSetting().getGameMode();
            if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                GameTeam team = getTeam();
                if (team != null) {

                    boolean isEliminated = team.isEliminated();
                    for (GamePlayer gamePlayer : team.getPlayersWithout(playerUUID)) {
                        Player teammate = gamePlayer.getPlayer();
                        if (teammate != null) {
                            teammate.sendTitlePart(TitlePart.TIMES, Title.Times.times(
                                    java.time.Duration.ofMillis(500),
                                    java.time.Duration.ofSeconds(3),
                                    java.time.Duration.ofMillis(500)
                            ));
                            if (isEliminated) {
                                title = Title.title(
                                        Component.text("Team Eliminated!").color(NamedTextColor.RED),
                                        Component.empty(),
                                        Title.Times.times(
                                                java.time.Duration.ofMillis(500),
                                                java.time.Duration.ofSeconds(3),
                                                java.time.Duration.ofMillis(500)
                                        )
                                );
                                teammate.showTitle(title);
                            } else {
                                title = Title.title(
                                        Component.empty(),
                                        Component.text("仲間がダウンした！").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true),
                                        Title.Times.times(
                                                java.time.Duration.ofMillis(500),
                                                java.time.Duration.ofSeconds(3),
                                                java.time.Duration.ofMillis(500)
                                        )
                                );
                                teammate.showTitle(title);
                                teammate.sendMessage(Component.text("\n[!] ケアパッケージを獲得して味方を蘇生せよ！\n").color(NamedTextColor.AQUA));
                            }
                        }
                    }
                }
            }

        } else {
            double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
            player.setHealth(maxHealth);
        }
    }
}