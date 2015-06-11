package kex2015;

import java.util.ArrayList;


/** Non player boat
 * @author Niklas
 */
public class NpBoat implements Runnable{
	
	public double posX;
	public double posY;
	public double heading;
	public double speed;
	
	long lastUpdate = 0;
	boolean stop = false;
	
	double minX = Double.MAX_VALUE;
	double maxX = Double.MIN_VALUE;
	
	double minY = Double.MAX_VALUE;
	double maxY = Double.MIN_VALUE;
	Thread t;
	
	public NpBoat(ArrayList<Double> polygonX, ArrayList<Double> polygonY) {
		
		//find min and max values
		for(int i=0;i< polygonX.size();i++) {
			if(polygonX.get(i) < minX)
				minX = polygonX.get(i);
			if(polygonX.get(i) > maxX)
				maxX = polygonX.get(i);
			if(polygonY.get(i) < minY)
				minY = polygonY.get(i);
			if(polygonY.get(i) > maxY)
				maxY = polygonY.get(i);
		}
		
		//pick a random position
		
		this.posX = minX + Math.random() * (maxX - minX);
		this.posY = minY + Math.random() * (maxY - minY);
		this.heading = Math.random() * Math.PI * 2;
		this.speed = 5 + Math.random() * 5;
		
		t =  new Thread(this);
		t.start();
		
	}
	
	public NpBoat(double[] pos, double heading, double speed, ArrayList<Double> polygonX, ArrayList<Double> polygonY) {
		this.posX = pos[0];
		this.posY = pos[1];
		
		this.heading = heading;
		this.speed = speed;
		
		//find min and max values to stop boat from going oob
		
		for(int i=0;i< polygonX.size();i++) {
			if(polygonX.get(i) < minX)
				minX = polygonX.get(i);
			if(polygonX.get(i) > maxX)
				maxX = polygonX.get(i);
			if(polygonY.get(i) < minY)
				minY = polygonY.get(i);
			if(polygonY.get(i) > maxY)
				maxY = polygonY.get(i);
		}
		
		//System.out.println("NpBoat created");
		
		t =  new Thread(this);
		t.start();
		//System.out.println("Thread started");
	}


	@Override
	public void run() {

		while(!stop) {
			if(lastUpdate == 0) {
				lastUpdate = System.currentTimeMillis();
			}
			
			double elapsedTime = (((double)System.currentTimeMillis() - lastUpdate))  / 1000;
			posX += speed * elapsedTime * Math.cos(heading);
			posY += speed * elapsedTime * Math.sin(heading);
					
			lastUpdate = System.currentTimeMillis();
			
			//System.out.println("NpBoat: " + posX + " " + posY);
			
			if(posX > (maxX+10)) {
				heading += Math.PI;
				posX = maxX;
			}
			
			if(posX < (minX-10)){
				heading += Math.PI;
				posX = minX;
			}
			
			if(posY > (maxY+10)) {
				heading += Math.PI;
				posY = maxY;
			}
			
			if(posY < (minY-10)) {
				heading += Math.PI;
				posY = minY;
			}
			
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void stop() {
		stop = true;
		System.out.println("Np boat stopped");
	}
	
}
