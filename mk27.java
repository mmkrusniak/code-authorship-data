package com.miolean.arena.entities;

import com.miolean.arena.framework.Debug;
import com.miolean.arena.framework.Option;
import com.miolean.arena.ui.FieldDisplayPanel;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Arena {

    private static final int MAX_ROBOTS = 32*16;
    private static final int MAX_COGS = 200*4;
    public  static final int MAX_ENTITIES = 255*255;
    public  static final int TOP_LIST_LENGTH = 10;
    public static final int ARENA_SIZE = 4*1024;
    public static final int BORDER = 20*2;

    private ConcurrentHashMap<Integer, Entity> entities;
    private List<Robot> robots;
    private List<Cog> cogs;
    private List<GeneticRobot> topRobots;

    private int time = 0;

    public Arena() {
        entities = new ConcurrentHashMap<>();
        robots = new ArrayList<>();
        cogs = new ArrayList<>();
        topRobots = new ArrayList<>();
        topRobots.add(new DefaultGeneticRobot(Option.class.getClassLoader().getResourceAsStream("gen/default.ergo"), this));
        topRobots.get(0).setName("Dummy");
        Wall wall = new Wall(200, 10, 10, this);
        wall.setX(300);
        wall.setY(300);
        add(wall);
    }

    public void updateAll() {

        long marker;
        time++;
        distribute();


        for(Entity e: entities.values()) {

            marker = System.nanoTime();
            e.tick();
            Debug.logTime("Tick", System.nanoTime() - marker);

            //Is it still alive?
            if(entities.get(e.getUUID()) == null) continue;

            marker = System.nanoTime();
            for(Entity g: entities.values()) {
                if(e != g && e.quickIntersects(g) && e.intersectsWith(g)) {
                    e.intersect(g);

                    double[] p = e.intersection(g, 1.0);
                    add(new TrackerDot(p[0], p[1], 8,  60, this));
                }
            }
            Debug.logTime("Intersect", System.nanoTime() - marker);

            //Is it still alive?
            if(entities.get(e.getUUID()) == null) continue;


            marker = System.nanoTime();
            if(e instanceof GeneticRobot && ((GeneticRobot) e).getFitness() > 0 && (((GeneticRobot) e).getFitness() > topRobots.get(0).getFitness()) || e == topRobots.get(0)) {
                GeneticRobot r = (GeneticRobot) e;
                if(! topRobots.contains(r)) {
                    topRobots.add(r);
                }
            }
            Debug.logTime("Fitness", System.nanoTime() - marker);
        }
        marker = System.nanoTime();
        Collections.sort(topRobots);
        while(topRobots.size() > TOP_LIST_LENGTH) topRobots.remove(0);
        for(int i = 0; i < topRobots.size(); i++) if(! topRobots.get(i).isAlive()) topRobots.get(i).setUUID(-(i+1+200));
        Debug.logTime("Fitness", System.nanoTime() - marker);
    }

    public void renderAll(Graphics2D g) {
        for(Entity e: entities.values()) {
            e.renderBody(g, (int) e.getX(), (int) e.getY(), Entity.RENDER_LOW_QUALITY);
        }
    }

    public void renderAll(Graphics2D g, Point mouse) {

        for(Entity e: entities.values()) {
            if(e.intersectsWith(new TrackerDot(mouse.x, mouse.y, 4, 1, this))) {
                e.renderBody(g, (int) e.getX(), (int) e.getY(), (byte) (Entity.RENDER_GLOWING | Entity.RENDER_DECORATED));
            } else e.renderBody(g, (int) e.getX(), (int) e.getY(), Entity.RENDER_DECORATED);
            g.setColor(Color.blue);
            g.drawPolygon(e.getBounds());
        }
    }

    public void add(Entity e) {

        if(e instanceof Robot && robots.size() >= MAX_ROBOTS) return;
        if(e instanceof Cog && cogs.size() >= MAX_COGS) return;

        entities.remove(e.getUUID());

        int uuid;
        do { uuid = Option.random.nextInt(MAX_ENTITIES-1) + 1; //Don't select 0.
        } while(entities.get(uuid) != null);

        entities.put(uuid, e);
        if(e instanceof Robot) robots.add((Robot)e);
        if(e instanceof Cog) cogs.add((Cog)e);
        e.appear(uuid);
    }

    public void remove(Entity e) {
        if(e instanceof Robot) {
            robots.remove(e);
        }
        if(e instanceof Cog) cogs.remove(e);
        entities.remove(e.getUUID());
    }

    public void remove(int uuid) { remove(fromUUID(uuid)); }

    public Entity fromUUID(int uuid) {
        if(uuid < 0) return topRobots.get(uuid*-1-200-1);
        return entities.get(uuid);
    }
    public Entity fromUUID(int great, int less) {
        return entities.get(great * 255 + less);
    }

    public Entity atLocation(int x, int y) {
        TrackerDot location = new TrackerDot(x, y, 4,0,this);

        for(Entity e: entities.values()) {
            if(e != null && e.intersectsWith(location)) return e;
        }
        return null;
    }

    public Entity fromHTML(String html) {

        Debug.breakpoint();

        if(html.contains("ergo_uuid_")) {
            html = html.replaceAll("ergo_uuid_", "");
            return fromUUID(Integer.parseInt(html));
        }
        return null;
    }

    public void distribute() {

        if(Option.random.nextFloat() < 0.05) {
            Cog cog = new Cog(5 + (int) (10 * Option.random.nextFloat()), this);
            cog.setX(Option.random.nextFloat() * ARENA_SIZE);
            cog.setY(Option.random.nextFloat() * ARENA_SIZE);
            cog.setR(Option.random.nextFloat() * ARENA_SIZE);
            add(cog);
        }

        if(Option.random.nextFloat() < 0.01) {
            Robot robot;
            robot = new DefaultGeneticRobot(getTopRobots().get(Option.random.nextInt(getTopRobots().size())), this);

            robot.setX(Option.random.nextFloat() * ARENA_SIZE);
            robot.setY(Option.random.nextFloat() * ARENA_SIZE);
            robot.setR(Option.random.nextFloat() * ARENA_SIZE);
            add(robot);
        }
    }

    public ConcurrentHashMap<Integer, Entity> getEntities() { return entities;}
    public List<GeneticRobot> getTopRobots() { return topRobots;}
    public List<Robot> getRobots() { return robots;}
    public List<Cog> getCogs() { return cogs;}

    public int getTime() {
        return time;
    }
}
