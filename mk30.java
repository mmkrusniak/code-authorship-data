package com.miolean.arena.entities;

import com.miolean.arena.framework.Option;

import java.awt.*;
import java.awt.geom.Point2D;

import static com.miolean.arena.entities.Arena.ARENA_SIZE;
import static com.miolean.arena.entities.Arena.BORDER;

public class Bullet extends Entity {

    private static final int ROGUE_SPEED = 8;
    private static final int SIZE = 8;
    private static final double ROGUE_TURN_SPEED = 0.1;
    private static final int ROGUE_OBSERVATION = 100;

    private Robot source;
    private Robot target;
    private int damage;

    public Bullet(Robot source, Arena arena) {
        super(SIZE, SIZE, 1, arena);
        this.source = source;

        if(source != null) {
            setX(source.getX());
            setY(source.getY());
            while(intersectsWith(source)) applyPhysics();
            damage = source.stats[Robot.STAT_DAMAGE].val();
            setVelX((15 + source.stats[Robot.STAT_BULLET_SPEED].val()) * Math.cos(source.getR() + source.stats[Robot.STAT_BULLET_SPREAD].val() / 128.0 * (Option.random.nextFloat() - .5)));
            setVelY((15 + source.stats[Robot.STAT_BULLET_SPEED].val()) * -Math.sin(source.getR() + source.stats[Robot.STAT_BULLET_SPREAD].val() / 128.0 * (Option.random.nextFloat() - .5)));
        } else {
            setX(ARENA_SIZE * Option.random.nextFloat());
            setY(ARENA_SIZE * Option.random.nextFloat());
            damage = 5;
        }

        setMass(3);
    }


    @Override
    public void renderBody(Graphics g, int x, int y, byte flags) {

        if(target != null || source != null) g.setColor(new Color(100 + damage / 2, 150 - damage / 2, 50));
        else g.setColor(new Color(100, 100, 255));

        if(source != null) {
            g.fillOval( x - SIZE / 2,  y - SIZE / 2, SIZE, SIZE);
            g.setColor(Color.black);
            g.drawOval( x - SIZE / 2,  y - SIZE / 2, SIZE, SIZE);
        } else {

            double sinR = Math.sin(getR() + Math.PI);
            double cosR = Math.cos(getR() + Math.PI);
            double sinExtra = .25867;
            double cosExtra = .88007;

            //Fun fact: This is stolen from Robot's gun barrel.
            int[] XPoints = {
                    (int) (x+SIZE*3*(cosR*cosExtra - sinR*sinExtra)),
                    (int) (x+SIZE*3*(cosR*cosExtra + sinR*sinExtra)),
                     (x)
            };

            int[] YPoints = {
                    (int) (y-SIZE*3*(sinR*cosExtra + sinExtra*cosR)),
                    (int) (y-SIZE*3*(sinR*cosExtra - sinExtra*cosR)),
                     (y)

            };

            g.fillPolygon(XPoints, YPoints, 3);
            g.setColor(Color.black);
            g.drawPolygon(XPoints, YPoints, 3);
        }
    }

    void forward(int force) {
       //Translate polar force into cartesian vector
        setAccX(force * Math.cos(getR()) / 16); //Scaling!
        setAccY(force * -Math.sin(getR()) / 16);
    }


    @Override
    public boolean intersectsWith(Entity e) {
        if( e instanceof TrackerDot) return false;
        if(e == null || e == source || (e instanceof Bullet && ((Bullet) e).source == source)) return false; //Don't interact with your own source
        if(super.intersectsWith(e)) return true;

        //This is going to be tedious if we don't make up some shorthands.
        //I know, memory usage and so forth, but it'll be OK
        double x = getX();
        double y = getY();
        double velX = getVelX();
        double velY = getVelY();
        double accX = getAccX();
        double accY = getAccY();

        Rectangle extendedBounds = new Rectangle(new Point((int)x, (int)y));
        extendedBounds.add(new Point((int) (x+velX+accX), (int) (y+velY+accY)));
        extendedBounds.setLocation((int) (extendedBounds.getX() - e.getWidth()), (int) (extendedBounds.getY() - e.getHeight()));
        extendedBounds.setSize((int) (extendedBounds.getWidth() + 2*e.getWidth()), (int) (extendedBounds.getHeight() + 2*e.getHeight()));

        if(extendedBounds.contains(new Point2D.Double(e.getX(), e.getY()))) {
            double slope = (velY + accY) / (velX + accX);

            double k = y - slope * x;

            double a = e.getX() - (e.getY()-k)/slope;
            double b = e.getY() - e.getX()*slope - k;
            double g2 = (a*a*b*b) / (a*a + b*b);

            if(g2 < (SIZE/2 + e.getWidth()/2)*(SIZE/2 + e.getWidth()/2)) {
                return true;
            }

        }

        return false;
    }


    //Unfortunately there's no quick way to fill in the skips for bullets
    @Override
    public boolean quickIntersects(Entity e) {return true;}

    @Override
    public void update() {

        applyPhysics();

        if(target != null && target.getHealth() <= 0) target = null;
        if(source != null && source.getHealth() <= 0) source = null;

        if(source != null) {
            if (Math.abs(getVelX()) < 1 && Math.abs(getVelY()) < 1) getArena().remove(getUUID());
            if ((getX() > ARENA_SIZE - BORDER) || (getX() < BORDER) || (getY() > ARENA_SIZE - BORDER) || (getY() < BORDER)) getArena().remove(getUUID());
        } else if(target != null){
            double xdis = target.getX() - getX();
            double ydis = target.getY() - getY();


            double targetR = Math.atan(xdis/ydis) + Math.PI/2;

            if(ydis > 0) targetR += Math.PI;

            // (A - B) mod C = (A mod C - B mod C) mod C
            double rdis = (getR() - targetR) % (2*Math.PI);
            if(rdis < -Math.PI) rdis += (2*Math.PI);

            if(rdis > -2*getVelR()) setAccR(-ROGUE_TURN_SPEED);
            else if(rdis < 2*getVelR()) setAccR(ROGUE_TURN_SPEED);
            else setAccR(0);

            forward(ROGUE_SPEED);

            //Meanwhile, look for closer targets

            for(int i = 0; i < ROGUE_OBSERVATION; i++) {

                Entity attemptedTarget = getArena().fromUUID(Option.random.nextInt(Arena.MAX_ENTITIES));

                if (attemptedTarget != null && attemptedTarget instanceof Robot) {
                    double axdis = attemptedTarget.getX() - getX();
                    double aydis = attemptedTarget.getY() - getY();

                    if(axdis*axdis + aydis*aydis < xdis*xdis + ydis*ydis) {
                        target = (Robot) attemptedTarget;
                    }
                }
            }

        } else {

            setVelR(0.1);

            for(int i = 0; i < ROGUE_OBSERVATION; i++){
                Entity attemptedTarget = getArena().fromUUID(Option.random.nextInt(Arena.MAX_ENTITIES));
                if(attemptedTarget != null && attemptedTarget instanceof Robot) {
                    target = (Robot) attemptedTarget;
                    System.out.println("Found target");
                }
            }
        }
    }

    @Override
    public void intersect(Entity e) {

        repel(e);
        if(! (e instanceof Cog)) e.damage(damage);
        if(! (e instanceof Wall)) damage(1);
    }

    @Override
    public void onBirth() {

    }

    @Override
    public void onDeath() {

    }

    @Override
    public String toHTML() {
        String result = "";

        if(source != null) {
            result += "<font color=\"red\">" + source.getName() + "'s Bullet";
        } else {
            result += "<font color=\"maroon\">Rogue Bullet";
        }
        return result;
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
