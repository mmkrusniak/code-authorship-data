package com.miolean.arena.ui;

import com.miolean.arena.entities.Entity;
import com.miolean.arena.entities.Robot;

public interface ActiveRobotListener {

    void viewholderChanged(Entity e);
    void infoholderChanged(Robot e);
}
