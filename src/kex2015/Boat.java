package kex2015;

import java.util.ArrayList;

public class Boat implements Runnable {
	
	private double[] position = {0,0};
	private double[] wayPoint = {0,0};
	public static double heading = 0;
	private double speed = 0;
	private long lastUpdate = 0;
	private double acceleration = 0;
	private double turningspeed = 0;
	private double maxSpeed = 30;
	private double maxAcc = 0;
	
	private long dt = 100;
	private boolean stop = false;
	
	private Map map;
	
	public static ArrayList<Double> xPos;
	public static ArrayList<Double> yPos;
	
	public Boat(Map map, long dt, double startLong, double startLat) {
		this.map = map;
		this.position[0] = startLong;
		this.position[1] = startLat;
		
		this.wayPoint[0] = startLong+500;
		this.wayPoint[1] = startLat+500;
		
		this.dt = dt;
		
		xPos = new ArrayList<Double>();
		yPos = new ArrayList<Double>();
		
		xPos.add(startLong);
		yPos.add(startLat);
		
		
		//TODO fixa saker
		
		System.out.println("Boat created");
	}

	public void setWayPoint(double longitude, double latitude) {
		wayPoint[0] = longitude;
		wayPoint[1] = latitude;		
	}
	
	/**
	 * @return
	 * 0: latitude
	 * 1: longitude
	 * 2: heading
	 * 3: speed
	 * 4: sonar 1 (depth)
	 * 5: sonar 2 (forward)
	 */
public double[] getSensordata() {
		
		double[] r = new double[6];
		r[0] = this.position[0];
		r[1] = this.position[1];
		r[2] = this.heading;
		r[3] = this.speed;
		r[4] = map.getDepth(this.position[0], this.position[1]);
		r[5] = 0;
		return r;
	}
	
	private void updatePos() {
		//System.out.println("UpdatePosition");
		if(lastUpdate == 0) {
			lastUpdate = System.currentTimeMillis();
			return;
		}
		
		double elapsedTime = (((double)System.currentTimeMillis() - lastUpdate))  / 1000;
		//TODO Update boat position;
		
		//calculate heading towards the target
		double x = wayPoint[0] - position[0];
		double y = wayPoint[1] - position[1];
		double d = Math.sqrt(x*x + y*y);
		
		heading = Math.asin(y/d);
		
		if(heading == Double.NaN) {
			lastUpdate = System.currentTimeMillis();
			return;
		}
		
		//move the boat a bit at max speed or slower if the boat will overshoot the target before next update
		speed = maxSpeed;
		if(d < dt*maxSpeed/1000) {
			speed = d/(((double)dt)/1000);
		}
		
		position[0] += speed * elapsedTime * Math.cos(heading);
		position[1] += speed * elapsedTime * Math.sin(heading);
		
		lastUpdate = System.currentTimeMillis();
		
		x = xPos.get(xPos.size()-1) - position[0];
		y = yPos.get(yPos.size()-1) - position[1];
		d = Math.sqrt(x*x + y*y);
		if(d > 0.5) {
			xPos.add(position[0]);
			yPos.add(position[1]);
		}
		
	}
	
	public void stop() {
		stop = true;
	}

	@Override
	public void run() {
		
		System.out.println("Boat running!");
		
		while(!stop) {
			
			updatePos();
			/*
			System.out.println("\nBoat position");
			System.out.println("x: " + position[0]);
			System.out.println("y: " + position[1]);
			System.out.println("speed: " + speed + "\n");
			*/
			
			System.out.println(map.getDepth(position[0], position[1]));
			
			try {
				Thread.sleep(dt);
			} catch (InterruptedException e) {
				System.err.println("Fel i Boat: " + e);
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Boat stopped");
		
	}

}
