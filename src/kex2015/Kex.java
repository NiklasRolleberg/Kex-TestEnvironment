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
	
	//KexView kexView;
	//Thread kexViewThread;
	
	
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
		
		//kexView = new KexView();
		//kexViewThread = new Thread(kexView);
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
			
			
			if (xPos < 10 || xPos > 990 || yPos < 10 || yPos > 990){
				System.out.println("Fel index");
				double newX = 500  + (Math.random()-0.5)*500;
				double newY = 500  + (Math.random()-0.5)*500;
				boat.setWayPoint(newX, newY);
				wayPoint[0]=newX;
				wayPoint[1]=newY;
			}
			else if(depth > -0.1){
				//System.out.println("LAND!");
				//reverse back a bit
				double newX = xPos + 40*Math.cos(heading+Math.PI);
				double newY = yPos + 40*Math.sin(heading+Math.PI);
				boat.setWayPoint(newX, newY);
				wayPoint[0]=newX;
				wayPoint[1]=newY;
			}
			else if(d<3){
				//System.out.println("Nära");
				double newX = xPos + (Math.random()-0.5)*2000;
				double newY = yPos + (Math.random()-0.5)*2000;
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
