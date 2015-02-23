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
	
	
	/**Main brain! =)
	 * @param delta is map resolution, not used yet
	 * */
	public Kex(Boat inBoat, ArrayList<Integer> x, ArrayList<Integer> y , double delta , int[] endPos, long dt ) { //double?
		
		this.boat = inBoat;
		this.polygonX = x;
		this.polygonY = y;
		this.delta = delta;
		this.endPos = endPos;
		this.dt = dt;
		System.out.println("Kex created");
	}

	public void stop() {
		System.out.println("KEX stop");
		stop = true;
	}
	
	/**Checks if out of bounds */
	private boolean oobCheck(double xpos, double ypos){
		if (xpos < 40 || xpos > 960 || ypos < 40 || ypos > 960){
			return true;
		}
		return false;
	}
	
	@Override
	public void run() {
		
		double[] wayPoint = {0, 0};
		while (!stop) {
			
			//System.out.println("Kex running");
			double[] sensorData = boat.getSensordata();
			double xPos = sensorData[0];
			double yPos = sensorData[1];
			double depth = sensorData[4];
			//System.out.println(depth);
			System.out.println(xPos);
			double x = wayPoint[0] - xPos;
			double y = wayPoint[1] - yPos;
			double d = Math.sqrt(x*x + y*y);
			if (oobCheck(xPos, yPos)){
				boat.setWayPoint(500, 500);
			}
			else if(depth > 0){
				double newX = Math.random()*1000;
				double newY = Math.random()*1000;
				boat.setWayPoint(newX, newY);
				wayPoint[0]=newX;
				wayPoint[1]=newY;
			}
			else if(d<10){
				double newX = Math.random()*1000;
				double newY = Math.random()*1000;
				boat.setWayPoint(newX, newY);
				wayPoint[0]=newX;
				wayPoint[1]=newY;
			}
				
			
			try {
				Thread.sleep(dt);
			} catch (InterruptedException e) {
				System.err.println("Fel i Kex");
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Kex stopped");
		
	}

}
