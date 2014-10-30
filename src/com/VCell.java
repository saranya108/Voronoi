package com;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VCell {
	private Set<Integer> vNodeSet = null;
	private Map<Integer,Map<Integer,Set<Integer>>> neighborBorderPointMap = null;
	private Set<Integer> borderPointSet = null;
	private Map<Integer, Path> genToVNodeSPMap = null;
	private Map<Integer, Map<Integer, Path>> bpTobpVnodeSPMap = null;
	private Map<Integer, Map<Integer, Path>> VNodeTobpSPMap = null;
	

	public VCell(){
		this.vNodeSet = new HashSet<Integer>();
		this.neighborBorderPointMap = new HashMap<Integer,Map<Integer,Set<Integer>>>();
		this.borderPointSet = new HashSet<Integer>();
		this.genToVNodeSPMap = new HashMap<Integer, Path>();
		this.bpTobpVnodeSPMap = new HashMap<Integer, Map<Integer, Path>>();
		this.VNodeTobpSPMap = new HashMap<Integer, Map<Integer, Path>>();
	}
	
	public Map<Integer, Map<Integer, Path>> getVNodeTobpSPMap() {
		return VNodeTobpSPMap;
	}

	public void setVNodeTobpSPMap(Map<Integer, Map<Integer, Path>> vNodeTobpSPMap) {
		VNodeTobpSPMap = vNodeTobpSPMap;
	}

	public Set<Integer> getvNodeSet() {
		return vNodeSet;
	}

	public void setvNodeSet(Set<Integer> vNodeSet) {
		this.vNodeSet = vNodeSet;
	}

	public Map<Integer,Map<Integer,Set<Integer>>> getNeighborBorderPointMap() {
		return neighborBorderPointMap;
	}

	public void setNeighborBorderPointMap(Map<Integer,Map<Integer,Set<Integer>>> neighborBorderPointMap) {
		this.neighborBorderPointMap = neighborBorderPointMap;
	}

	public Set<Integer> getBorderPointSet() {
		return borderPointSet;
	}

	public void setBorderPointSet(Set<Integer> borderPointSet) {
		this.borderPointSet = borderPointSet;
	}

	public Map<Integer, Path> getGenToVNodeSPMap() {
		return genToVNodeSPMap;
	}

	public void setGenToVNodeSPMap(Map<Integer, Path> genToVNodeSPMap) {
		this.genToVNodeSPMap = genToVNodeSPMap;
	}

	public Map<Integer, Map<Integer, Path>> getBpTobpVnodeSPMap() {
		return bpTobpVnodeSPMap;
	}

	public void setBpTobpVnodeSPMap(Map<Integer, Map<Integer, Path>> bpTobpVnodeSPMap) {
		this.bpTobpVnodeSPMap = bpTobpVnodeSPMap;
	}

/*	@Override
	public String toString() {
		return "VCell [genId=" + genId + ", vNodeSet=" + vNodeSet
				+ ", neighborBorderPointMap=" + neighborBorderPointMap
				+ ", borderPointSet=" + borderPointSet + ", genToVNodeSPMap="
				+ genToVNodeSPMap + ", bpTobpVnodeSPMap=" + bpTobpVnodeSPMap + "]";
	}
*/
	@Override
	public String toString() {
		return vNodeSet + "*" + neighborBorderPointMap	+ "*" + borderPointSet + "*"
				+ genToVNodeSPMap + "*" + bpTobpVnodeSPMap +"*"+ VNodeTobpSPMap;
		}
	
	
}
