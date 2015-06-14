package controller;

import java.util.ArrayList;
import java.util.LinkedList;

import kex2015.NpBoat;

public class MultiBeamSweepingPattern extends SearchPattern {
	
	SearchElement[][] matrix;
	boolean stop = false;
	double sonarRatio = 4;
	
	public MultiBeamSweepingPattern(Kex kex, SearchCell region, SearchElement[][] elementMatrix, double delta, long dt) {
		super(kex, region, delta, dt);
		matrix = elementMatrix;
	}
	
	private int[] getIndex(double x, double y) {
		
		int ix = (int)Math.round((x - kex.xMin) / kex.dx);
        int iy = (int)Math.round((y - kex.yMin) / kex.dy);

        //index out of bounds fix
        if (ix >= kex.nx){
            ix = kex.nx-1;
        }
        if (iy >= kex.ny){
            iy = kex.ny-1;
        }
        if(ix<0)
        	ix = 0;
        
        if(iy<0)
        	iy = 0;

		return new int[] {ix,iy};
	}

	@Override
	public void run() {
		
		boolean findNew = false;
		
		//find current element from position
		int[] iixy = getIndex(data.getPosX(), data.getPosY());
		
		int ix = iixy[0];
		int iy = iixy[1];
		
		int[] last; 
        int[] next = calculateNextWaypoint(ix,iy,-1,-1);
        last = next;
        if(next == null) {
        	this.stop();
        	return;
        }
        double targetX = matrix[next[0]][next[1]].xCoord;
        double targetY = matrix[next[0]][next[1]].yCoord;
        
        xte.setWaypoint(targetX, targetY);
        
        // TODO chose element to go towards
		// TODO go there
        //kex.setSpeed(15);
        
        
		while(!stop) {
			
			// TODO land?
			if(data.getDepth() > -0.5) {
				System.out.println("LAND!");
				followLand(30);
				findNew = true;
				continue;
			}
			
			double depth = Math.abs(data.getDepth());
			
			if(depth < 2)
				kex.setSpeed(3);
			else if(depth < 3)
				kex.setSpeed(5);
			else if(depth < 8)
				kex.setSpeed(10);
			else 
				kex.setSpeed(20);
			
			
			
			//kex.setSpeed(Math.max(-data.getDepth()*3, 5));
			
			// TODO target reached?
			double dx = targetX - data.getPosX();
			double dy = targetY - data.getPosY();
			
			if(Math.sqrt(dx*dx + dy*dy) < 3 || findNew) {
				//target reached choosing new target
				int[] current;
				
				//change status of scanned elements
				if(!findNew) {
					if(matrix[next[0]][next[1]].status == 0)
						kex.visitedCells ++;
					
					matrix[next[0]][next[1]].status = 1;
					//matrix[next[0]][next[1]].updateDepthData(data.getDepth());
					current  = next;
				} else {
					//last = next;
					current = getIndex(data.getPosX(), data.getPosY());
				}
				findNew = false;
				
				if(matrix[current[0]][current[1]].timesVisited > 3*delta) {
					this.stop();
					break;
				}
				
				next = calculateNextWaypoint(current[0],current[1],last[0],last[1]);
				last = current;
				if(next == null) {
					this.stop();
					break;
				}
				
		        targetX = matrix[next[0]][next[1]].xCoord;
		        targetY = matrix[next[0]][next[1]].yCoord;
		        
		        xte.setWaypoint(targetX, targetY);

			}
			
			// TODO mark scanned elements (based on depth)
			
			//update depth data for current cell
			double xpos = data.getPosX();
			double ypos = data.getPosY(); 
			
			int[] index = getIndex(xpos, ypos);
			matrix[index[0]][index[1]].updateDepthData(data.getDepth());
			
			//Unvisited, but scanned by multibeam
			//sonarRatio = 10.0; // scan width = 3*depth
			double dist = Math.abs(data.getDepth()) * (sonarRatio/2.0); // distance scanned on each side
			//System.out.println("Dist:" + dist);
			double h = data.getHeading() - (Math.PI/2);
			
			for(int i=0;i<2;i++) {
				h += (Math.PI);
				double p = 0.2;
				while(p<dist) {
					index = getIndex(xpos + p*Math.cos(h), ypos + p*Math.sin(h));
					p+=0.2;
					if(matrix[index[0]][index[1]].status == 2)
						break;
					
					if(matrix[index[0]][index[1]].status == 99)
						continue;
					
					if(matrix[index[0]][index[1]].status == 0)
						kex.visitedCells ++;
					
					matrix[index[0]][index[1]].status = 1;
					//System.out.println(p);
					
				}
			}
			
			sleep(dt);
		}
	}
	
	
	/** Calculate next waypoint based on "element value"
	 * @param ix
	 * current index x
	 * @param iy
	 * current index y
	 * @return
	 * new coordinates
	 */
	private int[] calculateNextWaypoint(int ix, int iy, int lastx, int lasty) {
		
		
		//calculate time until collisions with other boats
		//if close in time to collision, forbid the boat from traveling in the "collision direction(s)"
		
		ArrayList<double[]> directionsToAvoid = new ArrayList<double[]>();
		
		for(NpBoat other : kex.otherBoats) {
			
			//calculate time and direction.
			
			double[] P1 = {data.getPosX(), data.getPosY()};
			double V0 = data.getSpeed();
			
			double[] P2 = {other.posX, other.posY};
			double[] V2 = {other.speed * Math.cos(other.heading) , other.speed * Math.sin(other.heading)};
			
			double P2P1x =  P2[0]-P1[0];
			double P2P1y =  P2[1]-P1[1];
			
			double over = P2P1x*P2P1x + P2P1y*P2P1y;
			
			double under_1 = -1* ( (P2P1x*V2[0]) + (P2P1y*V2[1]));
			
			double under_2 = Math.sqrt( under_1*under_1 - over * ( (V2[0]*V2[0] + V2[1]*V2[1])  - V0) ); 
			
			
			
			double t1 = over / (under_1 + under_2);
			double t2 = over / (under_1 - under_2);
			
			
			if( !new Double(t1).isNaN()) {
				if(t1 > -0.5 && t1 < 4) {
					System.out.println("T1: " + t1);
					double[] temp = calculateDirection(t1, P1, V0, P2, V2);
					if(temp != null)
						directionsToAvoid.add(temp);
				}
			}
				
			if( !new Double(t2).isNaN()) {
				if(t2 > -0.5 && t2 < 4) {
					System.out.println("T2: " + t2);
					double[] temp = calculateDirection(t2, P1, V0, P2, V2);
					if(temp != null)
						directionsToAvoid.add(temp);
				}
			}
		}
		
		//find neighbouring elements and copy to another list
		ArrayList<SearchElement> targets = new ArrayList<SearchElement>();
		for(SearchElement n: matrix[ix][iy].neighbour)
			targets.add(n);
		
		
		for(int i=0;i<directionsToAvoid.size();i++) {
			double[] e = directionsToAvoid.get(i);
			for(double p=0.5;p<Math.min(2*delta,30);p+=1) {
				double xPos = data.getPosX() + e[0] * p;
				double yPos = data.getPosY() + e[1] * p;
				
				int x = (int)Math.round((xPos - kex.xMin) / kex.dx);
		        int y = (int)Math.round((yPos - kex.yMin) / kex.dy);
		        
		        //System.out.println("p: " + p + "\t" + x + "\t" + y);
		        
		        ArrayList<SearchElement> remove = new ArrayList<SearchElement>();
		        
		        for(SearchElement se : targets) {
		        	if(se.x == x && se.y == y) {
		        		remove.add(se);
		        	}
		        }
		        
		        for(int j=0;j<remove.size();j++) {
		        	targets.remove(remove.get(j));
		        	
		        	String dir = "";
		        	if(remove.get(j).x < (int)Math.round((data.getPosX() - kex.xMin) / kex.dx))
		        		dir += "left ";
		        	else if(remove.get(j).x > (int)Math.round((data.getPosX() - kex.xMin) / kex.dx))
		        		dir += "right ";
		        	
		        	if(remove.get(j).y < (int)Math.round((data.getPosY() - kex.yMin) / kex.dy))
		        		dir += "up ";
		        	else if(remove.get(j).y > (int)Math.round((data.getPosY() - kex.yMin) / kex.dy))
		        		dir += "down ";
		        	
		        	System.out.println("Direction removed " + dir);
		        }
				
			}
			
			
		}
		
		double max = Double.MIN_VALUE;
		int indexX = -1;
		int indexY = -1;
		
		double radius = Math.max((Math.abs(data.getDepth())*(sonarRatio/(2.0))), 1.5*delta);;
		
		for(int k = 0;k<targets.size();k++) {
			
			int i = targets.get(k).x;
			int j = targets.get(k).y;
			
			double value = elementValue(radius,i,j);
			if(value > max) {
				if(!(i == lastx && j==lasty)) {
					max = value;
					indexX = i;
					indexY = j;
				}
			}
		}
		if(indexX == -1)
			return null;
		
		System.out.println("New destination chosen: (" +indexX + "," + indexY + ") , value:" + max);
	
		return new int[] {indexX, indexY};
	}
	
	
	
	/** Calculate the direction to collide with another boat
	 * @param time
	 * @param P1
	 * @param V0
	 * @param P2
	 * @param V2
	 * @return
	 * direction (or null)
	 */
	private double[] calculateDirection(double time, double[] P1, double V0, double[] P2, double[] V2) {
		
		//calculate intersections point.
		double[] PI = {P2[0] + V2[0] * time, P2[1] + V2[1] * time};
		
		double[] v = {PI[0]-P1[0] , PI[1]-P1[1]};
		double dev = Math.sqrt(v[0]*v[0] + v[1]*v[1]);
		double[] e = {v[0] / dev, v[1] / dev}; 
		
		//System.out.println("Direction: (" + e[0] + " , " + e[1] + ")");
		
		return e;
	}
	
	
	/** Calculate the value of an element recursively
	 * @param recursiondepth
	 * current recursion depth
	 * @param x
	 * current element x- index
	 * @param y
	 * current element y- index
	 * @param dontVisit
	 * List of elements already calculated
	 * @return
	 */
	private double elementValue(double radius, int x, int y) {
		
		double startX = matrix[x][y].xCoord;
		double startY = matrix[x][y].yCoord;
		
		ArrayList<SearchElement> visited = new ArrayList<SearchElement>();
		LinkedList<SearchElement> queue = new LinkedList<SearchElement>();
		
		queue.add(matrix[x][y]);
		
		double sum = 0;
		
		while(!queue.isEmpty()) {
			SearchElement element = queue.pop();
			visited.add(element); //dont look at the same element more than one time
			
			double d = Math.sqrt( (element.xCoord - startX)*(element.xCoord - startX) + (element.yCoord - startY)*(element.yCoord - startY));
			
			if(d>radius) // element to far away
				continue;
			
			//check if there are boats at this position
			boolean boat = false;
			for(NpBoat npb : kex.otherBoats) {
				
		        if(Math.abs(element.xCoord-npb.posX) < delta && Math.abs(element.yCoord-npb.posY) < delta) {
		        	//System.out.println("Boat close");
		        	boat = true;
		        	break;
		        }
			}
			
			if(boat) {
				sum-= 200 / Math.max(1,d);
				continue;
			}
			
			//element is not scanned or unknown -> element is probably land
			if(element.status != 1 && element.status != 0) {
				continue;
			}
			
			if(element.status == 0)
				sum += 100.0 / Math.max(1, d); //don't divide by zero
			
			for(SearchElement se: element.neighbour) {
				if(!visited.contains(se)) {
					queue.add(se);
				}
			}
			//System.out.println("Queue size:" + queue.size());
			//element.status = 42;
		}
		
		//System.out.println("radius:" + radius);
		//System.out.println("sum:" + sum);
		return sum;
	}
	
	/**
	 * 
	 * @return
	 * true = line under reached
	 * false = line above reached
	 */
	 private void followLand(double distance) {
		
		kex.setSpeed(5);
		 
		System.out.println("Follow land");
		double dist = 0;
		
		double targetDepth = -1;
		
		//PID controller
		double KP = 0.2; //Proportional gain
		double KI = 1.0 / 5000; //integral gain
		double KD = 300; //derivative gain
		
		long time = System.currentTimeMillis();
		double Integral = 0; 
		double lastError = data.getDepth() - targetDepth;
		
		double maxAngle = Math.PI / 8;
		
		double lastX = data.getPosX();
		double lastY = data.getPosY();
		
		sleep(dt);
		
		while(dist < distance) {
			
			double dx = (data.getPosX() - lastX);
			double dy = (data.getPosY() - lastY);
			lastX = data.getPosX();
			lastY = data.getPosY();
			
			dist += Math.sqrt(dx*dx + dy*dy);
			
			//stop the boat from going outside the polygon
			if(outOfBounds(data.getPosX(), data.getPosY())) {
				kex.setSpeed(0);
				return;
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
			
			
			//change status of element
			int[] index = this.getIndex(lastX, lastY);
			matrix[index[0]][index[1]].updateDepthData(data.getDepth());
			
			double h = data.getHeading();
			if(data.getRightSonar() > data.getLeftSonar()) { // deeper on right side
				h-=Math.PI/2;
			}else { 
				h+=Math.PI/2;
			}
			
			double p = 0;
			double xpos = data.getPosX();
			double ypos = data.getPosY();
			while(p<delta*2) {
				int[] in = getIndex(xpos + p*Math.cos(h), ypos + p*Math.sin(h));
				p+=0.2;
				
				
				if(matrix[in[0]][in[1]].status == 1 && matrix[in[0]][in[1]].timesVisited != 0)
					continue;
				
				if(matrix[in[0]][in[1]].status == 0)
					kex.visitedCells ++;
				
				matrix[in[0]][in[1]].status = 2;
				//System.out.println(p);
			}
			//matrix[index[0]][index[1]].status = 1;
			//matrix[index[0]][index[1]].updateDepthData(data.getDepth());
			
			
			
			
			sleep(dt);
		}
		kex.setSpeed(0);
	}
	
	
	private boolean outOfBounds(double x, double y) {
		
		if(y > region.maxY() || y < region.minY() || x > region.maxX() || x < region.minX())
			return true;
		if(x < region.findX(y, false) || x > region.findX(y, true))
			return true;
		return false;
	}
	
	@Override
	void stop() {
		// TODO Auto-generated method stub
		stop = true;
		kex.setSpeed(0);
	}

	@Override
	boolean followingLand() { //might not be used in this version
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	boolean isDone() {
		return stop;
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}catch(Exception e) {}
	}
}
