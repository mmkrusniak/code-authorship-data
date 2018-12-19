package com.miolean.arena.input;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class NumericalInput extends Input implements ChangeListener {

    private Integer value = 0;
    private List<WeakReference<JSlider>> sliders = new ArrayList<WeakReference<JSlider>>();
    private int min;
    private int max;


    public NumericalInput(final String name, final String description, int min, int max, int init) {
        super(name, description);
        value = init;
        this.min = min;
        this.max = max;
    }

    @Override
    public JPanel toPanel() {

        JPanel panel = new JPanel();
        JLabel label = new JLabel(getName());
        JSlider slider = new JSlider();
        WeakReference<JSlider> weak = new WeakReference<>(slider);
        sliders.add(weak);
        slider.setMaximum(max);
        slider.setMinimum(min);
        slider.setValue(value);
        slider.addChangeListener(this);


        panel.add(label);
        panel.add(slider);
        slider.setValue(value);

        return panel;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = (Integer) value;
        alertListeners();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        value = ((JSlider)e.getSource()).getValue();
        System.out.println(value);
        for(WeakReference w: sliders) {
            if(w.get() == null) {
                sliders.remove(w);
                continue;
            }
            ((JSlider)w.get()).setValue(value);
        }
        alertListeners();
    }
}
