package com.miolean.jdisplayer;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class JDisplayer extends JFrame implements ActionListener, MouseListener, WindowListener {
	//JFrame is serializable
	private static final long serialVersionUID = 1L;
	
	private static final int WIDTH = 500;
	private static final int HIEGHT = 500;
	
	private JMenuBar menuBar;
	private JMenuItem optionMenu;
	private JMenuItem objectMenu;
	private JMenuItem removeAllItem;
	private JMenuItem addItem;
	private JMenuItem importItem;
	
	private JButton addImport;
	private JButton removeImport;
	private JTextField importField;
	private JList<String> importsList; 
	
	private JButton addObject;
	protected ObjectBench bench = new ObjectBench();
	private ObjectDialog newObjectDialog;
	private MethodDialog newMethodDialog;
	private FieldDialog newFieldDialog;
	private JPanel trash;
	private JPopupMenu popup;
	private JObjectMenuItem removeObject = new JObjectMenuItem("Remove");
	private JMenu methodsMenu = new JMenu("Methods");
	private JObjectMenuItem fieldsMenu = new JObjectMenuItem("Fields");
	private JObjectMenuItem copyMenu = new JObjectMenuItem("Copy");
	
	@SuppressWarnings("unused")
	private ObjectIO objectIO;
	
	public static void main(String[] args){
		new JDisplayer();
	}
	
	public JDisplayer() {
		super("JDisplayer");
		
		this.getContentPane().setBackground(new Color(140, 90, 0));
		
		//Uncomment for system look and feel
		//Comment for java look and feel
		/*try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}*/
		
		this.setSize(WIDTH, HIEGHT);
		this.setLayout(null);
		this.setLocation(100, 100);
		this.addWindowListener(this); //Listener-ception
		this.addMouseListener(this);
		
		URL file = this.getClass().getResource("trash.png");
		Image image = null;
		
		trash = new JPanel();
		trash.setLocation(10, 10);
		trash.setSize(50, 50);
		
		try {
			image = ImageIO.read(file).getSubimage(56, 44, 196 - 56, 207).getScaledInstance(trash.getWidth() - 10, trash.getHeight(), BufferedImage.SCALE_SMOOTH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel can = new JLabel(new ImageIcon(image));
		can.setSize(trash.getSize());
		trash.setOpaque(false);
		trash.add(can);
		//trash.setBackground(Color.BLACK);
		this.add(trash);
		
		//
		//Dialog init
		//
		newObjectDialog = new ObjectDialog(this);
		newObjectDialog.setSize(300, 200);
		newObjectDialog.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
		newObjectDialog.setLayout(null);
		
		newFieldDialog = new FieldDialog(this);
		newFieldDialog.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
		newFieldDialog.setLayout(null);
		
		newMethodDialog = new MethodDialog(this);
		newMethodDialog.setSize(300, 200);
		newMethodDialog.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
		newMethodDialog.setLayout(null);
		
		//
		//Button init
		//
		addObject = new JButton("Add Object...");
		addObject.setSize(WIDTH / 2, 20);
		addObject.setLocation(WIDTH / 4, 40);
		addObject.addActionListener(this);
		this.add(addObject);
		
		//
		//Menu init
		//
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		//"Option" menu
		optionMenu = new JMenu("Option");
		menuBar.add(optionMenu);
		importItem = new JMenuItem("Standard packages...");
		importItem.addActionListener(this);
		optionMenu.add(importItem);
		
		//"Object" menu
		objectMenu = new JMenu("Object");
		menuBar.add(objectMenu);
		removeAllItem = new JMenuItem("Remove all [recompile]");
		removeAllItem.setForeground(new Color(200, 0, 0));
		removeAllItem.addActionListener(this);
		addItem = new JMenuItem("Add object...");
		addItem.setForeground(new Color(0, 150, 150));
		addItem.addActionListener(this);
		objectMenu.add(removeAllItem);
		objectMenu.add(addItem);
		
		removeObject.addActionListener(this);
		objectIO = new ObjectIO(bench);
		this.setVisible(true);
		ObjectBox defaultObject = new ObjectBox(new Integer(1), "default");
		defaultObject.addMouseListener(this);
		bench.addObject(defaultObject);
		updateDisplay();
	}

	public void updateDisplay() {
		Point mouseLocation = getMouseLocation();
		mouseLocation.x = mouseLocation.x - 5;
		mouseLocation.y = mouseLocation.y - 50;
		System.out.println(mouseLocation);
		LinkedList<ObjectBox> removables = new LinkedList<ObjectBox>();
		for(ObjectBox b: bench.getObjects()) {
			if(trash.contains(mouseLocation)) removables.add(b);
			if(b.isBeingDragged && this.contains(mouseLocation)) {
				b.setLocation(mouseLocation.x - 90, mouseLocation.y - 10);
			} else if(! this.contains(mouseLocation) && b.isBeingDragged) {
				b.setLocation(100, 100);
			}
			if(! this.isAncestorOf(b)) this.add(b);
		}
		
		for(ObjectBox b: removables) {
			this.remove(b);
			bench.removeObject(b);
		}
	}
	private Point getMouseLocation() {
		Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(mouseLocation, this);
		return mouseLocation;
	}
	
	private void removeAllBoxes() {
		int size = bench.getObjects().size();
		for(int i = 0; i < size; i++) {
			this.remove(bench.getObjects().get(i));
			bench.getObjects().remove(i);
		}
		updateDisplay();
		paint(this.getGraphics());
	}
	
	/*
	private void paintBackground(Graphics g) {
		URL file = this.getClass().getResource("background.png");
		BufferedImage image;
		try {
			image = ImageIO.read(file);
			g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void paint(Graphics g) {
		for(Component c: this.getComponents()) c.paintAll(g.create(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
		paintBackground(g);
	}
	
	@Override
	public void paintAll(Graphics g) {
		paintBackground(g);
		for(Component c: this.getComponents()) c.paintAll(g.create(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
	}
	*/
	//Listener methods
	boolean isDragging = false;
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == addObject) {
			newObjectDialog.OpenObjectDialog();
			updateDisplay();
		}
		if(event.getSource() instanceof JMethodMenuItem) {
			JMethodMenuItem item = (JMethodMenuItem)event.getSource();
			newMethodDialog.setMethod(item.getMethod());
			newMethodDialog.setInvokee(item.getSource().getObject());
			newMethodDialog.OpenMethodDialog();
		}
		if(event.getSource() == removeObject) {
			System.out.println("Removing an objectbox...");
			bench.removeObject(removeObject.getObjectbox());
			this.remove(removeObject.getObjectbox());
			updateDisplay();
			paint(this.getGraphics());
		}
		if(event.getSource() == fieldsMenu) {
			System.out.println("Opening fields dialog...");
			newFieldDialog = new FieldDialog(this);
			newFieldDialog.setLocation(this.getLocation().x + 20, this.getLocation().y + 20);
			newFieldDialog.setLayout(null);
			newFieldDialog.setObject(fieldsMenu.getObjectbox());
			newFieldDialog.OpenFieldDialog();
		}
		if(event.getSource() == removeAllItem) removeAllBoxes();
		if(event.getSource() == copyMenu) {
			try {
				Method m = copyMenu.getObjectbox().getObject().getClass().getDeclaredMethod("clone");
				Object result = m.invoke(copyMenu.getObjectbox().getObject());
				ObjectBox box = new ObjectBox(result, copyMenu.getObjectbox().getName() + "Copy");
				bench.addObject(box);
				box.addMouseListener(this);
				this.add(box);
				this.paint(this.getGraphics());
				updateDisplay();
			} catch(Exception e) {e.printStackTrace();}
		}
		if(event.getSource() == importItem) {
			JDialog importsDialog = new JDialog(this, "[JDisplayer] Manage imports");
			
			String[] imports = new String[bench.getStandardImports().size()];
			for(int i = 0; i < bench.getStandardImports().size(); i++) imports[i] = (String) bench.getStandardImports().get(i);
			importsList = new JList<String>(imports);
			importsList.setSize(200, 100);
			importsList.setOpaque(true);
			importsList.setVisible(true);
			
			JScrollBar scrollbar = new JScrollBar(SwingConstants.VERTICAL);
			scrollbar.setUnitIncrement(3);
			scrollbar.setVisible(true);
			
			JScrollPane pane = new JScrollPane();
			pane.setSize(200, 250);
			pane.setLocation(45, 40);
			pane.setVisible(true);
			pane.setVerticalScrollBar(scrollbar);
			pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			pane.setViewportView(importsList);
			importsDialog.add(pane);
			
			JLabel label = new JLabel("Current imports:");
			label.setSize(200, 25);
			label.setLocation(45, 15);
			importsDialog.add(label);
			
			importField = new JTextField();
			importField.setSize(200, 25);
			importField.setLocation(45, 300);
			importsDialog.add(importField);
			
			addImport = new JButton("Add");
			addImport.setSize(90, 20);
			addImport.setLocation(45, 330);
			addImport.addActionListener(this);
			importsDialog.add(addImport);
			
			removeImport = new JButton("Remove");
			removeImport.setSize(90, 20);
			removeImport.setLocation(155, 330);
			removeImport.addActionListener(this);
			importsDialog.add(removeImport);
			
			importsDialog.setModal(true);
			importsDialog.setSize(300, 400);
			importsDialog.setLayout(null);
			importsDialog.setLocation(this.getX() + 100, this.getY() + 100);
			importsDialog.setVisible(true);
		}
		if(event.getSource() == addImport) {
			String importName = importField.getText();
			if(importName.equals("")) return;
			if(bench.hasStandardImport(importName)) return;
			bench.addStandardImport(importName);
			String[] imports = new String[bench.getStandardImports().size()];
			for(int i = 0; i < imports.length; i++) {
				imports[i] = bench.getStandardImports().get(i);
			}
			importsList.setListData(imports);
		}
		if(event.getSource() == removeImport) {
			String importName = importsList.getSelectedValue();
			if(importName.equals("")) return;
			bench.removeStandardImport(importName);
			String[] imports = new String[bench.getStandardImports().size()];
			for(int i = 0; i < imports.length; i++) {
				imports[i] = bench.getStandardImports().get(i);
			}
			importsList.setListData(imports);
		}
	}
	@Override
	public void mouseClicked(MouseEvent event) {
		
	}
	@Override
	public void mouseEntered(MouseEvent event) {
		
	}
	@Override
	public void mouseExited(MouseEvent event) {
		
	}
	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getButton() == MouseEvent.BUTTON1) {
			System.out.print("Left click on ");
			System.out.println(event.getComponent());
			ObjectBox clickedOn = null;
			for(ObjectBox o: bench.getObjects()) if(o == event.getComponent()) clickedOn = o;
			if(clickedOn == null) return;

			clickedOn.isBeingDragged = true;
			this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}
		
		if(event.getButton() == MouseEvent.BUTTON3 && event.getComponent() instanceof ObjectBox) {
			ObjectBox box = (ObjectBox) event.getComponent();
			Method[] methods = box.getObject().getClass().getMethods();
			ArrayList<JMenuItem> items = new ArrayList<JMenuItem>();
			
			
			System.out.print("Right click on ");
			System.out.println(event.getComponent());
			popup = new JPopupMenu();
			popup.setInvoker(event.getComponent());
			popup.show(this, getMouseLocation().x, getMouseLocation().y);
			
			for(int i = 0; i < methods.length; i++) {
				JMenu menu = null;
				for(int j = 0; j < items.size() && items.get(j) != null; j++) {
					String currentGroup = items.get(j).getName();
					String thisMethodClass = methods[i].getDeclaringClass().getName();
					if(currentGroup.equals(thisMethodClass)) menu = (JMenu) items.get(j);
				}
				if(menu == null) {
					
					menu = new JMenu();
					menu.setText(methods[i].getDeclaringClass().getName());
					menu.setName(methods[i].getDeclaringClass().getName());
					menu.setVisible(true);
					menu.setSize(200, 20);
					items.add(menu);
				}
				
				//Set up method item with proper name
				JMethodMenuItem item = new JMethodMenuItem(methods[i], box);
				item.setMethod(methods[i]);
				String descriptor = Modifier.toString(methods[i].getModifiers()) + " ";
				descriptor += methods[i].getReturnType().getSimpleName() + " ";
				descriptor += methods[i].getName() + "(";
				for(int j = 0; j < methods[i].getParameterTypes().length; j++) {
					Class<?> paramClass = methods[i].getParameterTypes()[j];
					descriptor += paramClass.getSimpleName();
					if(j != methods[i].getParameterTypes().length - 1) descriptor += ", ";
				}
				descriptor += ")";
				item.setText(descriptor);
				item.setOpaque(true);
				if(Modifier.isPublic(methods[i].getModifiers())) item.setBackground(new Color(255, 255, 220));
				if(Modifier.isStatic(methods[i].getModifiers())) item.setBackground(new Color(200, 220, 255));
				if(Modifier.isPrivate(methods[i].getModifiers())) item.setBackground(new Color(255, 100, 100));
				if(Modifier.isNative(methods[i].getModifiers())) item.setForeground(new Color(100, 0, 100));
				
				item.addActionListener(this);
				item.setSize(200, 20);
				menu.add(item);
			}
			
			removeObject.setObjectbox(box);
			removeObject.setForeground(new Color(200, 0, 0));
			popup.add(removeObject);
			
			methodsMenu = new JMenu("Methods");
			methodsMenu.setForeground(new Color(0, 0, 200));
			popup.add(methodsMenu);
			
			fieldsMenu = new JObjectMenuItem("Fields...");
			fieldsMenu.setObjectbox(box);
			fieldsMenu.setForeground(new Color(0, 200, 0));
			fieldsMenu.addActionListener(this);
			popup.add(fieldsMenu);
			
			try {
				box.getObject().getClass().getDeclaredMethod("clone");
				copyMenu = new JObjectMenuItem("Clone");
				copyMenu.setObjectbox(box);
				copyMenu.setForeground(new Color(150, 0, 150));
				copyMenu.addActionListener(this);
				popup.add(copyMenu);
			} catch(Exception e) {}
			
			for(JMenuItem j: items) methodsMenu.add(j);
			popup.setPopupSize(100, 80);
		}
	}
	@Override
	public void mouseReleased(MouseEvent event) {
		if(! (event.getComponent() instanceof ObjectBox)) return;
		updateDisplay();
		
		System.out.println("Unclick");
		for(ObjectBox o: bench.getObjects()) {
			o.isBeingDragged = false;
		}
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		updateDisplay();
	}
	@Override
	public void windowActivated(WindowEvent event) {}
	@Override
	public void windowClosed(WindowEvent event) {}
	@Override
	public void windowClosing(WindowEvent event) {
		System.exit(0);
	}
	@Override
	public void windowDeactivated(WindowEvent event) {}
	@Override
	public void windowDeiconified(WindowEvent event) {}
	@Override
	public void windowIconified(WindowEvent event) {}
	@Override
	public void windowOpened(WindowEvent event) {}
}