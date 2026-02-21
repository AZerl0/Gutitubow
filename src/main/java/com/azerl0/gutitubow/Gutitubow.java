package com.azerl0.gutitubow;

import org.bukkit.plugin.java.JavaPlugin;
import com.azerl0.gutitubow.listener.ProjectileHitListener;
import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.ability.abilities.*;
import com.azerl0.gutitubow.command.AbilityCommand;
import com.azerl0.gutitubow.listener.BowListener;

public final class Gutitubow extends JavaPlugin {

    private AbilityRegistry registry;
    private AbilityManager manager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(this), this);

        // Ability登録
        AbilityRegistry.register(new RicochetAbility());
        AbilityRegistry.register(new BombAbility());

        // Listener登録
        getServer().getPluginManager().registerEvents(
                new BowListener(),
                this
        );

        // Command登録
        getCommand("ability").setExecutor(
                new AbilityCommand()
        );

        getLogger().info("Loaded abilities: " + AbilityRegistry.getAllIds());


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
