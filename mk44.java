package com.miolean.arena.genetics;

//In other words, a KMEM entry.

import com.miolean.arena.entities.DefaultGeneticRobot;
import com.miolean.arena.entities.GeneticRobot;
import com.miolean.arena.entities.Robot;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Scanner;

public class Gene {

    private static String version;
    private Method meaning;
    private String description;
    private String category;
    private String arg0Description;
    private String arg1Description;
    private String arg2Description;
    private String notes;
    private int numParameters;
    private double cost;
    private boolean defined;


    private int weight;
    private int bonus;

    @Deprecated
    public static Gene[] loadFromOriginFile() {
        Gene[] KMEM = new Gene[256];
        String[][] data = new String[256][];

        Scanner in = new Scanner(Robot.class.getClassLoader().getResourceAsStream("cfg/ergo_origin.csv"));
        in.useDelimiter("\n");

        in.next(); //First line is blank, sorry
        version = in.next().replaceAll(",", "");
        in.next(); //Be rid of the headers


        for(int i = 0; i < 256 && in.hasNext(); i++) {
            data[i] = in.next().split(",");
        }

        int i;

        for (i = 0; i < data.length; i++) {

            KMEM[i] = new Gene();
            KMEM[i].category = (data[i][0].equals("")) ? "None" : data[i][0];
            //column 1 is ID (opcode); that's not useful here
            //column 2 is name (meaning); we'll get that at the end
            KMEM[i].description = data[i][3];
            KMEM[i].weight = (data[i][4].equals("")) ? 0 : Integer.parseInt(data[i][4]);
            KMEM[i].cost = (data[i][5].equals("")) ? 0 : Double.parseDouble(data[i][5]);
            KMEM[i].bonus = (data[i][6].equals("")) ? 0 : Integer.parseInt(data[i][6]);
            KMEM[i].arg0Description = data[i][7];
            KMEM[i].arg1Description = data[i][8];
            KMEM[i].arg2Description = data[i][9];
            KMEM[i].notes = data[i][10];

            //Get the number of parameters from how many descriptions are available
            if(! KMEM[i].arg0Description.equals("")) KMEM[i].numParameters++;
            if(! KMEM[i].arg1Description.equals("")) KMEM[i].numParameters++;
            if(! KMEM[i].arg2Description.equals("")) KMEM[i].numParameters++;

            try {
                if(KMEM[i].numParameters == 0) KMEM[i].meaning = DefaultGeneticRobot.class.getMethod("_" + data[i][2]);
                else if(KMEM[i].numParameters == 1) KMEM[i].meaning = DefaultGeneticRobot.class.getMethod("_" + data[i][2], int.class);
                else if(KMEM[i].numParameters == 2) KMEM[i].meaning = DefaultGeneticRobot.class.getMethod("_" + data[i][2], int.class, int.class);
                else KMEM[i].meaning = DefaultGeneticRobot.class.getMethod("_" + data[i][2], int.class, int.class, int.class);

            } catch (NoSuchMethodException e) {
                System.err.println("Gene mismatch: " + data[i][2] + " (with " + KMEM[i].numParameters + " parameters) " +
                        "defined in origin file but not substantiated");
            }
            if(KMEM[i].meaning == null) try {
                KMEM[i].meaning = DefaultGeneticRobot.class.getMethod("_UNDEF");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        for(Method m: DefaultGeneticRobot.class.getDeclaredMethods()) {
            if(m.getName().charAt(0) == '_') {
                //For all the methods that it looks like we've designated as reflected
                boolean defined = false;
                for(Gene g: KMEM) if(g.meaning.equals(m)) defined = true;
                if(! defined && !(m.getName().equals("_UNDEF"))) System.err.println("Gene mismatch: " + m.getName() + " present but not defined in origin file");
            }
        }

        return KMEM;
    }

    public static Gene[] loadFromClass(Class<? extends GeneticRobot> clazz) {
        Gene[] result = new Gene[256];

        int index = 0;
        Gene gene;
        GeneCommand geneInfo;
        GeneDescription geneDescription;

        for(Method m: clazz.getMethods()) {

            if(m.isAnnotationPresent(GeneCommand.class)) {
                gene = new Gene();
                geneInfo = m.getAnnotation(GeneCommand.class);
                gene.meaning = m;
                gene.cost = geneInfo.cost();
                gene.weight = geneInfo.weight();
                gene.bonus = geneInfo.bonus();
                gene.numParameters = geneInfo.args(); //I'm aware that there's a difference between parameters and arguments but refuse to change this
                gene.defined = geneInfo.defined();
                result[index] = gene;
                index++;

                if(m.isAnnotationPresent(GeneDescription.class)) {
                    geneDescription = m.getAnnotation(GeneDescription.class);
                    if(! geneDescription.description().equals("")) gene.description = geneDescription.description();
                    if(! geneDescription.arg0().equals("")) gene.arg0Description = geneDescription.arg0();
                    if(! geneDescription.arg1().equals("")) gene.arg1Description = geneDescription.arg1();
                    if(! geneDescription.arg2().equals("")) gene.arg2Description = geneDescription.arg2();
                }
            }

        }
        return result;
    }

    @Override
    public String toString() {
        return (meaning == null? "[no meaning]" : meaning.getName().substring(1));
    }

    public static Gene getUndefinedGene() {
        Gene result = new Gene();
        result.defined = false;
        return result;
    }

    public Method getMeaning() {
        return meaning;
    }
    public static String getVersion() {
        return version;
    }
    public String getDescription() {
        return description;
    }
    public String getCategory() {
        return category;
    }
    public String getArg0Description() {
        return arg0Description;
    }
    public String getArg1Description() {
        return arg1Description;
    }
    public String getArg2Description() {
        return arg2Description;
    }
    public String getNotes() {
        return notes;
    }
    public int getNumParameters() {
        return numParameters;
    }
    public double getCost() {
        return cost;
    }
    public int getWeight() {
        return weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }
    public int getBonus() {
        return bonus;
    }
    public boolean isDefined() {return defined;}

}
