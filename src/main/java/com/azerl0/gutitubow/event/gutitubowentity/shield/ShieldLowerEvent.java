package com.azerl0.gutitubow.event.gutitubowentity.shield;

import com.azerl0.gutitubow.entity.shield.Shield;

public class ShieldLowerEvent extends AbstractShieldEvent{

    public ShieldLowerEvent(Shield shield) {
        this.shield = shield;
    }
}
