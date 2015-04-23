package kex2015;

import java.awt.Color;
import java.util.ArrayList;

public class Boat implements Runnable {
	
	protected double[] position = {0,0};
	private double[] wayPoint = {0,0};
	public double heading = 0;
	protected double speed = 0;
	protected long lastUpdate = 0;
	protected double acceleration = 0;
	protected double turningspeed = 0;
	protected double maxSpeed = 30;
	protected double targetSpeed = 30;
	protected double maxAcc = 0;
	
	protected long dt = 100;
	protected boolean stop = false;
	
	protected Map map;
	
	private double windDirection = Math.random() * Math.PI * 2;
	private double windSpeed = 2;
	/*
	public static ArrayList<Double> xPos;
	public static ArrayList<Double> yPos;*/
	
	public Boat(Map map, int dt, double startLong, double startLat) {
		this.map = map;
		this.position[0] = startLong;
		this.position[1] = startLat;
		
		this.wayPoint[0] = startLong+500;
		this.wayPoint[1] = startLat+500;
		
		this.dt = (long) dt;
		
		/*
		xPos = new ArrayList<Double>();
		yPos = new ArrayList<Double>();
		
		xPos.add(wayPoint[0]);
		yPos.add(wayPoint[1]);
		
		xPos.add(startLong);
		yPos.add(startLat);
		*/
		
		//TODO fixa saker
		
		System.out.println("Boat created");
	}

	public void setWayPoint(double longitude, double latitude) {
		wayPoint[0] = longitude;
		wayPoint[1] = latitude;
	}
	
	/**
	 * @param speed
	 * speed for boat
	 */
	public void setTargetSpeed(double speed) {
		this.targetSpeed = speed;
	}
	
	/**
	 * @return
	 * current waypoint
	 */
	public double[] getWaypoint() {
		return this.wayPoint;
	}
	
	/**
	 * @return
	 * boat posistion
	 */
	public double[] getPos() {
		return this.position;
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
		r[5] = getFrontSonarData();
		return r;
	}
	
	//TODO gör om med fler punkter om det är möjligt utan prestandaförlust
	private double getFrontSonarData() {
		
		double depth = map.getDepth(this.position[0], this.position[1]);
		
		double deg = 20*(Math.PI / 180);
		double dist = -depth* Math.tan(deg);
		double posLong = position[0] + dist*Math.cos(heading);
		double posLat = position[1] + dist*Math.sin(heading);
		double depth1 = map.getDepth(posLong, posLat);
		if(depth > 0) 
			depth1 = 0;
		double ray1 = Math.sqrt(dist*dist + depth1*depth1);
		
		deg = 40*(Math.PI / 180);
		dist = -depth* Math.tan(deg);
		posLong = position[0] + dist*Math.cos(heading);
		posLat = position[1] + dist*Math.sin(heading);
		double depth2 = map.getDepth(posLong, posLat);
		if(depth > 0) 
			depth2 = 0;
		double ray2 = Math.sqrt(dist*dist + depth2*depth2);
		
		deg = 60*(Math.PI / 180);
		dist = -depth* Math.tan(deg);
		posLong = position[0] + dist*Math.cos(heading);
		posLat = position[1] + dist*Math.sin(heading);
		double depth3 = map.getDepth(posLong, posLat);
		if(depth > 0) 
			depth3 = 0;
		double ray3 = Math.sqrt(dist*dist + depth3*depth3);
		
		//System.out.println("Sensor: down= " + depth + "\t ray1= " + ray1 + "\t ray2= " + ray2 + "\t ray3= " + ray3);
		
		return Math.min(Math.min(ray1, ray2), ray3);
	}
	
	private void updatePos() {
		//System.out.println("UpdatePosition");
		if(lastUpdate == 0) {
			lastUpdate = System.currentTimeMillis();
			return;
		}
		
		double elapsedTime = (((double)System.currentTimeMillis() - lastUpdate))  / 1000;
		
		//calculate heading towards the target
		double x = wayPoint[0] - position[0];
		double y = wayPoint[1] - position[1];
		double d = Math.sqrt(x*x + y*y);
		
		heading = Math.acos(x/d);
		if(y<0)
			heading = - heading;
		
		if(heading == Double.NaN) {
			lastUpdate = System.currentTimeMillis();
			return;
		}
		
		//move the boat a bit at max speed or slower if the boat will overshoot the target before next update
		speed = Math.min(maxSpeed, targetSpeed);
		if(d < dt*speed/1000) {
			speed = maxSpeed/10;
		}
		
		position[0] += speed * elapsedTime * Math.cos(heading) + windSpeed*elapsedTime*Math.cos(windDirection);
		position[1] += speed * elapsedTime * Math.sin(heading) + windSpeed*elapsedTime*Math.cos(windDirection);;
		
		lastUpdate = System.currentTimeMillis();
		
		/*
		x = xPos.get(xPos.size()-1) - position[0];
		y = yPos.get(yPos.size()-1) - position[1];*/
		d = Math.sqrt(x*x + y*y);
		/*
		if(d > 0.5) {
			xPos.add(position[0]);
			yPos.add(position[1]);
		}*/
		
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
			
			//System.out.println(map.getDepth(position[0], position[1]));
			
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
