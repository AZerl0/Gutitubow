package com.azerl0.gutitubow.ability;

import net.kyori.adventure.text.Component;

public enum AbilityType {
    BARRIER(Component.text("障壁")),
    BOMB(Component.text("爆弾")),
    CONTROL(Component.text("操弾")),
    QUICKSHOT(Component.text("速射")),
    RICOCHET(Component.text("跳弾")),
    SNIPE(Component.text("狙撃")),
    DASH(Component.text("疾走")),
    JUMP(Component.text("跳躍"));

    private final Component name;

    AbilityType(Component name) {
            this.name = name;
    }

    public Component getName() {
        return this.name;
    }
}
