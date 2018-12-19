package com.miolean.jdisplayer;

public class DummyClass {
	String a;
	String b;
	String c;
	String d;
	String e;
	String f;
	String g;
	
	public void main(String[] args) {
		new DummyClass(1, "").doSomething(new Integer(9));
	}
	public DummyClass(int a, String b) {
		//do absolutely nothing
	}
	
	public void doSomething(int i) {
		System.out.println("Hello! Parameter = " + i);
	}
	@Override
	public DummyClass clone() throws CloneNotSupportedException {
		return new DummyClass(1, "");
	}
}
