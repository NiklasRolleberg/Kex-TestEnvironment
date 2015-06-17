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
	
	public double distance (int startX, int startY, int stopX, int stopY) {
		
		ArrayList<SearchElement> path = null;
		try {
			path = Astar(kex.elementMatrix[startX][startY],kex.elementMatrix[stopX][stopY],null);
		}
		catch(Exception e) {
			System.out.println("Distance: A* failed: " + e);
			return -1;
		}
		if(path == null) {
			System.out.println("No path found");
			return -1;
		}
		
		double dist = 0;
		
		double lastX = path.get(0).xCoord;
		double lastY = path.get(0).yCoord;
		for(int i=1;i<path.size();i++) {
			double newX = path.get(i).xCoord;
			double newY = path.get(i).yCoord;
			
			dist += Math.sqrt((newX-lastX)*(newX-lastX) + (newY-lastY)*(newY-lastY));
			lastX = newX;
			lastY = newY;
		}
		
		System.out.println("Path found: (" + startX + " , " + startY + ") -> (" + stopX + " , " + stopY + ")" );
		if(kex.elementMatrix[stopX][stopY].targeted != 0)
			dist*=10;
		
		return dist;
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

		
		
		//find current index
		int cX = kex.elementMatrix[startX][startY].x;
		int cY = kex.elementMatrix[startX][startY].y;
		
		double targetX = kex.elementMatrix[startX][startY].xCoord;
		double targetY = kex.elementMatrix[startX][startY].yCoord;
		
		/*double targetX = kex.elementMatrix[stopX][stopY].xCoord;
		double targetY = kex.elementMatrix[stopX][stopY].yCoord;
		int cX = kex.elementMatrix[stopX][stopY].x;
		int cY = kex.elementMatrix[stopX][stopY].y;*/
		
		kex.setWaypoint(targetX, targetY);
		kex.setSpeed(15);
		
		double data[] = kex.getData();
		while(true) {
			data = kex.getData();
			double dx = targetX - data[0];
			double dy = targetY - data[1];
			
			//target reached, pick a new target
			if(Math.sqrt(dx*dx + dy*dy) < 3) {
				distance += Math.sqrt(dx*dx + dy*dy);
				
				if(cX == stopX && cY == stopY) {
					System.out.println("Target Reached");
					kex.elementMatrix[cX][cY].targeted += 1;
					return distance;
				}
				
				int[] xIndex = {cX+1, cX-1, cX+0, cX+0,    cX+1, cX-1, cX+1, cX-1};
				int[] yIndex = {cY+0, cY+0, cY+1, cY-1,    cY+1, cY-1, cY-1, cY+1};
				
				double min = Double.MAX_VALUE;
				int index = -1;
				
				for(int i=0;i<8;i++) {
					if(xIndex[i] < 0 || xIndex[i] >= kex.nx)
						continue;
					if(yIndex[i] < 0 || yIndex[i] >= kex.ny)
						continue;
					
					if(costMatrix[xIndex[i]][yIndex[i]] < min && costMatrix[xIndex[i]][yIndex[i]] != -1) {
						min = costMatrix[xIndex[i]][yIndex[i]];
						index = i;
					}
				
				}
				
				if(index != -1) {
					targetX = kex.elementMatrix[xIndex[index]][yIndex[index]].xCoord;
					targetX = kex.elementMatrix[xIndex[index]][yIndex[index]].xCoord;
					
					kex.setWaypoint(targetX, targetY);
					cX = kex.elementMatrix[xIndex[index]][yIndex[index]].x;
					cY = kex.elementMatrix[xIndex[index]][yIndex[index]].y;
				}
				else{
					System.out.println("Go failed");
					
					for(int i=0;i<8;i++) {
						if(xIndex[i] < 0 || xIndex[i] >= kex.nx)
							continue;
						if(yIndex[i] < 0 || yIndex[i] >= kex.ny)
							continue;
						
						System.out.println("Cost: " + costMatrix[xIndex[i]][yIndex[i]]);
					
					}
					return -1;
				}
			}
			
			sleep(kex.dt);
		}
			

		/*
		ArrayList<SearchElement> path = null;
		
		if(startX == stopX && startY == stopY) {
			System.out.println("Already at target point");
			return -1;		
		}
		
		
		
		
		// TODO Find a path
		try {
			path = Astar(kex.elementMatrix[startX][startY],kex.elementMatrix[stopX][stopY], null);
		}
		catch(Exception e) {
			System.out.println("A* failed " + e);
			return -1;
		}
		
		//No path found
		if(path == null) {
			System.out.println("no path found");
			return -1;
		}
				
		
		
		kex.elementMatrix[stopX][stopY].targeted = true;
		double travelSpeed = 15;
		// TODO try to follow that path
		//follow the path
		int index = path.size()-1;
		double targetX = path.get(index).xCoord;
		double targetY = path.get(index).yCoord;
		double data[] = kex.getData();
		kex.elementMatrix[stopX][stopY].targeted = true;
		double dist = 0;
		while (index >= 0) {
			kex.setWaypoint(targetX, targetY);
			kex.setSpeed(travelSpeed);
			data = kex.getData();
			double dx = targetX-data[0];
			double dy = targetY-data[1];
			
			//target reached -> choose new target
			if(Math.sqrt(dx*dx + dy*dy) < 3) {
				index--;
				if(index < 0) {
					break;
				}
				
				dist += Math.sqrt(dx*dx + dy*dy);
				
				targetX = path.get(index).xCoord;
				targetY = path.get(index).yCoord;
				kex.setWaypoint(targetX, targetY);
				
				//check if a collision is about to happen;
				double d = 0;
				for(int i = index; i>0 && (index - i)<2;i--) {
					
					double t = d/travelSpeed;
					
					double[] pos1 = {path.get(i).xCoord, path.get(i).yCoord};
					double[] pos2 = {path.get(i+1).xCoord, path.get(i+1).yCoord};
					
					d += Math.sqrt( (pos2[0]-pos1[0])*(pos2[0]-pos1[0]) + (pos2[1]-pos1[1])*(pos2[1]-pos1[1])  );
					
					
					if(willCollide(pos1, pos2, t, travelSpeed)) {
						//calculate a new path
						System.out.println("calculating new path");
						try {
							ArrayList<SearchElement> a = new ArrayList<SearchElement>();
							a.add(kex.elementMatrix[path.get(i+1).x][path.get(i+1).y]);
							kex.elementMatrix[path.get(i+1).x][path.get(i+1).y].status = 42;
							path = Astar(kex.elementMatrix[path.get(i).x][path.get(i).y],kex.elementMatrix[stopX][stopY], a);
							index = path.size()-1;
							break;
						}
						catch(Exception e) {
							System.out.println("A* failed " + e);
							//return -1;
						}
					}
				}
				
			}
			sleep(100);
		}
		
		
		
			// TODO give points in path for boat to travel to.
			// TODO check distance to other boats
				// TODO if distance is close, calculate the risk of collision
					// TODO if risk is high, calculate a new path
		// TODO When the end of the path is reached, return the distance traveled.
		

		System.out.println("Target reached");
		kex.setSpeed(0);
		return dist;*/
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
	
	/** A* shortest path algorithm
	 * @param startElement
	 * start
	 * @param stopElement
	 * goal
	 * @return
	 * path, or null if no path was found 
	 */
	public ArrayList<SearchElement> Astar(SearchElement startElement, SearchElement stopElement, ArrayList<SearchElement> avoid) {
		if(stopElement.status != 1)
			return null;
		
		int maxX = kex.nx;
		int maxY = kex.ny;
		
		ArrayList<SearchElement> closedSet = new ArrayList<SearchElement>();
		ArrayList<SearchElement> openSet = new ArrayList<SearchElement>();
		
		if (avoid != null)
			closedSet.addAll(avoid);
		
		SearchElement[][] came_from = new SearchElement[maxX][maxY]; 
		double[][] g_score = new double[maxX][maxY];
		double[][] f_score = new double[maxX][maxY];
				
		
		openSet.add(startElement);
		
		g_score[startElement.x][startElement.y] = 0;
		f_score[stopElement.x][stopElement.y] = heuristic_cost_estimate(startElement, stopElement);
		
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
			
			
			if(current == stopElement) {
				System.out.println("goal found");
				return reconstructPath(startElement, current, came_from);
			}
			
			openSet.remove(current);
			closedSet.add(current);
			
			//itterate through neighbours
			for(SearchElement n:current.neighbour) {
				if(closedSet.contains(n) || n.status != 1)
					continue;

				
				double dx = n.xCoord-current.x;
				double dy = n.yCoord-current.y;
				double tentative_g_score = g_score[current.x][current.y] + Math.sqrt(dx*dx + dy*dy);
				
				if (!openSet.contains(n) || tentative_g_score < g_score[n.x][n.y]) {
					double m = 1;
					
					if(Math.abs(n.getRecordedDepth()) < 3)
						m = 2;
					if(Math.abs(n.getRecordedDepth()) < 1)
						m=50;
					
					came_from[n.x][n.y] = current;
					f_score[n.x][n.y] = tentative_g_score + (m * heuristic_cost_estimate(n, stopElement));
					
					if(!openSet.contains(n)) {
							openSet.add(n);
						}
					} // (1.0/((-1)*(n.accumulatedDepth/n.timesVisited)))
			}
			
		}
		System.out.println("No path found");
		return null;
	}
	
	private double heuristic_cost_estimate(SearchElement one, SearchElement two) {
		double dx = one.xCoord - two.xCoord;
		double dy = one.yCoord - two.yCoord;;
		
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	ArrayList<SearchElement> reconstructPath(SearchElement start, SearchElement current, SearchElement[][] came_from) { 
		ArrayList<SearchElement> path = new ArrayList<SearchElement>();
		
		SearchElement temp = current;
		path.add(temp);
		
		while(true) {
			temp = came_from[temp.x][temp.y];
			path.add(temp);
			
			if(temp == start)
				break;
			
			//System.out.println("Path size: " + path.size());
		}
		return path;
	}
	

		
}
