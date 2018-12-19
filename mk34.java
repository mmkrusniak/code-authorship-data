package com.miolean.arena.entities;

import static com.miolean.arena.framework.Option.*;
import static com.miolean.arena.framework.UByte.ub;

public class ControlledRobot extends Robot {

    public ControlledRobot(int x, int y, Arena arena) {
        super(arena);
        setX(x);
        setY(y);
        setHealth(100);
        setName("Controlled Bot");

        //Do better default stats.
        stats[STAT_SPEED] = ub(20);
        stats[STAT_BULLET_SPEED] = ub(20);
        stats[STAT_ROTATE_SPEED] = ub(40);
        stats[STAT_FIRE_SPEED] = ub(20);
        stats[STAT_DAMAGE] = ub(0);
        stats[STAT_MAX_HEALTH] = ub(100);
        stats[STAT_BULLET_SPREAD] = ub(30);
    }

    @Override
    public void update() {
        applyPhysics();
        if(KEY[KEY_W]) forward(100);
        else if(KEY[KEY_S]) forward(-100);
        else forward(0);

        if(KEY[KEY_A]) rotate(100);
        else if(KEY[KEY_D]) rotate(-100);
        else rotate(0);

        if(KEY[KEY_SPACE]) fire();
        if(KEY[KEY_F]) damage(1);
        if(KEY[KEY_R]) heal(1);
    }

    @Override
    public void onBirth() {

    }

    @Override
    public void onDeath() {

    }

    @Override
    public String toHTML() {
        return "<font color=\"gray\">Controlled Bot";
    }
}
