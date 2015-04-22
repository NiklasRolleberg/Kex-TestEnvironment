package controller;

import controller.Kex.SearchCell;

public class CircularPattern extends SearchPattern {
	
	boolean stop = false;

	public CircularPattern(Kex kex, SearchCell region, double delta, long dt) {
		super(kex, region, delta, dt);
	}

	@Override
	public void run() {

		double[] data = kex.getData(); 
		
		double centerX = data[0];
		double centerY = data[1];
		
		double radius = delta;
		double angle = 0;
		
		double points = 50;
		double step = 2 * Math.PI / points;
		
		double targetX = centerX + radius * Math.cos(angle);
		double targetY = centerY + radius * Math.sin(angle);
		
		kex.setWaypoint(targetX, targetY);
		
		while(!stop) {
			
			data = kex.getData();		
			
			double dx = targetX-data[0];
			double dy = targetY-data[1];
			
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
				
				kex.setWaypoint(targetX,targetY);
				kex.setSpeed(30);
			}
			
			
			
			if(data[4] > -0.5) {
				
				double x1 = data[0] - centerX;
				double y1 = data[1] - centerY;
				
				//aim for center
				kex.setWaypoint(centerX,centerY);
				sleep(dt/3);
				kex.setSpeed(5);
				followLand(centerX,centerY,radius);
				kex.setSpeed(30);
				data = kex.getData();
				
				double x2 = data[0] - centerX;
				double y2 = data[1] - centerY;
				
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
				
				kex.setWaypoint(targetX, targetY);
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

		double[] data = kex.getData();
		double targetDepth = -1;//data[4];
		//kex.setSpeed(5);
		
		
		//PID controller
		
		double KP = 0.4; //Proportional gain
		double KI = 1.0 / 5000; //integral gain
		double KD = 300; //derivative gain
		
		long time = System.currentTimeMillis();
		double Integral = 0; 
		double lastError = data[4] - targetDepth;
		
		double maxAngle = Math.PI / 8;
		
		sleep(dt);

		double rx = centerX-data[0];
		double ry = centerY-data[1];
		
		
		while(Math.sqrt(rx*rx + ry*ry) <= radius) {
			data = kex.getData();
			
			rx = centerX-data[0];
			ry = centerY-data[1];
			
			
			//stop the boat from going outside the polygon
			if(outOfBounds(data[0], data[1])) {
				if(data[0] < ((region.maxX()-region.minX())/2))
					kex.setWaypoint(region.findX(data[1],false), data[1]);
				else
					kex.setWaypoint(region.findX(data[1],true), data[1]);
				
			}
			
			//System.out.println("Math.sqrt(rx*rx + ry*ry) = " + Math.sqrt(rx*rx + ry*ry));
			
			double timeStep = (System.currentTimeMillis() - time);
			time = System.currentTimeMillis();
			double error = data[4] - targetDepth;
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
			if(data[5] > data[6]) {
				turnAngle *= -1;
			}
			
			//System.out.println("TurnAnlge: " + turnAngle);
			//System.out.println("derivative: " + derivative);
			//System.out.println("Integral " + Integral);
			kex.setWaypoint(data[0] + Math.cos(data[2] - turnAngle) * 50, data[1] + Math.sin(data[2] - turnAngle) * 50);
		
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
	}
	
	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}catch(Exception e) {}
	}

}
