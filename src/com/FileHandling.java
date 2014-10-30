package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileHandling {

	/**
	 * Reads VCell.txt
	 */
	public static void readVCellFile() {
		java.nio.file.Path f = Paths.get(ProcessData.root+"VCell.txt");
		BufferedReader reader = null;
		String[] vcell_attr = new String[7];
		try {
			reader = Files.newBufferedReader(f, Charset.defaultCharset());
			String line = null;
			while((line = reader.readLine())!= null){
				vcell_attr = line.split("\\*");

				int genId = Integer.parseInt(vcell_attr[0]);

				VCell vc = new VCell();

				String vNodeSetVal = vcell_attr[1];
				if(!vNodeSetVal.equals("")){
					Set<Integer> vNodeSet = new HashSet<Integer>();
					String[] nid = new String[500];
					vNodeSetVal = vNodeSetVal.replace("[", " ");
					vNodeSetVal = vNodeSetVal.replace("]", " ");
					vNodeSetVal = vNodeSetVal.replace(" ", "");
					nid = vNodeSetVal.split(",");
					for(String id:nid)
						vNodeSet.add(Integer.parseInt(id));
					System.out.println();
					for(int n: vNodeSet){
						NPoint np = ProcessData.getById(n);
						np.gen = genId;
					}
					vc.setvNodeSet(vNodeSet);
				}

				/*String neighborBorderPointMapVal = vcell_attr[2];
				if(!neighborBorderPointMapVal.equals("{}")){
					Map<Integer,Map<Integer,Set<Integer>>> neighborBorderPointMap  = new HashMap<Integer,Map<Integer,Set<Integer>>>();
					String[] neighbors = new String[20];

					neighborBorderPointMapVal = neighborBorderPointMapVal.replaceFirst("\\{", "");
					neighborBorderPointMapVal = neighborBorderPointMapVal.substring(0, neighborBorderPointMapVal.length()-1);
					neighborBorderPointMapVal = neighborBorderPointMapVal.replace(" ", "");
					neighbors = neighborBorderPointMapVal.split("},");

					for(String neighbor: neighbors){
						neighbor = neighbor.replace(" ", "");
						neighbor = neighbor.replace("{", "");
						neighbor = neighbor.replace("}", " ");
						neighbor = neighbor.replace(",", " ");
						neighbor = neighbor.replace("=", " ");

						String[] neighbourItems = neighbor.split(" ");
						Map<Integer,Set<Integer>> bpbp = new HashMap<Integer,Set<Integer>>();
						for(int i = 1;i<neighbourItems.length;i+=2){
							bpbp.put(Integer.parseInt(neighbourItems[i]), Integer.parseInt(neighbourItems[i+1]));
						}
						neighborBorderPointMap.put(Integer.parseInt(neighbourItems[0]),bpbp);
					}
					vc.setNeighborBorderPointMap(neighborBorderPointMap);
				}*/

				String borderPointSetVal = vcell_attr[3];
				if(!borderPointSetVal.equals("")){
					Set<Integer> borderPointSet = new HashSet<Integer>();
					String[] bpid = new String[50];

					borderPointSetVal = borderPointSetVal.replace("[", " ");
					borderPointSetVal = borderPointSetVal.replace("]", " ");
					borderPointSetVal = borderPointSetVal.replace(" ", "");
					bpid = vNodeSetVal.split(",");
					for(String id:bpid)
						borderPointSet.add(Integer.parseInt(id));
					vc.setBorderPointSet(borderPointSet);
				}

				String p = vcell_attr[4];
				if(!p.equals("")){
					Map<Integer, Path> genToVNodeSPMap = new HashMap<Integer, Path>();
					p = p.replace("{", "");
					p = p.replace("}", "");
					p = p.replace(" ", "");

					String[] rawpaths = p.split("=Path\\[path=\\["); 
					String temp = rawpaths[0];
					for(int i=1;i<rawpaths.length;i++){
						String rawpath = rawpaths[i];
						String path = rawpath.substring(0,rawpath.indexOf("]"));// have to remove ]
						String rest = rawpath.substring(rawpath.indexOf("]")+2);
						String[] pathlist = path.split(",");

						Path newPath = new Path();
						ArrayList<Integer> plist = new ArrayList<Integer>();
						for(String pl: pathlist)
							plist.add(Integer.parseInt(pl));
						newPath.path = plist;

						String[] restItems = new String[4];
						rest = rest.replace("]", "");
						restItems = rest.split(",");
						for(String item: restItems){
							if(item.startsWith("start="))
								newPath.start = Integer.parseInt(item.substring(item.indexOf("=")+1));
							else if (item.startsWith("end="))
								newPath.end = Integer.parseInt(item.substring(item.indexOf("=")+1));
							else if (item.startsWith("pathdist=")){
								newPath.pathdist = Double.parseDouble(item.substring(item.indexOf("=")+1,item.length()-1));
								genToVNodeSPMap.put(Integer.parseInt(temp), newPath);
							}
							else
								temp = item;
						}
					}
					vc.setGenToVNodeSPMap(genToVNodeSPMap);
				}

				String s = vcell_attr[5];
				if(!s.equals("")){
					Map<Integer, Map<Integer, Path>> bpTobpVnodeSPMap = new HashMap<Integer, Map<Integer, Path>>();

					s = s.replace(" ", "");
					s = s.replaceFirst("\\{", "");
					s = s.substring(0,s.length()-1);

					String[] perGen = s.split("\\=\\{");
					String outertemp = perGen[0];

					for(int j = 1;j<perGen.length;j++){
						String pg = perGen[j];
						Map<Integer, Path> bptobpMap = new HashMap<Integer, Path>();
						String[] pgItem = pg.split("}");
						String nextTemp = null;
						if(pgItem.length > 1)
							nextTemp = pgItem[1].replace(",", "");


						String[] rawpaths2 = p.split("=Path\\[path=\\["); 
						String temp2 = rawpaths2[0];
						for(int i=1;i<rawpaths2.length;i++){
							String rawpath = rawpaths2[i];
							String path = rawpath.substring(0,rawpath.indexOf("]"));// have to remove ]
							String rest = rawpath.substring(rawpath.indexOf("]")+2);
							String[] pathlist = path.split(",");

							Path newPath = new Path();
							ArrayList<Integer> plist = new ArrayList<Integer>();
							for(String pl: pathlist)
								plist.add(Integer.parseInt(pl));
							newPath.path = plist;

							String[] restItems = new String[4];
							rest = rest.replace("]", "");
							restItems = rest.split(",");
							for(String item: restItems){
								if(item.startsWith("start="))
									newPath.start = Integer.parseInt(item.substring(item.indexOf("=")+1));
								else if (item.startsWith("end="))
									newPath.end = Integer.parseInt(item.substring(item.indexOf("=")+1));
								else if (item.startsWith("pathdist=")){
									newPath.pathdist = Double.parseDouble(item.substring(item.indexOf("=")+1,item.length()-1));
									bptobpMap.put(Integer.parseInt(temp2), newPath);
								}
								else
									temp2 = item;
							}
							bpTobpVnodeSPMap.put(Integer.parseInt(outertemp), bptobpMap);	
						}
						outertemp = nextTemp;
					}
					vc.setBpTobpVnodeSPMap(bpTobpVnodeSPMap);
				}

				/*		String s1 = vcell_attr[6];
				if(!s1.equals("")){
					Map<Integer, Map<Integer, Path>> VNodeTobpSPMap = new HashMap<Integer, Map<Integer, Path>>();

					s1 = s1.replace(" ", "");
					s1 = s1.replaceFirst("\\{", "");
					s1 = s1.substring(0,s1.length()-1);

					String[] perGen1 = s1.split("\\=\\{");
					String outertemp1 = perGen1[0];

					for(int j = 1;j<perGen1.length;j++){
						String pg = perGen1[j];
						Map<Integer, Path> bptobpMap = new HashMap<Integer, Path>();
						String[] pgItem = pg.split("}");
						String nextTemp = null;
						if(pgItem.length > 1)
							nextTemp = pgItem[1].replace(",", "");

						String[] rawpaths2 = p.split("=Path\\[path=\\["); 
						String temp2 = rawpaths2[0];
						for(int i=1;i<rawpaths2.length;i++){
							String rawpath = rawpaths2[i];
							String path = rawpath.substring(0,rawpath.indexOf("]"));// have to remove ]
							String rest = rawpath.substring(rawpath.indexOf("]")+2);
							String[] pathlist = path.split(",");

							Path newPath = new Path();
							ArrayList<Integer> plist = new ArrayList<Integer>();
							for(String pl: pathlist)
								plist.add(Integer.parseInt(pl));
							newPath.path = plist;

							String[] restItems = new String[4];
							rest = rest.replace("]", "");
							restItems = rest.split(",");
							for(String item: restItems){
								if(item.startsWith("start="))
									newPath.start = Integer.parseInt(item.substring(item.indexOf("=")+1));
								else if (item.startsWith("end="))
									newPath.end = Integer.parseInt(item.substring(item.indexOf("=")+1));
								else if (item.startsWith("pathdist=")){
									newPath.pathdist = Double.parseDouble(item.substring(item.indexOf("=")+1,item.length()-1));
									bptobpMap.put(Integer.parseInt(temp2), newPath);
								}
								else
									temp2 = item;
							}
							VNodeTobpSPMap.put(Integer.parseInt(outertemp1), bptobpMap);	
						}
						outertemp1 = nextTemp;
					}
					vc.setVNodeTobpSPMap(VNodeTobpSPMap);
				}*/
				PreComputeVoronoi.nvd.put(Integer.parseInt(vcell_attr[0]), vc);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(PreComputeVoronoi.nvd.size());
		System.out.println("Done");
	}
}
