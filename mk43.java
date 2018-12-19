package com.miolean.arena.ui;

import com.miolean.arena.entities.*;
import com.miolean.arena.entities.Robot;
import com.miolean.arena.framework.Option;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import static com.miolean.arena.entities.Arena.ARENA_SIZE;
import static com.miolean.arena.entities.Arena.BORDER;

public class FieldDisplayPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ActiveRobotListener{

    java.util.List<ActiveRobotListener> listenerList = new ArrayList<>();

    Entity viewholder;
    Arena arena;

    public static final Color BACKGROUND_COLOR = new Color(170, 170, 160);

    private boolean isRunning = true;


    public FieldDisplayPanel(Arena arena) {

        this.arena = arena;

        requestFocus();

        viewholder = new ControlledRobot(300, 300, arena);
        arena.add(viewholder);

        Bullet rogue = new Bullet(null, arena);
        rogue.setX(200);
        rogue.setY(200);
        arena.add(rogue);

        com.miolean.arena.entities.Robot dummy = new DefaultGeneticRobot(Option.class.getClassLoader().getResourceAsStream("gen/cain.ergo"), arena);
        dummy.setHealth(256);

        arena.add(dummy);

        this.setBackground(BACKGROUND_COLOR);

        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        requestFocus();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        render((Graphics2D) g); //It is always safe to make this cast apparently
    }



    public void setViewholder(int x, int y) {
        Entity e = arena.atLocation(x, y);

        if (e == null || ! e.isAlive() || e instanceof ControlledRobot) {
            if (viewholder instanceof ControlledRobot && viewholder.isAlive()) {
                viewholder.setX(x);
                viewholder.setY(y);
                return;
            } else {
                e = new ControlledRobot(x, y, arena);
                arena.add(e);
            }
        }

        if (viewholder instanceof ControlledRobot) {
            arena.remove(viewholder);
        }

        if (e instanceof GeneticRobot)
            alertInfoholderChange((Robot)e);

        viewholder = e;

        alertViewholderChange(e);
    }

    public void setViewholder(Entity e) {

        if(e == null) throw new NumberFormatException("Null viewholder.");

        if (viewholder instanceof ControlledRobot) arena.remove(viewholder);
        viewholder = e;

        if (viewholder instanceof com.miolean.arena.entities.Robot && !(viewholder instanceof ControlledRobot))
            alertInfoholderChange((Robot)e);
        alertViewholderChange(e);
    }

    private void render(Graphics2D g) {

        double scale = Option.scale.getValue()/5.0;

        g.setColor(Color.BLACK);
        g.drawOval(this.getWidth()/2, this.getHeight()/2, 2, 2);

        g.translate((int) (-viewholder.getX() + this.getWidth()/2*scale)/scale, (int) (-viewholder.getY() + this.getHeight()/2*scale)/scale);
        g.scale(1.0/scale,1.0/scale);

        g.setColor(Color.GRAY);
        for(int i = 0; i < ARENA_SIZE / 64; i++) {
            g.drawLine(i*64, BORDER, i*64, ARENA_SIZE -BORDER);
            g.drawLine(BORDER, i*64, ARENA_SIZE - BORDER, i*64);
        }

        g.setColor(Color.RED);
        g.drawRect(10, 10, ARENA_SIZE - BORDER, ARENA_SIZE - BORDER);

        Point mouse = new Point(
                MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x -(int)(-viewholder.getX() + this.getWidth()/2),
                MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y -(int)(-viewholder.getY() + this.getHeight()/2)
                );
        arena.renderAll(g, mouse);
        g.scale(scale, scale);

        g.translate((int) -(-viewholder.getX() + this.getWidth()/2), (int) -(-viewholder.getY() + this.getHeight()/2));

        //Apply no translations to these things
        viewholder.renderStatus(g, 20, getHeight()-100, Entity.RENDER_LOW_QUALITY);

    }

    @Override public void keyTyped(KeyEvent e) {
        char key = e.getKeyChar();
        if(key == 'l') {
            System.out.println("============Top robots============");
            for(com.miolean.arena.entities.GeneticRobot t: arena.getTopRobots()) {
                System.out.println("Robot " + t + " [Fitness " + t.getFitness() + "]");
            }
        }
    }
    @Override public void keyPressed(KeyEvent e) {
        char key = e.getKeyChar();
        if(key == 'q') Option.KEY[Option.KEY_Q] = true;
        if(key == 'w') Option.KEY[Option.KEY_W] = true;
        if(key == 'e') Option.KEY[Option.KEY_E] = true;
        if(key == 'r') Option.KEY[Option.KEY_R] = true;
        if(key == 'a') Option.KEY[Option.KEY_A] = true;
        if(key == 's') Option.KEY[Option.KEY_S] = true;
        if(key == 'd') Option.KEY[Option.KEY_D] = true;
        if(key == 'f') Option.KEY[Option.KEY_F] = true;
        if(key == ' ') Option.KEY[Option.KEY_SPACE] = true;
    }
    @Override public void keyReleased(KeyEvent e) {
        char key = e.getKeyChar();
        if(key == 'q') Option.KEY[Option.KEY_Q] = false;
        if(key == 'w') Option.KEY[Option.KEY_W] = false;
        if(key == 'e') Option.KEY[Option.KEY_E] = false;
        if(key == 'r') Option.KEY[Option.KEY_R] = false;
        if(key == 'a') Option.KEY[Option.KEY_A] = false;
        if(key == 's') Option.KEY[Option.KEY_S] = false;
        if(key == 'd') Option.KEY[Option.KEY_D] = false;
        if(key == ' ') Option.KEY[Option.KEY_SPACE] = false;
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }
    @Override public void mouseClicked(MouseEvent e) {

        int x = (int) (e.getX() + viewholder.getX() - this.getWidth() / 2);
        int y = (int) (e.getY() + viewholder.getY() - this.getHeight() / 2);

        if(e.getButton() == MouseEvent.BUTTON1) {
            if (!this.hasFocus()) {
                this.requestFocus();
                return;
            }

            setViewholder(x, y);
        }

        if(e.getButton() == MouseEvent.BUTTON3) {
            Cog cog = new Cog(30, arena);
            cog.setX(x);
            cog.setY(y);
            arena.add(cog);
        }

        if(e.getButton() == MouseEvent.BUTTON2) {
            com.miolean.arena.entities.Robot creation = new DefaultGeneticRobot(Option.class.getResourceAsStream("cain.ergo"), arena);
            creation.setX(x);
            creation.setY(y);
            creation.setName("creation");
            arena.add(creation);
        }
    }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}

    public void addActiveRobotListener(ActiveRobotListener l) {
        listenerList.add(l);
    }

    public void removeActiveRobotListener(ActiveRobotListener l) {
        listenerList.remove(l);
    }

    @Override
    public void viewholderChanged(Entity e) {
        viewholder = e;
    }

    @Override
    public void infoholderChanged(Robot e) {
        //We don't really care about this
    }

    public void alertViewholderChange(Entity e) {
        for(ActiveRobotListener arl: listenerList) arl.viewholderChanged(e);
    }
    public void alertInfoholderChange(Robot e) {
        for(ActiveRobotListener arl: listenerList) arl.infoholderChanged(e);
    }
}