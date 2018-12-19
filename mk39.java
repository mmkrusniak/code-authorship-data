package com.miolean.arena.input;

import com.miolean.arena.entities.Arena;
import com.miolean.arena.entities.Entity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class EntityInput extends Input {

    private Entity value;
    private ArenaInput arena;

    public EntityInput(String name, String description, ArenaInput arena) {
        super(name, description);
        this.arena = arena;
    }

    @Override
    public JPanel toPanel() {
        JPanel result = new EntityInputPanel();
        return result;
    }

    private class EntityInputPanel extends JPanel implements MouseMotionListener, MouseListener {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int row = 0;
            final int ROW_HEIGHT = 5 + g.getFontMetrics().getHeight();
            final int VERTICAL_OFFSET = 20;

            int mouseRow = (MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y - 8) / ROW_HEIGHT;

            for(Entity e: ((Arena) arena.getValue()).getEntities().values()) {

                if(row == mouseRow) {
                    g.setColor(Color.blue);
                }
                else g.setColor(Color.black);
                g.drawString(e.toString(), 5, VERTICAL_OFFSET + ROW_HEIGHT*row);
                row++;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }


    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = (Entity) value;
        alertListeners();
    }
}
