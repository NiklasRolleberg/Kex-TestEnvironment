package controller;

import controller.Kex.SearchCell;

public class CircularPattern extends SearchPattern {
	
	boolean stop = false;

	public CircularPattern(Kex kex, SearchCell region, double delta, long dt) {
		super(kex, region, delta, dt);
	}

	@Override
	public void run() {
		double centerX = data.getPosX();
		double centerY = data.getPosY();
		
		double radius = delta;
		double angle = 0;
		
		double points = 50;
		double step = 2 * Math.PI / points;
		
		double targetX = centerX + radius * Math.cos(angle);
		double targetY = centerY + radius * Math.sin(angle);
		
		xte.setWaypoint(targetX, targetY);
		
		while(!stop) {
			
			double dx = targetX-data.getPosX();
			double dy = targetY-data.getPosY();
			
			if(Math.sqrt(dx*dx + dy*dy) < 3) {
				
				angle += step;
				
				if(angle > (2*Math.PI)) {
					angle -= (2 * Math.PI);
					radius += delta;
				}
				
				targetX = centerX + radius * Math.cos(angle);
				targetY = centerY + radius * Math.sin(angle);
				
				//target point is outside polygon -> calculate new point
				if(outOfBounds(targetX,targetY)) {
					System.out.println("Waypoint out of bounds");
					
					kex.setSpeed(1);
					
					double tempAngle = angle;
					double tempX = centerX + radius * Math.cos(tempAngle);
					double tempY = centerY + radius * Math.sin(tempAngle);
					
					while (outOfBounds(tempX,tempY)) {
						
						if(Math.abs(tempAngle - angle) > 2*Math.PI) {
							System.out.println("area is cleared");
							stop = true;
							kex.setSpeed(30);
							break;
						}
						
						tempAngle += Math.PI / 32;
						tempX = centerX + radius * Math.cos(tempAngle);
						tempY = centerY + radius * Math.sin(tempAngle);
					}
					angle = tempAngle;
					targetX = tempX;
					targetY = tempY;
				}
				
				xte.setWaypoint(targetX,targetY);
				kex.setSpeed(30);
			}
			
			
			
			if(data.getDepth() > -0.5) {
				
				double x1 = data.getPosX() - centerX;
				double y1 = data.getPosY() - centerY;
				
				//aim for center
				xte.setWaypoint(centerX,centerY);
				sleep(dt/3);
				kex.setSpeed(5);
				followLand(centerX,centerY,radius);
				kex.setSpeed(30);
				
				double x2 = data.getPosX() - centerX;
				double y2 = data.getPosY() - centerY;
				
				double diffAngle =  Math.acos((x1*x2 + y1*y2) / (radius*radius));
				if(!Double.isNaN(diffAngle))
					angle += diffAngle;
				
				
				while (angle - 2*Math.PI > 0) {
					angle -= 2 * Math.PI;
					radius += delta;
				}
				
				
				targetX = centerX + radius * Math.cos(angle);
				targetY = centerY + radius * Math.sin(angle);
				
				//target point is outside polygon -> calculate new point
				if(outOfBounds(targetX,targetY)) {
					System.out.println("Waypoint out of bounds");
					
					kex.setSpeed(1);
					
					double tempAngle = angle;
					double tempX = centerX + radius * Math.cos(tempAngle);
					double tempY = centerY + radius * Math.sin(tempAngle);
					
					while (outOfBounds(tempX,tempY)) {
						
						if(Math.abs(tempAngle - angle) > 2*Math.PI) {
							System.out.println("area is cleared");
							stop = true;
							break;
						}
						tempAngle += Math.PI / 32;
						tempX = centerX + radius * Math.cos(tempAngle);
						tempY = centerY + radius * Math.sin(tempAngle);
					}
					angle = tempAngle;
					targetX = tempX;
					targetY = tempY;
				}	
				xte.setWaypoint(targetX, targetY);
			}
			sleep(dt);
		}
		//region is done
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
	private void followLand(double centerX, double centerY, double radius) {
		
		System.out.println("Follow land");

		double targetDepth = -1;		
		
		//PID controller
		double KP = 0.4; //Proportional gain
		double KI = 1.0 / 5000; //integral gain
		double KD = 300; //derivative gain
		
		long time = System.currentTimeMillis();
		double Integral = 0; 
		double lastError = data.getDepth() - targetDepth;
		double maxAngle = Math.PI / 8;
		
		sleep(dt);

		double rx = centerX-data.getPosX();
		double ry = centerY-data.getPosY();
		
		
		while(Math.sqrt(rx*rx + ry*ry) <= radius) {
			
			rx = centerX-data.getPosX();
			ry = centerY-data.getPosY();
			
			//stop the boat from going outside the polygon
			if(outOfBounds(data.getPosX(), data.getPosY())) {
				if(data.getPosX() < ((region.maxX()-region.minX())/2))
					xte.setWaypoint(region.findX(data.getPosY(),false), data.getPosY());
				else
					xte.setWaypoint(region.findX(data.getPosY(),true), data.getPosY());
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
			
			//System.out.println("TurnAnlge: " + turnAngle);
			//System.out.println("derivative: " + derivative);
			//System.out.println("Integral " + Integral);
			xte.setWaypoint(data.getPosX() + Math.cos(data.getHeading() - turnAngle) * 50, data.getPosY() + Math.sin(data.getHeading() - turnAngle) * 50);
		
			sleep(dt);
		}
	}

	private boolean outOfBounds(double x, double y) {
		if(y > region.yMax || y < region.yMin || x > region.xMax || x < region.xMin)
			return true;
		if(x < region.findX(y, false) || x > region.findX(y, true))
			return true;
		return false;
	}

	@Override
	void stop() {
		stop = true;
		data.stop();
		xte.stop();
	}
	
	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}catch(Exception e) {}
	}

}
