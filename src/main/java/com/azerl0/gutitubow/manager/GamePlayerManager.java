package com.azerl0.gutitubow.manager;

import com.azerl0.gutitubow.data.GutitubowDamageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GamePlayerManager {
    private final JavaPlugin plugin;
    private final NamespacedKey playerArrowReloadTickKey;
    private final NamespacedKey playerArrowMaxStackSizeKey;
    private final NamespacedKey playerTotalHitKey;
    private final NamespacedKey gutitubowArrowItemKey;
    private final Map<UUID, Integer> playerBorderOutsideTimers = new HashMap<>();
    private final Map<UUID, GamePlayer> gamePlayers = new HashMap<>();


    public GamePlayerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        playerArrowReloadTickKey = new NamespacedKey(plugin, "gameplayer.arrowReloadTick");
        playerArrowMaxStackSizeKey = new NamespacedKey(plugin, "gameplayer.arrowMaxStackSize");
        playerTotalHitKey = new NamespacedKey(plugin, "player.totalHIt");
        gutitubowArrowItemKey = new NamespacedKey(plugin, "gutitubow.arrow");
    }

    public GamePlayer getGamePlayer(Entity player) {
        if (!gamePlayers.containsKey(player.getUniqueId())) {
            gamePlayers.put(player.getUniqueId(), new GamePlayer(player));
        }
        return gamePlayers.get(player.getUniqueId());

    }


    public class GamePlayer {
        private final Entity player;
        private BukkitTask borderOutsideTimerTask = null;
        private BukkitTask arrowReloadTask = null;
        private BukkitTask firstElytraTask = null;


        public GamePlayer(Entity player) {
            this.player = player;
        }

        public void damage(double amount, Entity entity, double distance) {
            if (!(this.player instanceof LivingEntity livingEntity)) return;

            if (livingEntity.getHealth() <= amount) {
                this.kill(entity, distance);

            } else {
                damage(amount);
                LogManager.broadcastHitLog(entity, this.player, distance);
            }
        }

        public void damage(double amount, GutitubowDamageType damageType) {
            if (!(this.player instanceof LivingEntity livingEntity)) return;

            if (livingEntity.getHealth() <= amount) {
                this.death(damageType, false);

            } else {
                damage(amount);
            }

        }

        public void damage(double amount, Entity entity) {
            double distance = entity.getLocation().distance(this.player.getLocation());
            damage(amount, entity, distance);

        }

        public void damage(double amount) {
            ((LivingEntity) this.player).damage(amount);
        }

        public void kill(Entity attacker, double distance) {
            this.death(GutitubowDamageType.ARROW_SHOT, true);
            LogManager.broadcastKillLog(attacker, this.player, distance);
        }

        public void death(GutitubowDamageType damageType, boolean hideLog) {

            World world = this.player.getWorld();
            Location location = this.player.getLocation();
            world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 3);
            world.strikeLightningEffect(location);
            if (this.player instanceof Player _player) {
                _player.setGameMode(GameMode.SPECTATOR);
                if (hideLog) return;
                if (damageType == GutitubowDamageType.OUTSIDE_BORDER) {
                    LogManager.broadcastDeathLog(_player);
                }
            } else if (this.player instanceof LivingEntity livingEntity) {
                livingEntity.damage(999999);
            }

        }

        public void death(GutitubowDamageType damageType) {
            this.death(damageType, false);
        }


        private int getBorderOutsideTicks() {
            if (!(this.player instanceof Player)) return 0;
            return playerBorderOutsideTimers.getOrDefault(this.player.getUniqueId(), 0);
        }

        private void setBorderOutsideTicks(int ticks) {
            if (!(this.player instanceof Player)) return;
            playerBorderOutsideTimers.put(this.player.getUniqueId(), ticks);
        }

        public void borderOutside() {
            if (!(this.player instanceof Player player)) return;

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
                            damage(1, GutitubowDamageType.OUTSIDE_BORDER);
                        } else {
                            ((LivingEntity) player).playHurtAnimation(0.0f);
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
                        }
                    }

                }

            }.runTaskTimer(plugin, 0L, 1L);
        }

        public int getTotalHit() {
            if (!this.player.getPersistentDataContainer().has(playerTotalHitKey)) this.resetTotalHit();
            return this.player.getPersistentDataContainer().getOrDefault(playerTotalHitKey, PersistentDataType.INTEGER, 0);
        }

        public void setTotalHit(int count) {
            this.player.getPersistentDataContainer().set(playerTotalHitKey, PersistentDataType.INTEGER, count);
        }

        public void resetTotalHit() {
            this.setTotalHit(0);
        }

        public int getArrowReloadTick() {
            if (!this.player.getPersistentDataContainer().has(playerArrowReloadTickKey)) this.resetArrowReloadTick();
            return this.player.getPersistentDataContainer().getOrDefault(playerArrowReloadTickKey, PersistentDataType.INTEGER, 0);
        }

        public void setArrowReloadTick(int tick) {
            this.player.getPersistentDataContainer().set(playerArrowReloadTickKey, PersistentDataType.INTEGER, tick);
        }

        public void resetArrowReloadTick() {
            this.setArrowReloadTick(5 * 20);
        }

        public int getMaxArrowStackSize() {
            if (!this.player.getPersistentDataContainer().has(playerArrowMaxStackSizeKey))
                this.resetMaxArrowStackSize();
            return this.player.getPersistentDataContainer().getOrDefault(playerArrowMaxStackSizeKey, PersistentDataType.INTEGER, 0);
        }

        public void setMaxArrowStackSize(int size) {
            this.player.getPersistentDataContainer().set(playerArrowMaxStackSizeKey, PersistentDataType.INTEGER, size);
        }

        public void resetMaxArrowStackSize() {
            this.setMaxArrowStackSize(1);
        }

        public boolean upgradeArrowReloadTick() {
            int reload = this.getArrowReloadTick();
            if (reload > 3 * 20) {
                this.setArrowReloadTick(reload - 20);
                Component message = Component.text("リロード時間短縮");
                this.player.sendMessage(message);
                return true;
            } else {
                return false;
            }
        }

        public boolean upgradeMaxArrowStackSize() {
            int size = this.getMaxArrowStackSize();
            if (size < 3) {
                this.setMaxArrowStackSize(size + 1);
                Component message = Component.text("矢最大保持数の強化");
                this.player.sendMessage(message);
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
            if (!(player instanceof Player)) return;
            Player player = (Player) this.player;
            int slot = this.getArrowItemSlot();
            if (slot == -1) slot = this.getCanAddArrowSlot();
            if (slot != -1) player.getInventory().setItem(slot, getArrowItem(amount));
            this.updateArrow();

        }

        public void addArrow(int amount) {
            this.setArrowAmount(getArrowAmount() + amount);
        }

        public void addArrow() {
            this.addArrow(1);
        }

        public void removeArrow(int amount) {
            this.setArrowAmount(getArrowAmount() - amount);
        }

        public void removeArrow() {
            removeArrow(1);
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
            if (!(player instanceof Player)) return 0;
            Player player = (Player) this.player;
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
            if (!(player instanceof Player)) return 0;
            Player player = (Player) this.player;
            List<Integer> slots = List.of(8, 7, 6, 5, 4, 3, 2, 1, 0, 35, 34, 33, 32, 31, 30, 29, 28, 27);

            for (Integer slot : slots) {
                ItemStack item = player.getInventory().getItem(slot);
                if (item == null) {
                    return slot;
                }
            }
            return -1;
        }

        public void updateArrow() {
            if (!(this.player instanceof Player player)) return;

            if (this.getArrowAmount() >= this.getMaxArrowStackSize()) {
                arrowReloadTask = null;
                return;
            }


            if (arrowReloadTask != null ) return;

            arrowReloadTask = new BukkitRunnable() {
                @Override
                public void run() {
                    GamePlayer gamePlayer = getGamePlayer(player);

                    if (gamePlayer.getArrowAmount() >= gamePlayer.getMaxArrowStackSize()) {
                        cancel();
                        arrowReloadTask = null;
                        return;
                    }

                    if (!player.isOnline()) {
                        cancel();
                        arrowReloadTask = null;
                        return;
                    }

                    gamePlayer.addArrow(1);
                }
            }.runTaskTimer(plugin, this.getArrowReloadTick(), getArrowReloadTick());
        }

        public void firstElytra() {
            if (!(this.player instanceof Player pl)) return;

            ItemStack elytra = ItemStack.of(Material.ELYTRA);
            ItemMeta meta = elytra.getItemMeta();
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.setUnbreakable(true);
            elytra.setItemMeta(meta);
            pl.getInventory().setChestplate(elytra);

            if (firstElytraTask != null && !firstElytraTask.isCancelled()) return;
            firstElytraTask = new BukkitRunnable() {
                private boolean startedGliding = false;

                @Override
                public void run() {
                    if (pl.isGliding()) {
                        startedGliding = true;
                        return;
                    }
                    if (startedGliding) {
                        pl.getInventory().setChestplate(new ItemStack(Material.AIR));
                        this.cancel();
                    }
                    pl.setGliding(true);
                }
            }.runTaskTimer(plugin, 1L, 1L);
        }
    }
}
