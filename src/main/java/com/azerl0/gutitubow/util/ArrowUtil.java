package com.azerl0.gutitubow.util;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.util.Vector;

public class ArrowUtil {

    public static void setFixRotation(AbstractArrow arrow, Vector velocity) {
        // https://bugs.mojang.com/browse/MC-112474
        double x = -velocity.getX();
        double y = -velocity.getY();
        double z = velocity.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(-x, z));
        float pitch = (float) Math.toDegrees(Math.atan2(-y, Math.sqrt(x * x + z * z)));

        arrow.setRotation(yaw, pitch);

    }
}
