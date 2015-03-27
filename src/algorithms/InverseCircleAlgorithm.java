package algorithms;

import java.util.ArrayList;

import kex2015.Boat;

public class InverseCircleAlgorithm extends Kex {
	
	boolean run = true;

	public InverseCircleAlgorithm(Boat inBoat, ArrayList<Double> x,
			ArrayList<Double> y, double delta, int[] endPos, long dt) {
		super(inBoat, x, y, delta, endPos, dt);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		run = false;
	}

	@Override
	public void run() {
		//set first goal
		int index = 0;
		int varv = 0;
		boat.setWayPoint(polygonX.get(index), polygonY.get(index));
		
		double[] wayPoint = {polygonX.get(index), polygonY.get(index)};
		double[] sensor = boat.getSensordata();
		
		double xDist;
		double yDist;
		
		double xCenter = 0;
		double yCenter = 0;
		for(int i=0;i<polygonX.size();i++) {
			xCenter += polygonX.get(i);
			yCenter += polygonY.get(i);
		}
		xCenter /= polygonX.size();
		yCenter /= polygonX.size();
		
		
		while (run) {
			
			sensor = boat.getSensordata();
			xDist = sensor[0]-wayPoint[0];
			yDist = sensor[1]-wayPoint[1];
			
			if(Math.sqrt(xDist*xDist + yDist*yDist) < 3) {
				
				index++;
				index = index % polygonX.size();
				
				//find distance to center
				double diffX = xCenter - polygonX.get(index);
				double diffY = yCenter - polygonY.get(index);
			
				//normalize
				double l = Math.sqrt(diffX*diffX + diffY*diffY);
				diffX /=l;
				diffY /=l;
				
				//move waypoint
				wayPoint[0] = polygonX.get(index) + (diffX * varv*10);
				wayPoint[1] = polygonY.get(index) + (diffY * varv*10);
				
				boat.setWayPoint(wayPoint[0], wayPoint[1]);
				
				if(index == 0)
					varv ++;
			}
			
			try {
				Thread.sleep(100);
			} catch(Exception e) {};
			
		}

	}

}
