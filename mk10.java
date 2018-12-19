package com.miolean.jdisplayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

public class ObjectDialog extends JDialog implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private JDisplayer source;
	
	private static int newNameNumber = 1;
	private JTextField objectDialogName;
	private JTextField objectDialogParameters;
	private JButton objectDialogGo;
	private JLabel objectDialogOutput;
	private ObjectIO objectIO;
	private JLabel classNamelabel;
	private JLabel parametersLabel;
	
	public ObjectDialog(JDisplayer source) {
		super(source, "[JDisplayer] Instantiate object...");
		this.setVisible(false);
		this.source = source;
		objectIO = new ObjectIO(source.bench);
		this.setModal(true);
		
		classNamelabel = new JLabel("New object name: ");
		classNamelabel.setSize(200, 20);
		classNamelabel.setLocation(50, 10);
		this.add(classNamelabel);
		
		objectDialogOutput = new JLabel("");
		objectDialogOutput.setSize(280, 20);
		objectDialogOutput.setLocation(10, 135);
		this.add(objectDialogOutput);
		
		objectDialogName = new JTextField();
		objectDialogName.setSize(200, 20);
		objectDialogName.setLocation(50, 30);
		this.add(objectDialogName);
		
		parametersLabel = new JLabel("Initializer [ i.e. new Integer(1) ]");
		parametersLabel.setSize(200, 20);
		parametersLabel.setLocation(50, 60);
		this.add(parametersLabel);
		
		objectDialogParameters = new JTextField();
		objectDialogParameters.setSize(200, 20);
		objectDialogParameters.setLocation(50, 80);
		this.add(objectDialogParameters);
		
		objectDialogGo = new JButton("Create");
		objectDialogGo.setSize(180, 20);
		objectDialogGo.setLocation(60, 110);
		objectDialogGo.addActionListener(this);
		this.add(objectDialogGo);
		
	}
	
	public void OpenObjectDialog() {
		objectDialogOutput.setText("");
		
		this.setVisible(true);
	}

	//**************************************
	//Following are Listener methods.
	//**************************************
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == objectDialogGo) {
			//Object makin' time
			String objectName = "";
			if(objectDialogName.getText().equals("")) {
				objectName = "newObject" + newNameNumber;
				newNameNumber++;
			} else objectName = objectDialogName.getText();
			
			for(int i = 0; i < objectName.length(); i++) {
				char c = objectName.charAt(i);
				if((!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_'))) || objectName.charAt(0) == '_') {
					objectDialogOutput.setText("Not a valid Java identifier.");
					return;
				}
			}
			
			String params = objectDialogParameters.getText();
			//Get rid of that stupid semicolon that crashes JDisplayer
			if(params.charAt(params.length() - 1) == ';') params = params.substring(0, params.length() - 1);
			//add new if it isn't there already
			if(params.substring(0, 4).equals("new ")) params = "new " + params.substring(4);
			
			//Parse the class name
			String className = "?";
			try {
				className = objectIO.parseClass(params).getSimpleName();
			} catch (ClassNotFoundException e2) {
				doInitializationError("Could not find that class.");
			}
			ObjectBox objectBox = null;
			
			if(params.length() < 7 || !(params.contains("(") && params.contains(")"))) {
				doInitializationError("Could not parse parameters.");
				return;
			}
			
			//Make a new object with ObjectIO - what could go wrong?
			try {
				objectBox = new ObjectBox(objectIO.makeObject(params), objectName);
			} catch (ClassNotFoundException e1) {
				doInitializationError("Could not find class " + className);
				return;
			} catch (InstantiationException e1) {
				doInitializationError("Cannot instantiate " + className);
				return;
			} catch (IllegalAccessException e1) {
				doInitializationError("Cannot access " + className);
				return;
			} catch (NoSuchMethodException e1) {
				doInitializationError("No such constructor in " + className);
				return;
			} catch (SecurityException e1) {
				doInitializationError("Security blocked creation of new " + className);
				return;
			} catch (IllegalArgumentException e1) {
				doInitializationError("Illegal arguments");
				return;
			} catch (InvocationTargetException e1) {
				doInitializationError(e1.getTargetException() + " thrown in constructor");
				return;
			}
			
			source.bench.addObject(objectBox);
			System.out.println(objectBox);
			objectDialogOutput.setText("Object created.");
			objectBox.addMouseListener(source);
			objectDialogName.setText("");
			objectDialogParameters.setText("");
			
			source.paint(source.getGraphics());
			source.updateDisplay();
		}
	}
	
	private void doInitializationError(String message) {
		objectDialogOutput.setText(message);
	}
}
