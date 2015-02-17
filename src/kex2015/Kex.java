package kex2015;

import java.util.ArrayList;

public class Kex implements Runnable {
	
	Boat boat;
	
	ArrayList<Integer> polygonX;
	ArrayList<Integer> polygonY;
	int[] endPos;
	
	double delta;
	long dt;
	
	boolean stop = false;
	
	
	
	public Kex(Boat boat, ArrayList<Integer> x, ArrayList<Integer> y , double delta , int[] endPos, long dt ) { //double?
		
		this.boat = boat;
		this.polygonX = x;
		this.polygonY = y;
		this.delta = delta;
		this.endPos = endPos;
		this.dt = dt;
		System.out.println("Kex created");
	}

	public void stop() {
		stop = true;
	}
	
	@Override
	public void run() {
		
		while (!false) {
			
			try {
				Thread.sleep(dt);
			} catch (InterruptedException e) {
				System.err.println("Fel i Kex");
				e.printStackTrace();
			}
			
		}
		
	}

}
