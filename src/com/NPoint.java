package com;



public class NPoint extends Point implements Comparable<NPoint>{
	private int id;
	public double minDist;
	public double f;
	public NPoint previous;
	public boolean used;
	public int gen;

	public int compareTo(NPoint other){
		//  return Double.compare(this.minDist + this.f, other.minDist + other.f);
		return Double.compare(minDist, other.minDist);
	}
	@Override
	public String toString() { return ""+this.id; }

	public NPoint(int id, double x, double y) {
		super(x, y);
		this.id = id;
		this.used = false;
		this.minDist = Double.POSITIVE_INFINITY;
		this.f = -1;
		this.gen = -1; 
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
}
