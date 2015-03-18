package algorithms;

import java.awt.Polygon;
import java.util.ArrayList;

import kex2015.Boat;

public class TurningAlgorithm extends Kex {

	boolean stop = false;
	Polygon polygon;
	
	public TurningAlgorithm(Boat inBoat, ArrayList<Double> x,
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
	
	private void turn(double x, double y, double heading, boolean right) {
		double r = Math.random()*0.3;
		if (right) {
			double newx =  x + 1000*Math.cos(heading + Math.PI/3 + r);
			double newy =  y + 1000*Math.sin(heading + Math.PI/3 + r);
			boat.setWayPoint(newx, newy);
		} else {
			double newx =  x + 1000*Math.cos(heading - Math.PI/3 + r);
			double newy =  y + 1000*Math.sin(heading - Math.PI/3 + r);
			boat.setWayPoint(newx, newy);
		}
		
		try{
			Thread.sleep(50);
		} catch (Exception e){};
		
	}
	
	@Override
	public void run() {
		
		double[] wayPoint = {polygon.getBounds().x + (polygon.getBounds().width / 2)
				, polygon.getBounds().y + (polygon.getBounds().height /2 )}; //centerpos
		boat.setWayPoint(wayPoint[0], wayPoint[1]);
		
		boolean right = true;
		boolean change = false;

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

			if (!oobCheck(xPos, yPos) || (depth > -0.1)) {// || (depth > -0.1) || (d<3)){
				turn(xPos,yPos, heading, right);
				change = true;
			}
			else if(change) {
				right = (right == false);
			}
			else if(d < 10) {
				turn(xPos,yPos,heading, true);
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
