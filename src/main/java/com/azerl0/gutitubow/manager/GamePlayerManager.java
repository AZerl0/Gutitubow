package com.azerl0.gutitubow.manager;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class GamePlayerManager {
    private final NamespacedKey playerArrowReloadTickKey;
    private final NamespacedKey playerArrowMaxStackSizeKey;
    private final NamespacedKey playerTotalHitKey;

    public GamePlayerManager(JavaPlugin plugin) {
        playerArrowReloadTickKey = new NamespacedKey(plugin, "gameplayer.arrowReloadTick");
        playerArrowMaxStackSizeKey = new NamespacedKey(plugin, "gameplayer.arrowMaxStackSize");
        playerTotalHitKey = new NamespacedKey(plugin, "player.totalHIt");
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

        }

        public  int getTotalHit() {
            return this.player.getPersistentDataContainer().getOrDefault(playerTotalHitKey, PersistentDataType.INTEGER, 0);
        }

        public void setTotalHit(int count ) {
            this.player.getPersistentDataContainer().set(playerTotalHitKey, PersistentDataType.INTEGER, count);
        }

        public int getArrowReloadTick() {
            return this.player.getPersistentDataContainer().getOrDefault(playerArrowReloadTickKey, PersistentDataType.INTEGER, 0);
        }

        public void setArrowReloadTick(int tick) {
            this.player.getPersistentDataContainer().set(playerArrowReloadTickKey, PersistentDataType.INTEGER, tick);
        }

        public int getMaxArrowStackSize() {
            return this.player.getPersistentDataContainer().getOrDefault(playerArrowMaxStackSizeKey, PersistentDataType.INTEGER, 0);
        }

        public void setMaxArrowStackSize(int size) {
            this.player.getPersistentDataContainer().set(playerArrowMaxStackSizeKey, PersistentDataType.INTEGER, size);
        }
    }
}
