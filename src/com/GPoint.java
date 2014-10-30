package com;

public class GPoint extends Point {
	private int id;
	
	public GPoint(int id, double x, double y) {
		super(x, y);
		this.id = id;
	}
	@Override
	public String toString() { return ""+this.id; }
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}