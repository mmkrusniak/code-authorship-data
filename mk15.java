package com.miolean.arena.framework;

import com.miolean.arena.input.CheckboxInput;
import com.miolean.arena.input.*;
import com.miolean.random.WordRandom;

import java.util.List;
import java.util.Random;

/**
 * Created by commandm on 5/21/17.
 *
 */


public class Option {

    public static NumericalInput updateSpeed = new NumericalInput("Update speed", "The number of update actions to run per second", 1, 1000, 20);
    public static NumericalInput displaySpeed = new NumericalInput("Display speed", "The number of info-display actions to run per second", 1, 1000, 50);
    public static NumericalInput renderSpeed = new NumericalInput("Render speed", "The number of render actions to run per second", 1, 1000, 50);
    public static CompoundInput speedOptions = new CompoundInput("Arena speed options", "Affect the performance of Ergo", updateSpeed, displaySpeed, renderSpeed);

    public static NumericalInput robotSize = new NumericalInput("Robot size", "The default robot size", 5, 300, 40);
    public static NumericalInput scale = new NumericalInput("Render scale", "The magnification of the arena display", 1, 60, 5);
    public static CheckboxInput showDataInRegistries = new CheckboxInput("Show registry data", "Shows the actual value being passed to commands in memory displays (instead of a registry number) ");
    public static CheckboxInput showWireframes = new CheckboxInput("Show wireframs", "Draws the actual bounds of entities");
    public static ArenaInput currentArena = new ArenaInput("Arena", "The current playing area for the program");
    public static EntityInput viewholder = new EntityInput("Entities", "Select robots or other things to view", currentArena);


    private static Input[] inputs = {updateSpeed, displaySpeed, renderSpeed, robotSize, scale, speedOptions, showDataInRegistries, viewholder};


    //Publicly accessible random instances
    public static Random random = new Random();
    public static WordRandom wordRandom = new WordRandom();

    public static boolean[] KEY = new boolean[9];

    //Key constants
    public static final int KEY_Q = 0;
    public static final int KEY_W = 1;
    public static final int KEY_E = 2;
    public static final int KEY_R = 3;
    public static final int KEY_A = 4;
    public static final int KEY_S = 5;
    public static final int KEY_D = 6;
    public static final int KEY_F = 7;
    public static final int KEY_SPACE = 8;

    public static Input fromName(String name) {
        for(Input i: inputs) if(i.getName().equals(name)) return i;
        return null;
    }

}
