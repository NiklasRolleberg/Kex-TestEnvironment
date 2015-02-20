package kex2015;

import java.util.ArrayList;

public class TestEnvironment {
	
	Map map;
	Boat boat;
	Kex kex;
	View view;
	
	Thread boatThread;
	Thread kexThread;
	Thread viewThread;
	
	public TestEnvironment() {
		
		/**Create map*/
		map = new Map("MapTest.csv");
		
		/**Create boat*/
		boat = new Boat(map, 200);
		
		/**Create kex*/
		//polygon
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer>();
		int[] endPos = {1,3};
		kex = new Kex(boat, x, y , 0.24 , endPos , 200 );
		
		/**Create view*/
		view = new View(map, boat,100);
		
		/**create and start threads*/
		boatThread = new Thread(boat);
		kexThread = new Thread(kex);
		viewThread = new Thread(view);
		
		boatThread.start();
		kexThread.start();
		viewThread.start();
		
		try {
			Thread.sleep(1000);
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
		
		TestEnvironment t = new TestEnvironment();
		
	}
}
