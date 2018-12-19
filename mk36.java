package com.miolean.arena.ui;

import com.miolean.arena.framework.Debug;

import javax.swing.*;
import java.awt.*;

public class DebugPanel extends JPanel {

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Debug.drawDebugLog(g);
        setPreferredSize(new Dimension(400, 3000));
    }

    public void updateInfo() {
        setPreferredSize(new Dimension(400, 3000));
        Debug.refresh();
        repaint();
    }
}
