package com.miolean.jdisplayer;

import javax.swing.JMenuItem;

public class JObjectMenuItem extends JMenuItem {
	private ObjectBox objectbox;
	public JObjectMenuItem(String name) {
		super(name);
	}
	public ObjectBox getObjectbox() {
		return objectbox;
	}
	public void setObjectbox(ObjectBox objectbox) {
		this.objectbox = objectbox;
	}
}
