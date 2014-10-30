package com;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GCell {

	private GPoint gpl[] = new GPoint[4];
	private NPoint[] rpl =  new NPoint[4];
	private Set<NPoint> dpl = new HashSet<NPoint>();
	private Set<NPoint> npl = new HashSet<NPoint>();
	private Map<GPoint,NPoint> gprpl = new HashMap<GPoint,NPoint>();
	private int present;
	private int x = 0;
	private int y = 0;

	
	@Override
	public String toString() {
		return "GCell [gpl=" + Arrays.toString(gpl) + ", rpl="
				+ Arrays.toString(rpl) + ", dpl=" + dpl + ", npl=" + npl
				+ ", gprpl=" + gprpl + ", present="
				+ present + ", x=" + x + ", y=" + y + "]";
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public GPoint[] getGpl() {
		return gpl;
	}
	public void setGpl(GPoint[] gpl) {
		this.gpl = gpl;
	}
	public void setGplVal(int index, GPoint gp) {
		gpl[index] = gp;
	}	
	public NPoint getRpl(int index) {
		return rpl[index];
	}
	public void setRplVal(NPoint rp, int index) {
		rpl[index] = rp;
	}
	public void setGprplVal(GPoint gp, NPoint np) {
		gprpl.put(gp, np);
	}
	public int getPresent() {
		return present;
	}
	public void setPresent(int present) {
		this.present = present;
	}
	public Set<NPoint> getDpl() {
		return dpl;
	}
	public Set<NPoint> getNpl() {
		return npl;
	}
	public NPoint[] getRpl() {
		return rpl;
	}
}
