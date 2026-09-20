package com.azerl0.gutitubow.util;

import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Random;

public class MathUtil {
    public static double getLerp(double value, double low1, double high1, double low2, double high2) {
        return low2 + (high2 - low2) * (value - low1) / (high1 - low1);
    }

    public static boolean isInView(Player player, Location target, double horizontalDeg, double verticalDeg) {
        Location eye = player.getEyeLocation();

        Vector toTarget = target.toVector().subtract(eye.toVector());

        // プレイヤーの向き
        float yaw = eye.getYaw();
        float pitch = eye.getPitch();

        // 対象の方向
        Location targetLoc = target.clone();
        targetLoc.setDirection(toTarget);

        float targetYaw = targetLoc.getYaw();
        float targetPitch = targetLoc.getPitch();

        // 差分
        float yawDiff = getAngleDiff(yaw, targetYaw);
        float pitchDiff = Math.abs(pitch - targetPitch);

        return yawDiff <= horizontalDeg / 2 && pitchDiff <= verticalDeg / 2;
    }

    private static float getAngleDiff(float a, float b) {
        float diff = Math.abs(a - b) % 360;
        return diff > 180 ? 360 - diff : diff;
    }

    public static Location rayTraceEntityHit(Location location, Entity entity) {
        Vector direction = entity.getLocation().toVector().subtract(location.toVector()).normalize();

        RayTraceResult result = location.getWorld().rayTraceEntities(
                location,
                direction,
                location.distance(entity.getLocation()),
                e -> e.equals(entity)
        );

        return result != null ? result.getHitPosition().toLocation(location.getWorld()) : null;
    }

    public static Location randomLocation(Location center, double width) {
        Random random = new Random();

        double r = width / 2;
        double offsetX = random.nextDouble(-r, r);
        double offsetZ = random.nextDouble(-r, r);

        return center.clone().add(offsetX, 0, offsetZ);
    }

    public static Location getLocationByProgress(Location start, Location end, float progress) {
        if (!start.getWorld().equals(end.getWorld())) return start;
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        Vector offset = end.clone().toVector().subtract(start.clone().toVector()).multiply(progress);
        return start.clone().add(offset);
    }
}
