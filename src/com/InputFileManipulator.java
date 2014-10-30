package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InputFileManipulator {

	public static void main(String[] args) {
		BufferedReader reader = null;
		Map<Integer,NPoint> np = new HashMap<Integer,NPoint>();
		Map<Integer,Integer> idMap = new HashMap<Integer,Integer>();
		PrintWriter writer = null;
		java.nio.file.Path f = Paths.get("C:/Users/sec/Dropbox/Courses - Auburn/COMP7120/ACMGIS2014/AANN/AANN/src/com/OL_raw_node.txt");
		try {
			writer = new PrintWriter(new File("C:/Users/sec/Dropbox/Courses - Auburn/COMP7120/ACMGIS2014/AANN/AANN/src/com/OL_raw_node_smaller.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			reader = Files.newBufferedReader(f, Charset.defaultCharset());
			String line = null;
			double x,y;
			String[] el = new String[2];
			int id = 0;
			int i = 0;
			while((line = reader.readLine())!= null){
				el = line.split(" ");
				x = Double.parseDouble(el[1]);
				y = Double.parseDouble(el[2]);
				id = Integer.parseInt(el[0]);
				if(x>4000 && x<6000 && y>2000 && y<4000){
					NPoint n = new NPoint(i,x,y);
					idMap.put(id, i);
					np.put(id,n);
					writer.write(i+" "+x+" "+y+"\n");
					i++;
				}
			}
		}catch (Exception e) {
				e.printStackTrace();
		}
		writer.close();
		java.nio.file.Path f1 = Paths.get("C:/Users/sec/Dropbox/Courses - Auburn/COMP7120/ACMGIS2014/AANN/AANN/src/com/OL_raw_edges.txt");
		try {
			writer = new PrintWriter(new File("C:/Users/sec/Dropbox/Courses - Auburn/COMP7120/ACMGIS2014/AANN/AANN/src/com/OL_raw_edges_smaller.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			reader = Files.newBufferedReader(f1, Charset.defaultCharset());
			String[] el = new String[3];
			String line = null;
			int sid,eid;
			float dist = 0;
			int i = 0;
			while((line = reader.readLine())!= null){
				el = line.split(" ");
				sid = Integer.parseInt(el[1]);
				eid = Integer.parseInt(el[2]);
				dist = Float.parseFloat(el[3]);
				if(np.keySet().contains(sid)&& np.keySet().contains(eid)){
					writer.write(i+" "+idMap.get(sid)+" "+idMap.get(eid)+" "+dist+"\n");
					i++;
				}
			}
		}catch (Exception e) {
				e.printStackTrace();
		}
		writer.close();
		try {
			writer = new PrintWriter("C:/Users/sec/Dropbox/Courses - Auburn/COMP7120/ACMGIS2014/AANN/AANN/src/com/OL_data_points_smaller.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Random random = new Random();
		List<Integer> keys = new ArrayList<Integer>(np.keySet());
		int size = keys.size();
		for(int i = 1;i<=50;i++){
			int ran = random.nextInt(size);
			int dpId = keys.get(ran);
			NPoint dp = np.get(dpId);
			writer.write(dp.getId()+" "+dp.getX()+" "+ dp.getY()+"\n");
			keys.set(ran, keys.get(size-1));
			size--;			
		}
		writer.close();
	}
}
