package com.azerl0.gutitubow.event.gutitubowentity.shield;

import com.azerl0.gutitubow.entity.shield.Shield;

public class ShieldRaiseEvent extends AbstractShieldEvent{

    public ShieldRaiseEvent(Shield shield) {
        this.shield = shield;
    }
}
