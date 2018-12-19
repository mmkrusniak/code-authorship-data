package com.miolean.arena.entities;

import com.miolean.arena.framework.Debug;
import com.miolean.arena.framework.Option;
import com.miolean.arena.framework.UByte;
import com.miolean.arena.ui.FieldDisplayPanel;

import javax.swing.*;
import java.awt.*;

import static com.miolean.arena.framework.UByte.ub;

/**
 * Created by commandm on 5/13/17.
 */

/* Memories:
 *
 * K: Genome (all possible genes)
 * U: Super memory; executed rarely
 * P: Program memory; executed regularly
 * S: Storage; mundane
 * W: Registries; used in calculations
 * I: No memory; refers to immediate (literal) values
 */

public abstract class Robot extends Entity {

    //General constants
    protected static final int DEFAULT_STAT_VALUE = 10;
    protected static final int MAX_BULLET_RECHARGE = 40;
    protected static final int MAX_HEAL_RECHARGE = 64;
    protected static final int INITIAL_COGS = 40;
    protected static final double DIFFICULTY = 0.05;


    //Stat constants
    static final int STAT_MAX_HEALTH = 0x0;

    static final int STAT_REGEN = 0x2; //regeneration
    static final int STAT_FIRE_SPEED = 0x3; //shot speed
    static final int STAT_BULLET_SPEED = 0x4;
    static final int STAT_BULLET_SPREAD = 0x5;

    static final int STAT_ROTATE_SPEED = 0x6;
    static final int STAT_SPEED = 0x7; //acceleration (translates to speed due to drag)
    static final int STAT_DAMAGE = 0x8; //shot damage


    protected UByte[] stats = new UByte[9];


    //Color:
    private int hue = 100;

    //State variables:
    private double cogs = 0;



    private String name = "";

    private long lastFireTime = getArena().getTime();
    private long lastHealTime = getArena().getTime();

    //Create a totally blank Robot (for whatever reason)
    Robot(Arena arena) {
        super(Option.robotSize.getValue(), Option.robotSize.getValue(), 10, arena);
        for(int i = 0; i < stats.length; i++) stats[i] = ub(10);
        setMass(10);
    }

    @Override
    public void renderBody(Graphics f, int x, int y, byte flags) {

        int SIZE = getWidth();

        Graphics2D g = (Graphics2D) f;
        if((flags & RENDER_GLOWING) == RENDER_GLOWING) EntityUtils.drawCircularGlow(g, this, x, y);

        g.setColor(Color.black);
        g.drawOval( (x - SIZE/2),  (y - SIZE/2), SIZE, SIZE);


        //Trigonometric functions are expensive so let's use math to minimize
        //how many times we have to calculate them.
        //And yeah, r is rotation. Not radius. Sorry about that, but my keyboard
        //doesn't have a theta. (not that Java would support it probably anyway)
        double sinR = Math.sin(getR());
        double cosR = Math.cos(getR());
        double sind5 = .47943; //As in sine decimal five, or sin(0.5). There is no pi and that is not a mistake.
        double cosd5 = .87758;
        double sind2 = .19867;
        double cosd2 = .98007;
        //sin(r +- u) = sin(r)cos(u) +- sin(u)cos(r)
        //cos(r +- u) = cos(r)cos(u) +- sin(r)sin(u)
        //tan(r +- u) = sin(r +- u) / cos(r +- u) [not that division is so much better]


        //Wheels!
        int[] wheelXPoints = {
                (int) (x+SIZE*.7*(sinR*cosd5 - sind5*cosR)),
                (int) (x+SIZE*.7*(sinR*cosd5 + sind5*cosR)),
                (int) (x-SIZE*.7*(sinR*cosd5 - sind5*cosR)),
                (int) (x-SIZE*.7*(sinR*cosd5 + sind5*cosR))
        };
        int[] wheelYPoints = {
                (int) (y+SIZE*.7*(cosR*cosd5 + sinR*sind5)),
                (int) (y+SIZE*.7*(cosR*cosd5 - sinR*sind5)),
                (int) (y-SIZE*.7*(cosR*cosd5 + sinR*sind5)),
                (int) (y-SIZE*.7*(cosR*cosd5 - sinR*sind5))
        };


        //Gun barrel!
        int[] gunXPoints = {
                (int) (x+SIZE*.7*(cosR*cosd2 - sinR*sind2)), //y + size * a little bit more * cos(r - .5)
                (int) (x+SIZE*.7*(cosR*cosd2 + sinR*sind2)), //y + size * a little bit more * cos(r - .5)
                (x)
        };

        int[] gunYPoints = {
                (int) (y-SIZE*.7*(sinR*cosd2 + sind2*cosR)),
                (int) (y-SIZE*.7*(sinR*cosd2 - sind2*cosR)),
                (y)

        };


        g.setColor(Color.DARK_GRAY);
        g.fillPolygon(wheelXPoints, wheelYPoints, 4); //Wheels
        g.setColor(Color.BLACK);
        g.drawPolygon(wheelXPoints, wheelYPoints, 4); //Wheel outline

        g.setColor(Color.GRAY);
        g.fillPolygon(gunXPoints, gunYPoints, 3); //Barrel

        double healthPercent = (getHealth() / stats[STAT_MAX_HEALTH].val());
        if(healthPercent < 0) healthPercent = 0;
        if(healthPercent > 1) healthPercent = 1;
        g.setColor(new Color((int) (healthPercent * 100 + 100), (int) (healthPercent * 100 + 100), (int) (healthPercent * 100 + 100)));
        g.fillOval( x - SIZE/2,  y - SIZE/2, SIZE, SIZE); //Body

        g.setColor(Color.GRAY);
        g.fillOval( x - SIZE/8,  y - SIZE/8, SIZE/4, SIZE/4); //Beacon
        g.setColor(Color.getHSBColor(hue/256.0f,0.9f,0.5f));
        g.fillOval( x - SIZE/10,  y - SIZE/10, SIZE/5, SIZE/5); //Beacon

        g.setColor(Color.black);
        if((flags & RENDER_DECORATED) == RENDER_DECORATED) g.drawString(name,  x - SIZE,  y - SIZE);
    }

    @Override
    public boolean intersectsWith(Entity e) {
        long marker = System.nanoTime();

        if(e == null) return false;
        if(! (e instanceof Robot || e instanceof TrackerDot)) return false;

        boolean result = (Math.sqrt(getWidth() * getWidth() / 2.0 + e.getWidth() * e.getWidth() / 2.0)
            > Math.sqrt((getX() - e.getX())*(getX() - e.getX())+(getY() - e.getY())*(getY() - e.getY())));
        Debug.logTime("Robot intersections", marker - System.nanoTime());

        return result;
    }

    @Override
    public void intersect(Entity e) {
        repel(e);
        e.damage(getMass());
    }

    @Override
    public String toString() {
        return name + getUUID();
    }

    protected void fire() {
        if(lastFireTime + MAX_BULLET_RECHARGE - stats[STAT_FIRE_SPEED].val() < getArena().getTime()) {
            Bullet bullet = new Bullet(this, getArena());
            add(bullet);
            lastFireTime = getArena().getTime();
        }
    }
    protected void repair() {
        if(lastHealTime + MAX_HEAL_RECHARGE - stats[STAT_REGEN].val()/4 < getArena().getTime() ) {
            cogs--;
            heal(1);
            lastHealTime= getArena().getTime();
        }
    }
    protected void forward(int force) {
        //Essentially, accelerate the tank in the direction it's facing.
        //This will typically take a tank to its max speed (based on drag.)
        //To go slower a tank has to monitor when it's moving forwards.
        if(force > stats[STAT_SPEED].val()) force = stats[STAT_SPEED].val(); //Tanks can't move faster than a certain limit
        if(force < -stats[STAT_SPEED].val()) force = -stats[STAT_SPEED].val(); //Tanks can't move faster than a certain limit

        //Translate polar force into cartesian vector
        setAccX(force *  Math.cos(getR()) / 16); //Scaling!
        setAccY(force * -Math.sin(getR()) / 16);
    }
    protected void rotate(int force) {
        if(force > stats[STAT_ROTATE_SPEED].val()) force = stats[STAT_ROTATE_SPEED].val(); //Robots can't rotate faster than a certain limit
        if(force < -stats[STAT_ROTATE_SPEED].val()) force = -stats[STAT_ROTATE_SPEED].val();

        setAccR( ((double) force )/512 );
    }
    protected void upgrade(UByte stat, int amount) {
        //TODO manage conversion problems with signed UBytes
        //TODO probably needs a cooldown
        cogs -= amount;
        int newValue = amount + stats[Math.abs(stat.val()>>5)].val();
        stats[Math.abs(stat.val()>>5)] = (amount > 255)? ub(255):ub(amount);
    }

    public void onDeath() {
        if(cogs <= 5) cogs=5;
        Cog cog;
        int value;
        int maxValue = (int)(cogs/4)+1;
        while(cogs > 1) {
            value = (int) Math.min(Option.random.nextInt(maxValue-1)+1, cogs);
            cog = new Cog(value, getArena());
            cogs -= value;
            cog.setX(getX());
            cog.setY(getY());
            cog.setVelX(10*(Option.random.nextFloat()-0.5));
            cog.setVelY(10*(Option.random.nextFloat()-0.5));
            add(cog);
        }
    }

    @Override
    public String toHTML() {
        String result = "";

        result += "<a href=ergo_uuid_"+getUUID() + ">";
        if(!isAlive()) result += "<font color=\"red\">";
        else result += "<font color=\"blue\">";
        result += getName();
        if(!isAlive()) result += "</font>";
        return result;
    }

    public double getCogs() { return cogs; }
    public void setCogs(double cogs) { this.cogs = cogs; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHue() {return hue;}
    public void setHue(int hue) {this.hue = hue;}


    @Override
    public void renderStatus(Graphics f, int x, int y, byte flags) {
        super.renderStatus(f, x, y, RENDER_LOW_QUALITY);

        Graphics2D g = (Graphics2D) f;

        if((flags & RENDER_GLOWING) == RENDER_GLOWING) {
            float[] dist = {0.0f, 0.7f};
            Color[] colors = {Color.BLUE, FieldDisplayPanel.BACKGROUND_COLOR};
            g.setPaint(new RadialGradientPaint((float) getX(), (float) getY(), (float) (getWidth() + 8), dist, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE));
            g.fillOval((int) (getX() - (getWidth() + 8)/2), (int) (getY() - (getHeight() + 8)/2), (int) (getWidth() + 8), (int) (getHeight() + 8));

        }

        g.setColor(new Color(100, 100, 255, 200));
        g.fillRect(x, y + 25, (int) getCogs(), 20);
        g.setColor(Color.BLACK);
        g.drawRect(x, y + 25, (int) getCogs(), 20);
        if(getCogs() < 20) g.drawString((int) getCogs() + "", x + 3 + (int) getCogs(), y + 25 + 17);
        else g.drawString(getCogs() + "", x+3, y + 25 + 17);

        Color[] statColors = {
                new Color(240, 96, 117), //Max health
                new Color(0, 0, 0), //Max cogs
                new Color(196, 36, 240), //Regen rate
                new Color(240, 186, 122), //Fire speed
                new Color(18, 240, 120), //Bullet speed
                new Color(240, 103, 32), //Bullet spread
                new Color(240, 195, 53), //Rotate speed
                new Color(56, 240, 231), //Movement speed
                new Color(240, 24, 8), //Damage

        };

        for(int statNumber = 0; statNumber < 8; statNumber++) {

            g.setColor(statColors[statNumber]);
            g.fillRect(x + 110 + (statNumber/3)*40, y + statNumber%3*25, 30, 20);
            g.setColor(Color.black);
            g.drawRect(x + 110 + (statNumber/3)*40, y + statNumber%3*25, 30, 20);
            g.drawString(stats[statNumber].val() + "",x + 110 + (statNumber/3)*40 + 2, y + statNumber%3*25 + 17);
        }
    }

    @Override
    public JPanel toPanel() {
        JPanel entityPanel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setFont(g.getFont().deriveFont(Font.BOLD));
                g.drawString(name, 15, 25);
                g.setFont(g.getFont().deriveFont(Font.PLAIN));

                g.drawRoundRect(15, 35, Robot.this.getWidth() + 30, Robot.this.getHeight() + 30, 4, 4);
                Robot.this.renderBody(g, 15 + (Robot.this.getWidth() + 30) / 2, 35 + (Robot.this.getHeight() + 30) / 2, RENDER_GLOWING);
                Robot.this.renderStatus(g, Robot.this.getWidth() + 70, 35, RENDER_LOW_QUALITY);

                g.drawLine(5, 125, getWidth()-5, 125);
            }
        };
        return entityPanel;
    }

    @Override
    public Polygon getBaseBounds() {
        return new Polygon(
                new int[] {
                        (int) (getWidth()/1.4), //Barrel, outer right vertex
                        getWidth()/2,           //Barrel, inner right vertex
                        (int) (getWidth()/3.0), //Right wheel, inner upper vertex
                        (int) (getWidth()/3.0), //Right wheel, outer upper vertex
                        (int) -(getWidth()/3.0),//Right wheel, outer lower vertex
                        (int) -(getWidth()/3.0),//Right wheel, inner lower vertex
                        -getWidth()/2,          //Back end
                        (int) -(getWidth()/3.0),//Left wheel, inner lower vertex
                        (int) -(getWidth()/3.0),//Left wheel, outer lower vertex
                        (int) (getWidth()/3.0), //Left wheel, outer upper vertex
                        (int) (getWidth()/3.0), //Left wheel, inner upper vertex
                        getWidth()/2,           //Barrel, outer left vertex
                        (int) (getWidth()/1.4)},//Barrel, inner left vertex
                new int[] {
                        getHeight()/6,          //Barrel, outer right vertex
                        getHeight()/7,          //Barrel, inner right vertex
                        (int) (getHeight()/2.4),//Right wheel, inner upper vertex
                        (int) (getHeight()/1.6),//Right wheel, outer upper vertex
                        (int) (getHeight()/1.6),//Right wheel, outer lower vertex
                        (int) (getHeight()/2.4),//Right wheel, inner lower vertex
                        0,                      //Back end
                        (int) -(getHeight()/2.4),//Left wheel, inner lower vertex
                        (int) -(getHeight()/1.6),//Right wheel, outer lower vertex
                        (int) -(getHeight()/1.6),//Right wheel, outer upper vertex
                        (int) -(getHeight()/2.4),//Left wheel, inner upper vertex
                        -getHeight()/7,         //Barrel, outer left vertex
                        -getHeight()/6},        //Barrel, inner left vertex
                13
        );
    }
}
