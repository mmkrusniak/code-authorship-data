package com.miolean.arena.ui;

import com.miolean.arena.entities.*;
import com.miolean.arena.entities.Robot;
import com.miolean.arena.framework.Debug;
import com.miolean.arena.framework.Option;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.util.ArrayList;

public class GeneralDisplayPanel extends JPanel implements HyperlinkListener, ActiveRobotListener {

    private JTabbedPane tabbedPane;
    MemoryPanel memoryPanel;
    EvolutionPanel evolutionPanel;
    EntityPanel entityPanel;
    DebugPanel debugPanel;
    JPanel controlPanel;

    java.util.List<ActiveRobotListener> listenerList = new ArrayList<ActiveRobotListener>();
    Entity viewholder;
    Entity infoholder;

    Arena arena;


    public GeneralDisplayPanel(Arena arena) {
        this.arena = arena;

        LayoutManager layout = new GridBagLayout();
        setLayout(layout);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        makeMainLayout();
    }

    public void makeMainLayout() {

        memoryPanel = new MemoryPanel(null);
        evolutionPanel = new EvolutionPanel(arena);
        entityPanel = new EntityPanel(arena);
        debugPanel = new DebugPanel();

        JScrollPane debugScrollPane = new JScrollPane();
        debugScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        debugScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        debugScrollPane.setViewportView(debugPanel);

        evolutionPanel.addHyperlinkListener(this);
        entityPanel.addHyperlinkListener(this);

        GridBagConstraints c;

        //Add the info panel:
        //Because of the way borders work we have to use multiple panels...
        tabbedPane = new JTabbedPane();
        JPanel infoPanelPanel = new JPanel();
        infoPanelPanel.setLayout(new BorderLayout());
        infoPanelPanel.add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.setBackground(new Color(0, 155, 0));

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

        tabbedPane.addTab("Program Memory", memoryPanel);
        tabbedPane.addTab("Entities", entityPanel);
        tabbedPane.addTab("Evolution", evolutionPanel);
        tabbedPane.addTab("Debug", debugScrollPane);

        infoPanelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(infoPanelPanel, c);

        //Add the control panel:
        //controlPanel = ((Entity) arena.getEntities().values().toArray()[1]).toPanel();
        controlPanel = Option.viewholder.toPanel();
        JPanel controlPanelPanel = new JPanel();

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.ipadx = 5;
        c.ipady = 5;
        c.weightx = .2;
        c.weighty = .3;
        controlPanelPanel.setLayout(new BorderLayout());
        controlPanelPanel.add(controlPanel, BorderLayout.CENTER);
        controlPanelPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createEtchedBorder()
        ));
        this.add(controlPanelPanel, c);

        //controlPanel.add(Option.speedOptions.toPanel(), c);




    }

    public void display() {

//        memoryPanel.updateInfo();
//        evolutionPanel.updateInfo();
//        entityPanel.updateInfo();
        debugPanel.updateInfo();
        controlPanel.repaint();
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
            infoholder = arena.fromHTML(e.getDescription());
            alertInfoholderChange(((Robot)infoholder));
            if(infoholder.isAlive()) {
                viewholder = infoholder;
                alertViewholderChange(viewholder);
            }
            tabbedPane.setSelectedComponent(memoryPanel);
        }
    }


    public void addActiveRobotListener(ActiveRobotListener l) {
        listenerList.add(l);
    }

    public void removeActiveRobotListener(ActiveRobotListener l) {
        listenerList.remove(l);
    }

    @Override
    public void viewholderChanged(Entity e) {
        Debug.breakpoint();
        viewholder = e;
    }

    @Override
    public void infoholderChanged(Robot e) { if(e instanceof GeneticRobot) memoryPanel.source = (GeneticRobot) e;
    }

    public void alertViewholderChange(Entity e) {
        for(ActiveRobotListener arl: listenerList) arl.viewholderChanged(e);
    }
    public void alertInfoholderChange(Robot e) {
        for(ActiveRobotListener arl: listenerList) arl.infoholderChanged(e);
    }
}
