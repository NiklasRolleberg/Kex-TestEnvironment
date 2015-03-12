package kex2015;

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
		
		//find min and max X,Y
		minX = polygonX.get(0);
		maxX = polygonX.get(0);
		minY = polygonY.get(0);
		maxY = polygonY.get(0);
		
		for(int i=0;i<polygonX.size();i++) {
			if (polygonX.get(i) < minX)
				minX = polygonX.get(i);
			if (polygonX.get(i) > maxX)
				maxX = polygonX.get(i);
			if (polygonY.get(i) < minY)
				minY = polygonY.get(i);
			if (polygonY.get(i) > maxY)
				maxY = polygonY.get(i);
		}

		System.out.println("Kex created");
		
	}

	public void stop() {
		System.out.println("KEX stop");
		stop = true;
	}
	
	/**Checks if out of bounds */
	private boolean oobCheck(double xpos, double ypos){
		if (xpos < minX+2 || xpos > maxX-2 || ypos < minY+2 || ypos > maxX-2){
			return true;
		}
		return false;
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
			
			
			if (oobCheck(xPos, yPos)){
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
				
				double newHeading = heading - (1.0-Math.random())*Math.PI;
				double newX = xPos + 10*Math.cos(newHeading);
				double newY = yPos + 10*Math.sin(newHeading);
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
