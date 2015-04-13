package algorithms;

import java.util.ArrayList;

import kex2015.Boat;

public class LawnMoverPattern extends Kex {
	
	boolean stop = false;
	
	double passes = 10;
	double dist;
	
	double maxY = Double.MIN_VALUE;;
	double minY = Double.MAX_VALUE;;
	
	double targetX;
	double targetY;
	
	boolean rightSide = true;
	boolean travelToNextLine = true;
	

	public LawnMoverPattern(Boat inBoat, ArrayList<Double> x,
			ArrayList<Double> y, double delta, int[] endPos, long dt) {
		super(inBoat, x, y, delta, endPos, dt);
		// calculate start point = top left corner
	
		int index = 0;
		for(int i=0;i<y.size();i++) {
			if(y.get(i) < minY) {
				minY = y.get(i);
				index = i;
			}
		}
		
		for(int i=0;i<y.size();i++) {
			if(y.get(i) > maxY) {
				maxY = y.get(i);
			}
		}
		
		targetY = minY;
		targetX = x.get(index);
		
		dist = (maxY-minY)/passes;
	}
	
	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}catch(Exception e) {}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		stop = true;
	}
	
	private double findX(double y, boolean right) {
		int[] l1 = {-1,-1};
		int[] l2 = {-1,-1};
		
		int l = super.polygonY.size();
		for(int i=0; i < l; i++) {
			
			if(((polygonY.get((i+1)%l) > targetY) && (polygonY.get(i) < targetY))
			|| ((polygonY.get((i+1)%l) < targetY) && (polygonY.get(i) > targetY))) {
				//interpolate
				if(l1[0] == -1) {
					l1[0] = (i+1)%l;
					l1[1] = i%l;
					System.out.println("L1 set" + "\t (" + polygonX.get(l1[0]) + " , " + polygonY.get(l1[0]) + ") -> ("
														 + polygonX.get(l1[1]) + " , " + polygonY.get(l1[1]) + ")");
				}else {
					l2[0] = (i+1)%l;
					l2[1] = i%l;
					System.out.println("L2 set" + "\t (" + polygonX.get(l2[0]) + " , " + polygonY.get(l2[0]) + ") -> ("
							 + polygonX.get(l2[1]) + " , " + polygonY.get(l2[1]) + ")");
				}
			}
		}
		
		/*error return some default value*/
		if(l1[0] == -1 || l1[1] == -1 || l2[0] == -1 || l2[1] == 0) {
			double meanX = 0;
			for(int i=0;i<polygonX.size();i++) {
				meanX += polygonX.get(i);
			}
			meanX /= polygonX.size();
			return meanX;
		}
			
		
		//interpolate x
		
		double x0 = polygonX.get(l1[0]);
		double y0 = polygonY.get(l1[0]);
		
		double x1 = polygonX.get(l1[1]);
		double y1 = polygonY.get(l1[1]);
		
		//how far is y on the line
		double p = (targetY-y0) / (y1-y0);
		double l1X = (1-p)*x0 + p*x1;
		
		System.out.println("p1=" + p);
		
		x0 = polygonX.get(l2[0]);
		y0 = polygonY.get(l2[0]);
		
		x1 = polygonX.get(l2[1]);
		y1 = polygonY.get(l2[1]);
		
		//how far is y on the line
		p = (targetY-y0) / (y1-y0);
		double l2X = (1-p)*x0 + p*x1;
		System.out.println("p2=" + p);
		
		if(right) {
			return Math.max(l1X, l2X);
		}
		return Math.min(l1X, l2X);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		//boat.setTargetSpeed(2);
		boat.setWayPoint(targetX, targetY);
		
		while (!stop) {
			double data[] = boat.getSensordata();
			
			double xDist = data[0]-targetX;
			double yDist = data[1]-targetY;
			
			//find next target
			if(Math.sqrt(xDist*xDist + yDist*yDist) < 3 ) {// || data[4] > -0.5) {
				
				//set y
				//targetY = targetY+dist;
				if (targetY < minY || targetY > maxY) {
					dist*=-1;
					targetY = targetY + dist;
				}
				
				if(rightSide && travelToNextLine) {
					targetY +=dist;
					targetX = findX(targetY,true);
					rightSide = true;
					travelToNextLine = false;
				}
				else if(rightSide && !travelToNextLine) {
					targetX = findX(targetY,false);
					rightSide = false;
					travelToNextLine = true;
				}
				else if(!rightSide && travelToNextLine) {
					targetY+=dist;
					targetX = findX(targetY,false);
					rightSide = false;
					travelToNextLine = false;
				}
				else if(!rightSide && !travelToNextLine) {
					targetX = findX(targetY,true);
					rightSide = true;
					travelToNextLine = true;
				}
				else {
					System.out.println("FEL!");
				}
				
				boat.setWayPoint(targetX, targetY);
				
				System.out.println("target set: " + targetX + "\t" + targetY);
				
			}
			sleep(dt);
		}

	}

}
