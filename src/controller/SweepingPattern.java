package controller;

public class SweepingPattern extends SearchPattern {

	boolean stop = false;
	
	public SweepingPattern(Kex kex, Kex.SearchCell subregion, double delta, long dt) {
		super(kex, subregion, delta, dt);
	}

	@Override
	public void run() {
		
		
		double targetY = kex.getData()[1];//region.minY();
		double targetX = region.findX(targetY, false);
		System.out.println("Targets:" + targetX +"  "+targetY);
		
		boolean rightSide = true;
		boolean travelToNextLine = false;
		
		travelToPoint(targetX,targetY);
		
		while(!stop) {
			
			if(rightSide && travelToNextLine) {
				targetY +=delta;
				targetX = region.findX(targetY,true);
				rightSide = true;
				travelToNextLine = false;
			}
			else if(rightSide && !travelToNextLine) {
				targetX = region.findX(targetY,false);
				rightSide = false;
				travelToNextLine = true;
			}
			else if(!rightSide && travelToNextLine) {
				targetY+=delta;
				targetX = region.findX(targetY,false);
				rightSide = false;
				travelToNextLine = false;
			}
			else if(!rightSide && !travelToNextLine) {
				targetX = region.findX(targetY,true);
				rightSide = true;
				travelToNextLine = true;
			}
			else {
				System.out.println("FEL!");
			}
			
			
			if (targetY < region.minY() || targetY > region.maxY()) {
				delta*=-1;
				targetY = targetY + 3*(delta/2);
			}
			
			travelToPoint(targetX,targetY);
		}
	}
	
	/**Gives the boat coordinates to travel to, this method should handle contour following*/
	private boolean travelToPoint(double x, double y) { //xDir == true -> crossing the region from left to right
		
		double[] data;
		
		while(!stop) {
			data = kex.getData();
			double xDist = data[0]-x;
			double yDist = data[1]-y;
			//kex.setSpeed(100);
			
			//target reached
			if(Math.sqrt(xDist*xDist + yDist*yDist) < 3 ) {// || data[4] > -0.5) {
				return true;
			}
			
			//close to land
			if(data[4] > -0.5) {
				double turnAngle = Math.PI/8;
				double heading = kex.getData()[2];
				//kex.setSpeed(3);
				//turn left
				if(data[5] > data[6]) {
					System.out.println("Left");
					kex.setWaypoint(data[0] + Math.cos(heading + turnAngle) * 50, data[1] + Math.sin(heading + turnAngle) * 50);
				}
				//turn right
				else if(data[5] < data[6]){
					System.out.println("Right");
					kex.setWaypoint(data[0] + Math.cos(heading - turnAngle) * 50, data[1] + Math.sin(heading - turnAngle) * 50);
				}
				//choose random direction
				else {
					System.out.println("random");
					double r  = 0.5*(turnAngle*(Math.random()-0.5) );
					kex.setWaypoint(data[0] + Math.cos(heading + r) * 50, data[1] + Math.sin(heading + r) * 50);
				}
				//if(Math.abs(yDist) > delta*1.1)
					//return false;
			}
			else {
				//double k = 0.2;
				
				//double kX = data[0] - xDist*k;
				//double kY = y;//data[1] - yDist*k;
				//kex.setWaypoint(kX, kY);
				
				kex.setWaypoint(x, y);
			}
			sleep(dt);
		}
		//fail
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
