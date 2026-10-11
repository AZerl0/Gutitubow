package com.azerl0.gutitubow.gameevent;

import com.azerl0.gutitubow.Gutitubow;
import com.azerl0.gutitubow.api.game.Game;

public record GameEventContext(Gutitubow gutitubow, Game game) {
}
