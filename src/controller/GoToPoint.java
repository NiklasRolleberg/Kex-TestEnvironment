package controller;

import java.util.LinkedList;

public class GoToPoint implements Runnable{

	public GoToPoint() {
		int limit = 12;
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
		for(int i=0;i<12;i++) {
			for(int j=0;j<12;j++) {
				System.out.print(array[i][j] + " ");
			}
			System.out.println("");
		}
		
		byte[][] sol = new byte[limit][limit];
		
		System.out.println("Map");
		for(int i=0;i<12;i++) {
			for(int j=0;j<12;j++) {
				sol[i][j] = array[i][j];
			}
		}
		
		//Pathfinding
		
		//BFS
		LinkedList<Node> queue = new LinkedList<Node>();
		
		int startX, startY, goalX, goalY;
		startX = 0;
		startY = 0;
		
		goalX = 10;
		goalY = 10;
		
		LinkedList<Integer> px = new LinkedList<Integer>();
		px.add(startX);
		LinkedList<Integer> py = new LinkedList<Integer>();
		py.add(startY);
		
		Node currentNode = new Node(startX,startY,px,py,0);
		
		queue.add(currentNode);
		
		while(!queue.isEmpty()) {
			
			System.out.println("Queue size: " + queue.size());
			
			currentNode = queue.pop();
			int x = currentNode.x;
			int y = currentNode.y;
			
			System.out.println("x = " + x  + "\t y = " + y);
			
			
			//goal?
			if(x == goalX && y == goalY) {
				System.out.println("Goal reached");
				queue.clear();
				break;
			}
			
			
			//expand node;			
			int newX;
			int newY;
			
			//x+1,y
			newX = x+1;
			newY = y;
			if((newX < limit) && (newX >= 0) && (newY < limit) && (newY >= 0) && (array[newX][newY] != 0)) {
				LinkedList<Integer> newPathX = currentNode.pathX;
				LinkedList<Integer> newPathY = currentNode.pathY;
				newPathX.add(newX);
				newPathY.add(newY);
				double newDistance = currentNode.distance + 1 * array[newX][newY];
				queue.add(new Node(newX, newY , newPathX, newPathY, newDistance));
				array[newX][newY] = 0;
			}
			
			//x+1,y-1
			newX = x+1;
			newY = y-1;
			if((newX < limit) && (newX >= 0) && (newY < limit) && (newY >= 0) && (array[newX][newY] != 0)) {
				LinkedList<Integer> newPathX = currentNode.pathX;
				LinkedList<Integer> newPathY = currentNode.pathY;
				newPathX.add(newX);
				newPathY.add(newY);
				double newDistance = currentNode.distance + 1.41 * array[newX][newY];
				queue.add(new Node(newX, newY , newPathX, newPathY, newDistance));
				array[newX][newY] = 0;
			}
			
			//x,y-1
			newX = x;
			newY = y-1;
			if((newX < limit) && (newX >= 0) && (newY < limit) && (newY >= 0) && (array[newX][newY] != 0)) {
				LinkedList<Integer> newPathX = currentNode.pathX;
				LinkedList<Integer> newPathY = currentNode.pathY;
				newPathX.add(newX);
				newPathY.add(newY);
				double newDistance = currentNode.distance + 1 * array[newX][newY];
				queue.add(new Node(newX, newY , newPathX, newPathY, newDistance));
				array[newX][newY] = 0;
			}
			
			//x-1,y-1
			newX = x-1;
			newY = y-1;
			if((newX < limit) && (newX >= 0) && (newY < limit) && (newY >= 0) && (array[newX][newY] != 0)) {
				LinkedList<Integer> newPathX = currentNode.pathX;
				LinkedList<Integer> newPathY = currentNode.pathY;
				newPathX.add(newX);
				newPathY.add(newY);
				double newDistance = currentNode.distance + 1.41 * array[newX][newY];
				queue.add(new Node(newX, newY , newPathX, newPathY, newDistance));
				array[newX][newY] = 0;
			}
			
			//x-1,y
			newX = x-1;
			newY = y;
			if((newX < limit) && (newX >= 0) && (newY < limit) && (newY >= 0) && (array[newX][newY] != 0)) {
				LinkedList<Integer> newPathX = currentNode.pathX;
				LinkedList<Integer> newPathY = currentNode.pathY;
				newPathX.add(newX);
				newPathY.add(newY);
				double newDistance = currentNode.distance + 1 * array[newX][newY];
				queue.add(new Node(newX, newY , newPathX, newPathY, newDistance));
				array[newX][newY] = 0;
			}
			
			//x-1,y+1
			newX = x;
			newY = y+1;
			if((newX < limit) && (newX >= 0) && (newY < limit) && (newY >= 0) && (array[newX][newY] != 0)) {
				LinkedList<Integer> newPathX = currentNode.pathX;
				LinkedList<Integer> newPathY = currentNode.pathY;
				newPathX.add(newX);
				newPathY.add(newY);
				double newDistance = currentNode.distance + 1.41 * array[newX][newY];
				queue.add(new Node(newX, newY , newPathX, newPathY, newDistance));
				array[newX][newY] = 0;
			}
			
			//x+1,y
			newX = x+1;
			newY = y;
			if((newX < limit) && (newX >= 0) && (newY < limit) && (newY >= 0) && (array[newX][newY] != 0)) {
				LinkedList<Integer> newPathX = currentNode.pathX;
				LinkedList<Integer> newPathY = currentNode.pathY;
				newPathX.add(newX);
				newPathY.add(newY);
				double newDistance = currentNode.distance + 1 * array[newX][newY];
				queue.add(new Node(newX, newY , newPathX, newPathY, newDistance));
				array[newX][newY] = 0;
			}
			
			//x+1,y+1
			newX = x+1;
			newY = y+1;
			if((newX < limit) && (newX >= 0) && (newY < limit) && (newY >= 0) && (array[newX][newY] != 0)) {
				LinkedList<Integer> newPathX = currentNode.pathX;
				LinkedList<Integer> newPathY = currentNode.pathY;
				newPathX.add(newX);
				newPathY.add(newY);
				double newDistance = currentNode.distance + 1.41 * array[newX][newY];
				queue.add(new Node(newX, newY , newPathX, newPathY, newDistance));
				array[newX][newY] = 0;
			}
		}
		
		if(currentNode == null)
			return;
		
		
		while(!currentNode.pathX.isEmpty()) {
			int x = currentNode.pathX.pop();
			int y = currentNode.pathY.pop();
			sol[x][y] = 7;
		}
		
		System.out.println("Path");
		for(int i=0;i<limit;i++) {
			for(int j=0;j<limit;j++) {
				System.out.print(sol[i][j] + " ");
			}
			System.out.println("");
		}
	}
	
	private class Node {
		
		//index
		int x;
		int y;
		
		LinkedList<Integer> pathX;
		LinkedList<Integer> pathY;
		double distance;
		
		public Node(int indexX, int indexY, LinkedList pX, LinkedList pY, double distance) {
			this.x = indexX;
			this.y = indexY;
			pathX = pX;
			pathY = pY;
			this.distance = distance;
		}
	}
		
	
	public static void main(String[] args) {
		GoToPoint g = new GoToPoint();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
