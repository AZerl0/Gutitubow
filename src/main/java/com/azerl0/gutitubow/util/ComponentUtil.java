package com.azerl0.gutitubow.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ComponentUtil {

    public static Component getGaugeComponent(
            Component prefix,
            int value,
            int maxValue,
            NamedTextColor gaugeColor,
            NamedTextColor gaugeOutsideColor
    ) {
        Component gauge = Component.empty();

        double ratio = (double) value / maxValue;

        for (int i = 0; i < 10; i++) {
            boolean filled = i < ratio * 10;
            gauge = gauge.append(
                    Component.text("■")
                            .color(filled ? gaugeColor : NamedTextColor.GRAY)
            );
        }

        gauge = Component.text("[")
                .color(gaugeOutsideColor)
                .append(gauge)
                .append(Component.text("]").color(gaugeOutsideColor));

        return prefix.append(gauge);
    }

    public static Component getGaugeComponent(Component prefix, int value, int maxValue, NamedTextColor gaugeColor) {
        return getGaugeComponent(prefix, value, maxValue, gaugeColor, NamedTextColor.GRAY);
    }

    public static Component getGaugeComponent(int value, int maxValue, NamedTextColor gaugeColor, NamedTextColor gaugeOutsideColor) {
        return getGaugeComponent(Component.empty(), value, maxValue, gaugeColor, gaugeOutsideColor);
    }
}
