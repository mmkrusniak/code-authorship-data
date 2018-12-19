package com.miolean.arena.framework;

import com.miolean.arena.entities.Arena;
import com.miolean.arena.input.Input;
import com.miolean.arena.ui.FieldDisplayPanel;
import com.miolean.arena.ui.GeneralDisplayPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main implements Runnable, WindowListener, ActionListener {

    private FieldDisplayPanel fieldDisplayPanel;
    private GeneralDisplayPanel generalDisplayPanel;



    JMenu optionMenu = new JMenu("Option");

    Arena arena;
    JFrame window;
    private Handler handler;
    private boolean isRunning = true;

    public static void main(String[] args) {
        Main main = new Main();

        Thread ergoThread = new Thread(main, "ergoloop");
        ergoThread.run();
    }

    private Main() {
        arena = new Arena();
        fieldDisplayPanel = new com.miolean.arena.ui.FieldDisplayPanel(arena);
        generalDisplayPanel = new GeneralDisplayPanel(arena);
        initializeGUI();
        handler = new Handler(arena);

        Option.currentArena.setValue(arena);

    }

    public void initializeGUI() {
        window = new JFrame("Ergo");
        window.setSize(1200, 700);
        window.setLocation(20, 200);
        window.setLayout(new GridBagLayout());
        window.setResizable(true);
        window.addWindowListener(this);



        JMenuBar menuBar = new JMenuBar();
        window.setJMenuBar(menuBar);

        menuBar.add(optionMenu);


        quickAddMenuItem(Option.speedOptions, optionMenu, "Run speed...", KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_MASK));
        quickAddMenuItem(Option.scale, optionMenu, "Scale...", null);
        quickAddMenuItem(Option.showWireframes, optionMenu, "Show wireframes", null);
        quickAddMenuItem(Option.showDataInRegistries, optionMenu, "Data display...", KeyStroke.getKeyStroke(KeyEvent.VK_CAPS_LOCK, 0));


        //Add the main panel:
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BorderLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 2;
        c.gridwidth = 1;
        c.ipadx = 5;
        c.ipady = 5;
        c.weightx = .7;
        c.weighty = .5;
        mainContainer.add(fieldDisplayPanel, BorderLayout.CENTER);
        mainContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLoweredBevelBorder()
        ));
        window.add(mainContainer, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.ipadx = 5;
        c.ipady = 5;
        c.weightx = .2;
        c.weighty = .5;
        window.add(generalDisplayPanel, c);

        generalDisplayPanel.addActiveRobotListener(fieldDisplayPanel);
        fieldDisplayPanel.addActiveRobotListener(generalDisplayPanel);
        window.setVisible(true);
    }

    private void quickAddMenuItem(Input input, JMenu parent, String text, KeyStroke accelerator) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(this);
        item.setAccelerator(accelerator);
        item.setActionCommand(input.getName());
        parent.add(item);
    }

    public void run() {
        System.out.println("Running...");


        long lastUpdate = System.nanoTime();
        long lastRender = System.nanoTime();
        long lastDisplay = System.nanoTime();

        int updateCycle = (int) (1000000000.0/Option.updateSpeed.getValue());
        int renderCycle = (int) (1000000000.0/Option.renderSpeed.getValue());
        int displayCycle = (int) (1000000000.0/Option.displaySpeed.getValue());

        while(isRunning) {

            long time = System.nanoTime();

            if(time > lastUpdate + updateCycle) {
                handler.tick();
                lastUpdate = System.nanoTime();
                Debug.logTime("Update", -(System.nanoTime()-time));
            }
            time = System.nanoTime();

            if(time > lastRender + renderCycle) {
                fieldDisplayPanel.repaint();
                lastRender = System.nanoTime();
                Debug.logTime("Render", System.nanoTime()-time);
            }
            time = System.nanoTime();

            if(time > lastDisplay + displayCycle) {
                generalDisplayPanel.display();
                lastDisplay = System.nanoTime();
                Debug.logTime("Display", System.nanoTime()-time);

                //Updating the value of the cycles is actually also on the display cycle
                //(it's generally the least urgent cycle)
                updateCycle = (int) (1000000000.0/Option.updateSpeed.getValue());
                renderCycle = (int) (1000000000.0/Option.renderSpeed.getValue());
                displayCycle = (int) (1000000000.0/Option.displaySpeed.getValue());
            }
        }
    }

    @Override public void windowOpened(WindowEvent e) {}
    @Override public void windowClosing(WindowEvent e) {isRunning = false; System.exit(0);}
    @Override public void windowClosed(WindowEvent e) {    }
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() instanceof JMenuItem) {
            JMenuItem m = (JMenuItem) e.getSource();
            System.out.println(m.getActionCommand());
            JPanel launchedPanel = null;
            Input option = Option.fromName(m.getActionCommand());
            if(option != null) launchedPanel = option.toPanel();

            if(launchedPanel != null) {
                JDialog dialog = new JDialog();
                dialog.setTitle(m.getText());
                dialog.add(launchedPanel);
                dialog.pack();
                dialog.setLocation(window.getX() + 40, window.getY() + 40);
                dialog.setModal(true);
                dialog.setVisible(true);
            }
        }
    }
}
