package com.azerl0.gutitubow;

import com.azerl0.gutitubow.command.CarePackageCommand;
import com.azerl0.gutitubow.command.GameCommand;
import com.azerl0.gutitubow.listener.EntityListener;
import com.azerl0.gutitubow.manager.CarePackageManager;
import com.azerl0.gutitubow.manager.GameManager;
import com.azerl0.gutitubow.manager.GamePlayerManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.azerl0.gutitubow.listener.ProjectileHitListener;
import com.azerl0.gutitubow.ability.*;
import com.azerl0.gutitubow.ability.abilities.*;
import com.azerl0.gutitubow.command.AbilityCommand;
import com.azerl0.gutitubow.listener.BowListener;
import com.azerl0.gutitubow.listener.GamePlayerListener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class Gutitubow extends JavaPlugin {
    @Override
    public void onEnable() {

        // Managers
        CarePackageManager cpm = new CarePackageManager(this);
        GamePlayerManager gamePlayerManager = new GamePlayerManager(this);
        GameManager gameManager = new GameManager(this);

        // MainGame
        GameManager.Game game = gameManager.newGame();

        // Ability登録
        AbilityContext abilityContext = new AbilityContext(this, gamePlayerManager);
        AbilityRegistry.register(new RicochetAbility(abilityContext));
        AbilityRegistry.register(new BombAbility(abilityContext));
        AbilityRegistry.register(new ControlAbility(abilityContext));
        AbilityRegistry.register(new BarrierAbility(abilityContext));
        AbilityRegistry.register(new SnipeAbility(abilityContext));
        AbilityRegistry.register(new QuickShotAbility(abilityContext));
        AbilityRegistry.register(new DashAbility(abilityContext));

        // Listener登録
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ProjectileHitListener(this), this);
        pm.registerEvents(new BowListener(), this);
        pm.registerEvents(new GamePlayerListener(gamePlayerManager), this);
        pm.registerEvents(new EntityListener(cpm), this);

        // Command登録
        getCommand("ability").setExecutor(new AbilityCommand());
        getCommand("carepackage").setExecutor(new CarePackageCommand(cpm));
        getCommand("game").setExecutor(new GameCommand(game));

        getLogger().info("Loaded abilities: " + AbilityRegistry.getAllIds());


    }

    @Override
    public void onDisable() {
        // グローバル発光カラー用スコアボードの削除
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        for (NamedTextColor color : NamedTextColor.NAMES.values()) {

            Team team = board.getTeam("glow_" + color.toString().toLowerCase());
            if (team != null) {
                team.unregister();
            }
        }
    }
}
