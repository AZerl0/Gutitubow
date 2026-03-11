package com.azerl0.gutitubow.game;

import com.azerl0.gutitubow.ability.Ability;
import com.azerl0.gutitubow.data.GutitubowDamageType;
import com.azerl0.gutitubow.game.scoreboard.PlayerBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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

public class GamePlayer {
    private final JavaPlugin plugin;
    private final Player player;
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
    private PlayerBoard playerBoard;
    private BukkitTask flashParticleTask = null;



    public GamePlayer(JavaPlugin plugin, Player player, Game game) {
        this.plugin = plugin;
        this.player = player;
        this.game = game;
        this.playerArrowReloadTick = 100;
        this.playerArrowMaxStackSize = 1;
        this.playerTotalHit = 0;
        this.playerTotalKill = 0;
        this.playerBorderOutsideTick = 0;
        gutitubowArrowItemKey = new NamespacedKey(this.plugin, "gutitubow.arrow");
        this.playerBoard = game.getPlayerBoard(this);
    }

    @Nullable
    public Ability getAbility() {
        return game.getAbilityManager().getAbility(this.player);
    }

    public Player getPlayer() {
        return this.player;
    }

    public Game getGame() {
        return this.game;
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
                if (player.getWorld().getWorldBorder().isInside(player.getLocation()) || player.getGameMode() != GameMode.ADVENTURE) {
                    setBorderOutsideTicks(0);
                    this.cancel();
                    return;
                }

                int borderOutsideTick = getBorderOutsideTicks() + 1;
                setBorderOutsideTicks(borderOutsideTick);
                int borderOutsideDamageTick = 30;
                if (borderOutsideTick % borderOutsideDamageTick == 0) {
                    if (borderOutsideTick % (10 * borderOutsideDamageTick) == 0) {
                        game.damage(player, 1, GutitubowDamageType.OUTSIDE_BORDER);
                    } else {
                        player.playHurtAnimation(0.0f);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
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
            Component message = Component.text("[!]").color(NamedTextColor.GOLD)
                    .append(Component.text(" リロード時間短縮").color(NamedTextColor.AQUA)
                            .append(Component.text(" の強化を得た！").color(NamedTextColor.WHITE))
                    );
            this.player.sendMessage(Component.empty());
            this.player.sendMessage(message);
            this.player.sendMessage(Component.empty());
            return true;
        } else {
            return false;
        }
    }

    public boolean upgradeMaxArrowStackSize() {
        int size = this.getMaxArrowStackSize();
        if (size < 3) {
            this.setMaxArrowStackSize(size + 1);
            Component message = Component.text("[!]").color(NamedTextColor.GOLD)
                    .append(Component.text(" 矢所持数増加").color(NamedTextColor.AQUA)
                            .append(Component.text(" の強化を得た！").color(NamedTextColor.WHITE))
                    );
            this.player.sendMessage(Component.empty());
            this.player.sendMessage(message);
            this.player.sendMessage(Component.empty());
            return true;
        } else {
            return false;
        }
    }

    public int getArrowAmount() {
        if (!(this.player instanceof Player _player)) return 0;
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
        int slot = this.getArrowItemSlot();
        if (slot == -1) slot = this.getCanAddArrowSlot();
        if (slot != -1) this.player.getInventory().setItem(slot, getArrowItem(amount));
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
        return item;
    }

    private int getArrowItemSlot() {
        List<Integer> slots = List.of(8, 7, 6, 5, 4, 3, 2, 1, 0, 35, 34, 33, 32, 31, 30, 29, 28, 27);

        for (Integer slot : slots) {
            ItemStack item = this.player.getInventory().getItem(slot);
            if (item != null && item.getItemMeta().getPersistentDataContainer().has(gutitubowArrowItemKey)) {
                return slot;
            }
        }
        return -1;
    }

    private int getCanAddArrowSlot() {
        List<Integer> slots = List.of(8, 7, 6, 5, 4, 3, 2, 1, 0, 35, 34, 33, 32, 31, 30, 29, 28, 27);

        for (Integer slot : slots) {
            ItemStack item = this.player.getInventory().getItem(slot);
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
                if (!player.isOnline()) {
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

        ItemStack elytra = ItemStack.of(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        meta.setUnbreakable(true);
        elytra.setItemMeta(meta);
        this.player.getInventory().setChestplate(elytra);

        if (firstElytraTask != null && !firstElytraTask.isCancelled()) return;
        firstElytraTask = new BukkitRunnable() {
            private boolean startedGliding = false;

            @Override
            public void run() {
                if (player.isGliding()) {
                    startedGliding = true;
                    return;
                }
                if (startedGliding) {
                    resetFirstElytra();
                }
                player.setGliding(true);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void resetFirstElytra() {
        if (this.firstElytraTask == null) return;
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        this.firstElytraTask.cancel();
    }

    public void updatePlayerBoard() {
        this.playerBoard.update();
    }

    public void setFlashParticleTask() {
        if (flashParticleTask != null && !flashParticleTask.isCancelled()) return;

        flashParticleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    this.cancel();
                    flashParticleTask = null;
                    return;
                }
                player.getWorld().spawnParticle(Particle.INSTANT_EFFECT, player.getEyeLocation(),3, 0.15, 0.15, 0.15, new Particle.Spell(Color.fromRGB(255, 255, 255), 1));
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}