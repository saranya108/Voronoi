package com;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

public class ShortestPath {
	// Point to point shortest path calculation
	public static ArrayList<Integer> getSPOnRawNetwork(NPoint target, NPoint source, Set<Integer> toBeChanged){
		source.minDist = 0;
		source.f =  computeDistance(source, target);
		toBeChanged.add(source.getId());
		PriorityQueue<NPoint> npQueue = new PriorityQueue<NPoint>();
		Set <Integer> processed =  new HashSet<Integer>();
		NPoint u = null;
		npQueue.add(source);
		while (!npQueue.isEmpty()){
			u = npQueue.poll();
			if(u == target)
				break;
			for (Entry<NPoint, Double> e : ProcessData.getRnet(u).entrySet()){
				NPoint v = e.getKey();
				if(!processed.contains(v.getId())){
					double weight = e.getValue();
					double distanceThroughU = u.minDist + weight;
					if (Double.compare(distanceThroughU, v.minDist) < 0) {
						v.minDist = distanceThroughU;
						if(v.f < 0)
							v.f = computeDistance(v, target);
						toBeChanged.add(v.getId());
						v.previous = u;
						npQueue.add(v);
					}
				}
			}
			processed.add(u.getId());
		}
		if(npQueue.isEmpty() && u != target){
			System.out.println("HERE");
		}
		ArrayList<Integer> path = new ArrayList<Integer>();
		for (NPoint p = target; p != null; p = p.previous){
			path.add(p.getId());
		}
		Collections.reverse(path);
		return path;
	}

	// Calculates shortest path from single source to one of the points in the target list provided using raw network distances
	public static ArrayList<Integer> getSPOnRawNetworkToTargetList(Set<NPoint> targetList, NPoint source, Set<Integer> toBeChanged, NPoint dest){
		source.minDist = 0;
		source.f =  computeDistance(source, dest);
		toBeChanged.add(source.getId());
		PriorityQueue<NPoint> npQueue = new PriorityQueue<NPoint>();
		Set <NPoint> processed =  new HashSet<NPoint>();
		NPoint u = null;
		npQueue.add(source);
		while (!npQueue.isEmpty()){
			u = npQueue.poll();
			processed.add(u);
			if(targetList.contains(u))
				break;
			for (Entry<NPoint, Double> e : ProcessData.getRnet(u).entrySet()){
				NPoint v = e.getKey();
				if(!processed.contains(v)){
					double weight = e.getValue();
					double distanceThroughU = u.minDist + weight;
					if (Double.compare(distanceThroughU, v.minDist) < 0) {
						npQueue.remove(v);
						v.minDist = distanceThroughU;
						if(v.f < 0)
							v.f =  computeDistance(v, dest);
						toBeChanged.add(v.getId());
						v.previous = u;
						npQueue.add(v);
					}
				}
			}
		}
		if(npQueue.isEmpty() && !targetList.contains(u)){
			System.out.println("HERE");
		}
		ArrayList<Integer> path = new ArrayList<Integer>();
		for (NPoint p = u; p != null; p = p.previous){
			path.add(p.getId());
		}
		Collections.reverse(path);
		return path;
	}

	// Calculates shortest path from single source to one of the points in the target list provided using pre-computed grid distances
	public static ArrayList<Integer> getSPOnGrid(Set<NPoint> targetRPList, NPoint sourceRP, Set<Integer> toBeChanged, NPoint dest){
		sourceRP.minDist = 0;
		sourceRP.f =  computeDistance(sourceRP, dest);
		Set <NPoint> processed =  new HashSet<NPoint>();
		toBeChanged.add(sourceRP.getId());
		NPoint u = null;
		PriorityQueue<NPoint> npQueue = new PriorityQueue<NPoint>();
		npQueue.add(sourceRP);
		while (!npQueue.isEmpty()){
			u = npQueue.poll();
			processed.add(u);
			if(targetRPList.contains(u))
				break;
			List<Path> spaths = Grid.gnet.get(u);
			if(spaths == null)
				continue;
			Iterator <Path> itr = spaths.iterator();
			while(itr.hasNext()){
				Path p = itr.next();
				int vid = p.end;
				NPoint v = ProcessData.getById(vid);
				if(!processed.contains(v)){
					double weight = p.pathdist;
					double distanceThroughU = u.minDist + weight;
					if (Double.compare(distanceThroughU, v.minDist) < 0) {
						npQueue.remove(v);
						v.minDist = distanceThroughU;
						if(v.f < 0) 
							v.f =  computeDistance(v, dest);
						toBeChanged.add(v.getId());
						v.previous = u;
						npQueue.add(v);
					}
				}
			}
		}
		if(npQueue.isEmpty() && !targetRPList.contains(u)){
			System.out.println("HERE RP");
		}
		ArrayList<Integer> path = new ArrayList<Integer>();
		for (NPoint p = u; p != null; p = p.previous){
			path.add(p.getId());
		}
		Collections.reverse(path);
		return path;
	}
	
	// Calculates shortest path from single source to one of the points in the target list provided NVD
		public static Path getSPOnNVD(Set<Integer> targetBPList, NPoint sourcebp, Set<Integer> toBeChanged, NPoint dest, VCell qvc){
			sourcebp.minDist = 0;
			sourcebp.f =  computeDistance(sourcebp, dest);
			Set <Integer> processed =  new HashSet<Integer>();
			toBeChanged.add(sourcebp.getId());
			int uid = -1;
			NPoint u = null;
			PriorityQueue<NPoint> npQueue = new PriorityQueue<NPoint>();
			npQueue.add(sourcebp);
			while (!npQueue.isEmpty()){
				u= npQueue.poll();
				uid = u.getId();
				processed.add(uid);
				if(targetBPList.contains(uid))
					break;
				Map<Integer, Path> spaths = qvc.getBpTobpVnodeSPMap().get(uid);
				if(spaths == null)
					continue;
				for(Entry<Integer,Path> e : spaths.entrySet()){
					u = ProcessData.getById(uid);
					Path p = e.getValue();
					int vid = p.end;
					NPoint v = ProcessData.getById(vid);
					if(!processed.contains(vid)){
						double weight = p.pathdist;
						double distanceThroughU = u.minDist + weight;
						if (Double.compare(distanceThroughU, v.minDist) < 0) {
							npQueue.remove(v);
							v.minDist = distanceThroughU;
							if(v.f < 0) 
								v.f =  computeDistance(v, dest);
							toBeChanged.add(v.getId());
							v.previous = u;
							npQueue.add(v);
						}
					}
				}
			}
			if(npQueue.isEmpty() && !targetBPList.contains(uid)){
				System.out.println("HERE RP");
			}
			ArrayList<Integer> path = new ArrayList<Integer>();
			for (NPoint p = u; p != null; p = p.previous){
				path.add(p.getId());
			}
			Collections.reverse(path);
			Path spath = new Path(sourcebp.getId(),u.getId(),path,u.minDist);
			return spath;
		}

	//Shortest path distance computation for voronoi
	public static double[] getSPVoronoi(int sourceId, Set<Integer> toBeChanged){
		NPoint source = ProcessData.getById(sourceId);
		source.minDist = 0;
		toBeChanged.add(sourceId);
		PriorityQueue<NPoint> npQueue = new PriorityQueue<NPoint>();
		Set <Integer> processed =  new HashSet<Integer>();
		int uid = -1;
		NPoint u = null;
		npQueue.add(source);
		while (!npQueue.isEmpty()){
			u = npQueue.poll();
			uid = u.getId();
			if(ProcessData.getRnetById(uid)!=null){
				for (Entry<NPoint, Double> e : ProcessData.getRnetById(uid).entrySet()){
					//u = ProcessData.getById(uid);
					NPoint v = e.getKey();
					if(!processed.contains(v.getId())){
						double weight = e.getValue();
						double distanceThroughU = u.minDist + weight;
						if (Double.compare(distanceThroughU, v.minDist) < 0) {
							v.minDist = distanceThroughU;
							toBeChanged.add(v.getId());
							v.previous = u;
							npQueue.add(v);
						}
					}
				}
				processed.add(uid);
			}
		}	
		double[] spaths = new double[ProcessData.rnet.keySet().size()];
		for(NPoint target:ProcessData.rnet.keySet())
			spaths[target.getId()] = (float) target.minDist;
		return spaths;
	}

	//Shortest path computation for voronoi
	public static Map<Integer, Path> getSPVoronoi(int sourceId, Set<Integer> toBeChanged, Set<NPoint> targetSet, boolean specificAccess){
		NPoint source = ProcessData.getById(sourceId);
		source.minDist = 0;
		toBeChanged.add(sourceId);
		PriorityQueue<NPoint> npQueue = new PriorityQueue<NPoint>();
		Set <Integer> processed =  new HashSet<Integer>();
		int uid = -1;
		NPoint u = null;
		npQueue.add(source);
		while (!npQueue.isEmpty()){
			u = npQueue.poll();
			uid = u.getId();

			if(ProcessData.getRnetById(uid)!=null){
				for (Entry<NPoint, Double> e : ProcessData.getRnetById(uid).entrySet()){
					NPoint v = e.getKey();
					if(!processed.contains(v.getId())){
						double weight = e.getValue();
						double distanceThroughU = u.minDist + weight;
						if (Double.compare(distanceThroughU, v.minDist) < 0) {
							v.minDist = distanceThroughU;
							toBeChanged.add(v.getId());
							v.previous = u;
							npQueue.add(v);
						}
					}
				}
				processed.add(uid);
			}
		}		
		Set<NPoint> tSet = new HashSet<NPoint>();
		if(specificAccess)
			tSet.addAll(targetSet);
		else
			tSet.addAll(ProcessData.rnet.keySet());
		Map<Integer, Path> spaths = new HashMap<Integer,Path>();
		for(NPoint target:tSet){
			if(target.minDist!=Double.POSITIVE_INFINITY){
				ArrayList<Integer> pathlist = new ArrayList<Integer>();
				for (NPoint p = target; p != null; p = p.previous)
					pathlist.add(p.getId());
				Collections.reverse(pathlist);
				
				Path path = new Path(source.getId(),target.getId(),pathlist,target.minDist);
				spaths.put(target.getId(),path);
			}
		}
		return spaths;
	}

	//Shortest path distance computation for voronoi
		public static void getSPOnVoronoiGrid(int qid, Set<Integer> toBeChanged, Map<Integer, Map<Integer, Double>> an){
			NPoint q = ProcessData.getById(qid);
			q.minDist = 0;
			toBeChanged.add(qid);
			PriorityQueue<NPoint> npQueue = new PriorityQueue<NPoint>();
			Set <Integer> processed =  new HashSet<Integer>();
			int uid = -1;
			NPoint u = null;
			npQueue.add(q);
			while (!npQueue.isEmpty()){
				u = npQueue.poll();
				uid = u.getId();
				if(an.get(uid)!=null){
					for (Entry<Integer, Double> e : an.get(uid).entrySet()){
						NPoint v = ProcessData.getById(e.getKey());
						if(!processed.contains(e.getKey())){
							double weight = e.getValue();
							double distanceThroughU = u.minDist + weight;
							if (Double.compare(distanceThroughU, v.minDist) < 0) {
								v.minDist = distanceThroughU;
								toBeChanged.add(e.getKey());
								v.previous = u;
								npQueue.add(v);
							}
						}
					}
					processed.add(uid);
				}
			}	
		}

	public static double computeDistance(NPoint start, NPoint end) {
		double xd = start.getX()-end.getX();
		double yd = start.getY()-end.getY();
		double res = Math.sqrt(xd*xd + yd*yd);
		return res;
	}
}
