package kex2015;

import java.awt.Polygon;
import java.util.ArrayList;
public class Kex implements Runnable {
	
	Boat boat;
	
	ArrayList<Double> polygonX;
	ArrayList<Double> polygonY;
	int[] endPos;
	
	double delta;
	long dt;
	
	boolean stop = false;
	
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	
	Polygon polygon;
	
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
		
		polygon = new Polygon();
		
		for(int i = 0;i < polygonX.size();i++) {
			polygon.addPoint(polygonX.get(i).intValue(), polygonY.get(i).intValue());
		}
		
		System.out.println("KEX: polygon points = " + polygon.npoints);
		
		System.out.println("Polyhon test: (10,10)" + polygon.contains(10,10));
		System.out.println("Polyhon test: (-10,-10)" + polygon.contains(-10,-10));
		
		System.out.println("Kex created");
		
	}

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
		//kexViewThread.start();
		
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
			
			//kexView.addData(xPos, yPos, depth);
			
			
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
