package algorithms;

import java.util.ArrayList;

import kex2015.Boat;


/**extend this class to create a new algorithm*/
public abstract class Kex implements Runnable {
	
	Boat boat;
	
	ArrayList<Double> polygonX;
	ArrayList<Double> polygonY;
	int[] endPos;
	
	double delta;
	long dt;
	
	
	/**Main brain! =)
	 * @param delta is map resolution, not used yet
	 * */
	public Kex(Boat inBoat, ArrayList<Double> x, ArrayList<Double> y , double delta , int[] endPos, long dt ) { //double?
		
		this.boat = inBoat;
		this.polygonX = x;
		this.polygonY = y;
		this.delta = delta;
		this.endPos = endPos;
		this.dt = dt;
	}

	/**Should stop the thread*/
	public abstract void stop();
	 
	@Override
	public abstract void run();

}
