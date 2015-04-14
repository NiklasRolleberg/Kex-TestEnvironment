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
				//targetX = region.findX(targetY,true);
				rightSide = true;
				travelToNextLine = false;
			}
			else if(rightSide && !travelToNextLine) {
				//targetX = region.findX(targetY,false);
				rightSide = false;
				travelToNextLine = true;
			}
			else if(!rightSide && travelToNextLine) {
				targetY+=delta;
				//targetX = region.findX(targetY,false);
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
	private boolean travelToPoint(double x, double y) {
		
		double[] data;
		
		while(!stop) {
			data = kex.getData();
			double xDist = data[0]-x;
			double yDist = data[1]-y;
			
			//target reached
			if(Math.sqrt(xDist*xDist + yDist*yDist) < 3 ) {// || data[4] > -0.5) {
				return true;
			}
			
			//close to land
			if(data[4] > -0.5) {
				System.out.println("Close to land");
			}
			
			kex.setWaypoint(x, y);
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
