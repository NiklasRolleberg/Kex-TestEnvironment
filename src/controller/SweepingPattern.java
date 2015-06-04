package controller;

import kex2015.NpBoat;

public class SweepingPattern extends SearchPattern {

	boolean stop = false;
	boolean followingLand = false;
	
	public SweepingPattern(Kex kex, SearchCell subregion, double delta, long dt) {
		super(kex, subregion, delta*0.8, dt);
	}

	
	@Override
	public void run() {
		
		
		boolean goToRight = false;//(Math.random() < 0.5); //traveling from left side to right
		boolean goToNextLine = true;
		boolean skipRest = false; //true -> the boat has to find a new waypoint
		
		double targetY = data.getPosY();
		double targetX = region.findX(targetY, !goToRight);
	
		xte.setWaypoint(data.getPosX(), data.getPosY(), targetX, targetY);
		double dx = targetX-data.getPosX();
		double dy = targetY-data.getPosY();
			
		double targetLine = targetY;
		
		//start sweeping
		while(!stop) {
			
			//calculate distance to other boats
			
			
			
			for(NpBoat other: kex.otherBoats) {
				double ox = other.posX;
				double oy = other.posY;
				
				double distX = ox - data.getPosX();
				double distY = oy - data.getPosY();
				double distance = Math.sqrt(distX*distX + distY*distY);
				
				//System.out.println("Distance to other boat: " + distance);
				
				//calculate if the boat is about to hit another boat, ignore boats far away
				if(distance < 30) {
					System.out.println("Another boat is close!, calculating non-evasive maneuver");
					//calculate speed to hit the boat
					
					// equation from http://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
					
					//Points
					/*
					// L1
					double x1 = other.posX;
					double y1 = other.posY;
					double x2 = other.posX + 100 * Math.cos(other.heading);
					double y2 = other.posY + 100 * Math.sin(other.heading);;
					
					double x3 = data.getPosX();
					double y3 = data.getPosY();
					double x4 = data.getPosX() + 100 * Math.cos(data.getHeading());
					double y4 = data.getPosY() + 100 * Math.sin(data.getHeading()); 
					
					//intersectionPoint
					double Px = ( (x1*y2 - y1*x2)*(x3-x4) - (x1-x2)*(x3*y4 - y3*x4) ) /
									( (x1-x2)*(y3-y4) - ()    );
					
					*/
					
					
				}
			}
			
			
			
			
			dx = targetX-data.getPosX();//data[0];
			dy = targetY-data.getPosY();//data[1];
			//kex.setSpeed(Math.max(-0.1-data.getDepth()*3,3));
			
			if(data.getDepth() < 3)
				kex.setSpeed(30);
			if(data.getDepth() < 1)
				kex.setSpeed(Math.max(-0.1-data.getDepth()*3,3));
			
			
			//target reached -> choose new target
			if(Math.sqrt(dx*dx + dy*dy) < 3 || skipRest) {
				
				double lastTargetX = targetX;
				double lastTargetY = targetY;
				
				//System.out.println("Waypoint reached");
				
				if(goToNextLine) {
					//System.out.println("GO TO NEXT LINE");
					targetLine +=delta;
					targetY = targetLine;
					targetX = region.findX(targetY, !goToRight);
					goToNextLine = false;
				}
				else if(goToRight && !goToNextLine) {
					//System.out.println("GO TO RIGHT");
					targetY = targetLine;
					targetX = region.findX(targetY,true);
					goToRight = false;
					goToNextLine = true;
				}
				else if(!goToRight && !goToNextLine) {
					//System.out.println("GO TO LEFT");
					targetY = targetLine;
					targetX = region.findX(targetY,false);
					goToRight = true;
					goToNextLine = true;
				}
				
				if(targetY > region.maxY() || targetY < region.minY()) {
					System.out.println("Scanning completed, min/max y reached");
					this.stop();
					kex.setSpeed(0);
				}
				
				//System.out.println("TARGET: " +targetX + "  " + targetY);
				xte.setWaypoint(lastTargetX, lastTargetY, targetX, targetY);
				
				if(skipRest) {
					skipRest = false;
					sleep(dt);
				}
			}
			
			//close to land
			if(data.getDepth() > -0.5 && !stop) {

				//System.out.println("Close to land " + data.getDepth());
				//System.out.println("Coordinates: (" + data.getPosX() + "),(" + data.getPosY() + ")");
				
				double lastTargetX = data.getPosX();
				double lastTargetY = data.getPosY();
				
				//make the boat face the next line)
				double depth = data.getDepth();
				double sign = depth/Math.abs(depth);
				
				xte.setWaypoint(data.getPosX(), targetLine + delta); 
				kex.setSpeed(0);
				sleep(dt*5);
				kex.setSpeed(2*Math.sqrt(Math.abs(depth)+1) * -sign); // 4*
				followingLand = true;
				if(followLand(targetLine, targetLine + delta)) {
					targetLine +=delta;
					//System.out.println("Lower line reached");
					skipRest = true;
					goToNextLine = false;
					targetY = targetLine;
				}
				else {
					targetY = targetLine;				
					//xte.setWaypoint(targetX, targetY);
					xte.setWaypoint(lastTargetX, lastTargetY, targetX, targetY);
					//System.out.println("Upper line reached");
				}
				followingLand = false;
			}	
			sleep(dt);
		}
		
		System.out.println("SweepingPattern done");
		kex.setSpeed(0);
	}
	
	
	/**
	 * @param line1
	 * line above
	 * 
	 * @param line2
	 * line under
	 * 
	 * @return
	 * true = line under reached
	 * false = line above reached
	 */
	private boolean followLand(double line1, double line2) {
		
		System.out.println("Follow land");
		
		double targetDepth = -1;
		
		//PID controller
		double KP = 0.01; //Proportional gain //0.2
		double KI = 1.0 / 5000; //integral gain // 1/5000
		double KD = 900; //derivative gain //300
		
		long time = System.currentTimeMillis();
		double Integral = 0; 
		double lastError = data.getDepth() - targetDepth;
		
		double maxAngle = Math.PI / 8;
		
		sleep(dt);
		
		
		double mean = (line1 + line2) / 2;
		while(Math.abs(mean - data.getPosY()) < Math.abs(delta*0.55) && !stop) {

			//stop the boat from going outside the polygon
			if(outOfBounds(data.getPosX(), data.getPosY())) {
				if(data.getPosX() < ((region.maxX()-region.minX())/2))
					xte.setWaypoint(region.findX(data.getPosY(),false), data.getPosY());
				else
					xte.setWaypoint(region.findX(data.getPosY(),true), data.getPosY());
					//System.out.println("target out of bounds");
				return true;
			}
			
			
			double timeStep = (System.currentTimeMillis() - time);
			time = System.currentTimeMillis();
			double error = data.getDepth() - targetDepth;
			double derivative = (error - lastError) / timeStep;
			lastError = error;
			Integral += error * timeStep;
			
			//reduce integral value to prevent oscillations
			if(Integral > 0)
				Integral = Math.min(Integral, 200);
			else 
				Integral = Math.max(Integral, -200);
			
			double turnAngle = KP * error + KI*Integral + KD * derivative;
			
			if(turnAngle > 0)
				turnAngle = Math.min(maxAngle,turnAngle);
			else 
				turnAngle = Math.max(-maxAngle,turnAngle);
			
			//for boat with two front sonars
			if(data.getRightSonar() > data.getLeftSonar()) {
				turnAngle *= -1;
			}
		
			xte.setWaypoint(data.getPosX() + Math.cos(data.getHeading() - turnAngle) * 50, data.getPosY() + Math.sin(data.getHeading()- turnAngle) * 50);
			
			sleep(dt);
		}
		//close to upper line
		if(Math.abs(data.getPosY()-line1) < Math.abs(delta/2))
			return false;
		//close to line below
		return true;
	}
	
	@Override
	void stop() {
		System.out.println("Sweeping pattern aborted");
		stop = true;
		data.stop(); //stop data object
		xte.stop();
		kex.setSpeed(0);
	}
	
	private boolean outOfBounds(double x, double y) {
		
		if(y > region.maxY() || y < region.minY() || x > region.maxX() || x < region.minX())
			return true;
		if(x < region.findX(y, false) || x > region.findX(y, true))
			return true;
		return false;
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}catch(Exception e) {}
	}

	@Override
	boolean followingLand() {
		return followingLand;
	}

	@Override
	boolean isDone() {
		return stop;
	}
}
