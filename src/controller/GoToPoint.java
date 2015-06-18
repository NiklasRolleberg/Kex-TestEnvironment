package controller;

import java.util.ArrayList;

import kex2015.NpBoat;

public class GoToPoint {
	
	Kex kex;
	
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
		kex.setSpeed(15);
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
					
					System.out.println("GO: next destination chosen: (" +nextIndex[0] + "," + nextIndex[1] + ")");
					
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
		
		int[] xIndex = {x+1, x-1, x+0, x+0,    x+1, x-1, x+1, x-1};
		int[] yIndex = {y+0, y+0, y+1, y-1,    y+1, y-1, y-1, y+1};
		
		double min = Double.MAX_VALUE;
		int index = -1;
		
		for(int i=0;i<8;i++) {
			if(xIndex[i] < 0 || xIndex[i] >= kex.nx)
				continue;
			if(yIndex[i] < 0 || yIndex[i] >= kex.ny)
				continue;
			
			if(cost[xIndex[i]][yIndex[i]] < min && cost[xIndex[i]][yIndex[i]] != -1) {
				min = cost[xIndex[i]][yIndex[i]];
				index = i;
			}
		}
		return new int[] {xIndex[index],yIndex[index]};
	}
	
	
	/** Calculates if a collision is about to happen between two coordinates
	 * @param startpos
	 * 			pos1
	 * @param targetPos
	 * 			pos2
	 * @param time
	 * 			time until the boat reaches pos1
	 * @return
	 */
	private boolean willCollide(double[] startpos, double[] targetPos, double time, double speed) {
		
		for(NpBoat other : kex.otherBoats) {
			
			//calculate time and direction.
			
			double[] P1 = startpos; //current boatPos
			double V0 = speed;
			
			double[] P2 = {other.posX, other.posY};
			double[] V2 = {other.speed * Math.cos(other.heading) , other.speed * Math.sin(other.heading)};
			P2[0] += V2[0]*time;
			P2[1] += V2[1]*time;
			
			double P2P1x =  P2[0]-P1[0];
			double P2P1y =  P2[1]-P1[1];
			
			double over = P2P1x*P2P1x + P2P1y*P2P1y;
			
			double under_1 = -1* ( (P2P1x*V2[0]) + (P2P1y*V2[1]));
			
			double under_2 = Math.sqrt( under_1*under_1 - over * ( (V2[0]*V2[0] + V2[1]*V2[1])  - V0) ); 
			
			
			
			double t1 = over / (under_1 + under_2);
			double t2 = over / (under_1 - under_2);
			
			
			if( !new Double(t1).isNaN()) {
				if(t1 > -0.5 && t1 < 4) {
					return true;
				}
			}
				
			if( !new Double(t2).isNaN()) {
				if(t2 > -0.5 && t2 < 4) {
					return true;
				}
			}
		}
		return false;
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
