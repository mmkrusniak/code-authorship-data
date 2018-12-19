package com.miolean.arena.framework;

import com.miolean.arena.entities.*;

/**
 * Created by commandm on 2/19/17.
 */
public class Handler implements Perpetual {

    Arena arena;
    private int lastUUIDUsed = 0;

    public static Robot BASE_ROBOT;

    public Handler(Arena arena) {

        this.arena = arena;
    }

    public void tick(Object... o) {
        arena.updateAll();
    }


//    public Entity closestToDistance(double x, double y, int distance) {
//        double record = 0xFFFF;
//        Entity result = null;
//        for(Entity e: entities) {
//            if(e == null) continue;
//            double closeness = ((x - e.x) * (x - e.x) + (y - e.y) * (y - e.y) - distance * distance);
//            if(closeness < record) {
//                record = closeness;
//                result = e;
//            }
//        }
//        return result;
//    }


    public void stop() {}
    public void start() {}
    public void pause() {}
    public void resume() {}
}
