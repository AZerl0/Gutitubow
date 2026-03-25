package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.data.GutitubowDamageType;
import com.azerl0.gutitubow.game.scoreboard.PlayerBoard;
import com.azerl0.gutitubow.team.GameTeam;
import com.azerl0.gutitubow.util.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
                        game.damage(getPlayer(), 1, GutitubowDamageType.OUTSIDE_BORDER);
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

    private ItemStack getArrowItem(int amount) {
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
        if (getPlayer() == null) return;
        ItemStack elytra = ItemStack.of(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        meta.setUnbreakable(true);
        elytra.setItemMeta(meta);
        getPlayer().getInventory().setChestplate(elytra);
        getPlayer().setGliding(true);

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
}