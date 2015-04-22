package controller;

public class SweepingPattern extends SearchPattern {

	boolean stop = false;
	
	public SweepingPattern(Kex kex, Kex.SearchCell subregion, double delta, long dt) {
		super(kex, subregion, delta, dt);
	}

	@Override
	public void run() {
		
		double targetY = kex.getData()[1];//region.minY()+4;
		double targetX = region.findX(targetY, true);
		
		System.out.println("Targets:" + targetX +"  "+targetY);
		
		//System.out.println("Going to target and ignoring land");
		kex.setWaypoint(targetX, targetY);
		double[] data = kex.getData();
		double dx = targetX-data[0];
		double dy = targetY-data[1];
		/*
		while(Math.sqrt(dx*dx + dy*dy) > 3 ) {
			data = kex.getData();
			dx = targetX-data[0];
			dy = targetY-data[1];
			sleep(dt);
		}
		*/
		
		boolean goToRight = false; //traveling from left side to right
		boolean goToNextLine = true;
		boolean skipRest = false; //true -> the boat has to find a new waypoint
		
		double targetLine = targetY;//+delta * 2; //*4; //TODO ta bort +delta...
		
		//Arrived at top left corner ->start sweeping
		while(!stop) {			
			data = kex.getData();
			dx = targetX-data[0];
			dy = targetY-data[1];
			kex.setSpeed(Math.max(-data[4]*3,3));
			
			//target reached -> choose new target
			if(Math.sqrt(dx*dx + dy*dy) < 3 || skipRest) {
				
				System.out.println("Waypoint reached");
				
				if(goToNextLine) {
					System.out.println("GO TO NEXT LINE");
					targetLine +=delta;
					targetY = targetLine;
					targetX = region.findX(targetY, !goToRight);
					goToNextLine = false;
					//goToRight = (goToRight==false);
				}
				else if(goToRight && !goToNextLine) {
					System.out.println("GO TO RIGHT");
					targetY = targetLine;
					targetX = region.findX(targetY,true);
					goToRight = false;
					goToNextLine = true;
				}
				else if(!goToRight && !goToNextLine) {
					System.out.println("GO TO LEFT");
					targetY = targetLine;
					targetX = region.findX(targetY,false);
					goToRight = true;
					goToNextLine = true;
				}
				
				if(targetY > region.maxY() || targetY < region.minY()) {
					delta *=-1;
					targetLine += delta;
					targetY = targetLine;
					goToNextLine = false;
					goToRight = (goToRight == false);
					targetX = region.findX(targetY, goToRight);
				}
				
				kex.setWaypoint(targetX, targetY);
				
				if(skipRest) {
					skipRest = false;
					sleep(3000);
				}
			}
			
			//close to land
			if(data[4] > -0.5) {

				System.out.println("Close to land " + data[4]);
				System.out.println("Coordinates: (" + data[0] + "),(" + data[1] + ")");
				
				//make the boat face the next line
				kex.setSpeed(5);
				kex.setWaypoint(data[0], targetLine + delta); 
				sleep(dt/3);
				
				if(followLand(targetLine, targetLine + delta)) {
					targetLine +=delta;
					System.out.println("Lower line reached");
					skipRest = true;
					//goToRight = (goToRight == false);
					goToNextLine = false;
				}
				else {
					targetY = targetLine;
					kex.setWaypoint(targetX, targetY);
					System.out.println("Upper line reached");
					//sleep(3000);
				}
				kex.setSpeed(30);
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

		double[] data = kex.getData();
		double targetDepth = -1;//data[4];

		//PID controller
		
		double KP = 0.4; //Proportional gain
		double KI = 1.0 / 5000; //integral gain
		double KD = 300; //derivative gain
		
		long time = System.currentTimeMillis();
		double Integral = 0; 
		double lastError = data[4] - targetDepth;
		
		double maxAngle = Math.PI / 8;
		
		sleep(dt);
		
		while( ((line1 < line2) && (data[1] > line1-10) && (data[1] < line2)) ||
				((line1 > line2) && (data[1] > line2) && (data[1] < line1+10)) )  {
		
			data = kex.getData();
			
			//stop the boat from going outside the polygon
			if(outOfBounds(data[0], data[1])) {
				if(data[0] < ((region.maxX()-region.minX())/2))
					kex.setWaypoint(region.findX(data[1],false), data[1]);
				else
					kex.setWaypoint(region.findX(data[1],true), data[1]);
				return true;
			}
			
			
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
		//close to first line
		if(Math.abs(data[1]-line1) < Math.abs(delta/2))
			return false;
		//close to line below
		return true;
	}
	
	@Override
	void stop() {
		stop = true;
	}
	
	private boolean outOfBounds(double x, double y) {
		
		if(y > region.yMax || y < region.yMin || x > region.xMax || x < region.xMin)
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
}
