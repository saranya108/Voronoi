package com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

public class VANNQueryProcessing {

	public static PriorityQueue<CPoint> h = new PriorityQueue<CPoint>();
	public static Set<Integer> p = new HashSet<Integer>();
	public static Set<Integer> s = new HashSet<Integer>();
	public static Map<Integer,ArrayList<Integer>> si = new HashMap<Integer,ArrayList<Integer>>();
	public static Map<Integer,Set<Integer>> c = new HashMap<Integer,Set<Integer>>();
	public static Map<Integer,Set<Integer>> bpSet = new HashMap<Integer,Set<Integer>>();
	public static Map<Integer,Map<Integer,Double>> an = new HashMap<Integer,Map<Integer,Double>>();


	// Process query points using NVD approach
	public void processQueryNVD(ArrayList<Integer> qpList){
		/*try {
			PrintWriter pw = new PrintWriter(new File(ProcessData.root+"/nvd.txt"));
			for(NPoint n : ProcessData.rnet.keySet()){
				pw.write(n.getX()+","+n.getY()+","+n.gen+"\n");
			}
			for(Entry<Integer, VCell> e : PreComputeVoronoi.nvd.entrySet()){
				pw.write(e.getKey()+"\n"+e.getValue()+"\n");
			}
			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}*/
		if(!checkFirst())
			System.out.println("Error");
		p.addAll(PreComputeVoronoi.nvd.keySet());
		// Filter
		// Compute first nearest neighbor of all query points
		for(int qi: qpList){
			NPoint np = ProcessData.getById(qi);
			int firstNearestGen = np.gen;
			VCell genVC = new VCell();
			genVC = PreComputeVoronoi.nvd.get(firstNearestGen);
			//Load heap
			CPoint e = new CPoint();
			e.qid = qi;
			if(!genVC.getGenToVNodeSPMap().containsKey(qi)){
				System.out.println("Faulty points: "+np.getId()+" has generator "+np.gen);
			}
			e.dist = genVC.getGenToVNodeSPMap().get(qi).pathdist;
			h.add(e);

			//System.out.println(h.size());

			//Load si and candidate set
			ArrayList <Integer> genList = new ArrayList<Integer>();
			Set <Integer> genSet = new HashSet<Integer>();
			genList.add(firstNearestGen);
			genSet.add(firstNearestGen);
			si.put(qi, genList);
			c.put(qi, genSet);

			//create network an
			Map<Integer,Double> edges = new HashMap<Integer, Double>();
			for(int i: genVC.getBorderPointSet()){
				if(i!=qi){
					double edgeDist = genVC.getBpTobpVnodeSPMap().get(i).get(qi).pathdist; 
					edges.put(i, edgeDist);
					Map<Integer,Double> backEdges = new HashMap<Integer, Double>();
					backEdges.put(qi, edgeDist);
					an.put(i, backEdges);
				}
			}
			an.put(qi, edges);
			//bpSet.put(qi, genVC.getBorderPointSet());
		}

		int nextNN = -1;
		//Compute next nearest neighbor
		while(computeIntersection(si).isEmpty()){
			CPoint e = h.poll();
			int qp = e.qid;
			if(!qpList.contains(qp))
				System.out.println("Error");
			nextNN = findNextNearestNeighbor(qp);
			/*for(Entry<Integer, ArrayList<Integer>> ei:si.entrySet()){
				System.out.println(ei.getKey()+"-----"+ei.getValue().toString());
			}*/
		}
		//System.out.println("Error");
		//union
		for(Entry<Integer,ArrayList<Integer>> e: si.entrySet())
			s.addAll(e.getValue());

		//intersection
		Set<Integer> in = computeIntersection(si);
		if(!in.contains(nextNN))
			System.out.println("Error");
		Iterator<Integer> i = PreComputeVoronoi.nvd.get(nextNN).getBorderPointSet().iterator();
		int x = i.next();
		double best_dist = 0;
		for(int q: qpList){
			Set<Integer> toBeChanged = new HashSet<Integer>();
			ShortestPath.getSPOnVoronoiGrid(x, toBeChanged, an);
			best_dist = best_dist + ProcessData.getById(q).minDist;
			ProcessData.resetRnet(toBeChanged);
		}
		//	System.out.println("++++++ "+ best_dist);
		if(!Prune.getANNDist(qpList,best_dist))
			System.out.println(qpList.toString() + " VANN: "+ nextNN) ; 
	}

	/*
	 * Finds the nearest neighbor based on network voronoi diagram
	 */
	public static int findNextNearestNeighbor(int q){
		Set <Integer> candidateGenSet = new HashSet<Integer>(c.get(q));
		ArrayList <Integer> siGenList = new ArrayList<Integer>(si.get(q));
		Map<Integer,Set<Integer>> nnbpMap = new HashMap<Integer,Set<Integer>>();
		VCell candidateGenVC = new VCell();
		int nearestNeighbor = -1;
		VCell nearestGenVC = new VCell();

		candidateGenSet = extendNeighbor(candidateGenSet, siGenList, true);
		c.put(q, candidateGenSet);

		Set<Integer> toBeChanged = new HashSet<Integer>();
		ShortestPath.getSPOnVoronoiGrid(q, toBeChanged, an);

		double min_dist = Double.POSITIVE_INFINITY;
		for(int candidateGen: candidateGenSet){
			candidateGenVC = PreComputeVoronoi.nvd.get(candidateGen);
			Set<Integer> cNeighborSetOld = new HashSet<Integer>(candidateGenVC.getNeighborBorderPointMap().keySet());
			double dist = 0;
			Set<Integer> cNeighborSet = new HashSet<Integer>(cNeighborSetOld);
			cNeighborSet.retainAll(siGenList);
			//System.out.println(siGenList.size()+" "+cNeighborSetOld.size() +" "+candidateGenSet.size()+" "+candidateGenVC.getNeighborBorderPointMap().keySet().size());

			if(cNeighborSet.isEmpty())
				System.out.println();
			if(!cNeighborSet.isEmpty()){
				for(int cn: cNeighborSet){
					for(Entry<Integer,Set<Integer>> cbpair: candidateGenVC.getNeighborBorderPointMap().get(cn).entrySet()){
						Set<Integer> cdpairVal = new HashSet<Integer>(cbpair.getValue());
						for(int bp: cdpairVal){
							double dist1 = candidateGenVC.getGenToVNodeSPMap().get(cbpair.getKey()).pathdist;
							double dist2 = ProcessData.rnet.get(ProcessData.getById(cbpair.getKey())).get(ProcessData.getById(bp));
							double dist3 = ProcessData.getById(bp).minDist; 
							if(dist1==Double.POSITIVE_INFINITY||dist2==Double.POSITIVE_INFINITY||dist3==Double.POSITIVE_INFINITY)
								System.out.println("Error");
							dist = dist1 + dist2 + dist3;
							if(dist < min_dist){
								min_dist = dist;
								nearestNeighbor = candidateGen;
								nearestGenVC = candidateGenVC;
								nnbpMap.clear();
								nnbpMap.put(cbpair.getKey(), cdpairVal);
							}
						}
					}
				}
			}else
				System.out.println("Error");
		}
		ProcessData.resetRnet(toBeChanged);
		if(nearestNeighbor==-1)
			System.out.println("Error");
		//update si
		ArrayList<Integer>d = si.get(q);
		d.add(nearestNeighbor);
		si.put(q, d);
		// update heap
		CPoint e = new CPoint();
		e.qid = q;
		e.dist = min_dist;
		h.add(e);

		//update an
		Map<Integer,Double> edges = new HashMap<Integer, Double>();
		Set<Integer> nnbpKeySet = nnbpMap.keySet();
		if(nnbpKeySet.size() == 0)
			System.out.println("Error");
		else if(nnbpKeySet.size() >= 1){
			if(nnbpKeySet.size()>1)
				System.out.println("Error");
			for(Entry<Integer,Set<Integer>> entry: nnbpMap.entrySet()){
				Set<Integer> s = entry.getValue();
				if(s.size() == 0)
					System.out.println("Error");
				else if(s.size() >= 1){
					Iterator<Integer>itr = entry.getValue().iterator();
					while(itr.hasNext()){
						int val = itr.next();
						double eDist = ProcessData.rnet.get(ProcessData.getById(entry.getKey())).get(ProcessData.getById(val));
						if(an.containsKey(val)){
							Map<Integer,Double> m = an.get(val);
							m.put(entry.getKey(),eDist);
						}else{
							edges.put(entry.getKey(), eDist);
							an.put(val, edges);
						}	
						Map<Integer,Double> bEdges = new HashMap<Integer, Double>();
						if(an.containsKey(entry.getKey())){
							Map<Integer,Double> m = an.get(entry.getKey());
							m.put(val, eDist);
						}else{
							bEdges.put(val, eDist);
							an.put(entry.getKey(), bEdges);
						}
					}
				}
			}
		}

		for(int i: nnbpMap.keySet()){
			for(int j: nearestGenVC.getBorderPointSet()){
				if(i!=j){
					double eDist = nearestGenVC.getBpTobpVnodeSPMap().get(j).get(i).pathdist; 
					if(an.containsKey(j)){
						Map<Integer,Double> m = an.get(j);
						m.put(i,eDist);
					}else{
						edges.put(i, eDist);
						an.put(j, edges);
					}
					Map<Integer,Double> bEdges = new HashMap<Integer, Double>();
					if(an.containsKey(i)){
						Map<Integer,Double> m = an.get(i);
						m.put(j, eDist);
					}else{
						bEdges.put(j, eDist);
						an.put(i, bEdges);
					}
				}
			}
			//an.put(i, edges);
			//bpSet.get(q).remove(i);
		}
		return nearestNeighbor;
	}

	private static Set<Integer> extendNeighbor(Set<Integer> genSet, ArrayList<Integer> genList, boolean first) {
		if(genSet.isEmpty()||genList.isEmpty())
			System.out.println("Error");
		for(int i: genList){
			genSet.addAll(PreComputeVoronoi.nvd.get(i).getNeighborBorderPointMap().keySet());
		}
		genSet.removeAll(genList);
		return genSet;		
	}

	private static Set<Integer> computeIntersection(Map<Integer, ArrayList<Integer>> si) {
		Set<Integer> intersection = new HashSet<Integer>();
		int i = 0;
		for(Entry<Integer, ArrayList<Integer>> e : si.entrySet()){
			if(i==0)
				intersection.addAll(e.getValue());
			intersection.retainAll(e.getValue());
			i++;
		}
		return intersection;
	}

	private static boolean checkFirst() {
		boolean state = true;
		for(NPoint n: ProcessData.rnet.keySet()){
			//System.out.println(n.getId()+" "+n.gen);
			if(n.gen == -1 && !ProcessData.getDpoints().contains(n)){
				System.out.println("Generator not set");
				state = false;
			}
			if(!ProcessData.getDpoints().contains(ProcessData.getById(n.gen))&& n.gen != -1){
				System.out.println("generator not part of datapoint set");
				state = false;
			}
		}
		for(int i: PreComputeVoronoi.nvd.keySet()){
			//System.out.println("VCell gen: "+i);
			VCell vc = new VCell();
			vc = PreComputeVoronoi.nvd.get(i);
			if(!vc.getvNodeSet().containsAll(vc.getBorderPointSet())){
				System.out.println("vnode set does not contain all borderpoints");
				state = false;
			}
			for(int in : vc.getNeighborBorderPointMap().keySet()){
				VCell nvc = new VCell();
				nvc = PreComputeVoronoi.nvd.get(in);
				if(!nvc.getNeighborBorderPointMap().keySet().contains(i)){
					System.out.println("Error");
				}
			}


			for(int vnode:vc.getvNodeSet()){
				ArrayList<Integer> pathNode = vc.getGenToVNodeSPMap().get(vnode).path;
				if(vnode!=i){
					if(pathNode.indexOf(i)!=-1){
						pathNode.remove(pathNode.indexOf(i));
					}
				}
				if(!vc.getvNodeSet().containsAll(pathNode)){
					System.out.println("generator to vnode path contains points other than vnode set");
					state = false;
				}
			}
			if(!vc.getBorderPointSet().containsAll(vc.getBpTobpVnodeSPMap().keySet())){
				System.out.println("borderpoint does not match up");
				state = false;
			}
		}
		return state;	
	}
	public void clearAll(){
		h.clear();
		p.clear();
		s.clear();
		si.clear();
		c.clear();
		bpSet.clear();
		an.clear();
	}
}
