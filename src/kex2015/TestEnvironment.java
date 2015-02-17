package kex2015;

import java.util.ArrayList;

public class TestEnvironment {
	
	Map map;
	Boat boat;
	Kex kex;
	
	public TestEnvironment() {
		
		/**Create map*/
		//map = new map();
		
		/**Create boat*/
		boat = new Boat(map, 200);
		
		/**Create kex*/
		kex = new Kex(boat, null, null , (Double) null , null, 200 );
		
		
		/**start threads*/
		
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("JAG LEVER!");
		
		TestEnvironment t = new TestEnvironment();
		
	}
}
