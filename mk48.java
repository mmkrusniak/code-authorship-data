package com.miolean.arena.entities;

import com.miolean.arena.framework.Option;
import com.miolean.arena.framework.UByte;
import com.miolean.arena.genetics.Gene;
import com.miolean.arena.ui.FieldDisplayPanel;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import java.util.Stack;

import static com.miolean.arena.entities.Arena.ARENA_SIZE;
import static com.miolean.arena.framework.UByte.ub;
import static com.miolean.arena.framework.UByte.ubDeepCopy;

public class GeneticRobot extends Robot implements Comparable<GeneticRobot>{



    public static final Gene[] KMEM;
    protected UByte[][] UMEM;
    protected UByte[][] PMEM;
    protected UByte[][] SMEM;
    protected UByte[] WMEM;
    protected UByte[][] CURRENT = PMEM;

    //Program index:
    protected int index = 0;
    protected int loaded = 0;
    protected Stack<Point> stack = new Stack<>();


    private double fitness = 0;
    private int generation = 0;

    private static int totalKWeight;

    static {
        KMEM = Gene.loadAll(false);
        for(Gene g: KMEM) {
            if(g != null) totalKWeight += g.getWeight();
        }
    }

    //Create a Robot from a parent
    public GeneticRobot(GeneticRobot parent, Arena arena){
        super(arena);

        String name = Option.wordRandom.nextWord();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        setName(name);

        setGeneration(parent.getGeneration() + 1);

        UMEM = ubDeepCopy(parent.UMEM);
        PMEM = ubDeepCopy(parent.PMEM);
        SMEM = ubDeepCopy(parent.SMEM);
        WMEM = new UByte[256];
        for(int i = 0; i < 256; i++) WMEM[i] = ub(0);
        //KMEM is immutable
        //IMEM doesn't exist

        //Stats
        for(int i = 0; i < stats.length; i++) stats[i] = ub(DEFAULT_STAT_VALUE);

        int maxOffset = ARENA_SIZE / 4;
        setX(parent.getX() + maxOffset * (Option.random.nextFloat()*2-1));
        setY(parent.getY() + maxOffset * (Option.random.nextFloat()*2-1));
        setCogs(INITIAL_COGS);
    }

    //Create a Robot from a file
    public GeneticRobot(InputStream file, Arena arena) {

        super(arena);
        //0: Initial values.

        setName("Unnamed");
        setGeneration(0);

        //1: Initialize memories.
        UMEM = new UByte[256][];
        PMEM = new UByte[256][];
        SMEM = new UByte[256][];
        WMEM = new UByte[256];

        UMEM[0] = new UByte[256];
        PMEM[0] = new UByte[256];
        SMEM[0] = new UByte[256];


        //3: Initialize the memories at 0
        createMemory(SMEM, 0);
        createMemory(UMEM, 0);
        createMemory(PMEM, 0);

        WMEM = new UByte[256];
        for(int i = 0; i < 256; i++) WMEM[i] = ub(0);

        //4: Grab actual values from file
        compile(file);

        //5: Anything else?
        setCogs(INITIAL_COGS);
    }

    private void compile(InputStream file) {
        Scanner in = new Scanner(file);
        String next;

        int loadIndex = 0;
        int loadMode = 0;
        int loadMemory = 0;

        while (in.hasNext()) {
            next = undecorate(in.next()).trim();

            //If next is a comment: Run through it until it ends
            if (next.equals("##")) {
                do next = in.next().trim();
                while (!next.equals("#/"));
                next = undecorate(in.next()).trim();
            }

            //If next is a command: Translate it into its number
            if (next.startsWith("_")) {
                if (loadMode < 2) throwCompileError("Command in passive memory.");
                System.out.println("");
                for (int i = 0; i < KMEM.length; i++) {
                    if (KMEM[i].getMeaning() != null && KMEM[i].getMeaning().getName().equals(next)) next = i + "";
                }

                if (next.startsWith("_")) {
                    //So that didn't work
                    throwCompileError("Could not resolve command " + next);
                }
            }

            if (next.charAt(0) == '-') next = "0";

            //If next is a define statement, define a new memory
            if (next.equals("define")) {
                int defMemory;

                try {
                    next = undecorate(in.next()).trim();
                    defMemory = Integer.parseInt(undecorate(in.next()).trim());

                    switch (next) {
                        case "registry":
                            throwCompileError("Cannot define new registry.");
                            break;
                        case "storage":
                            createMemory(SMEM, defMemory);
                            break;
                        case "program":
                            createMemory(PMEM, defMemory);
                            break;
                        case "meta":
                            createMemory(UMEM, defMemory);
                            break;
                    }

                } catch (NumberFormatException e) {
                    throwCompileError("Memory and length expected after \"define\"");
                }
            }

            //If next is a edit statement: load the memory it specifies, and reset the index
            else if (next.equals("edit")) {
                loadIndex = 0;
                next = undecorate(in.next()).trim();
                switch (next) {
                    case "registry":
                        loadMode = 0;
                        break;
                    case "storage":
                        loadMode = 1;
                        break;
                    case "program":
                        loadMode = 2;
                        break;
                    case "meta":
                        loadMode = 3;
                        break;
                }

                //Load multi-memory if it's that kind of memory
                if (!next.equals("registry")) {
                    next = undecorate(in.next()).trim();
                    loadMemory = Integer.parseInt(next);

                    if (loadMemory > 255) throwCompileError("Memory number out of bounds; must be between 0 and 255");

                    switch (loadMode) {
                        case 1:
                            if (SMEM[loadMemory] == null) throwCompileError("No storage defined at " + loadMemory);
                            break;
                        case 2:
                            if (PMEM[loadMemory] == null) throwCompileError("No program defined at " + loadMemory);
                            break;
                        case 3:
                            if (UMEM[loadMemory] == null) throwCompileError("No meta defined at " + loadMemory);
                            break;

                    }
                }
            }

            //If next is an at statement: move the index to what is specified
            else if (next.equals(("at"))) {
                next = undecorate(in.next()).trim();
                try {
                    loadIndex = Integer.parseInt(next);
                } catch (NumberFormatException e) {
                    throwCompileError("Index expected after \"at\"; instead received \"" + next + "\"");
                }
                if (loadIndex > 255) throwCompileError("\"at\" index invalid (must be between 0 and 255)");
            }

            //If next is the "name" command: name the tank
            else if (next.equals("name")) {
                next = undecorate(in.next()).trim();
                this.setName(next);
            }
            //If next is the "print" command: print everything up to the next "#"
            else if (next.equals("print")) {
                do {
                    next = in.next().trim();
                    System.out.print(next + " ");
                } while (!next.equals("#"));
                System.out.println();
            }

            //At this point this is pretty clearly an entry.
            else {
                try {
                    switch (loadMode) {
                        case 0:
                            if (WMEM[loadIndex].val() != 0)
                                throwCompileError("Registry at " + loadIndex + " already defined.");
                            WMEM[loadIndex] = ub(Integer.parseInt(next));
                            break;
                        case 1:
                            if (SMEM[loadMemory][loadIndex].val() != 0)
                                throwCompileError("Storage " + loadMemory + " at " + loadIndex + " already defined.");
                            if (loadIndex >= SMEM[loadMemory].length)
                                throwCompileError("Storage " + loadMemory + " index out of bounds.");
                            SMEM[loadMemory][loadIndex] = ub(Integer.parseInt(next));
                            break;
                        case 2:
                            if (PMEM[loadMemory][loadIndex].val() != 0)
                                throwCompileError("Program " + loadMemory + " at " + loadIndex + " already defined.");
                            if (loadIndex >= PMEM[loadMemory].length)
                                throwCompileError("Program " + loadMemory + " index out of bounds.");
                            PMEM[loadMemory][loadIndex] = ub(Integer.parseInt(next));
                            break;
                        case 3:
                            if (UMEM[loadMemory][loadIndex].val() != 0)
                                throwCompileError("Meta " + loadMemory + " at " + loadIndex + " already defined.");
                            if (loadIndex >= UMEM[loadMemory].length)
                                throwCompileError("Meta " + loadMemory + " index out of bounds.");
                            UMEM[loadMemory][loadIndex] = ub(Integer.parseInt(next));
                            break;
                    }
                    loadIndex++;
                } catch (NumberFormatException e) {
                    throwCompileError("Entry expected; instead received \"" + next + "\"");
                }
            }
        }
    }
    private String undecorate(String input) {
        input = input.replace(';', ' ');
        input = input.replace(':', ' ');
        input = input.replace(',', ' ');
        input = input.replace('{', ' ');
        input = input.replace('}', ' ');
        return input;
    }
    private void throwCompileError(String reason) {
        System.err.println("Error compiling Robot " + getName() + ":");
        System.err.println(reason);
        //TODO Exiting immediately is not appropriate here; should throw an exception
    }

    @Override
    public void update() {

        double initialCogs = getCogs();

        //Reload relevant properties
        setWidth(Option.robotSize.getValue());
        setHeight(Option.robotSize.getValue());

        //Apply physics
        applyPhysics();

        //Run the loaded P memory!
        runGenes(PMEM);

        //Congratulations on not dying! Used cogs go towards fitness, excluding the survival ones
        //Unless you actually are dying. Then we have a problem.
        if(getCogs() > 0) setFitness(getFitness() + initialCogs - getCogs() - DIFFICULTY);

        //Make sure health is valid
        if(getHealth() > stats[STAT_MAX_HEALTH].val()) setHealth(stats[STAT_MAX_HEALTH].val());
        if(getCogs() <= 0) damage(1);
    }


    void runGenes(UByte[][] genes) {

        setCogs(getCogs() - DIFFICULTY);

        CURRENT = genes;
        index = 0;
        loaded = 0;

        while(index < genes.length) {

            if(getCogs() < 0) break;


            if(genes[loaded][index] == null) {
                System.err.println("Null gene");
            }
            Gene gene = KMEM[genes[loaded][index].val()];


            try {
                if(gene.getNumParameters() == 0) {
                    gene.getMeaning().invoke(this);
                } else if(gene.getNumParameters() == 1 && index < 255) {
                    gene.getMeaning().invoke(this, genes[loaded][index+1].val());
                } else if(gene.getNumParameters() == 2 && index < 254) {
                    gene.getMeaning().invoke(this, genes[loaded][index+1].val(), genes[loaded][index+2].val());
                } else if(index < 253){
                    gene.getMeaning().invoke(this, genes[loaded][index+1].val(), genes[loaded][index+2].val(), genes[loaded][index+3].val());
                }
                setCogs(getCogs() - gene.getCost());
                index += 1 + gene.getNumParameters();
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.err.println("Error when running gene " + gene + " with " + gene.getNumParameters() + " parameters");
                e.printStackTrace();
            }

            if(index >= genes.length && stack.size() > 0) {
                Point entry = stack.pop();
                loaded = entry.x;
                index = entry.y;
            }
        }

    }



    //Instantiate memory number [number] as a UByte[256].
    protected void createMemory(UByte[][] memory, int number) {
        memory[number] = new UByte[256];
        for(int i = 0; i < 256; i++) {
            memory[number][i] = ub(0);
        }
    }

    //Destroy memory number [number], making it null.
    void destroyMemory(UByte[][] memory, UByte number) {
        //Never ever destroy memory 0.
        if(number.val() != 0) {
            memory[number.val()] = null;
        }
        if(number.val() == loaded) loaded = 0;
    }

    public void onBirth() {
        //Run the loaded U memory!
        runGenes(UMEM);
    }

    void reproduce() {
        Robot offspring = new DefaultGeneticRobot(this, getArena());
        add(offspring);
    }

    private String activeMemoryToString(UByte[] genes, boolean color) {
        if(genes == null) return "§r No memory exists here.";

        String result = "";

        //You'll notice we stole this from runGenes(). That's intentional.

        for(int i = 0; i < genes.length-3; i++) {
            //For every entry in this list of genes (excluding the ones at the end that don't have enough others after them as arguments)

            if(genes[i] != null) { //we really can't afford to lose track of null genes
                if (genes[i].val() == 0x00)
                    continue; //Don't even bother with opcode 0x00, standing for "do nothing"
                if (KMEM[genes[i].val()] == null)
                    continue; //If the opcode doesn't actually stand for something meaningful, skip it too
            }

            //Since everything appears to be in order, let's try to parse that as a gene. (Normally we'd run it.)
            result += "§k" + i + " ";
            result += (genes[i] == null)? "§rNULL \n" : "§g" + KMEM[genes[i].val()].getMeaning().getName() + " §k(";
            result += (genes[i+1] == null)? "§rNULL \n" : "§b" + genes[i+1].val() + " §k[" + WMEM[genes[i+1].val()].val() + "], ";
            result += (genes[i+2] == null)? "§rNULL \n" : "§b" + genes[i+2].val() + " §k[" + WMEM[genes[i+2].val()].val() + "],";
            result += (genes[i+3] == null)? "§rNULL \n" : "§b" + genes[i+3].val() + " §k[" + WMEM[genes[i+3].val()].val() + "]) \n";


            //Assuming nothing went wrong we've completed a command by now. (If something did go wrong, we'll at least have a stack trace.)
            i += 3; //We don't want to run the arguments by accident, so let's skip them.

        }

        if(! color) {
            result = result.replaceAll("§.", "");
        }

        return result;
    }
    private String passiveMemoryToString(UByte[] genes, boolean color) {
        if(genes == null) return "§r No memory exists here.";
        String result = "§b";

        for(int i = 0; i < genes.length; i++) {
            if(i%4 == 0) result += "\n" + i/16 + i%16 + "\t";
            result += "§k|  " + genes[i].val() +"\t§b";
        }

        if(! color) {
            result.replaceAll("§\\D", "");
        }

        return result;
    }

    public String stringUMEM(int memory) {return activeMemoryToString(UMEM[memory], true);}
    public String stringPMEM(int memory) {return activeMemoryToString(PMEM[memory], true);}
    public String stringSMEM(int memory) {return passiveMemoryToString(SMEM[memory], true);}
    public String stringWMEM() {return passiveMemoryToString(WMEM, true);}

    static UByte randomAddress(UByte[] memory) {

        int totalExist = 0;
        for(UByte u: memory) if(u != ub(0)) totalExist++;

        int selection = (int) (totalExist * Option.random.nextFloat());
        int i;
        for(i = 0; selection > 0; i++) {
            if(memory[i] != ub(0)) selection--;
        }
        return ub(i);
    }
    static UByte randomGene() {

        int rand = (int) (Option.random.nextFloat() * totalKWeight);
        int selection = 0;
        while(rand > 0 && selection < KMEM.length) {
            if(KMEM[selection] != null) rand -= KMEM[selection].getWeight();
            selection++;
        }

        return ub(selection);


    }

    @Override
    public void renderStatus(Graphics f, int x, int y, byte flags) {
        super.renderStatus(f, x, y, flags);

        Graphics2D g = (Graphics2D) f;
        if((flags & RENDER_GLOWING) == RENDER_GLOWING) {
            float[] dist = {0.0f, 0.7f};
            Color[] colors = {Color.BLUE, FieldDisplayPanel.BACKGROUND_COLOR};
            g.setPaint(new RadialGradientPaint((float) getX(), (float) getY(), (float) (getWidth() + 8), dist, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE));
            g.fillOval((int) (getX() - (getWidth() + 8)/2), (int) (getY() - (getHeight() + 8)/2), (int) (getWidth() + 8), (int) (getHeight() + 8));

        }

        g.setColor(new Color(100, 255, 100, 200));
        g.fillRect(x, y + 50, (int) getFitness(), 20);
        g.setColor(Color.BLACK);
        g.drawRect(x, y + 50, (int) getFitness(), 20);
        if(getFitness() < 20) g.drawString(((int) getFitness()) + "", x + 3 + (int) getFitness(), y + 50 + 17);
        else g.drawString((int) getFitness() + "", x+3, y + 50 + 17);
    }


    @Override
    public int compareTo(GeneticRobot o) {
        return (int) (fitness - o.getFitness());
    }
    public double getFitness() { return fitness; }
    public void setFitness(double fitness) { this.fitness = fitness; }
    public int getGeneration() { return generation; }
    public void setGeneration(int generation) { this.generation = generation; }

    public UByte wmemAt(int i) {return WMEM[i];}
    public UByte smemAt(int i, int j) {return SMEM[i][j];}
    public UByte pmemAt(int i, int j) {return PMEM[i][j];}
    public UByte umemAt(int i, int j) {return UMEM[i][j];}
}
