package com.azerl0.gutitubow.event.gutitubowentity.shield;

import com.azerl0.gutitubow.entity.shield.Shield;

public class ShieldBreakEvent extends AbstractShieldEvent {

    public ShieldBreakEvent(Shield shield) {
        this.shield = shield;
    }
}
