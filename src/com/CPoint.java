package com;

public class CPoint implements Comparable<CPoint> {
	public int qid;
	public double dist;
	public CPoint() {
		this.qid = 0;
		this.dist = 0;
	}
	@Override
	public int compareTo(CPoint o) {
		if(this.dist < o.dist)
			return -1;
		else{
			if(this.dist == 0)
				return 0;
			else 
				return 1;
		}
	}
	
	
	@Override
	public String toString() {
		return "CPoint [qid=" + qid + ", dist=" + dist + "]";
	}
}
