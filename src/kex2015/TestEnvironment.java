package kex2015;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import algorithms.Kex;
import algorithms.RandomAlgorithm;

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

		
		/**Create boat*/
		boat = new Boat(map, 25, startLong, startLat); //25
		
		/**Create kex*/
		//polygon
		int[] endPos = {1,3};
		kex = new RandomAlgorithm(boat, polygonX, polygonY , 0.24 , endPos , 100); //100
		//kex = new Kex(boat, polygonX, polygonY , 0.24 , endPos , 100); //100
		
		/**Create view*/
		view = new View(map, boat,20 , polygonX, polygonY);
		
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
		
		
		kex.stop();
		boat.stop();
		view.stop();
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("JAG LEVER!");
		
		//generateRandomMap(1000,1000,"test.csv");
		
		ArrayList<Double> X = new ArrayList<Double>();
		ArrayList<Double> Y = new ArrayList<Double>();
		/*
		X.add(100.0);
		X.add(200.0);
		X.add(200.0);
		X.add(100.0);
		X.add(10.0);
		X.add(10.0);
		
		Y.add(10.0);
		Y.add(75.0);
		Y.add(150.0);
		Y.add(200.0);
		Y.add(150.0);
		Y.add(75.0);
		*/
		
		X.add(10.0);
		X.add(10.0);
		X.add(500.0);
		X.add(500.0);
		
		Y.add(10.0);
		Y.add(500.0);
		Y.add(500.0);
		Y.add(10.0);
		
		double startLong = 100;
		double startLat = 100;
		
		TestEnvironment t = new TestEnvironment("test.csv", X , Y, startLong, startLat);
		
		
	}
}
