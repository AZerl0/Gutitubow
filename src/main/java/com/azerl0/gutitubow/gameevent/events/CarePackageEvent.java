package com.azerl0.gutitubow.gameevent.events;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.api.game.Game;
import com.azerl0.gutitubow.entity.carepackage.CarePackage;
import com.azerl0.gutitubow.gameevent.GameEvent;
import com.azerl0.gutitubow.gameevent.GameEventContext;
import com.azerl0.gutitubow.gameevent.GameEventType;
import com.azerl0.gutitubow.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class CarePackageEvent extends GameEvent {
    public final long  SPAWN_SPAN_TICK = 25 * 20;

    private BukkitTask spawnTask;
    private final Set<CarePackage> carePackageSet = new HashSet<>();

    public CarePackageEvent(GameEventContext context) {
        super(context);
    }

    @Override
    public @NotNull GameEventType getEventType() {
        return GameEventType.CAREPACKAGE;
    }

    @Override
    protected void onActivate() {
        spawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnCarePackages();
            }
        }.runTaskTimer(Gutitubow.get(), 0L, SPAWN_SPAN_TICK);
    }

    @Override
    protected void onDeactivated() {
        spawnTask.cancel();
        for (CarePackage carePackage : carePackageSet) {
            if (!carePackage.isRemoved()) carePackage.remove();
        }
        carePackageSet.clear();
    }

    private void spawnCarePackages() {
        Game game = Gutitubow.get().getGame();
        for (int i = 0; i < 3; i++) {
            Location location = MathUtil.randomLocation(game.getSafeArea(), game.getNextSafeAreaSize() - 10);
            location.setY(175);
            CarePackage carePackage = Gutitubow.get().getGutitubowEntityManager().spawnGutitubowEntity(
                    location,
                    CarePackage.class
            );
            if (i == 1) carePackage.setFake(true);
            carePackageSet.add(carePackage);
        }
    }
}
