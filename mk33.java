package com.miolean.arena.input;

import javax.swing.*;
import java.awt.*;

public class CompoundInput extends Input {

    private Integer value = 0;
    private Input[] inputs;
    private int min;
    private int max;



    public CompoundInput(final String name, final String description, Input... inputs) {
        super(name, description);
        this.inputs = inputs;
    }

    @Override
    public JPanel toPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();

        JPanel internalPanel = new JPanel(new GridBagLayout());

        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(internalPanel, BorderLayout.CENTER);
        borderPanel.setBorder(BorderFactory.createEtchedBorder());

        internalPanel.setLayout(new GridBagLayout());
        for(Input i: inputs) {
            internalPanel.add(i.toPanel(), g);
            g.gridy += 2;
        }


        JLabel label = new JLabel(getName());
        label.setFont(new Font(null, Font.BOLD, 14));
        g.gridx = 0;
        g.gridy = 0;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(30, 10, 10, 10);
        panel.add(label, g);

        g = new GridBagConstraints();
        g.gridy = 1;
        g.insets = new Insets(10, 10, 10, 10);
        panel.add(borderPanel, g);

        return panel;
    }

    @Override
    public Object getValue() {
        return inputs;
    }

    @Override
    public void setValue(Object value) {
        inputs = (Input[]) value; //it feels ugly to cast an object to an array, but eh
    }
}
