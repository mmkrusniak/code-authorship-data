package com.miolean.arena.entities;

import com.miolean.arena.framework.Option;
import static com.miolean.arena.entities.Arena.ARENA_SIZE;
import static com.miolean.arena.entities.Arena.BORDER;

import java.awt.*;

public class Body {

    double x, y, r, velX, velY, velR, accX, accY, accR;

    private Polygon bounds;
    private int[] xPoints;
    private int[] yPoints;


    /*
    What does Body need to do?
    1) Given bounds, translate, rotate, etc. those bounds.
    2) Find center of mass.
    3) Determine a point of intersection with another Body (in t steps).
        How? Binary search (pan through time)
    4) Advance given number of steps when needed
     */

    void applyPhysics(double t) {

        final double DRAG = Option.drag.getValue() / 100.0;

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

    private Point.Double pointAtTime(double t, Point.Double p) {
        //Returns the location of p if it is moving and spinning with this body.

        double rPrime = r + velR*t + .5*accR*t*t;
        double xPrime = x + velX*t + .5*accX*t*t + p.x*Math.cos(rPrime) - p.y*Math.sin(rPrime);
        double yPrime = y + velY*t + .5*accY*t*t + p.y*Math.sin(rPrime) - p.y*Math.cos(rPrime);

        return new Point.Double(xPrime, yPrime);
    }

    //This is what our function lineup looks like:
    //boolean intersects(e, t) - does this entity intersect with e at all during t ticks?
    //boolean intersection(e, t) - at what point and what time does an intersection occur?


    private double[] intersection(Entity e, double t) {

        //What we're going to do is essentially a binary search for an intersection point
        //within t steps of time.
        //Our result will be an ordered triple (x, y, t) where t is the intersection time.
        //We want the earliest of all intersections.


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
            for(int i = 0; i < bounds.xpoints.length; i++) {
                if(e.getBounds().contains(bounds.xpoints[i], bounds.ypoints[i])) {
                    intersects = true;
                    result[0] = bounds.xpoints[i];
                    result[1] = bounds.ypoints[i];
                    break;
                }
            }
            //Check if there are any points that are in the other entity.
            //If not, scroll time forward a little until there are.
            //If so, scroll time back a bit until there aren't.


            attempt++;
        }
    return result;
    }

    void applyBorder() {
 //       TODO Border options

        if(x > -BORDER) {
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

}
