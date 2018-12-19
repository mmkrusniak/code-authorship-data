package com.miolean.jdisplayer;

import java.awt.event.*;
import java.lang.reflect.Field;

import javax.swing.*;

public class FieldDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L; //Eclipse requirement
	
	private JDisplayer source; //JDisplayer that this component comes from
	
	//Components
	private JList<NamedField> fieldList = new JList<NamedField>(); //List of fields for object
	private JButton get; //"Get" button
	private JButton inspect; //"Inspect" button
	private ObjectBox objectBox; //Object whose fields this dialog holds
	private JLabel output; //Displays result of "get" operation
	JLabel name;
	
	private ObjectIO objectIO;
	
	public FieldDialog(JDisplayer source) {
		super(source, "[JDisplayer] Inspect field...", true);
		this.source = source;
		this.setSize(300, 400);
		this.setModal(true);
		objectIO = new ObjectIO(source.bench);
		
		fieldList.setSize(200, 100);
		fieldList.setOpaque(true);
		fieldList.setVisible(true);
		
		JScrollBar scrollbar = new JScrollBar(SwingConstants.VERTICAL);
		scrollbar.setUnitIncrement(3);
		scrollbar.setVisible(true);
		
		JScrollPane pane = new JScrollPane();
		pane.setSize(200, 250);
		pane.setLocation(50, 40);
		pane.setVisible(true);
		pane.setVerticalScrollBar(scrollbar);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setViewportView(fieldList);
		this.add(pane);
		
		
		name = new JLabel();
		name.setSize(200, 30);
		name.setLocation(50, 10);
		name.setVisible(true);
		this.add(name);
		System.out.println(name);
		
		get = new JButton("Get");
		get.setSize(90, 20);
		get.setLocation(50, 290);
		get.setVisible(true);
		get.addActionListener(this);
		this.add(get);
		
		inspect = new JButton("Inspect");
		inspect.setSize(90, 20);
		inspect.setLocation(160, 290);
		inspect.setVisible(true);
		inspect.addActionListener(this);
		this.add(inspect);
		
		output = new JLabel("");
		output.setSize(200, 30);
		output.setLocation(50, 310);
		output.setVisible(true);
		this.add(output);
	}
	
	public void OpenFieldDialog() {
		Field[] rawFields = objectBox.getObject().getClass().getDeclaredFields();
		NamedField[] fields = new NamedField[rawFields.length];
		for(int i = 0; i < rawFields.length; i++) fields[i] = new NamedField(rawFields[i]);
		fieldList.setListData(fields);
		
		name.setText("Fields of " + objectBox.getObject().getClass().getSimpleName() + " \"" + objectBox.getName() + "\"");
		output.setText("");
	
		this.setVisible(true);
	}

	//**************************************
	//Following are Listener methods.
	//**************************************
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//Part 1: Get an object from the selected field
		Field field = fieldList.getSelectedValue().getField();
		if(field == null) return;
		Object result = null;
		
		try {
			result = field.get(objectBox.getObject());
		} catch (IllegalArgumentException e1) {
			doGetError("This object does not have that field");
			return;
		} catch (IllegalAccessException e1) {
			doGetError("Cannot access that field");
			return;
		} if(result == null) {
			doGetError("That field is null.");
			return;
		}
		
		
		if(e.getSource() == get) {
			
			
			System.out.println("Got a " + result.getClass().getSimpleName());
			if(objectIO.isWrapper(result.getClass())) {
				output.setText("Success! Field value = " + result);
			} else {
				output.setText("Success! Field sent to bench.");
				ObjectBox newBox = new ObjectBox(result, field.getName());
				source.bench.addObject(newBox);
				source.add(newBox);
				newBox.addMouseListener(source);
			}
		}
		
		if(e.getSource() == inspect) {
			FieldDialog subDialog = new FieldDialog(source);
			subDialog.setObject(new ObjectBox(result, field.getName()));
			subDialog.OpenFieldDialog();
		}
	}

	private void doGetError(String message) {
		output.setText(message);
	}
	
	public Object getObject() {
		return objectBox.getObject();
	}

	public void setObject(ObjectBox object) {
		this.objectBox = object;
	}
	
}
