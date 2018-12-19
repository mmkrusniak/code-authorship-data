package com.miolean.jdisplayer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.*;
import javax.swing.*;

public class ObjectBox extends JPanel {
	//Necessary to stop Eclipse from jabbering but not really supported
	private static final long serialVersionUID = 1L;
	
	private Object object;
	private String name;
	@Deprecated
	private Rectangle representation;
	private String parameters;
	private Color boxColor = new Color(50, 150, 255);
	private Color textColor = Color.BLACK;
	private Color dragBoxColor = Color.YELLOW;
	
	public boolean isBeingDragged = false;
	
	public ObjectBox(Object object, String name) {
		this.object = object;
		this.name = name;
		this.setSize(100, 100);
		this.setLocation(100, 100);
		this.setVisible(true);
		this.setSize(100, 50);
	}
	
	public Method[] getMethods() {
		return this.getClass().getMethods();
	}
	
	@Override
	public String toString() {
		String result = "";
		result += "ObjectBox containing a " + ((object == null)? "null object " :(object.getClass().getName() + "-type object ")) + "called " + name;
		return result;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		this.setBackground(boxColor);
		
		super.paintComponent(g);
		
		g.setColor(dragBoxColor);
		g.fillRect(this.getWidth() - 21, 0, 20, 20);
		
		g.setColor(textColor);
		g.drawLine(0, 0, this.getWidth() - 1, 0);
		g.drawLine(0, 0, 0, this.getHeight() - 1);
		g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight() - 1);
		g.drawLine(0, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - 1);
		
		g.drawString(name, 3, 15);
		g.drawString("{" + object.getClass().getSimpleName() + "}", 3, 35);
	}
	
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Deprecated
	public Rectangle getRepresentation() {
		return representation;
	}

	@Deprecated
	public void setRepresentation(Rectangle representation) {
		this.representation = representation;
		this.setLocation(representation.x, representation.y);
		this.setSize(representation.width, representation.height);
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
}
