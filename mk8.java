package com.miolean.jdisplayer;

import java.lang.reflect.*;
import java.util.LinkedList;

public class ObjectBench {
	private LinkedList<ObjectBox> objects;
	private LinkedList<String> standardImports = new LinkedList<String>();
	
	/*instance*/{
		standardImports.add("java.lang");
		standardImports.add(this.getClass().getPackage().getName());
	}
	
	public static void main(String[] args) {
		DummyClass d = new DummyClass(1, "");
		doMethod(d, "com.miolean.tests.DummyClass.doSomething()", 18);
	}
	
	public ObjectBench() {
		objects = new LinkedList<ObjectBox>();
	}
	
	void addObject(ObjectBox o) {
		objects.add(o);
	}
	
	void removeObject(ObjectBox o) {
		objects.remove(o);
	}
	
	private static boolean doMethod(Object invokee, String methodName, Object... parameters) {
		if(methodName.contains("()")) methodName = methodName.substring(0, methodName.length() - 2);
		
		Class<?>[] classes = new Class<?>[parameters.length];
		
		for(int j = 0; j < parameters.length; j++){
			//Unfold primitives, hooray!
			if(parameters[j] instanceof Integer) classes[j] = Integer.TYPE;
			else if(parameters[j] instanceof Boolean) classes[j] = Boolean.TYPE;
			else if(parameters[j] instanceof Short) classes[j] = Short.TYPE;
			else if(parameters[j] instanceof Long) classes[j] = Long.TYPE;
			else if(parameters[j] instanceof Character) classes[j] = Character.TYPE;
			else if(parameters[j] instanceof Float) classes[j] = Float.TYPE;
			else if(parameters[j] instanceof Double) classes[j] = Double.TYPE;
			else if(parameters[j] instanceof Byte) classes[j] = Byte.TYPE;
			else classes[j] = parameters[j].getClass();
		}
		
		//Find the '.' between the class and the method names
		int i = methodName.length() - 1;
		for( ; methodName.charAt(i) != '.' && i > 0; i--) ;
		
		try {
			Class<?> c = Class.forName(methodName.substring(0, i));
			
			System.out.println(c.getName());
			Method[] methods = c.getMethods();
			for(Method m: methods) System.out.println(m.getName());
				
			Method method = c.getMethod(methodName.substring(i + 1), classes);
			method.invoke(invokee, parameters);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public LinkedList<ObjectBox> getObjects() {
		return objects;
	}

	public void setObjects(LinkedList<ObjectBox> objects) {
		this.objects = objects;
	}

	public LinkedList<String> getStandardImports() {
		return standardImports;
	}

	public void addStandardImport(String import_) {
		this.standardImports.add(import_);
	}
	
	public void removeStandardImport(String import_) {
		int size = standardImports.size();
		for(int i = 0; i < size; i++) if(standardImports.get(i).equals(import_)) standardImports.remove(i);
	}
	
	public boolean hasStandardImport(String import_) {
		int size = standardImports.size();
		for(int i = 0; i < size; i++) if(standardImports.get(i).equals(import_)) return true;
		return false;
	}
}
