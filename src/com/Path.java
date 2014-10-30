package com;

import java.util.ArrayList;
import java.util.Collections;

public class Path {
	public ArrayList<Integer> path = new ArrayList<Integer>();
	public int start;
	public int end;
	public double pathdist;

	public Path(int start, int end, ArrayList<Integer> path, double pathdist){
		this.start = start;
		this.end = end;
		this.path = path;
		this.pathdist = pathdist;
	}
	
	public Path(){
		this.start = -1;
		this.end = -1;
		this.path = null;
		this.pathdist = -1;
	}

	public ArrayList<Integer> reversePath(){
		ArrayList<Integer> revPath = new ArrayList<Integer>();
		revPath.addAll(path);
		Collections.reverse(revPath);
		return revPath;
	}
	
	public Path concat(Path p){
		if(p.start == this.end)
			p.path.remove(p.start);
		if(p.end == this.start)
			p.path.remove(p.end);
		this.pathdist = this.pathdist + p.pathdist;
		this.path.addAll(p.path);
		return this;
	}

	@Override
	public String toString() {
		return "Path [path=" + path + ", start=" + start + ", end=" + end
				+ ", pathdist=" + pathdist + "]";
	}
}
