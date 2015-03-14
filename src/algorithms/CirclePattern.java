package algorithms;

import java.awt.Polygon;
import java.util.ArrayList;

import kex2015.Boat;

public class CirclePattern extends Kex {

	boolean stop = false;
	Polygon polygon;
	
	
	public CirclePattern(Boat inBoat, ArrayList<Double> x,
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
		
		double[] wayPoint = {polygon.getBounds().x + (polygon.getBounds().width / 2)
				, polygon.getBounds().y + (polygon.getBounds().height /2 )}; //centerpos
		
		
		double centerX = wayPoint[0];
		double centerY = wayPoint[1];
		
		boat.setWayPoint(centerX, centerY);
		
		double radius = 3;
		double points = 12;
		double deltaradians  = 2 * Math.PI / points;
		double radians = 0;
		
		
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

			if (!oobCheck(xPos, yPos) || (depth > -0.1)){
				System.out.println("Fel index");
				
				double newX = polygon.getBounds().x  + (polygon.getBounds().width)*Math.random();
				double newY = polygon.getBounds().y  + (polygon.getBounds().height)*Math.random();
				
				centerX = newX;
				centerY = newY;
				radius = 3;
				radians = 0;
				 
				wayPoint[0]=newX;
				wayPoint[1]=newY;
				
				boat.setWayPoint(newX, newY);
			}
			
			else if(d < 3){
				
				double newX = centerX  + radius*Math.cos(radians);
				double newY = centerY  + radius*Math.sin(radians);
				
				radians += deltaradians;
				radius += 1;
				
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
