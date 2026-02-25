package com.azerl0.gutitubow.manager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class GamePlayerManager {
    private JavaPlugin plugin;

    public GamePlayerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public GamePlayer getGamePlayer(Entity player) {
        return new GamePlayer(player);
    }


    public static class GamePlayer {
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
    }
}
