package controller;

import java.util.ArrayList;

import controller.Kex.SearchCell;

public class GoToPoint extends SearchPattern {
	
	boolean stop;
	int startX;
	int startY;
	int stopX;
	int stopY;
	
	public GoToPoint(Kex kex, SearchCell region, double delta, long dt) {
		super(kex, region, delta, dt);
	}
	
	public void GO(int startX, int startY, int stopX, int stopY) {
		this.startX = startX;
		this.startY = startY;
		this.stopX = stopX;
		this.stopY = stopY;
		
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		/*
		//fuska med djup
		for(int i=0;i<region.nx;i++) {
			for(int j=0;j<region.ny;j++) {
				if(region.elementMatrix[i][j].status == 99)
					continue;
				
				region.elementMatrix[i][j].accumulatedDepth = -5 + Math.random()* 6;
				
				if(region.elementMatrix[i][j].accumulatedDepth > 0)
					region.elementMatrix[i][j].status = 2;
				else 
					region.elementMatrix[i][j].status = 1;
				
			}
		}*/
		
		//add neighbors 
		//TODO gör inte såhär
		for(int i=0;i<region.nx;i++) {
			for(int j=0;j<region.ny;j++) {
				
				region.elementMatrix[i][j].x = i;
				region.elementMatrix[i][j].y = j;
				
				if(region.elementMatrix[i][j].status == 99)
					continue;
				
				int[] indexX = {i   , i+1, i+1 ,i+1 ,i   ,i-1 ,i-1 ,i-1};
	        	int[] indexY = {j-1 , j-1, j   ,j+1 ,j+1 ,j+1 ,j   ,j-1};
	        	
				//int[] indexX = {i   , i+1 , i   ,i-1};
	        	//int[] indexY = {j-1 , j   , j+1 ,j  };

				
				for(int k = 0; k < 8;k++) { //8
	        		int ii = indexX[k];
	        		int jj = indexY[k];
	        		if(ii >= 0 && ii < region.nx && jj >= 0 && jj < region.ny && region.elementMatrix[ii][jj].status != 99)
	            	{
	        			region.elementMatrix[i][j].neighbour.add(region.elementMatrix[ii][jj]);
	            	}
	        	}
			}
		}
		
		
		Kex.searchElement startElement = region.elementMatrix[startX][startY];
		Kex.searchElement stopElement = region.elementMatrix[stopX][stopY];
		
		ArrayList<Kex.searchElement> path = Astar(startElement, stopElement); 
		
		int index = path.size()-1;
		double targetX = path.get(index).xCoord;
		double targetY = path.get(index).yCoord;
		
		double oldTargetX = data.getPosX();
		double oldTargetY = data.getPosY();
		xte.setWaypoint(oldTargetX, oldTargetY,targetX, targetY);
		
		while (!stop && index >= 0) {
			
			double dx = targetX-data.getPosX();//data[0];
			double dy = targetY-data.getPosY();//data[1];
			
			kex.setSpeed(Math.max(-data.getDepth()*3,3));
			
			//target reached -> choose new target
			if(Math.sqrt(dx*dx + dy*dy) < 3) {
				index--;
				if(index < 0)
				{
					stop = true;
					break;
				}
				oldTargetX = targetX;
				oldTargetY = targetY;
				
				targetX = path.get(index).xCoord;
				targetY = path.get(index).yCoord;
				xte.setWaypoint(oldTargetX, oldTargetY,targetX, targetY);
			}
			sleep(dt);
		}
		
		kex.setSpeed(0);
		
	}

	@Override
	void stop() {
		stop = true;
		kex.setSpeed(0);
	}

	@Override
	boolean followingLand() {
		return false;
	}

	@Override
	boolean isDone() {
		return stop;
	}
	
	private void sleep(long delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<Kex.searchElement> Astar(Kex.searchElement startElement, Kex.searchElement stopElement ) {
		
		int maxX = region.nx;
		int maxY = region.ny;
		
		ArrayList<Kex.searchElement> closedSet = new ArrayList<Kex.searchElement>();
		ArrayList<Kex.searchElement> openSet = new ArrayList<Kex.searchElement>();
		
		Kex.searchElement[][] came_from = new Kex.searchElement[maxX][maxY]; 
		double[][] g_score = new double[maxX][maxY];
		double[][] f_score = new double[maxX][maxY];
		
		
		openSet.add(startElement);
		
		//System.out.println("Nodes created and neighbours added");
		
		g_score[startX][startY] = 0;
		f_score[startX][startY] = heuristic_cost_estimate(startElement, stopElement);
		
		while(!openSet.isEmpty()) {
			//find node with lowest f_score value
			
			//System.out.println("A* openSet: " + openSet.size() + "\t closedSet: " + closedSet.size());
			
			double min = Double.MAX_VALUE;
			Kex.searchElement current =  null;
			for(Kex.searchElement n:openSet) {
				if(f_score[n.x][n.y] <= min || n.status != 1) {
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
			for(Kex.searchElement n:current.neighbour) {
				if(closedSet.contains(n) || n.status != 1)
					continue;
				
				double dx = n.xCoord-current.x;
				double dy = n.yCoord-current.y;
				double tentative_g_score = g_score[current.x][current.y] + Math.sqrt(dx*dx + dy*dy);
				
				if (!openSet.contains(n) || tentative_g_score < g_score[n.x][n.y]) {
					came_from[n.x][n.y] = current;
					f_score[n.x][n.y] = tentative_g_score + (1.0/((n.accumulatedDepth/n.timesVisited)*(n.accumulatedDepth/n.timesVisited))) * heuristic_cost_estimate(n, stopElement);
					if(!openSet.contains(n)) {
							openSet.add(n);
						}
					}
			}
			
		}
		System.out.println("No path found");
		return null;
	}
	
	private double heuristic_cost_estimate(Kex.searchElement one, Kex.searchElement two) {
		double dx = one.xCoord - two.xCoord;
		double dy = one.yCoord - two.yCoord;;
		
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	ArrayList<Kex.searchElement> reconstructPath(Kex.searchElement start, Kex.searchElement current, Kex.searchElement[][] came_from) { 
		ArrayList<Kex.searchElement> path = new ArrayList<Kex.searchElement>();
		
		Kex.searchElement temp = current;
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
