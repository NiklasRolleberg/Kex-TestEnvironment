package controller;

import java.util.ArrayList;

public class GoToPoint implements Runnable{

	public GoToPoint() {
		int limit = 13;
		byte[][] array = { {1,1,1,1,1,1,1,1,1,1,1,1,1}, 
						   {1,1,1,1,1,1,1,1,1,2,2,2,1}, 
						   {1,1,1,1,1,1,1,2,2,0,0,2,1},
						   {1,1,1,1,1,1,2,2,0,0,0,2,1},
						   {1,1,1,1,1,2,2,0,0,0,2,2,1},
						   {1,1,1,1,2,2,0,0,0,2,2,1,1},
						   {1,1,1,2,2,0,0,0,2,2,1,1,1},
						   {1,1,2,2,0,0,0,2,2,1,1,1,1},
						   {1,2,2,0,0,0,2,2,1,1,1,1,1},
						   {1,2,0,0,0,2,2,1,1,1,1,1,1},
						   {1,2,0,0,2,2,1,1,1,1,1,1,1},
						   {1,2,2,2,2,1,1,1,1,1,1,1,1},
						   {1,1,1,1,1,1,1,1,1,1,1,1,1},
						   {1,1,1,1,1,1,1,1,1,1,1,1,1}};
		
		System.out.println("Map");
		for(int i=0;i<limit;i++) {
			for(int j=0;j<limit;j++) {
				System.out.print(array[i][j] + " ");
			}
			System.out.println("");
		}
		
		//Pathfinding
		int[] start = {1,1};
		int[] goal = {10,10};
		
		ArrayList<Node> path = Astar(array, limit, limit, start[0], start[1], goal[0], goal[1]);
		
		for(int i=0;i<path.size();i++) {
			array[path.get(i).x][path.get(i).y] = 8;
		}
		
		System.out.println("path");
		for(int i=0;i<limit;i++) {
			for(int j=0;j<limit;j++) {
				System.out.print(array[i][j] + " ");
			}
			System.out.println("");
		}
		
		
		//heuristic test
		
		System.out.println("test");
		
		//System.out.println(heuristic_cost_estimate(new Node(0,0), new Node(10,0)));
	}
	
	public ArrayList<Node> Astar(byte[][] map, int maxX, int maxY ,int startX, int startY, int stopX, int stopY) {
		ArrayList<Node> closedSet = new ArrayList<Node>();
		ArrayList<Node> openSet = new ArrayList<Node>();
		
		Node[][] came_from = new Node[maxX][maxY]; 
		double[][] g_score = new double[maxX][maxY];
		double[][] f_score = new double[maxX][maxY];
		
		//add nodes
		Node[][] temp = new Node[maxX][maxY];
		for(int i=0;i<maxX;i++) {
			for(int j=0;j<maxY;j++) {
				temp[i][j] = new Node(i,j);
			}
		}
		
		//set neighbours
		for(int i=0;i<maxX;i++) {
			for(int j=0;j<maxY;j++) {
				int[] indexX = {i   , i+1, i+1 ,i+1 ,i   ,i-1 ,i-1 ,i-1};
	        	int[] indexY = {j-1 , j-1, j   ,j+1 ,j+1 ,j+1 ,j   ,j-1};
	        	
				//int[] indexX = {i   , i+1 ,i   ,i-1 };
	        	//int[] indexY = {j-1 , j   ,j+1 ,j   };
				for(int k = 0; k < 4;k++) {
	        		int ii = indexX[k];
	        		int jj = indexY[k];
	        		if(ii >= 0 && ii < maxX && jj >= 0 && jj < maxY)
	            	{
	        			temp[i][j].neighbour.add(temp[ii][jj]);
	            	}
	        	}
			}
		}
		
		Node start = temp[startX][startY];
		Node goal = temp[stopX][stopY];
		
		openSet.add(start);
		
		//System.out.println("Nodes created and neighbours added");
		
		g_score[startX][startY] = 0;
		f_score[startX][startY] = heuristic_cost_estimate(new Node(startX,startY), new Node(stopX,stopY));
		
		while(!openSet.isEmpty()) {
			//find node with lowest f_score value
			double min = Double.MAX_VALUE;
			Node current =  null;
			for(Node n:openSet) {
				if(f_score[n.x][n.y] <= min) {
					min = f_score[n.x][n.y];
					//System.out.println(min);
					current = n;
				}
			}
			
			if(current == goal) {
				System.out.println("goal found");
				return reconstructPath(start, current, came_from);
			}
			
			openSet.remove(current);
			closedSet.add(current);
			
			//itterate through neighbours
			for(Node n:current.neighbour) {
				if(closedSet.contains(n))
					continue;
				
				double dx = n.x-current.x;
				double dy = n.y-current.y;
				double tentative_g_score = g_score[current.x][current.y] + Math.sqrt(dx*dx + dy*dy);//*map[n.x][n.y];
				
				if (!openSet.contains(n) || tentative_g_score < g_score[n.x][n.y]) {
					came_from[n.x][n.y] = current;
					f_score[n.x][n.y] = tentative_g_score + heuristic_cost_estimate(n, goal);
					if(!openSet.contains(n)) {
							openSet.add(n);
						}
					}
			}
			
		}
		System.out.println("No path found");
		return null;
	}
	
	private double heuristic_cost_estimate(Node one, Node two) {
		double dx = one.x - two.x;
		double dy = one.y - two.y;
		
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	ArrayList<Node> reconstructPath(Node start, Node current, Node[][] came_from) { 
		ArrayList<Node> path = new ArrayList<Node>();
		
		Node temp = current;
		path.add(temp);
		
		while(true) {
			temp = came_from[temp.x][temp.y];
			path.add(temp);
			
			if(temp == start)
				break;
		}
		return path;
	}
	
	private class Node {
		
		//index
		int x;
		int y;
		ArrayList<Node> neighbour;
		
		public Node(int indexX, int indexY) {
			this.x = indexX;
			this.y = indexY;
			neighbour = new ArrayList<Node>();
		}
	}
		
	
	public static void main(String[] args) throws InterruptedException {
		//Thread.sleep(10000);
		GoToPoint g = new GoToPoint();
		
		//Thread.sleep(10000000);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
