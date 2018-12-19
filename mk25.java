package com.miolean.arena.ui;

import com.miolean.arena.entities.GeneticRobot;
import com.miolean.arena.entities.Robot;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MemoryPanel extends JPanel {

    private JTextPane textPane;
    private JSpinner spinner;
    private JComboBox<String> comboBox;
    private JScrollPane scrollPane;
    private Point scrollPosition;

    GeneticRobot source;

    private static final int INDEX_UMEM = 0;
    private static final int INDEX_PMEM = 1;
    private static final int INDEX_SMEM = 2;
    private static final int INDEX_WMEM = 3;

    MemoryPanel(GeneticRobot source) {

        this.source = source;

        GridBagConstraints c;
        LayoutManager layout = new GridBagLayout();
        setLayout(layout);


        JLabel label = new JLabel();
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = .05;
        c.weightx = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        label.setText("Memory and address:");
        this.add(label, c);

        comboBox = new JComboBox<>();
        comboBox.addItem("UMEM");
        comboBox.addItem("PMEM");
        comboBox.addItem("SMEM");
        comboBox.addItem("WMEM");
        comboBox.setSelectedItem("PMEM");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weighty = .05;
        c.weightx = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        this.add(comboBox, c);

        SpinnerModel model = new SpinnerNumberModel(0, 0, 255, 1);
        spinner = new JSpinner(model);
        spinner.getEditor().setFocusable(false);
        c.gridx = 2;
        c.gridy = 0;
        c.weighty = .05;
        c.weightx = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        this.add(spinner, c);

        scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        textPane = new JTextPane();
        textPane.setEditable(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1;
        c.weightx = 1;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);
        textPane.setSize(1000, 1000);
        scrollPane.setViewportView(textPane);
        this.add(scrollPane, c);

    }

//    void updateInfo() {
//
//
//        StyledDocument doc = new DefaultStyledDocument();
//        String text = "§";
//
//        if(source != null) {
//
//            text += (source.isAlive())? "c\nRobot \"" + source.getName() + "\"\n\n" : "r\nRobot \"" + source.getName() + "\" [dead]\n\n";
//
//            switch (comboBox.getSelectedIndex()) {
//            case INDEX_UMEM:
//                text += (source.stringUMEM((int) spinner.getValue()));
//                break;
//            case INDEX_PMEM:
//                text += (source.stringPMEM((int) spinner.getValue()));
//                break;
//            case INDEX_SMEM:
//                text += (source.stringSMEM((int) spinner.getValue()));
//                break;
//            case INDEX_WMEM:
//                text += (source.stringWMEM());
//                break;
//
//            }
//        } else text += "rNo Robot selected.";
//
//        Scanner scanner = new Scanner(text);
//        scanner.useDelimiter(Pattern.compile("§"));
//        while(scanner.hasNext()) {
//
//            Style style = textPane.addStyle("current", null);
//
//            String next = scanner.next();
//            switch(next.substring(0,1)){
//                case "r": StyleConstants.setForeground(style, Color.RED.darker()); break;
//                case "b": StyleConstants.setForeground(style, Color.BLUE.darker()); break;
//                case "g": StyleConstants.setForeground(style, Color.GREEN.darker()); break;
//                case "y": StyleConstants.setForeground(style, Color.YELLOW.darker()); break;
//                case "m": StyleConstants.setForeground(style, Color.MAGENTA.darker()); break;
//                case "c": StyleConstants.setForeground(style, Color.CYAN.darker()); break;
//                case "k": StyleConstants.setForeground(style, Color.BLACK.darker()); break;
//
//            }
//
//            try {
//                doc.insertString(doc.getLength(), next.substring(1), style);
//            }
//            catch (BadLocationException ignored){}
//        }
//
//        try {
//            scrollPosition = scrollPane.getViewport().getViewPosition();
//            textPane.setDocument(doc);
//
//            Runnable runnable = new Runnable() {
//                @Override
//                public void run() {
//                    scrollPane.getViewport().setViewPosition(scrollPosition);
//                }
//            };
//
//            SwingUtilities.invokeLater(runnable);
//        } catch (Exception e) {
//            System.err.println("Error caught from Swing internals");
//        }
//    }
}
