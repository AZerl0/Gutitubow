package com.azerl0.gutitubow.event.gutitubowentity.shield;

import com.azerl0.gutitubow.entity.shield.Shield;

public class ShieldRaisingEvent extends AbstractShieldEvent{

    public ShieldRaisingEvent(Shield shield) {
        this.shield = shield;
    }
}
