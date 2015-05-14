package controller;

import java.util.ArrayList;

public class GoToPoint {
	
	Kex kex;
	
	public GoToPoint(Kex kex) {
		this.kex = kex;
	}
	
	public double distance (int startX, int startY, int stopX, int stopY) {
		
		ArrayList<SearchElement> path = null;
		try {
			path = Astar(kex.elementMatrix[startX][startY],kex.elementMatrix[stopX][stopY]);
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
		if(kex.elementMatrix[stopX][stopY].targeted)
			dist*=10;
		
		return dist;
	}
	
	public boolean GO(int startX, int startY, int stopX, int stopY) {
		//this.startX = startX;
		//this.startY = startY;
		//this.stopX = stopX;
		//this.stopY = stopY;
		
		ArrayList<SearchElement> path = null;
		
		if(startX == stopX && startY == stopY) {
			System.out.println("Already at target point");
			return true;
		}
		
		try {
			path = Astar(kex.elementMatrix[startX][startY],kex.elementMatrix[stopX][stopY]);
		}
		catch(Exception e) {
			System.out.println("A* failed " + e);
			return false;
		}
			
		//No path found
		if(path == null) {
			System.out.println("no path found");
			return false;
		}
		
		//follow the path
		int index = path.size()-1;
		double targetX = path.get(index).xCoord;
		double targetY = path.get(index).yCoord;
		double data[] = kex.getData();
		kex.elementMatrix[stopX][stopY].targeted = true;
		
		while (index >= 0) {
			kex.setWaypoint(targetX, targetY);
			kex.setSpeed(15);
			data = kex.getData();
			double dx = targetX-data[0];
			double dy = targetY-data[1];
			
			//target reached -> choose new target
			if(Math.sqrt(dx*dx + dy*dy) < 3) {
				index--;
				if(index < 0)
				{
					break;
				}

				targetX = path.get(index).xCoord;
				targetY = path.get(index).yCoord;
				kex.setWaypoint(targetX, targetY);
			}
			sleep(100);
		}
		System.out.println("Target reached");
		kex.setSpeed(0);
		return true;
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
	public ArrayList<SearchElement> Astar(SearchElement startElement, SearchElement stopElement ) {
		if(startElement.status != 1 || stopElement.status != 1)
			return null;
		
		int maxX = kex.nx;
		int maxY = kex.ny;
		
		ArrayList<SearchElement> closedSet = new ArrayList<SearchElement>();
		ArrayList<SearchElement> openSet = new ArrayList<SearchElement>();
		
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
