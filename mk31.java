package com.miolean.arena.input;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CheckboxInput extends Input {

    private Action action;


    public CheckboxInput(final String name, final String description) {
        super(name, description);

        action = new AbstractAction() {
            {
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_SPACE);
                putValue(Action.SELECTED_KEY, false);
                putValue(Action.SHORT_DESCRIPTION, name);
                putValue(Action.LONG_DESCRIPTION, description);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Action performed! value " + this.getValue(Action.SELECTED_KEY));
                firePropertyChange(Action.SELECTED_KEY, this.getValue(Action.SELECTED_KEY), ((JCheckBox) e.getSource()).isEnabled());
            }
        };
    }

    @Override
    public JPanel toPanel() {

        JPanel panel = new JPanel();
        JLabel label = new JLabel(getName());
        JCheckBox checkBox = new JCheckBox();

        panel.add(checkBox);
        panel.add(label);
        checkBox.setAction(action);

        return panel;
    }

    @Override
    public Boolean getValue() {
        return (Boolean) action.getValue(action.SELECTED_KEY);
    }

    @Override
    public void setValue(Object value) {
        action.putValue(action.SELECTED_KEY, value);
    }
}
