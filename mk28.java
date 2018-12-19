package com.miolean.arena.input;

import com.miolean.arena.entities.Arena;

import javax.swing.*;

public class ArenaInput extends Input {

    private Arena value;

    public ArenaInput(String name, String description) {
        super(name, description);
    }

    @Override
    public JPanel toPanel() {
        return null;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = (Arena) value;
    }
}
