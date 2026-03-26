package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.data.GutitubowDamageType;
import com.azerl0.gutitubow.data.item.GutitubowItem;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerDamageEvent;
import com.azerl0.gutitubow.event.gameplayer.GamePlayerSneakingEvent;
import com.azerl0.gutitubow.game.scoreboard.PlayerBoard;
import com.azerl0.gutitubow.manager.LogManager;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.util.ComponentUtil;
import com.azerl0.gutitubow.util.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class GamePlayer {
    private final JavaPlugin plugin;
    private final UUID playerUUID;
    private final Game game;
    private BukkitTask borderOutsideTimerTask = null;
    private BukkitTask arrowReloadTask = null;
    private BukkitTask firstElytraTask = null;
    private int playerArrowReloadTick;
    private int playerArrowMaxStackSize;
    private int playerTotalHit;
    private int playerTotalKill;
    private int playerBorderOutsideTick;
    private final NamespacedKey gutitubowArrowItemKey;
    private final PlayerBoard playerBoard;
    private BukkitTask flashParticleTask = null;
    private BukkitTask sneakingTask = null;
    private final int REVIVE_TICK = 15 * 20;
    private final int COMPASS_TELEPORT_TICK = 10 * 20;
    private int sneakingTick = 0;
    private int revivingTick = 0;
    private int compassTeleportingTick = 0;
    private int remainedFlashTick;



    public GamePlayer(JavaPlugin plugin, Player player, Game game) {
        this.plugin = plugin;
        this.playerUUID = player.getUniqueId();
        this.game = game;
        this.playerArrowReloadTick = 100;
        this.playerArrowMaxStackSize = 1;
        this.playerTotalHit = 0;
        this.playerTotalKill = 0;
        this.playerBorderOutsideTick = 0;
        gutitubowArrowItemKey = new NamespacedKey(this.plugin, "gutitubow.arrow");
        this.playerBoard = game.getPlayerBoard(this);
        this.remainedFlashTick = 0;
    }

    @Nullable
    public Ability getAbility() {
        if (getPlayer() == null) return null;
        return game.getAbilityManager().getAbility(getPlayer());
    }

    @Nullable public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public Game getGame() {
        return this.game;
    }

    @Nullable public GameTeam getTeam() {
        if (getPlayer() == null) return null;
        return game.getTeamManager().getPlayerTeam(getPlayer());
    }

    private int getBorderOutsideTicks() {
        return this.playerBorderOutsideTick;
    }

    private void setBorderOutsideTicks(int ticks) {
        this.playerBorderOutsideTick = ticks;
    }

    public void borderOutside() {

        if (borderOutsideTimerTask != null && !borderOutsideTimerTask.isCancelled()) return;

        borderOutsideTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                World world = getPlayer().getWorld();
                if (world.getWorldBorder().isInside(getPlayer().getLocation()) || getPlayer().getGameMode() != GameMode.ADVENTURE) {
                    setBorderOutsideTicks(0);
                    this.cancel();
                    return;
                }

                int borderOutsideTick = getBorderOutsideTicks() + 1;
                setBorderOutsideTicks(borderOutsideTick);
                int borderOutsideDamageTick = 30;
                if (borderOutsideTick % borderOutsideDamageTick == 0) {
                    if (borderOutsideTick % (10 * borderOutsideDamageTick) == 0) {
                        damage(getPlayer(), 1, GutitubowDamageType.OUTSIDE_BORDER);
                    } else {
                        getPlayer().playHurtAnimation(0.0f);
                        world.playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
                    }
                }

            }

        }.runTaskTimer(plugin, 0L, 1L);
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
        this.updateArrow();
    }

    public void resetMaxArrowStackSize() {
        this.setMaxArrowStackSize(1);
    }

    public boolean upgradeArrowReloadTick() {
        int reload = this.getArrowReloadTick();
        if (reload > 3 * 20) {
            this.setArrowReloadTick(reload - 20);
            Component message = Component.text("\n[!]").color(NamedTextColor.GOLD)
                    .append(Component.text(" リロード時間短縮").color(NamedTextColor.AQUA)
                            .append(Component.text(" の強化を得た！\n").color(NamedTextColor.WHITE))
                    );
            if (getPlayer() != null) getPlayer().sendMessage(message);
            return true;
        } else {
            return false;
        }
    }

    public boolean upgradeMaxArrowStackSize() {
        int size = this.getMaxArrowStackSize();
        if (size < 3) {
            this.setMaxArrowStackSize(size + 1);
            Component message = Component.text("\n[!]").color(NamedTextColor.GOLD)
                    .append(Component.text(" 矢所持数増加").color(NamedTextColor.AQUA)
                            .append(Component.text(" の強化を得た！\n").color(NamedTextColor.WHITE))
                    );
            if (getPlayer() != null) getPlayer().sendMessage(message);
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
        if (!(getPlayer() instanceof Player _player)) return 0;
        int slot = getArrowItemSlot();
        if (slot != -1) {
            ItemStack item = _player.getInventory().getItem(slot);
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
        if (getPlayer() == null) return;
        int slot = this.getArrowItemSlot();
        if (slot == -1) slot = this.getCanAddArrowSlot();
        if (slot != -1) getPlayer().getInventory().setItem(slot, getArrowItem(amount));
        this.updateArrow();
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
        if (getPlayer() == null) return -1;
        List<Integer> slots = List.of(8, 7, 6, 5, 4, 3, 2, 1, 0, 35, 34, 33, 32, 31, 30, 29, 28, 27);

        for (Integer slot : slots) {
            ItemStack item = getPlayer().getInventory().getItem(slot);
            if (item == null) {
                return slot;
            }
        }
        return -1;
    }

    public void updateArrow() {

        if (arrowReloadTask != null ) return;

        arrowReloadTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (getPlayer() == null || !getPlayer().isOnline()) {
                    cancel();
                    arrowReloadTask = null;
                    return;
                }

                if (getArrowAmount() >= getMaxArrowStackSize()) {
                    cancel();
                    arrowReloadTask = null;
                    return;
                }

                addArrow(1);
            }
        }.runTaskTimer(plugin, getArrowReloadTick(), getArrowReloadTick());
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

        if (firstElytraTask != null && !firstElytraTask.isCancelled()) return;
        firstElytraTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (((Entity) getPlayer()).isOnGround()) {
                    resetFirstElytra();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void resetFirstElytra() {
        if (this.firstElytraTask == null || getPlayer() == null) return;
        getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
        this.firstElytraTask.cancel();
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
        setFlashParticleTask();
    }

    public void setFlashParticleTask() {
        if (flashParticleTask != null && !flashParticleTask.isCancelled()) return;

        flashParticleTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (player == null) return;
                if (!player.isOnline() || (!player.hasPotionEffect(PotionEffectType.BLINDNESS) && remainedFlashTick <= 0)) {
                    this.cancel();
                    flashParticleTask = null;
                    return;
                }
                if (remainedFlashTick > 0) remainedFlashTick--;
                player.getWorld().spawnParticle(
                        Particle.INSTANT_EFFECT,
                        player.getEyeLocation(),
                        3,
                        0.15, 0.15, 0.15,
                        new Particle.Spell(Color.fromRGB(255, 255, 255), 1)
                );
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void onDropPotion(PlayerDropItemEvent event) {
        if (game.isStarted() && event.getItemDrop().getItemStack().getType() == Material.POTION) {
            Player player = event.getPlayer();
            GutitubowGameMode gameMode = game.getGameMode();
            if (!game.getPlayers().contains(player.getUniqueId())) return;

            if (gameMode == GutitubowGameMode.DUO || gameMode == GutitubowGameMode.TRIO) {
                GameTeam team = game.getGameTeam(player);
                if (team == null) {
                    event.setCancelled(true);
                    return;
                }
                List<UUID> aliveTeammates = team.getAlivePlayersWithout(playerUUID);
                if (aliveTeammates.isEmpty()) {
                    if (!team.getPlayersWithout(playerUUID).isEmpty())
                        player.sendMessage(Component.text("チームメイトが全員ダウンしています！！\nダウンしているチームメイトにアイテムを渡すことはできません").color(NamedTextColor.RED));
                    event.setCancelled(true);
                    return;
                }
                for (UUID uuid : aliveTeammates) {
                    Player teammate = Bukkit.getPlayer(uuid);
                    if (teammate == null) continue;
                    GamePlayer gameTeammate = getGame().getGamePlayer(teammate);
                    if (!gameTeammate.hasEmptySlot()) continue;
                    giveItem(teammate, event.getItemDrop().getItemStack(), true, true);
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

    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            if (sneakingTask != null && !sneakingTask.isCancelled()) return;

            sneakingTask = new BukkitRunnable() {
                @Override
                public void run() {
                    sneakingTick++;
                    GamePlayerSneakingEvent event = new GamePlayerSneakingEvent(player, sneakingTick);
                    Bukkit.getPluginManager().callEvent(event);
                }
            }.runTaskTimer(plugin, 0L, 1L);
        } else {
            if (sneakingTask != null) sneakingTask.cancel();
            sneakingTask = null;
            sneakingTick = 0;
            compassTeleportingTick = 0;
            if (isReviving()) {
                revivingTick = 0;
                player.removePotionEffect(PotionEffectType.GLOWING);
            }
        }
    }

    public boolean isTeammate(Player target) {
        return  game.isTeammate(getPlayer(), target);
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
        Player player = getPlayer();
        if (player == null) return;

        GameTeam team = getTeam();
        if (team == null || team.getDeadPlayers().isEmpty()) return;

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
        }
        if (revivingTick >= REVIVE_TICK) {
            for (UUID uuid : team.getDeadPlayers()) {
                Player deadTeammate = Bukkit.getPlayer(uuid);
                if (deadTeammate == null || !deadTeammate.isOnline()) continue;
                GamePlayer deadTeammateGameplayer = getGame().getGamePlayer(deadTeammate);
                deadTeammateGameplayer.revive(player);
            }
            player.getInventory().remove(Material.BEACON);
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
        for (UUID uuid : team.getDeadPlayers()) {
            Player deadTeammate = Bukkit.getPlayer(uuid);
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
        GutitubowGameMode gameMode = getGame().getGameMode();
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
        List<UUID> aliveOtherPlayers = gameTeam.getAlivePlayersWithout(playerUUID);
        if (aliveOtherPlayers.isEmpty()) {
            message = Component.text("テレポート可能なチームメイトがいません！").color(NamedTextColor.RED);
        }
        player.sendActionBar(message);
    }

    private void compassTeleport() {
        Player player = getPlayer();
        if (player == null) return;
        GameTeam gameTeam = getTeam();
        if (gameTeam == null) return;

        List<UUID> aliveOtherPlayers = gameTeam.getAlivePlayersWithout(playerUUID);
        Player teleportTeammate = null;
        double teleportTeammateDistance = 999999999;
        for (UUID uuid : aliveOtherPlayers) {
            Player teammate = Bukkit.getPlayer(uuid);
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

    private void giveItem(Player receiver, ItemStack itemStack, boolean showReceiverMessage, boolean showSenderMessage) {
        GamePlayer receiverGameplayer = game.getGamePlayer(receiver);
        if (receiverGameplayer.hasEmptySlot()) {
            double distance = 0;
            if (getPlayer() != null) distance = Math.floor(getPlayer().getLocation().distance(receiver.getLocation()) * 10) / 10;
            if (showSenderMessage && getPlayer() != null) getPlayer().sendMessage(Component.text("§6[!] §a" + receiver.getName() + "にアイテムを渡した! (" + distance + "m)"));
            if (showReceiverMessage && getPlayer() != null) receiver.sendMessage(Component.text("§6[!] §a" + getPlayer().getName() + "からアイテムを受け取った! (" + distance + "m)"));
            receiver.give(itemStack);
        } else {
            if (showSenderMessage && getPlayer() != null) getPlayer().sendMessage(Component.text("チームメイトのインベントリに空きがありません！！").color(NamedTextColor.RED));
        }
    }


    public void damage(@NotNull Player player, double amount, Player attacker, double distance, boolean canUpgrade) {


        GamePlayerDamageEvent event = new GamePlayerDamageEvent(player, amount, attacker, GutitubowDamageType.ARROW_SHOT);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        double newAmount = event.getAmount();
        GamePlayer gamePlayer = game.getGamePlayer(attacker);
        if (game.getGameMode() == GutitubowGameMode.DUO || game.getGameMode() == GutitubowGameMode.TRIO) {
            if (game.isTeammate(player, attacker)) {
                return;
            }
        }

        if (player.getHealth() <= newAmount) {
            this.kill(player, attacker, distance);

        } else {
            player.damage(newAmount);
            if (canUpgrade) {
                gamePlayer.setTotalHit(gamePlayer.getTotalHit() + 1);
                if (gamePlayer.getTotalHit() % 2 == 0) {
                    Random random = new Random();
                    if (random.nextBoolean()) {
                        if (!gamePlayer.upgradeArrowReloadTick()) {
                            gamePlayer.upgradeMaxArrowStackSize();
                        }
                    } else {
                        if (!gamePlayer.upgradeMaxArrowStackSize()) {
                            gamePlayer.upgradeArrowReloadTick();
                        }
                    }
                }
            }
            attacker.playSound(attacker, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
            Title title = Title.title(
                    Component.empty(),
                    Component.text("§cHIT §6§bᐅ §f" + player.getName() + "§r§6 (" + Math.floor(distance * 10) / 10 + "m)").color(NamedTextColor.LIGHT_PURPLE),
                    Title.Times.times(
                            java.time.Duration.ofMillis(100),
                            java.time.Duration.ofMillis(1250),
                            java.time.Duration.ofMillis(500)
                    )
            );
            attacker.showTitle(title);
            LogManager.broadcastHitLog(attacker, player, distance);
        }
    }

    public void damage(Player player, double amount, GutitubowDamageType damageType) {

        GamePlayerDamageEvent event = new GamePlayerDamageEvent(player, amount, damageType);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        if (player.getHealth() <= amount) {
            this.death(player, damageType, false);

        } else {
            player.damage(amount);
        }
    }

    public void damage(@NotNull Player player, double amount, Player attacker, boolean canUpgrade) {
        double distance = player.getLocation().distance(attacker.getLocation());
        damage(player, amount, attacker, distance, canUpgrade);
    }

    public void kill(Player player, Player attacker, double distance) {
        GamePlayer gamePlayer = game.getGamePlayer(attacker);
        gamePlayer.setTotalKill(gamePlayer.getTotalKill() + 1);
        LogManager.broadcastKillLog(attacker, player, distance);
        this.death(player, GutitubowDamageType.ARROW_SHOT, true);
    }

    public void death(Player player, GutitubowDamageType damageType, boolean hideLog) {
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

        if (game.isStarted()) {
            player.setGameMode(GameMode.SPECTATOR);
            game.addDeadPlayer(player);

            if (game.getGameMode() == GutitubowGameMode.DUO || game.getGameMode() == GutitubowGameMode.TRIO) {
                GameTeam team = getTeam();
                if (team != null) {

                    boolean isEliminated = team.isEliminated();
                    for (UUID uuid : team.getPlayersWithout(playerUUID)) {
                        Player teammate = Bukkit.getPlayer(uuid);
                        if (teammate != null) {
                            teammate.sendTitlePart(TitlePart.TIMES, Title.Times.times(
                                    java.time.Duration.ofMillis(500),
                                    java.time.Duration.ofSeconds(3),
                                    java.time.Duration.ofMillis(500)
                            ));
                            if (isEliminated) {
                                teammate.sendTitlePart(TitlePart.TITLE, Component.text("Team Eliminated!").color(NamedTextColor.RED));
                            } else {
                                teammate.sendTitlePart(TitlePart.SUBTITLE, Component.text("仲間がダウンした！").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true));
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
        if (!hideLog) {
            if (damageType == GutitubowDamageType.OUTSIDE_BORDER) {
                LogManager.broadcastDeathLog(player);
            }
        }
        if (game.isStarted()) game.checkGameOver();
    }

    public void revive(Player reviver) {
        Player player = getPlayer();
        if (player == null || !player.isOnline()) return;
        World world = player.getWorld();

        game.addRevivePlayer(player);
        player.setGameMode(GameMode.ADVENTURE);
        if (player.getUniqueId() != reviver.getUniqueId()) player.teleport(reviver);
        LogManager.broadcastReviveLog(player, reviver);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);

        Firework firework = world.spawn(player.getLocation().add(0.0, 1.5, 0.0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BALL).build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);
    }
}