package controller;

import java.util.ArrayList;

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
			
			ArrayList<NpBoat> boatsToRemove = new ArrayList<NpBoat>();
			
			//time
			double t = Double.MAX_VALUE;
			//intersection point
			double[] point = new double[2];
			boolean set = false;
			
			
			for(NpBoat other: kex.otherBoats) {
				double ox = other.posX;
				double oy = other.posY;
				
				double distX = ox - data.getPosX();
				double distY = oy - data.getPosY();
				double distance = Math.sqrt(distX*distX + distY*distY);
				
				//System.out.println("Distance to other boat: " + distance);
				
				//calculate if the boat is about to hit another boat, ignore boats far away
				if(distance < 3) {
					boatsToRemove.add(other);
				}
				else if(distance < 100) {
					//System.out.println("Another boat is close!, calculating non-evasive maneuver");
					//calculate speed to hit the boat
					
					// equation from http://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
					
					//Points
					
					// L1
					double x1 = other.posX;
					double y1 = other.posY;
					double x2 = other.posX + 2 * Math.cos(other.heading);
					double y2 = other.posY + 2 * Math.sin(other.heading);
					
					double x3 = data.getPosX();
					double y3 = data.getPosY();
					double x4 = data.getPosX() + 2 * Math.cos(data.getHeading());
					double y4 = data.getPosY() + 2 * Math.sin(data.getHeading()); 
					
					//intersectionPoint
					double Px = ( (x1*y2 - y1*x2)*(x3-x4) - (x1-x2)*(x3*y4 - y3*x4) ) /
										 ((x1-x2)*(y3-y4) - (y1-y2)*(x3-x4));
					
					double Py = ( (x1*y2 - y1*x2)*(y3-y4) - (y1-y2)*(x3*y4 - y3*x4) ) /
							 ((x1-x2)*(y3-y4) - (y1-y2)*(x3-x4));
					
					
					//calculate time for other boat to reach that point
					double d1 = Math.sqrt((x1-Px)*(x1-Px) + (y1-Py)*(y1-Py));
					double tTemp = d1/other.speed;
					
					//check if boat is travelling to or from intersectionpoint
					double tempdx1 = x3 - Px;
					double tempdy1 = y3 - Py;
					double tempdx2 = x4 - Px;
					double tempdy2 = y4 - Py;
					
					double xt1 = data.getPosX() - Px;
					double yt1 = data.getPosY() - Py;
					
					double xt2 = data.getPosX() + Math.cos(data.getHeading()) - Px;
					double yt2 = data.getPosY() + Math.sin(data.getHeading()) - Py;
					
					if((Math.sqrt((tempdx1 * tempdx1) + (tempdy1 * tempdy1)) < Math.sqrt((tempdx2 * tempdx2) + (tempdy2 * tempdy2))) || ((Math.sqrt(xt1*xt1 + yt1*yt1) < Math.sqrt(xt2*xt2 + yt2*yt2))))
						continue;
					
					//handle the collision closest in time first
					if(tTemp < t) {
						t = tTemp;
						set = true;
						point[0] = Px;
						point[1] = Py;
					}
				}
			}
			
			
			if(set) {
				//Calculate distance to intersection point
				double d2 = Math.sqrt((data.getPosX()-point[0])*(data.getPosX()-point[0]) + (data.getPosY()-point[1])*(data.getPosY()-point[1]));
				//t  =time until other boat reaches intersection point
				
				if(d2 != 0)  {
					/*
					double margin = 10; //margin from intersection point
					
					//case 1 break
					double targetDist = d2 - margin;
					double speed1 = targetDist / t;
					
					
					//case 2 speed up
					targetDist = d2 + margin;
					double speed2 = targetDist / t;
					
					//best case, both works
					if(speed1 >= 0 && speed2 <= 30) 
						kex.setSpeed(speed2);
					else if(speed1 >= 0 && speed2 > 30)
						kex.setSpeed(speed1);
					else if (speed1 < 0 && speed2 <= 30)
						kex.setSpeed(speed2);
					else {
						// worst case none works
						System.out.println("Cant avoid crash");
						kex.setSpeed(-2);
					}
					*/
					
					
					double speed = 30;//Math.max(data.getSpeed(),1);
					
					//calculate how far the boat will travel until the other boat reached the intersection point
					
					double dist = speed * t;
					double sign = 1;
					while((Math.abs(dist - d2) < 30) && speed > 0) {
						
						if(speed > 30) {
							sign*=-1;
							speed = 30;
						}
						
						speed +=sign;
						dist = speed*t;
					}
					System.out.println("Adapting speed, new speed: " + speed);
					kex.setSpeed(speed);
					
					
					/*
					//calculate highest speed possible wile still avoiding collision
					double speed = 30;
					double t1 = d2 / speed;			
					
					
					
					
					if(Math.abs(t1 - t) < 2) {
						//Adapt speed
						int i = 0;
						while(Math.abs(t1 - 2) < 1 && i < 60) {
							speed -= 1;
							t1 = d2 / speed;
							i++;
						}
						
						System.out.println("Adapting speed, new speed: " + speed);
						kex.setSpeed(speed);
					}*/
				}
			}
			else if(Math.abs(data.getDepth()) < 3) {
				
				kex.setSpeed(Math.min(30, data.getSpeed()+1));
				if(Math.abs(data.getDepth()) < 1)
					kex.setSpeed(Math.max(-0.1-data.getDepth()*3,3));
			}
			
			//remove boats
			for(int i=0;i<boatsToRemove.size();i++) {
				kex.otherBoats.remove(boatsToRemove.get(i));
				System.out.println("Boat removed");
			}
			
			dx = targetX-data.getPosX();//data[0];
			dy = targetY-data.getPosY();//data[1];
			//kex.setSpeed(Math.max(-0.1-data.getDepth()*3,3));
			
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
