package com.miolean.arena.entities;

import com.miolean.arena.framework.Debug;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

import static com.miolean.arena.entities.Arena.ARENA_SIZE;
import static com.miolean.arena.entities.Arena.BORDER;

/**
 * Created by commandm on 2/16/17.
 * Anything that moves according to set physics is an Entity.
 * Entities can update themselves every tick and renderBody themselves.
 * They can also interact with Entities with which they intersect.
 */

public abstract class Entity implements Serializable {

    //Motion components:
    private double x; //X position, in pixels
    private double y; //Y position, in pixels
    private double r; //Rotation, in radians
    private double velX; //X velocity, in pixels per tick.
    private double velY; //Y velocity, in pixels per tick.
    private double velR; //Rotational velocity, in degrees per tick.
    private double accX; //X acceleration, in pixels per tick per tick.
    private double accY; //Y acceleration, in pixels per tick per tick.
    private double accR; //Rotational acceleration, in degrees per tick per tick.

    private final static double DRAG = 0.1; //The amount that an Entity naturally slows down each tick, per unit of velocity.
    private final static double RDRAG = 0.5;

    //Rendering flags

    //Rendering flags
    public static final byte RENDER_HIGH_QUALITY = 0b00000011;
    public static final byte RENDER_MID_QUALITY = 0b00000010;
    public static final byte RENDER_LOW_QUALITY = 0b00000000;
    public static final byte RENDER_MINISCULE = 0b00000001;
    public static final byte RENDER_GLOWING = 0b00000100;
    public static final byte RENDER_DECORATED = 0b00001000;

    //Size components:
    private int width;
    private int height;
    private double mass = 1;

    //Entities can also be destroyed:
    private double health = 1;
    private boolean alive = true;
    private long age = 0;

    //Other things:
    private Arena arena;

    //ID management
    private int uuid = -1;


    Entity(int width, int height, int health, Arena arena) {
        this.width = width;
        this.height = height;
        this.health = health;
        this.arena = arena;
    }

    void applyPhysics() {
        r %= 6.28;

        velX -= DRAG * velX;
        velY -= DRAG * velY;
        velR -= RDRAG * velR;

        velX += accX;
        velY += accY;
        velR += accR;

        x += velX;
        y += velY;
        r += velR;

        if(x > ARENA_SIZE - BORDER) {
            x = ARENA_SIZE - BORDER;
            velX = -velX;
        }
        if(x < BORDER) {
            x = BORDER;
            velX = -velX;
        }
        if(y > ARENA_SIZE - BORDER){
            y = ARENA_SIZE - BORDER;
            velY = -velY;
        }
        if(y < BORDER) {
            y = BORDER;
            velY = -velY;
        }
    }

    void applyPhysics(double t) {

        //Here's the thing: if we want to be able to go backwards steps, we have to reverse this whole thing.

        if(t > 0) {
            x += t*velX;
            y += t*velY;
            r += t*velR;

            velX += t*accX - t*DRAG*velX;
            velY += t*accY - t*DRAG*velY;
            velR += t*accR - t*DRAG*velR;
        }

        if(t < 0) {
            velX -= t*accX - t*DRAG*velX;
            velY -= t*accY - t*DRAG*velY;
            velR -= t*accR - t*DRAG*velR;

            x -= t*velX;
            y -= t*velY;
            r -= t*velR;
        }


    }


    void repel(Entity e) {
//
//
//
//        //We're going to push them by their mass's share of the velocity. Does that make sense?
//
//        e.move((e.mass/(e.mass + mass) * (ourCompoundVel + theirCompoundVel)), angleOfIncidence + Math.PI);

//
        double compoundVel = Math.sqrt(velX*velX + velY*velY);
        double rposX = x - e.getX();
        double rposY = y  - e.getY();

        //Lay down the law for impossibly direct collisions
        double velX = (Math.abs(this.velX) < 1)? this.velX+1:this.velX;
        double velY = (Math.abs(this.velY) < 1)? this.velY+1:this.velY;
        if(rposX == 0) rposX = 1;
        if(rposY == 0) rposY = 1;


        double xdis = e.getX() - getX();
        double ydis = e.getY() - getY();
        double angleOfIncidence = Math.atan(xdis/ydis) + Math.PI/2;
        double ourCompoundVel = (x)*(Math.cos(angleOfIncidence)) + (y)*(Math.sin(angleOfIncidence));
        double theirCompoundVel = (e.x)*(Math.cos(Math.PI + angleOfIncidence)) + (e.y)*(Math.sin(Math.PI + angleOfIncidence));
        double momentumOfImpact = (ourCompoundVel * mass) + (theirCompoundVel * e.mass);

        //System.out.println("Found impact to be " + momentumOfImpact);

        double collisionAngle = Math.atan(rposY/rposX);
        //System.out.println("  Collision angle " + Math.toDegrees(collisionAngle) + "\n  Incidence angle " + Math.toDegrees(angleOfIncidence));

        double forceAngle = ((velX > 0)? Math.PI:0) + Math.atan(velY/velX);
        double reflectAngle = 2*collisionAngle-forceAngle;

        double percentMass = mass / (mass + e.getMass()); //basically for translating momentum into force

        move(reflectAngle,compoundVel * (1-percentMass));
        e.move( reflectAngle + Math.PI,compoundVel * (percentMass));
    }

    boolean quickIntersects(Entity e) {


        //The idea is to decide ASAP that e doesn't intersect.

        //First check: Are these things moving?
        if(Math.abs(e.getVelX()) < 0.05 && Math.abs(velX) < 0.05
                && Math.abs(e.getVelY()) < 0.05 && Math.abs(velY) < 0.05
                && ! (this instanceof Wall)) {
            //Neither of these appear to really be moving, so it's unlikely that they intersect.
            return false;
        }

        //Second check: Are these things close enough to come into contact?
        int maxBounds = Math.max(Math.max(width, height), Math.max(e.width, e.height));
        if(Math.abs(x - e.getX()) > maxBounds || Math.abs(y - e.getY()) > maxBounds) return false;



        //We have to assume that this might intersect then, unfortunately.
        return true;


    }

    double[] intersection(Entity e, double t) {

        //What we're going to do is essentially a binary search for an intersection point
        //within t steps of time.
        //Our result will be an ordered triple (x, y, t) where t is the intersection time.
        //We want the earliest of all intersections.

        applyPhysics(-t);

        final int ATTEMPTS = 15; //This basically determines how fine intersections are!
        int attempt = 0;
        boolean intersects = false;
        boolean previousIntersects = false;
        double[] result = new double[3];
        double elapse = t/ATTEMPTS;

        while(attempt < ATTEMPTS) {

            if(intersects && ! previousIntersects || previousIntersects && ! intersects) elapse /= 2;

            if(intersects) {
                applyPhysics(-elapse);
                result[2] -= elapse;
            } else {
                applyPhysics(elapse);
                result[2] += elapse;
            }

            previousIntersects = intersects;
            intersects = false;
            for(int i = 0; i < getBounds().xpoints.length; i++) {
                if(e.getBounds().contains(getBounds().xpoints[i], getBounds().ypoints[i])) {
                    intersects = true;
                    result[0] = getBounds().xpoints[i];
                    result[1] = getBounds().ypoints[i];
                    break;
                }
            }
            //Check if there are any points that are in the other entity.
            //If not, scroll time forward a little until there are.
            //If so, scroll time back a bit until there aren't.

            attempt++;
        }

        System.out.printf("Found a collision at %.2f, %.2f, %.2f\n", result[0], result[1], result[2]);
        applyPhysics(t-result[2]);

        return result;
    }

    void accelerate(double direction, double magnitude) {
        accX += magnitude * Math.cos(direction) / mass;
        accY += magnitude * Math.sin(direction) / mass;
    }

    void move(double direction, double magnitude) {
        velX += magnitude * Math.cos(direction) / mass;
        velY += magnitude * Math.sin(direction) / mass;
    }

    void tick() {
        if(health <= 0 || arena == null) die();
        else if(age > 0) update();
        age++;
    }

    protected abstract void update();
    public boolean intersectsWith(Entity e) {
        Polygon ourBounds = getBounds();
        Polygon theirBounds = e.getBounds();
        for(int i = 0; i < theirBounds.npoints; i++) {
            if(ourBounds.contains(theirBounds.xpoints[i], theirBounds.ypoints[i])) return true;
        }
        for(int i = 0; i < ourBounds.npoints; i++) {
                if(theirBounds.contains(ourBounds.xpoints[i], ourBounds.ypoints[i])) return true;
        }

        return false;
    }
    public abstract void intersect(Entity e);


    protected abstract void onBirth();
    protected abstract void onDeath();
    public abstract String toHTML();

    public boolean isAlive() {return alive;}
    public double getX() { return x; }
    public double getY() { return y; }
    public double getR() { return r; }
    public double getVelX() { return velX; }
    public double getVelY() { return velY; }
    public double getVelR() { return velR; }
    public double getAccX() { return accX; }
    public double getAccY() { return accY; }
    public double getAccR() { return accR; }
    public double getHealth() { return health; }
    public double getMass() { return mass; }
    public long getAge() { return age; }
    public int getUUID() { return uuid; }
    public int getWidth() { return width; }
    public int getHeight() { return height;}
    public Arena getArena() { return arena; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setR(double r) { this.r = r; }
    public void setVelX(double velX) { this.velX = velX; }
    public void setVelY(double velY) { this.velY = velY; }
    public void setVelR(double velR) { this.velR = velR; }
    public void setAccX(double accX) { this.accX = accX; }
    public void setAccY(double accY) { this.accY = accY; }
    public void setAccR(double accR) { this.accR = accR; }
    public void setHealth(double health) { this.health = health;}
    public void setWidth(int width) { this.width = width;}
    public void setHeight(int height) { this.height = height;}
    public void setMass(double mass) { this.mass = mass;}
    protected void setUUID(int uuid) {
        if(isAlive()) throw new IllegalStateException("Cannot change UUID of live entity");
        else this.uuid = uuid;
    }

    public final void die() {
        alive = false;
        onDeath();
        arena.remove(this);
    }
    public final void appear(int uuid) {
        alive = true;
        this.uuid = uuid;
        onBirth();
    }
    public void damage(double amount) {health -= amount;}
    public void heal(double amount) {health += amount;}
    public void add(Entity e) {arena.add(e);}

    public abstract Polygon getBaseBounds();
    public Polygon getBounds() {
        Polygon base = getBaseBounds();
        double cosR = Math.cos(-r);
        double sinR = Math.sin(-r);
        int[] xPoints = new int[base.npoints];
        int[] yPoints = new int[base.npoints];

        for(int i = 0; i < base.npoints; i++) {
            xPoints[i] = (int) (x + base.xpoints[i]*cosR - base.ypoints[i]*sinR);
            yPoints[i] = (int) (y + base.xpoints[i]*sinR + base.ypoints[i]*cosR);
        }

        return new Polygon(xPoints, yPoints, base.npoints);
    }
    public abstract void renderBody(Graphics g, int x, int y, byte flags);
    public void renderStatus(Graphics g, int x, int y, byte flags) {
        g.setColor(new Color(255, 100, 100, 200));
        g.fillRect(x, y, (int) getHealth(), 20);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, (int) getHealth(), 20);
        if(getHealth() < 20) g.drawString((int) getHealth() + "", x + 3 + (int) getHealth(), y+17);
        else g.drawString(getHealth() + "", x+3, y+17);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
    public JPanel toPanel() {


        JPanel entityPanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                long oldTime = System.nanoTime();
                super.paintComponent(g);
                Entity.this.renderBody(g, this.getWidth()/2, 50, RENDER_LOW_QUALITY);
                Debug.logTime("Rendering Entity panel",oldTime - System.nanoTime());
            }
        };

        return entityPanel;
    }


}
