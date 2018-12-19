package com.miolean.arena.entities;

import java.awt.*;

public class Wall extends Entity {

    Wall(int width, int height, int health, Arena arena) {
        super(width, height, health, arena);
        setMass(100);
        setR(2.0);
        setAccR(0.04);
    }

    @Override
    protected void update() {
        applyPhysics();
    }

    @Override
    public void intersect(Entity e) {
        repel(e);
    }

    @Override
    protected void onBirth() {

    }

    @Override
    protected void onDeath() {

    }

    @Override
    public String toHTML() {
        return null;
    }

    @Override
    public Polygon getBaseBounds() {
        int[] xPoints = {
                getWidth()/2,
                -getWidth()/2,
                -getWidth()/2,
                getWidth()/2
        };
        int[] yPoints = {
                getHeight()/2,
                getHeight()/2,
                -getHeight()/2,
                -getHeight()/2
        };

        return new Polygon(xPoints, yPoints, 4);
    }

    @Override
    public void renderBody(Graphics g, int x, int y, byte flags) {
        double sinR = Math.sin(-getR());
        double cosR = Math.cos(-getR());

        int[] xPoints = {
                (int) (getX() + (getWidth()/2)*cosR - (getHeight()/2)*sinR),
                (int) (getX() + (-getWidth()/2)*cosR - (getHeight()/2)*sinR),
                (int) (getX() + (-getWidth()/2)*cosR - (-getHeight()/2)*sinR),
                (int) (getX() + (getWidth()/2)*cosR - (-getHeight()/2)*sinR)
        };
        int[] yPoints = {
                (int) (getY() + (getWidth()/2)*sinR + (getHeight()/2)*cosR),
                (int) (getY() + (-getWidth()/2)*sinR + (getHeight()/2)*cosR),
                (int) (getY() + (-getWidth()/2)*sinR + (-getHeight()/2)*cosR),
                (int) (getY() + (getWidth()/2)*sinR + (-getHeight()/2)*cosR)
        };

        Polygon p = new Polygon(xPoints, yPoints, 4);
        g.setColor(Color.GRAY);
        g.fillPolygon(p);
        g.setColor(Color.BLACK);
        g.drawPolygon(p);

    }
}
