package controller;

import java.util.ArrayList;

public class MultiBeamSweepingPattern extends SearchPattern {
	
	SearchElement[][] matrix;
	boolean stop = false;
	
	public MultiBeamSweepingPattern(Kex kex, SearchCell region, SearchElement[][] elementMatrix, double delta, long dt) {
		super(kex, region, delta, dt);
		matrix = elementMatrix;
		
		//change some valus to simulate land
		/*
		for(int i=0;i<40;i++) {
			matrix[(int) (((double)kex.nx)*Math.random())][(int) (((double)kex.ny)*Math.random())].status = 2;
		}
		*/
		
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
				//followLand(Math.abs(data.getDepth() * ratio))
				followLand(30);
				findNew = true;
				continue;
			}
			
			kex.setSpeed(Math.max(-data.getDepth()*3, 5));
			
			// TODO target reached?
			double dx = targetX - data.getPosX();
			double dy = targetY - data.getPosY();
			
			if(Math.sqrt(dx*dx + dy*dy) < 3 || findNew) {
				//target reached choosing new target
				int[] current;
				
				//change status of scanned elements
				if(!findNew) {
					matrix[next[0]][next[1]].status = 1;
					matrix[next[0]][next[1]].updateDepthData(data.getDepth());
					current  = next;
				} else {
					//last = next;
					current = getIndex(data.getPosX(), data.getPosY());
				}
				findNew = false;
				
				next = calculateNextWaypoint(current[0],current[1],last[0],last[1]);
				last = current;
				if(next == null) {
					this.stop();
					break;
				}
				if(matrix[next[0]][next[1]].timesVisited > 3*delta) {
					this.stop();
					break;
				}
				
		        targetX = matrix[next[0]][next[1]].xCoord;
		        targetY = matrix[next[0]][next[1]].yCoord;
		        
		        xte.setWaypoint(targetX, targetY);

			}
			
			/*
			// TODO land?
			if(data.getDepth() > -1) {
				System.out.println("LAND!");
				//followLand(Math.abs(data.getDepth() * ratio))
				followLand(delta);
				findNew = true;
				continue;
			}*/
			
			// TODO mark scanned elements (based on depth)
			
			//Unvisited, but scanned by multibeam
			double ratio = 5.0; // scan width = 3*depth
			double dist = Math.abs(data.getDepth()) * (ratio/2.0); // distance scanned on each side
			//System.out.println("Dist:" + dist);
			double h = data.getHeading() - (Math.PI/2);
			double xpos = data.getPosX();
			double ypos = data.getPosY();
			
			for(int i=0;i<2;i++) {
				h += (Math.PI);
				double p = 0.2;
				while(p<dist) {
					int[] index = getIndex(xpos + p*Math.cos(h), ypos + p*Math.sin(h));
					if(matrix[index[0]][index[1]].status == 2)
						break;
					
					matrix[index[0]][index[1]].status = 1;
					//System.out.println(p);
					p+=0.2;
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
		
		int[] i = {ix,ix,ix+1,ix-1};
		int[] j = {iy+1,iy-1,iy,iy};
		
		double max = Double.MIN_VALUE;
		int index = -1;
		
		for(int k = 0;k<4;k++) {
			double value = elementValue(0,i[k],j[k], new ArrayList<SearchElement>());
			if(value > max) {
				if(!(i[k] == lastx && j[k]==lasty)) {
					max = value;
					index = k;
				}
			}
		}
		if(index == -1)
			return null;
		
		System.out.println("New destination chosen: (" +i[index] + "," + j[index] + ") , value:" + max);
	
		return new int[] {i[index], j[index]};
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
	private double elementValue(int recursiondepth, int x, int y, ArrayList<SearchElement> dontVisit) {
		
		//check oob
		if (x >= kex.nx){
			return 0;
        }
        if (y >= kex.ny){
        	return 0;
        }
        if(x<0)
        	return 0;
        
        if(y<0)
        	return 0;
		
		if(recursiondepth > 2) {
			//maximum depth reached
			int status = matrix[x][y].status;
			
			if(status == 0) // not scanned
				return 10;
			if(status == 1) //scanned 
				return 0;
			return 0;
		}
		
		dontVisit.add(matrix[x][y]);
		
		//check status
		int status = matrix[x][y].status;
		
		double sum = 0;
		
		if(status == 0) // this element is unscanned
			sum = 10;
		if(status == 1) // element is scanned
			sum = 0;
		if(status != 1 && status != 0) //land or oob
			return 0;
		
		sum += elementValue(recursiondepth+1,x+1,y,dontVisit);
		sum += elementValue(recursiondepth+1,x-1,y,dontVisit);
		sum += elementValue(recursiondepth+1,x,y+1,dontVisit);
		sum += elementValue(recursiondepth+1,x,y-1,dontVisit);
		
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
			
			double h = data.getHeading();
			if(data.getRightSonar() > data.getLeftSonar()) { // deeper on right side
				h-=Math.PI/2;
			}else { 
				h+=Math.PI/2;
			}
			
			double p = 0.2;
			double xpos = data.getPosX();
			double ypos = data.getPosY();
			while(p<delta) {
				int[] in = getIndex(xpos + p*Math.cos(h), ypos + p*Math.sin(h));
				matrix[in[0]][in[1]].status = 2;
				//System.out.println(p);
				p+=0.2;
			}
			matrix[index[0]][index[1]].status = 1;
			
			
			
			
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
