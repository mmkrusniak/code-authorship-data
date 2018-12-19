package com.miolean.arena.ui;

import com.miolean.arena.framework.Perpetual;

import javax.swing.*;
import java.awt.*;

public abstract class LivePanel extends JPanel {

    private JPanel topBar;

    public LivePanel() {
        super.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1.0;
        g.weighty = 0.1;
        g.fill = GridBagConstraints.BOTH;


        topBar = new JPanel();
        topBar.setBackground(new Color(220, 220, 220));
        this.setBackground(new Color(220, 220, 220));
        add(topBar, g);
    }

    public abstract void display();

    @Override
    public Component add(Component c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 1;
        g.gridx = 0;
        g.weighty = 0.9;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.BOTH;
        super.add(c, g);
        return c;
    }

}
