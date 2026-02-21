package com.azerl0.gutitubow;

import org.bukkit.plugin.java.JavaPlugin;
import com.azerl0.gutitubow.listener.ProjectileHitListener;
import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.ability.abilities.RicochetAbility;
import com.azerl0.gutitubow.command.AbilityCommand;
import com.azerl0.gutitubow.listener.BowListener;

public final class Gutitubow extends JavaPlugin {

    private AbilityRegistry registry;
    private AbilityManager manager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(this), this);

        registry = new AbilityRegistry();
        manager = new AbilityManager();

        // Ability登録
        registry.register(new RicochetAbility());

        // Listener登録
        getServer().getPluginManager().registerEvents(
                new BowListener(registry, manager),
                this
        );

        // Command登録
        getCommand("ability").setExecutor(
                new AbilityCommand(registry, manager)
        );

        getLogger().info("Loaded abilities: " + registry.getAllIds());


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
