package com;

public class Edge {
	private NPoint end;
	private double dist;
	
	public Edge(NPoint start, NPoint end, double dist) {
		this.end = end;
	//	this.dist = Math.sqrt(((start.getX()-end.getX())*(start.getX()-end.getX())) + ((start.getY()-end.getY())*(start.getY()-end.getY())));
		this.dist = dist;
	}
	
	public String toString() { return end+","+dist; }
	
	public NPoint getEnd() {
		return end;
	}
	public void setEnd(NPoint end) {
		this.end = end;
	}
	public double getDist() {
		return dist;
	}
}
