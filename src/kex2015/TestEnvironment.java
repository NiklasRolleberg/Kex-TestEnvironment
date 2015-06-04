package kex2015;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import controller.*;


public class TestEnvironment {
	
	Map map;
	Boat boat;
	Kex kex;
	View view;
	
	Thread boatThread;
	Thread kexThread;
	Thread viewThread;
	
	public TestEnvironment(String mapFile, ArrayList<Double> polygonX, ArrayList<Double> polygonY, double startLong, double startLat) {
		
		/**Create map*/
		//map = new Map("MapTest.csv");
		map = new Map(mapFile);
		
		// Create np Boats
		ArrayList<NpBoat> otherBoats = new ArrayList<NpBoat>();
		
		//max
		double maxY = Double.MIN_VALUE;
		int index = -1;
		for(int i=0;i<polygonY.size();i++) {
			if(polygonY.get(i) > maxY) {
				maxY = polygonY.get(i);
				index = i;
			}
		}
		
		//otherBoats.add(new NpBoat(new double[]{polygonX.get(index),polygonY.get(index)},-Math.PI/2, 10, polygonX,polygonY));
		for(int i = 0; i< 40;i++) {
			otherBoats.add(new NpBoat(polygonX,polygonY));
		}
		
		/**Create boat*/
		//boat = new Boat(map, 25, startLong, startLat); //25
		boat = new TwoFrontSonarBoat(map, 25, startLong, startLat); //25
		
		/**Create kex*/
		//polygon
		int[] endPos = {1,3};
		kex = new Kex(boat, polygonX, polygonY, otherBoats , 10 , endPos , 200); //100
				
		/**Create view*/
		view = new View(map, boat, otherBoats, 20 , polygonX, polygonY);
		
		/**create and start threads*/
		boatThread = new Thread(boat);
		kexThread = new Thread(kex);
		viewThread = new Thread(view);
		
		boatThread.start();
		kexThread.start();
		viewThread.start();
		
		try {
			Thread.sleep(900000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//kex.stop();
		//boat.stop();
		//view.stop();
	}
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("JAG LEVER!");
		
		
		
		ArrayList<Double> X = new ArrayList<Double>();
		ArrayList<Double> Y = new ArrayList<Double>();
		
		//try to read polygon from fileprivate void read() 
		boolean fail = false;
			
		try
		{
			//read X
			FileInputStream fileInX = new FileInputStream("polygonx.ser");
			ObjectInputStream inX = new ObjectInputStream(fileInX);
			X = (ArrayList<Double>) inX.readObject();
			inX.close();
			fileInX.close();
			
			//read Y
			FileInputStream fileInY = new FileInputStream("polygony.ser");
			ObjectInputStream inY = new ObjectInputStream(fileInY);
			inY = new ObjectInputStream(fileInY);
			Y = (ArrayList<Double>) inY.readObject();
			inY.close();
			fileInY.close();	
			
		}catch(Exception e)
		{
			//e.printStackTrace();
			
			System.err.println("Fileread failed, taking default polygon values");
			
			fail = true;
		}
		/*default values*/
		if(fail) {
			X.clear();
			Y.clear();
			
			X.add(10.0);
			X.add(10.0);
			X.add(100.0);
			X.add(100.0);
			
			Y.add(10.0);
			Y.add(100.0);
			Y.add(100.0);
			Y.add(10.0);	
		}
		
		//find a start pos
		double startLong;// = meanX;
		double startLat;// = meanY;

		try {
			//read start.txt
			BufferedReader br = new BufferedReader(new FileReader("start.txt"));
	        startLong = Double.parseDouble(br.readLine());
	        startLat = Double.parseDouble(br.readLine());
	        br.close();
		}
		catch(Exception e) {
			System.out.println("Failed to read start position " + e);
			double meanX = 0;
			double meanY = 0;
			for(int i = 0; i<X.size();i++) {
				meanX += X.get(i);
				meanY += Y.get(i);
			}
			meanX *= 1.0/X.size();
			meanY *= 1.0/X.size();
			
			startLong = meanX;
			startLat = meanY;
		}
		
		TestEnvironment t = new TestEnvironment("test.csv", X , Y, startLong, startLat);
	}
}
