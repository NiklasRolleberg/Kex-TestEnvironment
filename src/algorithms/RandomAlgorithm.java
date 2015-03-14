package algorithms;

import java.awt.Polygon;
import java.util.ArrayList;

import kex2015.Boat;

public class RandomAlgorithm extends Kex {

	boolean stop = false;
	Polygon polygon;
	
	
	public RandomAlgorithm(Boat inBoat, ArrayList<Double> x,
			ArrayList<Double> y, double delta, int[] endPos, long dt) {
		super(inBoat, x, y, delta, endPos, dt);
		
		
		polygon = new Polygon();
		for(int i = 0;i < polygonX.size();i++) {
			polygon.addPoint(polygonX.get(i).intValue(), polygonY.get(i).intValue());
		}
	}

	
	/**should stop the thread*/
	public void stop() {
		System.out.println("KEX stop");
		stop = true;
	}
	
	/**Checks if out of bounds */
	private boolean oobCheck(double xpos, double ypos){
		return polygon.contains(xpos, ypos);
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
			double heading = sensorData[2];
			double x = wayPoint[0] - xPos;
			double y = wayPoint[1] - yPos;
			double d = Math.sqrt(x*x + y*y);

			if (!oobCheck(xPos, yPos)){
				System.out.println("Fel index");
				double newX = xPos  - 30*Math.cos(heading);
				double newY = yPos  - 30*Math.sin(heading); 
				boat.setWayPoint(newX, newY);
				wayPoint[0]=newX;
				wayPoint[1]=newY;
			}
			else if(depth > -0.1){
				//System.out.println("LAND!");
				//reverse back a bit
				
				double newX = xPos  - 30*Math.cos(heading);
				double newY = yPos  - 30*Math.sin(heading); 
				boat.setWayPoint(newX, newY);
				wayPoint[0]=newX;
				wayPoint[1]=newY;
			}
			else if(d<3){
				//System.out.println("Nära");
				
				double newHeading = Math.random()*2*Math.PI;
				double newX = xPos + 200*Math.cos(newHeading);
				double newY = yPos + 200*Math.sin(newHeading);
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
