package controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
    int currentCellIndex;
	int[] endPos;
	
	double delta;
	long dt;

    drawMatrix draw;
	
	
	
	/**Main controller thingy for the boat
	 * @param delta is map resolution, not used yet
	 * */
	public Kex(Boat inBoat, ArrayList<Double> x, ArrayList<Double> y , double delta , int[] endPos, long dt ) {
		
		this.boat = inBoat;
		this.polygonX = x;
		this.polygonY = y;
		this.delta = delta;
		this.endPos = endPos;
		this.dt = dt;
        currentCellIndex = 0;

        //TODO split polygon + store convex polygons
        SearchCell sc = new SearchCell(x, y);
        cellList = new ArrayList<SearchCell>();
        cellList.add(sc);



	}

    /**Updates cellmatrix depth data*/
    public void updateDepthValue(double xCoord, double yCoord, double depthValue){
        //TODO calculate index in the matrix
        int ix = (int)Math.round((xCoord - cellList.get(currentCellIndex).xMin) / cellList.get(currentCellIndex).dx);
        int iy = (int)Math.round((yCoord - cellList.get(currentCellIndex).yMin) / cellList.get(currentCellIndex).dy);

        cellList.get(currentCellIndex).cellMatrix[ix][iy].updateDepthData(depthValue);

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
        //Run the search pattern on the polygons
        //SearchPattern sp = new SweepingPattern(this, cellList.get(0), this.delta, this.dt);
        SearchPattern sp = new CircularPattern(this, cellList.get(0), this.delta, this.dt);
        Thread myThread = new Thread(sp);
        myThread.start();

        double[] sensorData;
        while(true){
            try {
                myThread.sleep((long)(Math.max(500-boat.getSensordata()[3]*10, 100)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sensorData = boat.getSensordata();
            updateDepthValue(sensorData[0],sensorData[1],sensorData[4]);
            draw.repaint();


        }


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
        int nx;
        int ny;
        double dx;
        double dy;
		searchElement[][] cellMatrix;
        double resolution;

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
            resolution = 1.0/delta;

            nx = (int)((Math.round(xMax)-Math.round(xMin))*resolution);
            ny = (int)((Math.round(yMax)-Math.round(yMin))*resolution);

            dx = (xMax - xMin)/nx;
            dy = (yMax - yMin)/ny;

            System.out.println("max x: " + maxX() + ", min x: " + minX()+ ", nx: " + nx);
			System.out.println("max y: " + maxY() + ", min y: " + minY()+ ", ny: " + ny);

            //initialize the 2D-array. Maybe make sure not to include oob cells at all to save memory?
            cellMatrix = new searchElement[nx][ny];
            double xLeft, xRight;
            boolean readCellIntoMemory;
            readCellIntoMemory = true;

            // made up status codes:
            // 99 is oob,
            // 0 is not yet scanned,
            // 1 is scanned water,
            // 2 is land/unreachable,
            // more?! unreachable?

            double xCoord = xMin;
            double yCoord = yMin;
            //noinspection ConstantConditions
            if (readCellIntoMemory){
                for(int iy = 0; iy<ny; iy++){
                    for (int ix = 0; ix<nx; ix++){
                        xLeft=findX(yCoord, false);
                        xRight=findX(yCoord, true);
                        if (xCoord<=xLeft || xCoord>=xRight){
                            cellMatrix[ix][iy] = new searchElement(xCoord, yCoord, 99);   //oob
                        }
                        else{
                            cellMatrix[ix][iy] = new searchElement(xCoord, yCoord, 0); //in bounds
                        }
                        xCoord += dx;

                    }
                    yCoord += dy;
                    xCoord = xMin;
                }
            //draw the cell

            draw = new drawMatrix(this);
            }

			System.out.println("----------Cell read done!----------");
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
	
	/**Smallest element of the map, x and y coords, status and depth data*/
	private class searchElement{
		double xCoord;
		double yCoord;
		int status;
        private double accumulatedDepth;
        int timesVisited;

		public searchElement(double x, double y, int s){
			xCoord = x;
			yCoord = y;
			status = s;
            timesVisited = 0;
		}
        private void updateDepthData(double inDepth){
            if (timesVisited == 0){
                accumulatedDepth = inDepth;
                timesVisited++;
            }
            else {
                timesVisited++;
                accumulatedDepth = (accumulatedDepth + inDepth);
            }
            status = 1;
        }
        private double getRecordedDepth(){
            return accumulatedDepth /timesVisited;
        }
	}
	
	private class drawMatrix extends JPanel{
		SearchCell myCell;
		JFrame myFrame;
        int xScale, yScale;

		public drawMatrix(SearchCell inCell){
			myFrame = new JFrame();
            myCell = inCell;
            myFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            myFrame.setPreferredSize(new Dimension(500, 500));
            myFrame.add(this);
            myFrame.pack();
            myFrame.setVisible(true);

			this.repaint();
			
		}
		
		@Override
		public void paint(Graphics g){
            int[] coords;
            for (searchElement[] ea : myCell.cellMatrix){
                for (searchElement e : ea){
                    if (e.status ==  0){
                        g.setColor(Color.black);
                    }
                    else if (e.status == 99){
                        g.setColor(Color.gray);
                    }
                    else if (e.status == 1){
                        double depth = e.getRecordedDepth();
                        Color color = Color.BLUE;
                        if(depth < -21) {
                            color = new Color(0x050068);
                        }
                        else if(depth < -17)	{
                            color = new Color(0x1208D7);
                        }
                        else if(depth < -15)	{
                            color = new Color(0x0D00FF);
                        }
                        else if(depth < -12)	{
                            color = new Color(0x035EC7);
                        }
                        else if(depth < -9)	{
                            color = new Color(0x0066DA);
                        }
                        else if(depth < -6)	{
                            color = new Color(0x1CA6D9);
                        }
                        else if(depth < -3)	{
                            color = new Color(0x43B3DC);
                        }
                        else if(depth < 0.001)	{
                            color = new Color(0x0AF4FF);
                        }
                        else{
                            color = Color.GREEN;
                        }
                        g.setColor(color);
                    }
                    else{
                        System.out.println("Dafuq?! Wrong status in initial cell read");
                        g.setColor(Color.red);
                    }
                    coords = correctCoords((int)e.xCoord,(int)e.yCoord);
                    g.fillRect(coords[0]-(int)Math.round(myCell.dx/2),coords[1]-(int)Math.round(myCell.dx/2),(int)myCell.dx+1,(int)myCell.dy+1);
                    g.setColor(Color.gray);
                    //vertical lines
                    int[] vCoords = correctCoords((int)Math.round(e.xCoord), (int)Math.round(myCell.xMax));
                    g.drawLine(coords[0]-(int)Math.round(myCell.dx/2), coords[1]-(int)Math.round(myCell.dx/2), vCoords[0]-(int)Math.round(myCell.dx/2),vCoords[1]-(int)Math.round(myCell.dy/2));
                    //horizontal lines
                    int[] hCoords = correctCoords((int)Math.round(myCell.xMax), (int)Math.round(e.yCoord));
                    g.drawLine(coords[0]-(int)Math.round(myCell.dx/2), coords[1]-(int)Math.round(myCell.dx/2), hCoords[0]-(int)Math.round(myCell.dx/2),hCoords[1]-(int)Math.round(myCell.dy/2));




                }
            }

			//draw polygon edges
			for(int i=0; i<myCell.xpos.size()-1; i++) {
				int x0 = polygonX.get(i).intValue();
				int y0 = polygonY.get(i).intValue();
				
				int x1 = polygonX.get(i+1).intValue();
				int y1 = polygonY.get(i+1).intValue();
				g.setColor(Color.RED);
				g.drawLine(x0-(int)myCell.xMin, y0-(int)myCell.yMin, x1-(int)myCell.xMin, y1-(int)myCell.yMin);
			}
			g.drawLine(polygonX.get(polygonX.size()-1).intValue()-(int)myCell.xMin, (polygonY.get(polygonX.size()-1).intValue()-(int)myCell.yMin)
									,polygonX.get(0).intValue()-(int)myCell.xMin, polygonY.get(0).intValue()-(int)myCell.yMin);

        }
        private int[] correctCoords(int x, int y){
            //return new int[] {x,y};
            return new int[] {x - (int)myCell.xMin, y - (int)myCell.yMin};
        }
	}
	
}
