package com.miolean.jdisplayer;

import java.lang.reflect.Method;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

public class JMethodMenuItem extends JMenuItem {
	//Keep Eclipse from being annoying
	private static final long serialVersionUID = 1L;
	
	private Method method;
	private ObjectBox source;
	
	public JMethodMenuItem(Method m, ObjectBox source) {
		method = m;
		this.source = source;
	}

	public JMethodMenuItem() {
		super();
	}

	public JMethodMenuItem(Action arg0) {
		super(arg0);
	}

	public JMethodMenuItem(Icon arg0) {
		super(arg0);
	}

	public JMethodMenuItem(String arg0, Icon arg1) {
		super(arg0, arg1);
	}

	public JMethodMenuItem(String arg0, int arg1) {
		super(arg0, arg1);
	}

	public JMethodMenuItem(String arg0) {
		super(arg0);
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public ObjectBox getSource() {
		return source;
	}

	public void setSource(ObjectBox source) {
		this.source = source;
	}
}
