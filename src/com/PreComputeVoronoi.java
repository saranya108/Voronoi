package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

public class PreComputeVoronoi {
	static Map<Integer,VCell> nvd = new HashMap<Integer,VCell>();

	/*
	 * Process given data and network points and generate network Voronoi diagram
	 * dpList - List of data points
	 * npMap - Map of network point ids that are mapped to network point objects
	 */ 
	public static void processDataForVorornoi(ArrayList<NPoint> dpList){
		int s = ProcessData.rnet.keySet().size();
		Set<Integer> toBeChanged = new HashSet<Integer>();
		/*PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File(ProcessData.root+"VCell.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
		computeNVD(dpList, s);
		//For each generator point
		for(int genId: nvd.keySet()){
			VCell vc = new VCell();
			vc = nvd.get(genId);
			//Set<Integer> toBeChanged = new HashSet<>();
			Set<NPoint> tSet = new HashSet<NPoint>();
			Map<Integer,Set<Integer>> unionbp = new HashMap<Integer,Set<Integer>>();
			//System.out.println(genId);
			Map<Integer,Map<Integer,Set<Integer>>> neighborBorderPointMap = new HashMap<Integer,Map<Integer,Set<Integer>>>();

			//Compute and store neighbors and border points
			for(int njid: nvd.keySet()){				
				Map<Integer, Set<Integer>> bpoints = new HashMap<Integer,Set<Integer>>();
				if(genId != njid){
					bpoints = getBorderPoints(genId,njid);
					if(!bpoints.keySet().isEmpty()){
						if(neighborBorderPointMap.containsKey(njid)){
							Map<Integer, Set<Integer>> a = new HashMap<Integer, Set<Integer>>(neighborBorderPointMap.get(njid));
							a.putAll(bpoints);
							neighborBorderPointMap.put(njid, a);
						}else
							neighborBorderPointMap.put(njid,bpoints);
						unionbp.putAll(bpoints);	
					}
				}
			}
			vc.setNeighborBorderPointMap(neighborBorderPointMap);
			vc.setBorderPointSet(unionbp.keySet());

			//Compute shortest path between generator and voronoi nodes
			for(int i: vc.getvNodeSet())
				tSet.add(ProcessData.getById(i));
			Map<Integer,Path> spath = new HashMap<Integer,Path>();
			spath.putAll(ShortestPath.getSPVoronoi(genId, toBeChanged, tSet, true));
			if(!spath.keySet().containsAll(vc.getvNodeSet())){
				System.out.println("SOMETHING WRONG");
			}
			vc.setGenToVNodeSPMap(spath);
			ProcessData.resetRnet(toBeChanged);
			toBeChanged.clear();
			tSet.clear();
			// Compute shortest path between border point to voronoi nodes
			for(int i: vc.getvNodeSet())
				tSet.add(ProcessData.getById(i));
			Map<Integer,Map<Integer, Path>> bpTobpVnode = new HashMap<Integer,Map<Integer, Path>>();
			Map<Integer,Map<Integer, Path>> VnodebpTobp = new HashMap<Integer,Map<Integer, Path>>();

			for(int startId: unionbp.keySet()){
				NPoint startPoint = ProcessData.getById(startId);
				tSet.remove(startPoint);
				Map<Integer, Path> vNodePathMap = new HashMap<Integer,Path>();
				vNodePathMap.putAll(ShortestPath.getSPVoronoi(startId, toBeChanged, tSet, true));
				bpTobpVnode.put(startId,vNodePathMap);
				for(Entry<Integer,Path> e:vNodePathMap.entrySet()){
					Map<Integer, Path> bpPathMap = null;
					if(vc.getVNodeTobpSPMap().containsKey(e.getKey()))
						bpPathMap = vc.getVNodeTobpSPMap().get(e.getKey());
					else
						bpPathMap = new HashMap<Integer,Path>();
					Path revPath = new Path(e.getKey(),startId,vNodePathMap.get(e.getKey()).reversePath(),e.getValue().pathdist);
					bpPathMap.put(startId, revPath);
					VnodebpTobp.put(e.getKey(),bpPathMap);
				}
				ProcessData.resetRnet(toBeChanged);
				toBeChanged.clear();
				tSet.add(startPoint);
				vc.setVNodeTobpSPMap(VnodebpTobp);
			}
			vc.setBpTobpVnodeSPMap(bpTobpVnode);
		}
		//	writer.close();
		//FileHandling.readVCellFile();
	}

	private static void computeNVD(ArrayList<NPoint> dpList, int s) {
		//Used when storing distances of shortest paths - spaths[][] - into a text file to expediate debugging process
		/*		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File(ProcessData.root+"spaths_smaller.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/

		double[][] spaths = new double[s][s];
		Set<Integer> toBeChanged = new HashSet<Integer>();
		for(int nid : ProcessData.npMap.keySet()){
			//System.out.println(nid);
			spaths[nid] = ShortestPath.getSPVoronoi(ProcessData.npMap.get(nid).getId(),toBeChanged);
			ProcessData.resetRnet(toBeChanged);
			toBeChanged.clear();
			//	writer.write(nid+":"+ printArr(spaths[nid])+"\n");
		}
		//writer.close();


		//Used when shortest paths distances - spaths[][] - are to be retrieved from a text file

		/*java.nio.file.Path f = Paths.get(ProcessData.root+"spaths.txt");
		BufferedReader reader = null;
		String[] spathsStr = new String[s];
		try {
			reader = Files.newBufferedReader(f, Charset.defaultCharset());
			String line = null;
			while((line = reader.readLine())!= null){
				int nid = Integer.parseInt(line.substring(0,line.indexOf(":")));
				System.out.println(nid);
				spathsStr = line.substring(line.indexOf(":")+1).split(",");
				for(int x=0;x<spathsStr.length;x++){
					if(spathsStr[x].equals("Infinity"))
						spaths[nid][x] = Double.POSITIVE_INFINITY;
					else
						spaths[nid][x] = Double.parseDouble(spathsStr[x]);				
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} */
		int dpSetSize = dpList.size();
		Set<Integer> pkids = new HashSet<Integer>();
		//Pkids contain all network point ids that are to be considered as point pk. It is 
		//initialzed to contain all network point ids. This set reduces in size progressively 
		//so as to fasten the nvd computations
		for(int i=0;i<s;i++)
			pkids.add(i);


		//Write VCell to file
		/*		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File("ProcessData.root+"VCell_initial.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	*/

		// Extracting network Voronoi nodes
		for(int i=0;i<dpSetSize;i++){
			int piid = dpList.get(i).getId();
			Map<Integer, Set<Integer>> domSetMap = new HashMap <Integer, Set<Integer>>();
			Map<Integer, Set<Integer>> bisectorNodeSetMap = new HashMap<Integer, Set<Integer>>();
			Set<Integer> bisectorPoints = new HashSet<Integer>();
			Set<Integer> VnodePi = new HashSet<Integer>();
			//System.out.println("i = "+i+":"+piid);
			for(int j=0;j<dpSetSize;j++){
				int pjid = dpList.get(j).getId();		
				Set <Integer> domSet = new HashSet<Integer>();
				Set <Integer> bisectorNodeSet = new HashSet<Integer>();
				if(piid != pjid){
					Iterator <Integer>itr = pkids.iterator();
					while(itr.hasNext()){
						int pkid = itr.next();	
						//if(pkid != piid && pkid != pjid){
						double distik = spaths[piid][pkid];
						double distjk = spaths[pjid][pkid];
						/*if(distik == Double.POSITIVE_INFINITY || distjk == Double.POSITIVE_INFINITY){
								System.out.println("Distance between "+ piid+" and "+pkid+" is "+distik);
								System.out.println("Distance between "+ pjid+" and "+pkid+" is "+distjk);
							}*/
						if(distik <= distjk){
							if(distik == distjk){
								bisectorNodeSet.add(pkid);
								bisectorPoints.add(pkid);
							}
							domSet.add(pkid);
						}
						//}
					}
					//Dom and bisector set for each data point pair (i,j)
					if(!domSet.isEmpty()){
						domSetMap.put(pjid, domSet);
						bisectorNodeSetMap.put(pjid, bisectorNodeSet);
					}
				}
			}
			// Compute intersection of Dom set of each pair (i,j) to get final network Voronoi node set for i
			for(int j=0;j<dpSetSize;j++){
				NPoint pj = dpList.get(j);
				int pjid = pj.getId();
				if(piid != pjid && bisectorNodeSetMap.get(pjid) != null && isWellBehaved(piid,pjid,bisectorNodeSetMap.get(pjid))){
					if(!domSetMap.get(pjid).isEmpty()){
						if(VnodePi.isEmpty())
							VnodePi = domSetMap.get(pjid);
						else
							VnodePi.retainAll(domSetMap.get(pjid));
					}
				}
			}
			/*java.nio.file.Path f = Paths.get("ProcessData.root+"VCell_initial.txt");
			BufferedReader reader = null;
			String[] vcell_attr = new String[5];
			try {
				reader = Files.newBufferedReader(f, Charset.defaultCharset());
				String line = null;
				while((line = reader.readLine())!= null){
					vcell_attr = line.split(":");
					int genid = Integer.parseInt(vcell_attr[0]);
					VCell vc = new VCell(genid);
					Set<Integer> vnodepi = new HashSet<Integer>();
					String[] nid = new String[500];
					String vcVal = vcell_attr[1];
					vcVal = vcVal.replace("[", " ");
					vcVal = vcVal.replace("]", " ");
					vcVal = vcVal.replace(" ", "");
					nid = vcVal.split(",");
					for(String id:nid)
						vnodepi.add(Integer.parseInt(id));
					vc.setvNodeSet(vnodepi);
					nvd.put(Integer.parseInt(vcell_attr[0]), vc);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}*/
			if(!VnodePi.isEmpty()){
				// Remove assigned network points from pkids set so as to not include it in further computations
				for(int n: VnodePi){
					NPoint np = ProcessData.getById(n);
					//if(!dpList.contains(np))
					if(np.gen != -1)
						System.out.println("Error");
					np.gen = piid;
					if(!bisectorPoints.contains(n))
						pkids.remove(n);
				}
				//System.out.println(VnodePi.size());
				VCell vc = new VCell();
				vc.setvNodeSet(VnodePi);
				nvd.put(piid,vc);
				//writer.write(piid+"*"+vc.toString()+"\n");
			}
			else
				System.out.println("Error");
		}
		//writer.close();
	}
	/*
	 * Get all ni's border points and their corresponding nj's border points by checking if edge exists between points belonging to different voronoi node sets
	 */
	private static Map<Integer,Set<Integer>> getBorderPoints(int ni, int nj) {
		Map<Integer, Set<Integer>> bpoints = new HashMap<Integer,Set<Integer>>();
		Set<Integer> niSet = nvd.get(ni).getvNodeSet();
		Set<Integer> njSet = nvd.get(nj).getvNodeSet();
		for(int nk: niSet){
			for(int nl: njSet){
				assert(ProcessData.rnet.keySet().contains(ProcessData.getById(nk)));
				if((niSet.contains(nl) && njSet.contains(nk))){
					if(bpoints.containsKey(nk)){
						Set<Integer> v = new HashSet<Integer>(bpoints.get(nk));
						v.add(nl);
						bpoints.put(nk, v);
					}else{
						Set<Integer> n = new HashSet<Integer>();
						n.add(nl);
						bpoints.put(nk, n);
					}
				}else if(ProcessData.rnet.keySet().contains(ProcessData.getById(nk))){
					
					if(ProcessData.rnet.get(ProcessData.getById(nk)).keySet().contains(ProcessData.getById(nl))){
						if((!niSet.contains(nl) && !njSet.contains(nk))){
							if(bpoints.containsKey(nk)){
								Set<Integer> v = new HashSet<Integer>(bpoints.get(nk));
								v.add(nl);
								bpoints.put(nk, v);
							}else{
								Set<Integer> n = new HashSet<Integer>();
								n.add(nl);
								bpoints.put(nk, n);
							}
						}
					}
				}

			}
		}
		return bpoints;
	}


	/*
	 * Returns the string form of the values in the provided array in the required format.
	 * Used only to expediate shortest path array computation by aiding write to file. 
	 */
	private static String printArr(double[] fs) {
		StringBuffer st = new StringBuffer();
		st.append(fs[0]);
		for(int i=1;i<fs.length;i++){
			st.append(","+fs[i]);
		}
		return st.toString();
	}


	/*
	 * Check to see if the bisector set so formed is well behaved and can be considered as a Voronoi node set. 
	 * This is done by checking if the given bisector set is empty or if there exists a common link between
	 * shortest paths of points selected within the bisector nodes
	 */
	private static boolean isWellBehaved(int pi, int pj, Set <Integer> bNodeSet) {
		if(bNodeSet.isEmpty()){
			return true;
		}else{
			boolean f = false;
			Set<Integer> toBeChanged = new HashSet<>();
			ArrayList<Integer> path1 = new ArrayList<Integer>();
			ArrayList<Integer> path2 = new ArrayList<Integer>();
			Map<Integer,Path> spathPi = new HashMap<Integer, Path>();
			Map<Integer,Path> spathPj = new HashMap<Integer, Path>();
			spathPi = ShortestPath.getSPVoronoi(pi, toBeChanged, null, false);
			ProcessData.resetRnet(toBeChanged);
			toBeChanged.clear();
			spathPj = ShortestPath.getSPVoronoi(pj, toBeChanged, null, false);
			ProcessData.resetRnet(toBeChanged);
			toBeChanged.clear();
			for(int pk : bNodeSet){
				if(spathPi.containsKey(pk)){
					path1 = spathPi.get(pk).path;
					for(int pl : bNodeSet){
						if(pl != pk){
							if(spathPj.containsKey(pl)){
								path2 = spathPj.get(pl).path;
								if(!hasCommonLink(path1, path2)){
									f = true;
									break;
								}
							}
						}
					}
					if(f) 
						break;
				}
			}
			if(f)
				return true;
		}
		return false;
	}


	/*
	 * Checks if there exists common links between the two shortest paths provided
	 */
	private static boolean hasCommonLink(ArrayList<Integer> path1, ArrayList<Integer> path2){
		for(int i=0;i<path1.size()-1;i++){
			if(path2.contains(path1.get(i)) && path2.contains(path1.get(i+1)))
				return true;
		}
		return false;
	}
}
