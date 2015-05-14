package controller;

public class SweepingPattern extends SearchPattern {

	boolean stop = false;
	boolean followingLand = false;
	
	public SweepingPattern(Kex kex, SearchCell subregion, double delta, long dt) {
		super(kex, subregion, delta*0.8, dt);
	}

	
	@Override
	public void run() {
		
		double targetY = data.getPosY();
		double targetX = region.findX(targetY, true);
	
		//kex.setWaypoint(targetX, targetY);
		xte.setWaypoint(data.getPosX(), data.getPosY(), targetX, targetY);
		double dx = targetX-data.getPosX();
		double dy = targetY-data.getPosY();
		
		boolean goToRight = false; //traveling from left side to right
		boolean goToNextLine = true;
		boolean skipRest = false; //true -> the boat has to find a new waypoint
		double targetLine = targetY;
		
		//start sweeping
		while(!stop) {			
			dx = targetX-data.getPosX();//data[0];
			dy = targetY-data.getPosY();//data[1];
			kex.setSpeed(Math.max(-data.getDepth()*3,3));
			
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
					this.stop();
					kex.setSpeed(0);
				}
				
				xte.setWaypoint(lastTargetX, lastTargetY, targetX, targetY);
				
				if(skipRest) {
					skipRest = false;
					sleep(dt);
				}
			}
			
			//close to land
			if(data.getDepth() > -0.5) {

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
				kex.setSpeed(4*Math.sqrt(Math.abs(depth)+1) * -sign);
				followingLand = true;
				if(followLand(targetLine, targetLine + delta)) {
					targetLine +=delta;
					System.out.println("Lower line reached");
					skipRest = true;
					goToNextLine = false;
					targetY = targetLine;
				}
				else {
					targetY = targetLine;				
					//xte.setWaypoint(targetX, targetY);
					xte.setWaypoint(lastTargetX, lastTargetY, targetX, targetY);
					System.out.println("Upper line reached");
				}
				followingLand = false;
			}	
			sleep(dt);
		}
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
		double KP = 0.2; //Proportional gain
		double KI = 1.0 / 5000; //integral gain
		double KD = 300; //derivative gain
		
		long time = System.currentTimeMillis();
		double Integral = 0; 
		double lastError = data.getDepth() - targetDepth;
		
		double maxAngle = Math.PI / 8;
		
		sleep(dt);
		
		
		double mean = (line1 + line2) / 2;
		while(Math.abs(mean - data.getPosY()) < Math.abs(delta*0.55)) {

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
		stop = true;
		data.stop(); //stop data object
		xte.stop();
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
