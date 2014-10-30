package com;

public class DPoint extends Point {
	private int id;
	
	public DPoint(int id, double x, double y) {
		super(x, y);
		this.id = id;
	}
		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	@Override
	public String toString() { return ""+this.id; }
}
