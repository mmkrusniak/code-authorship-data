package com.miolean.arena.entities;

import java.awt.*;

public class TrackerDot extends Entity {


    public TrackerDot(double x, double y, int size, int persistence, Arena arena) {
        super(size, size, persistence, arena);
        setX(x);
        setY(y);
    }

    @Override
    public void renderBody(Graphics g, int x, int y, byte flags) {
        g.setColor(Color.black);
        g.fillOval(x - 2, y - 2, 4, 4);
    }

    @Override
    public void update() {
        damage(1);
    }

    @Override
    public boolean intersectsWith(Entity e) {
        return false;
    }

    @Override
    public void intersect(Entity e) {}

    @Override
    public void onBirth() {}

    @Override
    public void onDeath() {}

    @Override
    public String toHTML() {
        //TODO
        return null;
    }

    @Override
    public Polygon getBaseBounds() {
        return new Polygon(
                new int[] {getWidth()/2, 0, -getWidth()/2, 0},
                new int[] {0, getHeight()/2, 0, -getHeight()/2},
                4
        );
    }
}
