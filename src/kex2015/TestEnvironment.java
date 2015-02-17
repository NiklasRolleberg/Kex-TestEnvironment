package kex2015;

import java.util.ArrayList;

public class TestEnvironment {
	
	Map map;
	Boat boat;
	Kex kex;
	
	Thread boatThread;
	Thread kexThread;
	
	public TestEnvironment() {
		
		/**Create map*/
		map = new Map();
		
		/**Create boat*/
		boat = new Boat(map, 100);
		
		/**Create kex*/
		//polygon
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer>();
		int[] endPos = {1,3};
		
		kex = new Kex(boat, x, y , 0.24 , endPos , 200 );
		
		
		/**create and start threads*/
		boatThread = new Thread(boat);
		kexThread = new Thread(kex);
		
		boatThread.start();
		kexThread.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		kex.stop();
		boat.stop();
		
		
		
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("JAG LEVER!");
		
		TestEnvironment t = new TestEnvironment();
		
	}
}
