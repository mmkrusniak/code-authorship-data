package com.miolean.jdisplayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ObjectIO {
	
	private ObjectBench bench;
	public ObjectIO(ObjectBench bench) {
		this.bench = bench;
	}
	//**************************************
	//Following are methods to
	//get / create Objects from Strings
	//based on Java syntax.
	//**************************************
	public LinkedList<Object> getObjectsFromParameters(String parameters) 
			throws ClassNotFoundException, 
			InstantiationException, 
			IllegalAccessException, 
			NoSuchMethodException, 
			SecurityException, 
			IllegalArgumentException, 
			InvocationTargetException {
		
		System.out.println("Getting objects from parameters " + parameters);
		LinkedList<Object> result = new LinkedList<Object>();
		
		Scanner scan = new Scanner(parameters);
		scan.useDelimiter(",");
		
		if(parameters.equals("")) {
			scan.close();
			return null;
		}
		
		while(scan.hasNext()) {
			String param = scan.next();
			while(param.charAt(0) == ' ') param = param.substring(1, param.length());
			try {
				while((param.length() - param.replace("\"", "").length()) % 2 == 1) param += scan.next();
			} catch(NoSuchElementException e) {
				scan.close();
				throw e;
			}
			Object o = makeObject(param);
			result.add(o);
			System.out.println("Made object: " + o);
		}
		
		scan.close();
		return result;
	}
	
	//Make a single object from the parameters in format ObjectType(newParam() or otherParam...) 
	public Object makeObject(String param) 
			throws ClassNotFoundException, 
			InstantiationException, 
			IllegalAccessException, 
			NoSuchMethodException, 
			SecurityException, 
			IllegalArgumentException, 
			InvocationTargetException {
		
		System.out.println("Param = " + param);
		if(param.equals("")) {
			throw new IllegalArgumentException();
		}
		
		if(param.length() > 4 && param.substring(0,4).equals("new ")) {
			//User wants to instantiate a new object as one of the parameters
			param = param.substring(4); //Out with the "new"
			
			int i; //defined as position of the first "(" in this initialization
			for(i = 0; param.charAt(i) != '('; i++) ; //Find the "("
			Class<?> newObjectClass = parseClass(param);
			
			//Figure out the type and the parameters based on Java syntax
			//Indirect recursion here
			LinkedList<Object> subparameters = getObjectsFromParameters(param.substring(i + 1, param.length() - 1));
			
			
			//The new object may have no parameters at all
			//In that case we don't have to muddle around with constructors
			if(subparameters == null) {
				return newObjectClass.newInstance();
			}
			
			//Figure out the classes of all of our parameters so we can get a constructor
			//Dealing with the primitives is kind of important
			Class<?>[] subParamClasses = null;
			subParamClasses = new Class<?>[subparameters.size()];
			for(int j = 0; j < subparameters.size() && subparameters.get(j) != null; j++) {
				if(subparameters.get(j) instanceof Integer) subParamClasses[j] = Integer.TYPE;
				else if(subparameters.get(j) instanceof Boolean) subParamClasses[j] = Boolean.TYPE;
				else if(subparameters.get(j) instanceof Short) subParamClasses[j] = Short.TYPE;
				else if(subparameters.get(j) instanceof Long) subParamClasses[j] = Long.TYPE;
				else if(subparameters.get(j) instanceof Character) subParamClasses[j] = Character.TYPE;
				else if(subparameters.get(j) instanceof Float) subParamClasses[j] = Float.TYPE;
				else if(subparameters.get(j) instanceof Double) subParamClasses[j] = Double.TYPE;
				else if(subparameters.get(j) instanceof Byte) subParamClasses[j] = Byte.TYPE;
				else subParamClasses[j] = subparameters.get(j).getClass();
			}
			
			//Try to get a constructor based on those classes
			Constructor<?> constructor = null;
			System.out.print("New constructor with parameters ");
			for(Class<?> s: subParamClasses) System.out.print(s.getCanonicalName() + " ");
			System.out.println(" (" + subParamClasses.length + " parameters)");
			constructor = newObjectClass.getConstructor(subParamClasses);
		
			
			//Create a new object using the constructor we just made
			System.out.println("Starting construction process with parameters " + subparameters);
			return constructor.newInstance(subparameters.toArray());
		} else return getOtherType(param);
	}
	
	public Object getOtherType(String param) {
		System.out.println("Found " + param + " as other type, boxing or formatting...");
		
		//First possibility: null
		//Easy enough
		if(param.equals("null") ) return null;
		//Second possibility: String
		//Empty strings are dealt with separately
		//Otherwise remove the quotes and we're good to go!
		if(param.contains("\"")) {
			if(param.length() == 2) return "";
			else return param.substring(1, param.length() - 1);
		}
		
		//Third possibility: Already-named object
		//Iterate through the objects that we have created to see if it's in there
		for(ObjectBox o: bench.getObjects()) if(param.equals(o.getName())) return o.getObject();
		
		//Fourth possibility: Primitive
		//We can use some tricks to figure out which is which
		if(param.equals("true")) return Boolean.valueOf(true);
		if(param.equals("false")) return Boolean.valueOf(false);
		if(param.charAt(0) == '\'') return new Character(param.charAt(1));
		
		//Any type beyond here is a numerical type, sometimes designated with a letter at the end
		if(param.contains(".") && (param.contains("f") || param.contains("F"))) return new Float(Float.parseFloat(param));
		if(param.contains(".")) return new Double(Double.parseDouble(param));
		if(param.contains("l") || param.contains("L")) return new Long(Long.parseLong(param));
		return new Integer(Integer.parseInt(param));
		//Short and byte are indistinguishable from int, so
		//they are not supported
		//Fortunately they are uncommon.
	}
	
	public Class<?> parseClass(String param) 
			throws ClassNotFoundException {
		
		int i; //defined as position of the "(" in this initialization
		for(i = 0; param.charAt(i) != '('; i++) ; //Find the "("
		String type = param.substring(0, i);
		
		if(type.contains("new ")) type = type.substring(4);
		
		//Try to find the type of the object - this is considered part of its parameters
		//First, try it as if the full package name was provided
		Class<?> newObjectClass = null;
		
		boolean classNotFound = true;
		try {
			newObjectClass = Class.forName(type);
			classNotFound = false;
		} catch (ClassNotFoundException e) {
			classNotFound = true;
		}
		//If that didn't work, iterate through the standard packages.
		int k = 0;
		while(classNotFound && k < bench.getStandardImports().size()) {
			String newType = "";
			try {
				newType = bench.getStandardImports().get(k) + "." + type;
				newObjectClass = Class.forName(newType);
				classNotFound = false;
			} catch (ClassNotFoundException e) {
				classNotFound = true;
			}
			k++;
		}
		//If full package name was specified the type will have had 1+ "."s in it
		if(classNotFound) {
			//Packaged but package or type is wrong (we would have found it otherwise)
			throw new ClassNotFoundException();
		}
		
		return newObjectClass;
	}
	
	public boolean doMethod(Object invokee, String methodName, Object... parameters) 
			throws IllegalAccessException, 
			IllegalArgumentException, 
			InvocationTargetException, 
			NoSuchMethodException, 
			SecurityException, 
			ClassNotFoundException {
		
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
		
		Class<?> c = Class.forName(methodName.substring(0, i));
		
		System.out.println(c.getName());
		Method[] methods = c.getMethods();
		for(Method m: methods) System.out.println(m.getName());
			
		Method method = c.getMethod(methodName.substring(i + 1), classes);
		method.invoke(invokee, parameters);
	
		return true;
	}
	
	public boolean isWrapper(Object o) {
		return isWrapper(o.getClass());
	}
	public boolean isWrapper(Class<?> c) {
		if(c == Integer.class) return true;
		if(c == Short.class) return true;
		if(c == Long.class) return true;
		if(c == Byte.class) return true;
		if(c == Double.class) return true;
		if(c == Float.class) return true;
		if(c == Boolean.class) return true;
		if(c == Character.class) return true;
		if(c == Void.class) return true;
		return false;
	}
}
