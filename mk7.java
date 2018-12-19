package com.miolean.jdisplayer;

import java.lang.reflect.*;

public class NamedField {
	private Field field;
	
	public NamedField(Field f) {
		field = f;
	}
	
	@Override
	public String toString() {
		String currentName = field.toString();
		String result = "";
		
		if(Modifier.isPrivate(field.getModifiers())) result += "private ";
		if(Modifier.isProtected(field.getModifiers())) result += "protected ";
		if(Modifier.isPublic(field.getModifiers())) result += "public ";
		if(Modifier.isVolatile(field.getModifiers())) result += "volatile ";
		if(Modifier.isStatic(field.getModifiers())) result += "static ";
		if(Modifier.isFinal(field.getModifiers())) result += "final ";
		
		result += field.getType().getSimpleName() + " ";
			
		int i = 0;
		while(i != -1) {
			i = currentName.indexOf(' ');
			currentName = currentName.substring(i + 1);
		}
		i = 0;
		while(i != -1) {
			i = currentName.indexOf('.');
			currentName = currentName.substring(i + 1);
		}
		result += currentName;
		return result;
	}
	
	public String getName() {
		String currentName = field.toString();
		int i = 0;
		while(i != -1) {
			i = currentName.indexOf(' ');
			currentName = currentName.substring(i + 1);
		}
		i = 0;
		while(i != -1) {
			i = currentName.indexOf('.');
			currentName = currentName.substring(i + 1);
		}
		return currentName;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}
}
