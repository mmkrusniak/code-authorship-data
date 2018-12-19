package com.miolean.arena.input;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public abstract class Input {

    private List<ChangeListener> listenerList = new ArrayList<>();

    private String name;
    private String description;

    public Input(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public abstract JPanel toPanel();
    public abstract Object getValue();
    public abstract void setValue(Object value);

    public String getName() { return name; }
    public String getDescription() { return description; }

    void alertListeners() {
        for(ChangeListener l: listenerList) l.stateChanged(new ChangeEvent(this));
    }

    public void addChangeListener(ChangeListener cl) {
        listenerList.add(cl);
    }
    public void removeChangeListener(ChangeListener cl) {
        listenerList.remove(cl);
    }

}
