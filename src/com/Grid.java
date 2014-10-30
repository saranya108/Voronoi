package com;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Grid {
	public static double gminx;
	public static double gminy;
	public static double gmaxx;
	public static double gmaxy;
	public static int row;
	public static int col;
	public static double xres;
	public static double yres;
	public static GCell[][] grid;
	public static Map<NPoint,List<Path>> gnet = new HashMap<NPoint,List<Path>>();


	public static void buildGrid(double minx, double maxx, double miny, double maxy, String rowcol) {

		gminx = minx;
		gminy = miny;
		gmaxx = maxx;
		gmaxy = maxy;
		row = Integer.parseInt(rowcol);
		col = row;
		xres = (gmaxx-gminx)/col;
		yres = (gmaxy-gminy)/row;
		grid = new GCell[row][col];

		int gid = 0;
		double i = gminx;
		double j = gminy;
		int a = 0;
		int b = 0;
		for(a = 0;a < row; a++){
			for(b = 0;b < col; b++){
				GCell gc = new GCell();
				if(b == col-1 && a != row-1){
					GPoint g1 = new GPoint(gid,i,j);
					GPoint g2 = new GPoint(gid+1,i+xres,j);
					gc.setGpl(new GPoint[]{g1,g2,null,null});
					gid = gid + 2;
				}else if(b != col-1 && a == row-1){
					GPoint g1 = new GPoint(gid,i,j);
					GPoint g2 = new GPoint(gid+1,i,j+yres);
					gc.setGpl(new GPoint[]{g1,null,g2,null});
					gid = gid + 2;
				}else if(a == row-1 && b == col-1){
					GPoint g1 = new GPoint(gid,i,j);
					GPoint g2 = new GPoint(gid+1,i,j+yres);
					GPoint g3 = new GPoint(gid+2,i+xres,j);
					GPoint g4 = new GPoint(gid+3,i+xres,j+yres);
					gc.setGpl(new GPoint[]{g1,g2,g3,g4});
					gid = gid + 4;
				}else{
					GPoint g = new GPoint(gid,i,j);
					gc.setGpl(new GPoint[]{g,null,null,null});
					gid = gid + 1;
				}
				gc.setX(a);
				gc.setY(b);
				grid[a][b] = gc;
				i = i + xres;
			}
			i = gminx;
			j = j + yres;
		}
	}

	public static void distributeDPoints(ArrayList<NPoint> dpoints) {
		Iterator<NPoint> itr = dpoints.iterator();
		while(itr.hasNext()){
			NPoint dp = itr.next();
			double dpx = dp.getX();
			double dpy = dp.getY();
			GCell gc = getGridCell(dpx,dpy);
			gc.getDpl().add(dp);
			gc.setPresent(gc.getPresent()+1);
		}
	}

	public static void distributeDPointsRandom() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("C:/Users/szs00_000/Dropbox/Courses - Auburn/COMP7120/ACMGIS2014/AANN/AANN/src/com/OL_data_points.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Random random = new Random();
		List<NPoint> keys = new ArrayList<NPoint>(ProcessData.rnet.keySet());
		int size = keys.size();
		for(int i = 1;i<=2000;i++){
			int ran = random.nextInt(size);
			NPoint dp = keys.get(ran);
			writer.write(dp.getId()+" "+dp.getX()+" "+ dp.getY()+"\n");
			keys.set(ran, keys.get(size-1));
			size--;			
		}
		writer.close();
	}

	public static GCell getGridCell(double x, double y) {
		int a = 0;
		int b = 0;
		a = Math.min((int) Math.floor((y-gminy)/yres),row-1);
		b = Math.min((int) Math.floor((x-gminx)/xres),col-1);
		return grid[a][b];
	}

	public static void distributeNPoints(Collection<NPoint> NPoints) {
		Iterator<NPoint> itr = NPoints.iterator();
		while(itr.hasNext()){
			NPoint np = itr.next();
			double npx = np.getX();
			double npy = np.getY();
			try{
				GCell gc = getGridCell(npx,npy);
				gc.getNpl().add(np);
			}catch(Exception e){		
				e.printStackTrace();
			}			
		}
	}

	public static void findRPShortestPath(){
		for(int i=0;i<row;i++){
			for(int j=0;j<col;j++){
				assignRPointsToGPoints(i,j);
			}
		}
		for(int a=0;a<row;a++){
			for(int b=0;b<col;b++){
				GCell currentGCell = grid[a][b];
				if(currentGCell.getRpl(0) != null){
					NPoint srp = currentGCell.getRpl(0);
					System.out.print(".");
					//System.out.println(g_count);
					if(b == 0){
						setSP(srp,grid[a][b].getRpl(1));
						setSP(srp,grid[a][b].getRpl(2));	
						setSP(srp,grid[a][b].getRpl(3));
					}else {
						setSP(srp,grid[a][b-1].getRpl(2));
						setSP(srp,grid[a][b].getRpl(1));
						setSP(srp,grid[a][b].getRpl(2));	
						setSP(srp,grid[a][b].getRpl(3));
					}
				}
			}
		}
	}

	public static void setSP(NPoint srp, NPoint erp) {
		if(srp != null && erp != null){
			Set<Integer> toBeChanged = new HashSet<Integer>();
			ArrayList<Integer> plist = ShortestPath.getSPOnRawNetwork(erp, srp, toBeChanged);
			Path spath = new Path(srp.getId(),erp.getId(),plist,erp.minDist);
			addSPToGnet(srp,spath);
			Path revspath = new Path(erp.getId(),srp.getId(),spath.reversePath(),erp.minDist);
			addSPToGnet(erp,revspath);
			ProcessData.resetRnet(toBeChanged);
		}
		//writer.println("SP: GCell: "+currentGCell+" Path: "+spath.toString());
		//writer.println("RSP: GCell: "+currentGCell+" Path: "+revspath.toString());
	}

	private static void addSPToGnet(NPoint rp, Path path) {
		List<Path> paths = gnet.get(rp);
		if(paths == null){
			paths = new ArrayList<Path>();
			paths.add(path); 
			gnet.put(rp, paths);			
		} else 	{	
			paths.add(path); 
			gnet.put(rp, paths);
		}
	}

	private static void assignRPointsToGPoints(int i, int j){
		Set<NPoint> np = new HashSet<NPoint>();
		int x = 0;
		GCell gc = grid[i][j];
		for(int k=0;k<4;k++){
			if(gc.getGpl()[k] != null){
				GPoint g = gc.getGpl()[k];
				np.clear();
				np.addAll(gc.getNpl());
				if(k == 0){
					if(i == 0 && j != 0){
						np.addAll(grid[i][j-1].getNpl());
						x = 1;
					}else if(i != 0 && j == 0){
						np.addAll(grid[i-1][j].getNpl());
						x = 2;
					}else if(i != 0 && j != 0){
						np.addAll(grid[i][j-1].getNpl());
						np.addAll(grid[i-1][j].getNpl());
						np.addAll(grid[i-1][j-1].getNpl());
						x = 3;
					}
				}else if(k == 1){
					if(i != 0){
						np.addAll(grid[i-1][j].getNpl());
						x = 5;
					}
				}else if(k == 2){
					if(j != 0){
						np.addAll(grid[i][j-1].getNpl());
						x = 7;
					}
				}
				NPoint rp = calculateNNP(g, np);		
				if(rp != null){
					rp.used = true;
					gc.setGprplVal(g, rp);
					gc.setRplVal(rp,k);
					switch(x){
					case 1: setGCAttributes(grid[i][j-1],g,rp,1);	
					break;
					case 2:	setGCAttributes(grid[i-1][j],g,rp,2);		
					break;
					case 3:	setGCAttributes(grid[i][j-1],g,rp,1);	
					setGCAttributes(grid[i-1][j],g,rp,2);	
					setGCAttributes(grid[i-1][j-1],g,rp,3);	
					break;
					case 5:	setGCAttributes(grid[i-1][j],g,rp,3);	
					break;
					case 7:	setGCAttributes(grid[i][j-1],g,rp,3);	
					break;
					default:break;
					}
				}
			}
		}
	}

	private static void setGCAttributes(GCell gc, GPoint gp, NPoint rp, int i) {
		gc.setGplVal(i, gp);
		gc.setGprplVal(gc.getGpl()[i], rp);
		gc.setRplVal(rp,i);
	}

	public static NPoint calculateNNP(Point gp, Set<NPoint> npl) {
		double minDist = Double.POSITIVE_INFINITY;
		NPoint nearest = null;
		/*if(npl.isEmpty())
			System.out.println();*/
		if(npl.contains(gp))
			return (NPoint) gp;
		Iterator <NPoint> itr = npl.iterator();
		while(itr.hasNext()){
			NPoint np = itr.next();
			double dist = Math.sqrt(((gp.getX()-np.x)*(gp.getX()-np.x)) + (gp.getY()-np.y)*(gp.getY()-np.y));
			if(minDist >= dist && np.used == false){
				minDist = dist;
				nearest = np;
			}
		}
		return nearest;
	}


	public static NPoint calculateNDP(Point p, Set<NPoint> dpl) {
		double minDist = Double.MAX_VALUE;
		NPoint nearest = null;

		Iterator<NPoint> itr = dpl.iterator();
		while(itr.hasNext()){
			NPoint dp = itr.next();
			double dist = (double) (Math.sqrt(((p.getX()-dp.x)*(p.getX()-dp.x)) + (p.getY()-dp.y)*(p.getY()-dp.y)));
			if(minDist >= dist){
				minDist = dist;
				nearest = dp;
			}
		}
		return nearest;
	}

	public static NPoint getDataPointForQuery(Point ip) {
		NPoint dpSelected = null;
		GCell gc = getGridCell(ip.getX(), ip.getY());
		if(gc.getPresent() > 0)
			dpSelected = calculateNDP(ip,gc.getDpl());
		else
			dpSelected = traverseCircular(ip,gc);
		return dpSelected;
	}

	private static NPoint traverseCircular(Point ip,GCell gc) {
		int x = gc.getX();
		int y = gc.getY();
		NPoint dpSelected = null;
		int l = 0;

		Set <NPoint> finaldpl = new HashSet<NPoint>();
		while(finaldpl.isEmpty()){
			l++;
			finaldpl.addAll(loopOnceDPL(x,y,l));
		}
		finaldpl.addAll(loopOnceDPL(x,y,l+1));
		dpSelected = calculateNDP(ip, finaldpl);
		return dpSelected;
	}

	private static Set <NPoint> loopOnceDPL(int x, int y, int l) {
		GCell currcell = null;
		Set<NPoint> dpl = new HashSet<NPoint>();
		for(int b=y-l;b<=y+l;b++){
			int a=x+l;
			if( b >= 0 && b<= col-1){
				if(a >=0 && a <= row-1){
					currcell = grid[a][b];
					if(currcell.getPresent()>0){
						dpl.addAll(currcell.getDpl());
					}
				}
				a=x-l;
				if(a >=0 && a <= row-1){
					currcell = grid[a][b];
					if(currcell.getPresent()>0){
						dpl.addAll(currcell.getDpl());
					}
				}
			}
		}
		for(int p=x-l;p<=x+l;p++){
			int q=y+l;
			if( p >= 0 && p<= row-1){
				if(q >=0 && q <= col-1){
					currcell = grid[p][q];
					if(currcell.getPresent()>0){
						dpl.addAll(currcell.getDpl());
					}
				}
				q=y-l;
				if(q >=0 && q <= col-1){
					currcell = grid[p][q];
					if(currcell.getPresent()>0){
						dpl.addAll(currcell.getDpl());
					}
				}
			}		
		}
		return dpl;
	}
}