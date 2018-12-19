package com.miolean.jdisplayer;

import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.*;

public class MethodDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JTextField methodDialogParameters;
	private JButton methodDialogGo;
	private JLabel methodDialogOutput;
	private JLabel parametersLabel;
	JTextArea label;
	
	private JDisplayer source;
	private ObjectBench bench;
	private Method method;
	private Object invokee;
	private ObjectIO objectIO;
	
	public MethodDialog(JDisplayer source) {
		super(source, "[JDisplayer] Invoke method...");
		this.setVisible(false);
		this.source = source;
		this.bench = source.bench;
		objectIO = new ObjectIO(bench);
		
		label = new JTextArea("");
		label.setSize(200, 50);
		label.setLocation(50, 10);
		label.setOpaque(false);
		label.setEditable(false);
		this.add(label);
		
		methodDialogOutput = new JLabel();
		methodDialogParameters = new JTextField();
		methodDialogGo = new JButton();
		parametersLabel = new JLabel("Enter parameters:");
		
		methodDialogOutput.setText("");
		methodDialogOutput.setSize(280, 20);
		methodDialogOutput.setLocation(10, 135);
		this.add(methodDialogOutput);
		
		parametersLabel.setSize(200, 20);
		parametersLabel.setLocation(50, 60);
		this.add(parametersLabel);
		
		methodDialogParameters.setText("");
		methodDialogParameters.setSize(200, 20);
		methodDialogParameters.setLocation(50, 80);
		this.add(methodDialogParameters);
		
		methodDialogGo.setText("Execute");
		methodDialogGo.setSize(180, 20);
		methodDialogGo.setLocation(60, 110);
		methodDialogGo.addActionListener(this);
		this.add(methodDialogGo);
		
		try {
			setMethod(Object.class.getDeclaredMethod("getClass")); //default method is methodception
		} catch (NoSuchMethodException | SecurityException e) {
			//this will not happen
			e.printStackTrace();
		}
		this.setModal(true);
	}
	
	public void OpenMethodDialog() {
		methodDialogOutput.setText("");
		this.setVisible(true);
	}
	
	public void setMethod(Method m) {
		System.out.println(m);
		this.method = m;
		String labelText = "Method ";
		labelText += m.getName() + "()\n";
		labelText += "Parameters: ";
		if(m.getParameterCount() == 0) {
			labelText += "None";
			methodDialogParameters.setVisible(false);
			parametersLabel.setText("No parameters.");
		} else {
			methodDialogParameters.setVisible(true);
			parametersLabel.setText("Enter parameters:");
		}
		
		for(int i = 0; i < m.getParameterCount(); i++) {
			Class<?> c = m.getParameterTypes()[i];
			labelText += c.getSimpleName();
			if(i != m.getParameterCount() - 1) labelText += ", ";
		}
		labelText += "\n";
		labelText += "Returns: " + m.getReturnType().getSimpleName();
		label.setText(labelText);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == methodDialogGo) {
			Object result = null;
			try {
				String parameters = (methodDialogParameters.getText());
				if(! parameters.equals("")) {
					result = method.invoke(invokee, objectIO.getObjectsFromParameters(parameters).toArray());
				} else result = method.invoke(invokee);
			} catch (IllegalAccessException e1) {
				doInvocationError("Cannot access method " + method.getName());
				return;
			} catch (IllegalArgumentException e1) {
				doInvocationError("Illegal arguments for method " + method.getName());
				return;
			} catch (InvocationTargetException e1) {
				doInvocationError(e1.getCause().getClass().getSimpleName() + " thrown by invokee");
				return;
			} catch (ClassNotFoundException e1) {
				doInvocationError("Could not find class " +  method.getClass().getName());
				return;
			} catch (InstantiationException e1) {
				doInvocationError("Could not instantiate parameters");
				return;
			} catch (NoSuchMethodException e1) {
				doInvocationError("No such method");
				return;
			} catch (SecurityException e1) {
				doInvocationError("Blocked by security");
				return;
			}
			System.out.println("Result: " + result.getClass() + "  " + result.toString());
			if(objectIO.isWrapper(result)) methodDialogOutput.setText("Sucess! Returned: " + result);
			else {
				ObjectBox resultObject = new ObjectBox(result, "Result");
				resultObject.addMouseListener(source);
				bench.addObject(resultObject);
				source.updateDisplay();
				methodDialogOutput.setText("Sucess! Result is on the bench.");
			}
		}
	}
	
	private void doInvocationError(String message) {
		methodDialogOutput.setText(message);
	}
	public void setInvokee(Object invokee) {
		this.invokee = invokee;
	}
}
