package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;

public class ProcessData {
	//variables to record min and max points of input dataset
	private static double minx = Double.POSITIVE_INFINITY;
	private static double miny = Double.POSITIVE_INFINITY;
	private static double maxx = Double.NEGATIVE_INFINITY;
	private static double maxy = Double.NEGATIVE_INFINITY;
	public static String root = "src/com/";
	private static int noOfNodes = 0;
	//area enclosed by lines joining the query points
	private static Polygon2D queryarea = null;
	private static PrintWriter writer = null;
	//Adjacency list of raw road network
	static Map<NPoint,Map<NPoint,Double>> rnet = new HashMap<NPoint,Map<NPoint,Double>>();
	static Map<Integer,NPoint> npMap = new HashMap<Integer,NPoint>();
	//List of data points
	private static ArrayList<NPoint> dpoints = new ArrayList<NPoint>();

	public static Map<NPoint,Double> getRnet(NPoint p) {
		return rnet.get(p);
	}
	public static Set<NPoint> getRnetNodes() {
		return rnet.keySet();
	}
	public static ArrayList<NPoint> getDpoints() {
		return dpoints;
	}

	public static void main(String[] args){
		processFile(Paths.get(root+"OL_raw_node_smaller_modified.txt"),"nn");
		processFile(Paths.get(root+"OL_raw_edges_smaller_modified.txt"),"ne");
		processFile(Paths.get(root+"OL_data_points_smaller_modified.txt"),"dn");

		//Create required input type for VANN
		//edgeSet = processEdgesForVoronoi(rnet);
		//PreComputeVoronoi.processDataForVorornoi(nSet, dpoints.size(), edgeSet);
		// To check if it is a connected graph
		/*	try {
			writer = new PrintWriter(root+"visited.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		depthFirstRecursive(npMap.get(0));
		writer.close();*/
		long startTime = System.nanoTime();
		PreComputeVoronoi.processDataForVorornoi(dpoints);
		long stopTime = System.nanoTime();
		double precompTime = (stopTime - startTime)/1e9;
		System.out.println("Voronoi Precomputation time:" + precompTime);
		
		
		//Below line is used to measure pre-computation time
		// Reads grid configurations from config.txt
		/*try {
			BufferedReader reader = Files.newBufferedReader(Paths.get("config.txt"), Charset.defaultCharset());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String line;
		String[] attr  = line.split(",");*/
		String rowscol = "10";//attr[0];
		String fname = "10X10.csv";//attr[1];
		System.out.println(fname + " Done file reading");

		// Build grid of required configuration
		Grid.buildGrid(minx,maxx,miny,maxy, rowscol);

		// Below line is used to create and distribute random data points from network points
		//Grid.distributeDPointsRandom();

		//Distribute data points among grid cells
		Grid.distributeDPoints(dpoints);

		//Distribute network points among grid cells
		Grid.distributeNPoints(rnet.keySet());
		//Compute and store shortest paths between reference points within grid cells
		Grid.findRPShortestPath();

		//Below lines are used to measure pre-computation time

		try {
			writer = new PrintWriter(fname);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		writer.write(precompTime+"\n");
		//Pre-computation ends
		//Query processing begins
		//writer.write("My Algo Time,Voronoi Algo Time,My Q1 Pathdist,Voronoi Q1 Pathdist,My Q2 Pathdist,"
				//+ "Std Q1 Pathdist,My Q3 Pathdist,Std Q3 Pathdist,Time diff,Path diff,Normalized Path diff\n");
		System.out.print("Running queries");

		// A thousand ANN queries are randomly generated and solved
		for(int i=0;i<1000;i++){
			System.out.println("ANN query "+i+" processed.");
			generateQueries();
		}
		/*				System.out.print("\n");
		// Reset variable values and empty all data structures to facilitate AANN processing with a different grid size
		rnet.clear();
		minx = Double.POSITIVE_INFINITY;
		miny = Double.POSITIVE_INFINITY;
		maxx = Double.NEGATIVE_INFINITY;
		maxy = Double.NEGATIVE_INFINITY;
		dpoints.clear();
		queryarea = null;
		Grid.gnet.clear();

		writer.close();
}*/
	}
	/*private static Map<NPoint,Set<Edge>> processEdgesForVoronoi(Map<NPoint, List<NPoint>> rnet2) {
		Map<NPoint,Set<Edge>> rnetForVoronoi = new HashMap <NPoint,Set<Edge>>();
		for(Entry <NPoint, List<NPoint>> entry : rnet2.entrySet()){
			NPoint start = entry.getKey();
			Set <Edge> eSet = new HashSet<Edge>();
			for(NPoint end : entry.getValue())
				 eSet.add(new Edge(start, end));
			rnetForVoronoi.put(start, eSet);
		}
		return rnetForVoronoi;
	}*/
	/* Generates queries and solves them
	 */
	public static void generateQueries(){
		Set<NPoint> temp = new HashSet<NPoint>();
		temp.addAll(rnet.keySet());
		temp.removeAll(dpoints);
		Random random = new Random();
		List<NPoint> keys = new ArrayList<NPoint>(temp);
		NPoint[] q = new NPoint[3];
		int size = keys.size();
		for(int i=0;i<3;i++){
			int ran = random.nextInt(size);
			q[i] = keys.get(ran);
			keys.set(ran, keys.get(size-1));
			size--;		
		}		
		Set<Integer> toBeChanged = new HashSet<>();

		Point2D iptemp = getIntermediatePoint(q[0],q[1],q[2]);
		Point ip = new Point(iptemp.getX(),iptemp.getY());
		// To measure time taken to compute shortest paths from intermediate point to all query points using Raw network and grid network (AANN)
		long startTime = System.nanoTime();			
		NPoint dpSelected = null;
		// Get data point closest to Fermat point
		dpSelected = Grid.getDataPointForQuery(ip);
		// Get grid cell which contains selected data point
		GCell dgc = Grid.getGridCell(dpSelected.getX(),dpSelected.getY());
		// Get network point nearest to selected data point using grid cell found 
		/*Set<NPoint> t = new HashSet<NPoint>();
		t.add(getById(503));
		t.add(getById(501));
		t.add(getById(502));*/
		//NPoint NNdpSelected = Grid.calculateNNP(ip,t);
		NPoint NNdpSelected = Grid.calculateNNP(dpSelected, dgc.getNpl());
		if(NNdpSelected == null)
			System.out.println();
		// Get shortest paths from each of the query points to selected network point
		com.Path spANN1 = getANNPath(q[0], NNdpSelected);
		com.Path spANN2 = getANNPath(q[1], NNdpSelected);
		com.Path spANN3 = getANNPath(q[2], NNdpSelected);		
		long stopTime = System.nanoTime();
		double mytime = (stopTime - startTime)/1e9;

		// To measure time taken to compute shortest paths from intermediate point to all query points using Raw network ONLY (ANN)
		long startVoronoiTime = System.nanoTime();
		//Raw network ANN
		com.Path spAANN[] =  new com.Path[3];
		for(int i=0;i<3;i++){
			ArrayList <Integer> tempList = ShortestPath.getSPOnRawNetwork(NNdpSelected, q[i], toBeChanged);
			spAANN[i] = new com.Path(NNdpSelected.getId(), q[i].getId(), tempList, NNdpSelected.minDist);
			resetRnet(toBeChanged);
			toBeChanged.clear();
		}
		ArrayList<Integer> qpList = new ArrayList<Integer>();
		for(NPoint p : q){
			qpList.add(p.getId());
		}
		VANNQueryProcessing vq = new VANNQueryProcessing();
		vq.processQueryNVD(qpList);
		vq.clearAll();
		long stopVoronoiTime = System.nanoTime();
		double VoronoiQPtime = (stopVoronoiTime - startVoronoiTime)/1e9;

		//Print result of comparison
		double myNetPathDist = spANN1.pathdist + spANN2.pathdist + spANN3.pathdist;
		System.out.println("AANN: "+dpSelected.getId()+" VANNTime: "+VoronoiQPtime+" AANNTime: "+mytime+" AANNDist: "+myNetPathDist);
		/*double stdPathDist = spAANN[0].pathdist + spAANN[1].pathdist + spAANN[2].pathdist;
		writer.write( mytime + ",");
		writer.write(stdtime + "," + spANN1.pathdist +","+ spAANN[0].pathdist +","+ spANN2.pathdist);
		writer.write("," + spAANN[1].pathdist + "," + spANN3.pathdist + "," + spAANN[2].pathdist);*/
		//writer.write("," + (VoronoiQPtime-mytime) + "," + (myNetPathDist-stdPathDist) + "," + ((myNetPathDist-stdPathDist)/stdPathDist));

		/*if(Double.compare(spANN1.pathdist,spAANN[0].pathdist) < 0 ||
				Double.compare(spANN2.pathdist,spAANN[1].pathdist) < 0 ||
				Double.compare(spANN3.pathdist,spAANN[2].pathdist) < 0) {
			writer.write(",Path dist of AANN less than ANN");
		}
		writer.write("\n");*/
	}

	/* Gets shortest paths from query point q to network point NNdpSelected in 
	 * three stages: 1) Raw network traversal from q to nearest reference point
	 * 2) Grid network traversal from that reference point to reference point of 
	 * grid cell containing NNdpSelected 3) Raw network traversal from reference 
	 * point reached in 2 to NNdpSelected 
	 */
	public static com.Path getANNPath(NPoint q, NPoint NNdpSelected){
		ArrayList <Integer> plist1 = new ArrayList<Integer>();
		ArrayList <Integer> plist2 = new ArrayList<Integer>();
		ArrayList <Integer> plist3 = new ArrayList<Integer>();
		Set<Integer> toBeChanged = new HashSet<Integer>();
		Set<NPoint> tlist = new HashSet<>();

		double dist = 0;
		// Get grid cell sgc containing q
		GCell sgc = Grid.getGridCell(q.getX(),q.getY());
		// Get grid cell tgc containing NNdpSelected
		GCell tgc = Grid.getGridCell(NNdpSelected.getX(),NNdpSelected.getY());

		// When q and NNdpSelected are in same grid cell
		if(sgc.equals(tgc)){
			plist1 = ShortestPath.getSPOnRawNetwork(NNdpSelected, q, toBeChanged);
			if (plist1.isEmpty())
				return null;
			dist = NNdpSelected.minDist;
			resetRnet(toBeChanged);
		}else{
			for(int i=0;i<sgc.getRpl().length;i++)
				tlist.add(sgc.getRpl()[i]);
			plist1 = ShortestPath.getSPOnRawNetworkToTargetList(tlist, q, toBeChanged, NNdpSelected);
			if (plist1.isEmpty())
				return null;
			int qRefId = plist1.get(plist1.size()-1);
			NPoint qRef = ProcessData.getById(qRefId);
			dist = qRef.minDist;
			resetRnet(toBeChanged);
			tlist.clear();
			toBeChanged.clear();

			for(int i=0;i<tgc.getRpl().length;i++){
				tlist.add(tgc.getRpl()[i]);
			}
			plist2 = ShortestPath.getSPOnGrid(tlist, qRef, toBeChanged, NNdpSelected);
			if (plist2.isEmpty())
				return null;
			int tRefId = plist2.get(plist2.size()-1);
			NPoint tRef = ProcessData.getById(tRefId);
			dist += tRef.minDist;
			resetRnet(toBeChanged);
			toBeChanged.clear();

			plist3 = ShortestPath.getSPOnRawNetwork(NNdpSelected, tRef, toBeChanged);
			if (plist3.isEmpty())
				return null;
			int targetId = plist3.get(plist3.size()-1);
			NPoint target = ProcessData.getById(targetId);
			dist += target.minDist;
			if(target.equals(NNdpSelected) == false) {
				System.out.println("target not query point\n");
				return null;
			}
			resetRnet(toBeChanged);

			plist1.addAll(plist2);
			plist1.addAll(plist3);
		}
		return new com.Path(NNdpSelected.getId(), q.getId(), plist1, dist);
	}

	public static NPoint getById(int id){
		return npMap.get(id);
	}
	public static Map<NPoint,Double> getRnetById(int id){
		for(Entry<NPoint,Map<NPoint,Double>> e: rnet.entrySet()){
			if(e.getKey().getId()==id){
				return e.getValue();
			}
		}
		return null;
	}
	public static void processFile(Path file, String type){
		if(Files.exists(file) && Files.isReadable(file)){
			try {
				BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset());
				String line;
				if(type.endsWith("n")){
					double x,y;
					int id;
					String[] el = new String[2];
					while((line = reader.readLine()) != null) {
						el = line.split(" ");
						x = Double.parseDouble(el[1]);
						y = Double.parseDouble(el[2]);
						id = Integer.parseInt(el[0]);
						if(type.equals("nn")){
							NPoint n = new NPoint(id,x,y);
							rnet.put(n, null);
							npMap.put(id, n);
							noOfNodes++;
							if(minx > x)
								minx = x;
							else if(maxx < x)
								maxx = x;
							if(miny > y)
								miny = y;
							else if(maxy < y)
								maxy = y;
						}else{
							if(x>=minx && x<= maxx && y>=miny && y<= maxy){
								if(npMap.containsKey(id)){
									dpoints.add(npMap.get(id));
								} else {
									System.out.println("Error!!");
									System.exit(0);
									//NPoint d = new NPoint(id,x,y);
									//dpoints.add(d);
								}
							}
						}
					}
				}else if(type.equals("ne")){
					String[] el = new String[3];
					int sid,eid;
					double dist = 0;
					while((line = reader.readLine()) != null) {
						el = line.split(" ");
						sid = Integer.parseInt(el[1]);
						eid = Integer.parseInt(el[2]);
						dist = Double.parseDouble(el[3]);
						NPoint sn = getById(sid);
						NPoint en = getById(eid);
						if(sn != null){
							Map<NPoint,Double> l1 = rnet.get(sn);
							Map<NPoint,Double> l2 = rnet.get(en);
							if(l1==null)
								l1 = new HashMap<NPoint,Double>();
							if(l2==null)
								l2 = new HashMap<NPoint,Double>();
							l1.put(en, dist);
							l2.put(sn, dist);
							rnet.put(sn, l1);
							rnet.put(en, l2);
						}
					}
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("File invalid");
		}
	}

	@SuppressWarnings("deprecation")
	public static Point2D getIntermediatePoint(NPoint qpoint1, NPoint qpoint2, NPoint qpoint3) {

		Point2D q1 = new Point2D(qpoint1.getX(),qpoint1.getY());
		Point2D q2 = new Point2D(qpoint2.getX(),qpoint2.getY());
		Point2D q3 = new Point2D(qpoint3.getX(),qpoint3.getY());
		boolean flag = false;
		LineSegment2D s12 = new LineSegment2D(q1, q2);
		LineSegment2D s23 = new LineSegment2D(q2, q3);

		ArrayList <Point2D> querypoints = new ArrayList<>();
		querypoints.add(q1);
		querypoints.add(q2);
		querypoints.add(q3);
		queryarea = Polygons2D.convexHull(querypoints);

		double d12 = q1.distance(q2);
		double d23 = q2.distance(q3);
		double d13 = q1.distance(q3);
		ArrayList<Point2D> ip12 = (ArrayList<Point2D>) Circle2D.getIntersections(new Circle2D(s12.firstPoint(), d12), new Circle2D(s12.lastPoint(), d12));
		ArrayList<Point2D> ip23 = (ArrayList<Point2D>) Circle2D.getIntersections(new Circle2D(s23.firstPoint(), d23), new Circle2D(s23.lastPoint(), d23));
		Point2D oq3 = new Point2D();
		Point2D oq1 = new Point2D();
		if(isSameSide(q1,q2,ip12.get(0),q3))
			oq3 = ip12.get(1);
		else
			oq3 = ip12.get(0);
		if(isSameSide(q2,q3,ip23.get(0),q1))
			oq1 = ip23.get(1);
		else
			oq1 = ip23.get(0);
		LineSegment2D line1 = new LineSegment2D(oq3, q3);
		LineSegment2D line2 = new LineSegment2D(oq1, q1);
		Point2D fermat = line1.intersection(line2);

		if(fermat != null){
			if(queryarea.contains(fermat))
				return fermat;
			else
				flag = true;
		}else
			flag = true;
		if(flag){
			if(d12 > d23 && d12 > d13)
				return q3;
			else if(d23 > d12 && d23 > d13)
				return q1;
			else
				return q2;
		}
		return fermat;
	}

	public static Point2D getIntermediatePoint_2(NPoint q1, NPoint q2) {
		Point2D qp1 = new Point2D(q1.getX(),q1.getY());
		Point2D qp2 = new Point2D(q2.getX(),q2.getY());
		LineSegment2D s12 = new LineSegment2D(qp1, qp2);
		return s12.point(s12.length()/2);
	}
	private static boolean isSameSide(Point2D q1, Point2D q2, Point2D a, Point2D b) {
		double x1 = q1.getX();
		double x2 = q2.getX();
		double y1 = q1.getY();
		double y2 = q2.getY();
		double val = ((y1-y2)*(a.getX()-x1)+(x2-x1)*(a.getY()-y1))*((y1-y2)*(b.getX()-x1)+(x2-x1)*(b.getY()-y1));
		if(val < 0)
			return false;
		return true;
	}

	public static void resetRnet(Set<Integer> toBeChanged){
		for(int j : toBeChanged){
			NPoint n = getById(j);
			n.previous = null;
			n.minDist =  Double.POSITIVE_INFINITY;
			n.f =  -1;
		}
	}
	public static void depthFirstRecursive(NPoint source){
		source.minDist = 0;
		source.gen = 1;
		Map<NPoint, Double> nv = ProcessData.getRnet(source);
		for(NPoint v:nv.keySet()){
			if(v.gen == -1){
				v.gen = 1;
				v.previous = source;
				v.minDist = source.minDist+1;
				depthFirstRecursive(v);
			}
		}
		source.gen = 2;
		writer.write(source.getId()+"\n");
	}
}
