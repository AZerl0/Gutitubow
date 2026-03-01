package com.azerl0.gutitubow.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GamePlayerManager {
    private final NamespacedKey playerArrowReloadTickKey;
    private final NamespacedKey playerArrowMaxStackSizeKey;
    private final NamespacedKey playerTotalHitKey;
    private final NamespacedKey gutitubowArrowItemKey;

    public GamePlayerManager(JavaPlugin plugin) {
        playerArrowReloadTickKey = new NamespacedKey(plugin, "gameplayer.arrowReloadTick");
        playerArrowMaxStackSizeKey = new NamespacedKey(plugin, "gameplayer.arrowMaxStackSize");
        playerTotalHitKey = new NamespacedKey(plugin, "player.totalHIt");
        gutitubowArrowItemKey = new NamespacedKey(plugin, "gutitubow.arrow");
    }

    public GamePlayer getGamePlayer(Entity player) {
        return new GamePlayer(player);
    }


    public class GamePlayer {
        private final Entity player;

        public GamePlayer(Entity player) {
            this.player = player;
        }

        public void damage(double amount, Entity entity, double distance) {
            damage(amount);
            LogManager.broadcastHitLog(entity, this.player, distance);
        }

        public void damage(double amount, Entity entity) {
            damage(amount);

            double distance = entity.getLocation().distance(this.player.getLocation());
            LogManager.broadcastHitLog(entity, this.player, distance);
        }

        public void damage(double amount) {
            ((LivingEntity) this.player).damage(amount);
        }

        public void kill() {
            World world = this.player.getWorld();
            Location location = this.player.getLocation();
            world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 3);
            world.spawn(location, LightningStrike.class, (LightningStrike c) -> {
            });
            if (this.player instanceof Player livingPlayer) {
                livingPlayer.setGameMode(GameMode.SPECTATOR);
            }


        }

        public void kill(Entity attacker) {
            this.kill();
        }

        public void kill(Entity attacker, double distance) {
            this.kill();
            LogManager.broadcastKillLog(attacker, this.player, distance);
        }

        public int getTotalHit() {
            return this.player.getPersistentDataContainer().getOrDefault(playerTotalHitKey, PersistentDataType.INTEGER, 0);
        }

        public void setTotalHit(int count) {
            this.player.getPersistentDataContainer().set(playerTotalHitKey, PersistentDataType.INTEGER, count);
        }

        public void resetTotalHit() {
            this.setTotalHit(0);
        }

        public int getArrowReloadTick() {
            return this.player.getPersistentDataContainer().getOrDefault(playerArrowReloadTickKey, PersistentDataType.INTEGER, 0);
        }

        public void setArrowReloadTick(int tick) {
            this.player.getPersistentDataContainer().set(playerArrowReloadTickKey, PersistentDataType.INTEGER, tick);
        }

        public void resetArrowReloadTick() {
            this.setArrowReloadTick(5 * 20);
        }

        public int getMaxArrowStackSize() {
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
            if (!(this.player instanceof Player player)) return 0;
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
            if (!(player instanceof Player)) return;
            Player player = (Player) this.player;
            int slot = this.getArrowItemSlot();
            if (slot == -1) slot = this.getCanAddArrowSlot();
            if (slot != -1) player.getInventory().setItem(slot, getArrowItem(amount));
        }

        public void addArrow(int amount) {
            setArrowAmount(getArrowAmount() + amount);
        }

        public void addArrow() {
            addArrow(1);
        }

        public void removeArrow(int amount) {
            setArrowAmount(getArrowAmount() - amount);
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


    }
}
