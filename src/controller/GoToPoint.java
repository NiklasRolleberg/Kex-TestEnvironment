package controller;

import java.util.ArrayList;

import kex2015.NpBoat;

public class GoToPoint {
	
	Kex kex;
	private double targetSpeed = 10;
	
	public GoToPoint(Kex kex) {
		this.kex = kex;
	}
	
	/** Use Dijkstra to calculate the cost of each element
	 * @param targetX
	 * 			x-index for target
	 * @param targetY
	 * 			Y-index for target
	 * @return
	 * 			nx x ny matrix with costs
	 */
	public double[][] calculateCost(int targetX, int targetY) {
		//double[][] costMatrix = new double[kex.nx][kex.ny];
		
		if(kex.elementMatrix[targetX][targetY].status != 1)
			System.out.println("Target is unknown ("+targetX + " , " + targetY + ")");
			
			//return null;
		
		System.out.println("Calculating cost matrix");
		
		int maxX = kex.nx;
		int maxY = kex.ny;
		
		ArrayList<SearchElement> closedSet = new ArrayList<SearchElement>();
		ArrayList<SearchElement> openSet = new ArrayList<SearchElement>();
		
		SearchElement[][] came_from = new SearchElement[maxX][maxY]; 
		double[][] g_score = new double[maxX][maxY];
		double[][] f_score = new double[maxX][maxY];
		
		for(int i=0;i < maxX;i++) {
			for(int j=0;j < maxY;j++) {
				g_score[i][j] = -1;
			}
		}
				
		
		openSet.add(kex.elementMatrix[targetX][targetY]);
		
		g_score[kex.elementMatrix[targetX][targetY].x][kex.elementMatrix[targetX][targetY].y] = 0;
		f_score[kex.elementMatrix[targetX][targetY].x][kex.elementMatrix[targetX][targetY].y] = 0;//heuristic_cost_estimate(startElement, stopElement);
		
		while(!openSet.isEmpty()) {
			//find node with lowest f_score value
			
			double min = Double.MAX_VALUE;
			SearchElement current =  null;
			for(SearchElement n:openSet) {
				if(f_score[n.x][n.y] <= min) {
					min = f_score[n.x][n.y];
					//System.out.println(min);
					current = n;
				}
			}
			
			openSet.remove(current);
			closedSet.add(current);
			
			//itterate through neighbours
			for(SearchElement n:current.neighbour) {
				if(closedSet.contains(n) || n.status != 1)
					continue;

				
				double dx = n.xCoord-current.x;
				double dy = n.yCoord-current.y;
				
				double tentative_g_score;
				if (g_score[current.x][current.y] != -1)
					tentative_g_score = g_score[current.x][current.y] + Math.sqrt(dx*dx + dy*dy);
				else
					tentative_g_score = Math.sqrt(dx*dx + dy*dy);
				
				if (!openSet.contains(n) || tentative_g_score < g_score[n.x][n.y]) {
					came_from[n.x][n.y] = current;
					g_score[n.x][n.y] = tentative_g_score;
					f_score[n.x][n.y] = tentative_g_score;// + (m * heuristic_cost_estimate(n, stopElement));
					
					//System.out.println("g_score updated: " + tentative_g_score);
					
					if(!openSet.contains(n)) 
					{
						openSet.add(n);
					}
				}
			}
		}
		return g_score;
	}
	

	
	/**Go from one point to another
	 * @param startX
	 * @param startY
	 * @param stopX
	 * @param stopY
	 * @return
	 * distance / -1 if fail
	 */
	public double GO(int startX, int startY, int stopX, int stopY) {
		
		double distance = 0;
		
		// calculate the cost matrix (cost 0 = target)
		double[][] costMatrix = this.calculateCost(stopX, stopY);
		
    	// DEBUG SAK
		System.out.println("GO: Going ("+startX + "," + stopY + ") -> ("+stopX + "," + stopY + ")" );
		/*
		System.out.println("\n\n ----GO----\n\n");
		
    	for(int i=0;i<kex.nx;i++) {
    		for(int j=0;j<kex.ny;j++) {
    			
    			if(i==startX && j==startY)
    				System.out.print("S \t");
    			else if(i==stopX && j==stopY)
    				System.out.print("G \t");
    			else if(costMatrix[i][j] >=0)
    				System.out.print("0 \t");
    			else
    				System.out.print("- \t");
    		}
    		System.out.println("");
    	}
    	System.out.println("\n\n");

		SearchElement se = kex.elementMatrix[startX][startY];
    	System.out.println("information about start: \nCoordinates: (" + se.xCoord + "," + se.yCoord + ") " + "\nStstus: " + se.status + "\nDepth:" + se.getRecordedDepth() + "\nTargeted: " + se.targeted);
    	se = kex.elementMatrix[stopX][stopY];
    	System.out.println("\n\ninformation about stop: \nCoordinates: (" + se.xCoord + "," + se.yCoord + ") " + "\nStstus: " + se.status + "\nDepth:" + se.getRecordedDepth() + "\nTargeted: " + se.targeted);
    	*/
    	
		
		//find target index (starting at current)
		
		int tX = kex.elementMatrix[startX][startY].x;
		int tY = kex.elementMatrix[startX][startY].y;
		
		double targetX = kex.elementMatrix[startX][startY].xCoord;
		double targetY = kex.elementMatrix[startX][startY].yCoord;
		
		/*
		int tX = kex.elementMatrix[stopX][stopY].x;
		int tY = kex.elementMatrix[stopX][stopY].y;
		
		double targetX = kex.elementMatrix[stopX][stopY].xCoord;
		double targetY = kex.elementMatrix[stopX][stopY].yCoord;
		*/
		
		kex.setWaypoint(targetX, targetY);
		kex.setSpeed(targetSpeed);
		boolean findNext = true;
		double data[] = kex.getData();
		while(true) {
			data = kex.getData();
			double dx = targetX - data[0];
			double dy = targetY - data[1];
			
			//target reached, pick a new target
			if(Math.sqrt(dx*dx + dy*dy) < 3 || findNext) {
				findNext = false;
				distance += Math.sqrt(dx*dx + dy*dy);
				
				if(tX == stopX && tY == stopY) {
					System.out.println("GO: Reached ("+stopX + "," + stopY + ") Distnace: " + distance );
					kex.elementMatrix[tX][tY].targeted += 1;
					kex.setSpeed(0);
					return distance;
				}
				
				int[] nextIndex = findNextWaypoint(costMatrix, tX,tY);
				
				if(nextIndex != null) {
					
					targetX = kex.elementMatrix[nextIndex[0]][nextIndex[1]].xCoord;
					targetY = kex.elementMatrix[nextIndex[0]][nextIndex[1]].yCoord;
					
					kex.setWaypoint(targetX, targetY);
					tX = kex.elementMatrix[nextIndex[0]][nextIndex[1]].x;
					tY = kex.elementMatrix[nextIndex[0]][nextIndex[1]].y;
				}
				else{
					System.out.println("Go failed");	
					return -1;
				}
			}
			
			sleep(kex.dt);
		}
	}
	
	/**Calculates the next waypoint based on cost and other boats
	 * @param cost
	 * 			nx X ny matrix with costs
	 * @param x
	 * 			current X
	 * @param y
	 * 			current Y
	 * @return
	 * 			index for next waypoint
	 */
	private int[] findNextWaypoint(double[][] cost, int x, int y) {
		
		ArrayList<double[]> directionsToAvoid = new ArrayList<double[]>();
		
		for(NpBoat other : kex.otherBoats) {
			
			//calculate time and direction.
			
			double[] P1 = {kex.boat.getPos()[0], kex.boat.getPos()[1]};
			double V0 = targetSpeed;
			
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
				if(t1 > 0 && t1 < 4) {
					System.out.println("T1: " + t1);
					double[] temp = calculateDirection(t1, P1, V0, P2, V2);
					if(temp != null)
						directionsToAvoid.add(temp);
				}
			}
				
			if( !new Double(t2).isNaN()) {
				if(t2 > 0 && t2 < 4) {
					System.out.println("T2: " + t2);
					double[] temp = calculateDirection(t2, P1, V0, P2, V2);
					if(temp != null)
						directionsToAvoid.add(temp);
				}
			}
		}
		
		//find neighbouring elements and copy to another list
		ArrayList<SearchElement> targets = new ArrayList<SearchElement>();
		int[] xIndex = {x+1, x-1, x+0, x+0,    x+1, x-1, x+1, x-1};
		int[] yIndex = {y+0, y+0, y+1, y-1,    y+1, y-1, y-1, y+1};
		
		
		for(int i=0;i<8;i++) {
			if(xIndex[i] < 0 || xIndex[i] >= kex.nx)
				continue;
			if(yIndex[i] < 0 || yIndex[i] >= kex.ny)
				continue;
			
			targets.add(kex.elementMatrix[xIndex[i]][yIndex[i]]);
		}
		
		
		for(int i=0;i<directionsToAvoid.size();i++) {
			double[] e = directionsToAvoid.get(i);
			for(double p=0.5;p<Math.min(2*kex.delta,30);p+=1) {
				double xPos = kex.boat.getPos()[0] + e[0] * p;
				double yPos = kex.boat.getPos()[1] + e[1] * p;
				
				int indexX = (int)Math.round((xPos - kex.xMin) / kex.dx);
		        int indexY = (int)Math.round((yPos - kex.yMin) / kex.dy);
		        
		        //System.out.println("p: " + p + "\t" + x + "\t" + y);
		        
		        ArrayList<SearchElement> remove = new ArrayList<SearchElement>();
		        
		        for(SearchElement se : targets) {
		        	if(se.x == indexX && se.y == indexY) {
		        		remove.add(se);
		        	}
		        }
		        
		        for(int j=0;j<remove.size();j++) {
		        	targets.remove(remove.get(j));
		        	
		        	String dir = "";
		        	if(remove.get(j).x < x)
		        		dir += "left ";
		        	else if(remove.get(j).x > x)
		        		dir += "right ";
		        	
		        	if(remove.get(j).y < y)
		        		dir += "up ";
		        	else if(remove.get(j).y > y)
		        		dir += "down ";
		        	
		        	System.out.println("GO: Direction removed " + dir);
		        }
				
			}
			
			
		}
		
		double min = Double.MAX_VALUE;
		int indexX = -1;
		int indexY = -1;
		
		for(int k = 0;k<targets.size();k++) {
			
			int i = targets.get(k).x;
			int j = targets.get(k).y;
			
			double value = cost[i][j];
			if((value < min) && (value != -1)) {
				min = value;
				indexX = i;
				indexY = j;
			}
		}
		if(indexX == -1) {
			System.out.println("No destination found");
			return null;
		}
		
		System.out.println("GO: next destination chosen: (" +indexX + "," + indexY + ") , value:" + min);
	
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
	

	private void sleep(long delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
