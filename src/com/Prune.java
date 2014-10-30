package com;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

public class Prune {
	public static boolean getANNDist(ArrayList<Integer>qpList,double best_dist){
		Set<Integer> s = VANNQueryProcessing.s;
		int nearestNeighbor = -1;
		VCell canGenVC = new VCell();
		for(int i: s){
			canGenVC = PreComputeVoronoi.nvd.get(i);
			Set<Integer> neighborSet = new HashSet<Integer>(canGenVC.getNeighborBorderPointMap().keySet());
			neighborSet.retainAll(s);			
			if(!neighborSet.isEmpty()){
				for(int cn: neighborSet){
					for(Entry<Integer,Set<Integer>> cbpair: canGenVC.getNeighborBorderPointMap().get(cn).entrySet()){
						Set<Integer> cdpairVal = new HashSet<Integer>(cbpair.getValue());
						double ann_dist = 0;
						for(int q: qpList){
							double dist = 0;
							double min_dist = Double.POSITIVE_INFINITY;
							Set<Integer> toBeChanged = new HashSet<Integer>();
							ShortestPath.getSPOnVoronoiGrid(q, toBeChanged, VANNQueryProcessing.an);
							for(int bp: cdpairVal){
								double dist1 = canGenVC.getGenToVNodeSPMap().get(cbpair.getKey()).pathdist;
								double dist2 = ProcessData.rnet.get(ProcessData.getById(cbpair.getKey())).get(ProcessData.getById(bp));
								double dist3 = ProcessData.getById(bp).minDist; 
								if(dist1==Double.POSITIVE_INFINITY||dist2==Double.POSITIVE_INFINITY||dist3==Double.POSITIVE_INFINITY)
									System.out.println("Error");
								dist = dist1 + dist2 + dist3;
								if(dist < min_dist){
									//System.out.println("Cell "+i+" q: "+q+" bp: "+bp+" min_dist: "+dist);
									min_dist = dist;
								}
							}
							ProcessData.resetRnet(toBeChanged);
							ann_dist = ann_dist + min_dist;
						}
						if(ann_dist < best_dist){
							//System.out.println("Cell "+i+" best_ann_dist: "+ann_dist);
							best_dist = ann_dist;
							nearestNeighbor = i;
						}
					}
				}
			}
			
		}
		System.out.println("VANNDist: "+ best_dist);
		if(nearestNeighbor == -1)
			return false;
		else {
			System.out.println(qpList.toString() + " VANN: "+ nearestNeighbor) ; 
			return true;
		}
	}
}
