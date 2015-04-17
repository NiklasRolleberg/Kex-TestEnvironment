package controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import kex2015.Boat;


/**extend this class to create a new algorithm*/
public class Kex implements Runnable{
	
	Boat boat;
	
	ArrayList<Double> polygonX;
	ArrayList<Double> polygonY;
	ArrayList<SearchCell> cellList;		//list of all the cells
	int[] endPos;
	
	double delta;
	long dt;
	
	
	
	/**Main brain! =)
	 * @param delta is map resolution, not used yet
	 * */
	public Kex(Boat inBoat, ArrayList<Double> x, ArrayList<Double> y , double delta , int[] endPos, long dt ) {
		
		this.boat = inBoat;
		this.polygonX = x;
		this.polygonY = y;
		this.delta = delta;
		this.endPos = endPos;
		this.dt = dt;
		
		SearchCell sc = new SearchCell(x, y);
		cellList = new ArrayList<SearchCell>();
		cellList.add(sc);
		
		
		//split polygon + store convex polygons
		
	}
	
	/**Boat sensordata*/
	public double[] getData() {
		return boat.getSensordata();
	}
	
	/**Set targetspeed for boat*/
	public void setSpeed(double speed) {
		boat.setTargetSpeed(speed);
	}
	
	/**set waypoint for boat*/
	public void setWaypoint(double x, double y) {
		boat.setWayPoint(x, y);
	}

	@Override
	public void run() {
		SearchPattern sp = new SweepingPattern(this, cellList.get(0), this.delta, this.dt);
		Thread myThread = new Thread(sp);
		myThread.start();
		
		//Run the search pattern on the polygons
		
	}
	
	
	/**Returns true if all cells are complete, false otherwise*/
	public boolean getScanStatus(){
		for (SearchCell sc : cellList){
			if (!sc.isComplete){
				return false;
			}
		}
		return true;
	}
	
	public class SearchCell{
		public boolean isComplete;
		ArrayList<Double> xpos;
		ArrayList<Double> ypos;
		double xMax, yMax, xMin, yMin;
		searchElement[][] cellMatrix;
		
		/**Constructor 
		 * @param xpos x-positions for the polygon
		 * @param ypos y-positions for the polygon
		 * */
		public SearchCell (ArrayList<Double> xpos, ArrayList<Double> ypos){
			isComplete = false;
			this.xpos = xpos;
			this.ypos = ypos;
			xMax = maxX();
			yMax = maxY();
			xMin = minX();
			yMin = minY();
			
			int dx = (int)(Math.round(maxX())-Math.round(minX()));
			int dy = (int)(Math.round(maxY())-Math.round(minY()));
			
			System.out.println("max x:" + maxX() + " min x:" + minX()+ " dx: " + dx);
			System.out.println("max y:" + maxY() + " min y:" + minY()+ " dy: " + dy);
			System.out.println("dy: " + dy);
			
			

			//idea: find out max and min x and y, divide into a suitable amount and initialize an array
			//make up some status codes: 99 is oob, 0 is not yet scanned, 1 is scanned water, 2 is land/unreachable, more?!
			//mark the ones that are out of bounds: 
			//visualise the results! (not in here obviously)
			cellMatrix = new searchElement[dx][dy];
			//index for the array
			int ix = 0; 
			int iy = 0;
			double xLeft, xRight;
			boolean readCellIntoMemory;
			readCellIntoMemory = true;
			if (readCellIntoMemory){
				for (int y=(int)Math.round(yMin); y<(int)Math.round(yMax); y++){
					for (int x=(int)Math.round(xMin); x <(int)Math.round(xMax);x++){
						xLeft = findX(y, false);
						xRight = findX(y, true);
						if (x < (int)Math.round(xLeft) || x> (int)Math.round(xRight)){
							cellMatrix[ix][iy] = new searchElement(x, y, 99);
						}
						else{
							cellMatrix[ix][iy] = new searchElement(x, y, 0);
						}
						ix++;
					}
					iy++;
					ix = 0;
				}
				
			}
			drawMatrix testDraw = new drawMatrix(this);
			//System.out.println("----------cell read done!");

			//check ALL the elements!
			/*for (searchElement[] ea : cellMatrix){
				for (searchElement e : ea){
					if (e.status ==  99){
						System.out.println("wat");
					}
				}
			}
			System.out.println("check done!");*/
			
			
		}
		
		
		public double findX(double y, boolean right) {
			int[] l1 = {-1,-1};
			int[] l2 = {-1,-1};
			
			int l = ypos.size();
			for(int i=0; i < l; i++) {
				
				if(((ypos.get((i+1)%l) >= y) && (ypos.get(i) <= y))
				|| ((ypos.get((i+1)%l) <= y) && (ypos.get(i) >= y))) {
					//interpolate
					if(l1[0] == -1) {
						l1[0] = (i+1)%l;
						l1[1] = i%l;
						//System.out.println("L1 set" + "\t (" + xpos.get(l1[0]) + " , " + ypos.get(l1[0]) + ") -> ("
						//									 + xpos.get(l1[1]) + " , " + ypos.get(l1[1]) + ")");
					}else {
						l2[0] = (i+1)%l;
						l2[1] = i%l;
						//System.out.println("L2 set" + "\t (" + xpos.get(l2[0]) + " , " + ypos.get(l2[0]) + ") -> ("
						//		 + xpos.get(l2[1]) + " , " + xpos.get(l2[1]) + ")");
					}
				}
			}
			
			/*error return some default value*/
			if(l1[0] == -1 || l1[1] == -1 || l2[0] == -1 || l2[1] == -1) {
				double meanX = 0;
				for(int i=0;i<xpos.size();i++) {
					meanX += xpos.get(i);
				}
				meanX /= xpos.size();
				return meanX;
			}
				
			
			//interpolate x
			
			double x0 = xpos.get(l1[0]);
			double y0 = ypos.get(l1[0]);
			
			double x1 = xpos.get(l1[1]);
			double y1 = ypos.get(l1[1]);
			
			//how far is y on the line
			double p = (y-y0) / (y1-y0);
			double l1X = (1-p)*x0 + p*x1;
			
			//System.out.println("p1=" + p);
			
			x0 = xpos.get(l2[0]);
			y0 = ypos.get(l2[0]);
			
			x1 = xpos.get(l2[1]);
			y1 = ypos.get(l2[1]);
			
			//how far is y on the line
			p = (y-y0) / (y1-y0);
			double l2X = (1-p)*x0 + p*x1;
			//System.out.println("p2=" + p);
			
			if(right) {
				return Math.max(l1X, l2X);
			}
			return Math.min(l1X, l2X);
		}
		
		public double maxX(){
			Double temp = Double.MIN_VALUE;
			for (Double value : xpos){
				if (value > temp)
					temp = value;
			}
			return temp;
		}
		
		public double maxY(){
			Double temp = Double.MIN_VALUE;
			for (Double value : ypos){
				if (value > temp)
					temp = value;
			}
			return temp;
		}
		
		public double minX(){

			Double temp = Double.MAX_VALUE;
			for (Double value : xpos){
				if (value < temp)
					temp = value;
			}
			return temp;
		}
		
		public double minY(){

			Double temp = Double.MAX_VALUE;
			for (Double value : ypos){
				if (value < temp)
					temp = value;
			}
			return temp;
		}
		
	}
	
	/**Smallest element of the map, x and y coords and a status*/
	private class searchElement{
		double xCoord;
		double yCoord;
		int status;
		public searchElement(double x, double y, int s){
			xCoord = x;
			yCoord = y;
			status = s;
		}
	}
	
	private class drawMatrix extends JPanel{
		SearchCell myCell;
		JFrame myFrame;
		
		public drawMatrix(SearchCell inCell){
			myFrame = new JFrame();
			myFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			myFrame.setPreferredSize(new Dimension(500, 500));
			myFrame.add(this);
			myFrame.pack();
			myFrame.setVisible(true);
			myCell = inCell;
			this.repaint();
			
		}
		
		@Override
		public void paint(Graphics g){
		
			Graphics2D g2d = (Graphics2D) g;
			//draw elements 
			for (searchElement[] ea : myCell.cellMatrix){
				for (searchElement e : ea){
					
					if (e.status ==  99){
						System.out.println("wat");
					}
				}
			}
			
			//draw polygon
			for(int i=0; i<myCell.xpos.size()-1; i++) {
				int x0 = polygonX.get(i).intValue();
				int y0 = polygonY.get(i).intValue();
				
				int x1 = polygonX.get(i+1).intValue();
				int y1 = polygonY.get(i+1).intValue();
				g.setColor(Color.RED);
				g.drawLine(x0, y0, x1, y1);
			}
			g.drawLine(polygonX.get(polygonX.size()-1).intValue(), (polygonY.get(polygonX.size()-1).intValue())
									,polygonX.get(0).intValue(), polygonY.get(0).intValue());
			
			
			
		}
	}
	
}
