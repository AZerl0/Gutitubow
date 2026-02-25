package com.azerl0.gutitubow;

import com.azerl0.gutitubow.command.CarePackageCommand;
import com.azerl0.gutitubow.listener.EntityListener;
import com.azerl0.gutitubow.manager.CarePackageManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.azerl0.gutitubow.listener.ProjectileHitListener;
import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.ability.abilities.*;
import com.azerl0.gutitubow.command.AbilityCommand;
import com.azerl0.gutitubow.listener.BowListener;
import com.azerl0.gutitubow.listener.GutitubowPlayerListener;

public final class Gutitubow extends JavaPlugin {
    @Override
    public void onEnable() {

        CarePackageManager cpm = new CarePackageManager(this);

        // Ability登録
        AbilityRegistry.register(new RicochetAbility(this));
        AbilityRegistry.register(new BombAbility(this));
        AbilityRegistry.register(new ControlAbility(this));
        AbilityRegistry.register(new BarrierAbility(this));
        AbilityRegistry.register(new SnipeAbility(this));

        // Listener登録
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ProjectileHitListener(this), this);
        pm.registerEvents(new BowListener(), this);
        pm.registerEvents(new GutitubowPlayerListener(), this);
        pm.registerEvents(new EntityListener(cpm), this);

        // Command登録
        getCommand("ability").setExecutor(new AbilityCommand());
        getCommand("carepackage").setExecutor(new CarePackageCommand(cpm));

        getLogger().info("Loaded abilities: " + AbilityRegistry.getAllIds());


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
