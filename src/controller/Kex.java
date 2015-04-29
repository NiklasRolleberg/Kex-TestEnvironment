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
    
    SearchPattern sp;
	
	
	
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
        SearchCell entireArea = new SearchCell(x, y);
        cellList = new ArrayList<SearchCell>();
        cellList.add(entireArea);
        ySplitRead(entireArea);
        draw = new drawMatrix(cellList);



	}
    /**Splits the initial searh area in the middle of the y-axis and creates 2 search cells*/
    private void ySplitRead(SearchCell initCell){
        ArrayList<Double> xTop, yTop, xBot, yBot;
        xTop = new ArrayList<Double>();
        yTop = new ArrayList<Double>();
        xBot = new ArrayList<Double>();
        yBot = new ArrayList<Double>();


        double yMid = initCell.yMin+(initCell.yMax-initCell.yMin)/2;
        System.out.println("yMid = " + yMid);
        int nNodes = initCell.xpos.size();
        for (int i = 0; i<nNodes; i++){
            double yValue = initCell.ypos.get(i);
            double xValue = initCell.xpos.get(i);
            if (yValue>yMid){
                yTop.add(yValue);
                xTop.add(xValue);
            }
            else {
                yBot.add(yValue);
                xBot.add(xValue);
            }
        }

        System.out.println("initX = " + initCell.xpos.size() + " , initY = " + initCell.ypos.size());
        System.out.println("xtop: " + xTop.size() + " xbot: " + xBot.size());
        System.out.println("ytop: " + yTop.size() + " ybot: " + yBot.size());

        SearchCell top = new SearchCell(xTop,yTop);
        SearchCell bot = new SearchCell(xBot,yBot);
        //cellList.add(top);
        //cellList.add(bot);
        System.out.println("cellList size: " + cellList.size());


    }

    /**Updates cellmatrix depth data*/
    public void updateDepthValue(double[] data){
    	
    	double xCoord = data[0];
    	double yCoord = data[1];
    	double heading = data[2];
    	double depthValue = data[4]; 
    	double rightSonar = data[5]; 
    	double leftSonar = data[6];
    	
        int ix = (int)Math.round((xCoord - cellList.get(currentCellIndex).xMin) / cellList.get(currentCellIndex).dx);
        int iy = (int)Math.round((yCoord - cellList.get(currentCellIndex).yMin) / cellList.get(currentCellIndex).dy);

        //index out of bounds fix
        if (ix >= cellList.get(currentCellIndex).nx){
            ix = cellList.get(currentCellIndex).nx-1;
        }
        if (iy >= cellList.get(currentCellIndex).ny){
            iy = cellList.get(currentCellIndex).ny-1;
        }
        cellList.get(currentCellIndex).elementMatrix[ix][iy].updateDepthData(depthValue);
        
       
        //might not be a good way of doing this
        if (sp.followingLand()) {
            int maxIndexX = cellList.get(currentCellIndex).nx;
            int maxIndexY = cellList.get(currentCellIndex).ny;
            
        	int[] indexX = {ix   , ix+1, ix+1 ,ix+1 ,ix   ,ix-1 ,ix-1 ,ix-1};
        	int[] indexY = {iy-1 , iy-1, iy   ,iy+1 ,iy+1 ,iy+1 ,iy   ,iy-1};
        	for(int k = 0; k < 8;k++) {
        		int i = indexX[k];
        		int j = indexY[k];
        		if(i >= 0 && i < maxIndexX && j >= 0 && j < maxIndexY)
            	{
            		if(cellList.get(currentCellIndex).elementMatrix[i][j].status != 1){
            			cellList.get(currentCellIndex).elementMatrix[i][j].status = 2;
            		}
            	}
        	}
        }

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
        sp = new SweepingPattern(this, cellList.get(0), this.delta, this.dt);
        //sp = new CircularPattern(this, cellList.get(0), this.delta, this.dt);
        Thread myThread = new Thread(sp);
        myThread.start();

        double[] sensorData;
        while(true){
            try {
                myThread.sleep((long)(Math.max(500-boat.getSensordata()[3]*10, 100)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //sensorData = boat.getSensordata();
            updateDepthValue(boat.getSensordata());
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
		searchElement[][] elementMatrix;
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

            System.out.println("max x: " + maxX() + ", min x: " + minX()+ ", nx: " + nx + ", dx: " + dx);
			System.out.println("max y: " + maxY() + ", min y: " + minY()+ ", ny: " + ny + ", dy: " + dy);
            //read here!
            populateElementMatrix();
            //draw = new drawMatrix(cellList);
        }


        /**Reads the whole polygon into one single searchcell, mostly for test purposes!*/
        private void populateElementMatrix() {
            //initialize the 2D-array. Maybe make sure not to include oob cells at all to save memory?
            elementMatrix = new searchElement[nx][ny];
            double xLeft, xRight;
            double xCoord = xMin;
            double yCoord = yMin;
            for (int iy = 0; iy < ny; iy++) {
                for (int ix = 0; ix < nx; ix++) {
                    xLeft = findX(yCoord, false);
                    xRight = findX(yCoord, true);
                    if (xCoord <= xLeft || xCoord >= xRight) {
                        elementMatrix[ix][iy] = new searchElement(xCoord, yCoord, 99);   //oob
                    } else {
                        elementMatrix[ix][iy] = new searchElement(xCoord, yCoord, 0); //in bounds
                    }
                    xCoord += dx;
                }
                yCoord += dy;
                xCoord = xMin;
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
		int status; // 0 = not scanned, 1 = scanned 2 = not accessible, 99 oob
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
		SearchCell currentCell;
		JFrame myFrame;

		public drawMatrix(ArrayList<SearchCell> cellList){
			myFrame = new JFrame();
            currentCell = cellList.get(0);
            myFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            int[] dim = correctCoords((int)Math.round(currentCell.xMax*1.2),(int)Math.round(currentCell.yMax*1.2));
            myFrame.setPreferredSize(new Dimension(dim[0],dim[1]));
            myFrame.add(this);
            myFrame.pack();
            myFrame.setVisible(true);
			this.repaint();
		}

		
		@Override
		public void paint(Graphics g){
            //draw a background
            g.setColor(new Color(211, 211, 211));
            int[] c0 = correctCoords((int)Math.round(currentCell.xMin),(int)Math.round(currentCell.yMin));
            int[] c1 = correctCoords((int)Math.round(currentCell.xMax)*2, (int) Math.round(currentCell.yMax)*2);
            g.fillRect(c0[0],c0[1],c1[0],c1[1]);
            //Color[] colorArray = {Color.green, Color.yellow, Color.magenta};
            //int i = 0;
            for (SearchCell currCell : cellList) {
                /*System.out.println("i = "+i);
                currentCell = cellList.get(i);
                if (i!=0){
                    drawEdges(g,colorArray[i]);
                }
                i++;*/
                for (searchElement[] ea : currCell.elementMatrix) {
                    for (searchElement e : ea) {
                        if (e.status == 0) {
                            g.setColor(Color.black);
                        } else if (e.status == 99) {
                            g.setColor(Color.gray);
                        } else if (e.status == 1) {
                            double depth = e.getRecordedDepth();
                            g.setColor(getColor(depth));
                        }else if (e.status == 2) {
                        	g.setColor(Color.red);
                        } else {
                            System.out.println("Dafuq?! Wrong status in initial cell read");
                            g.setColor(Color.pink);
                        }
                        drawElements(g, e);
                        drawEdges(g, Color.red);
                    }
                }
            }


        }
        private void drawElements(Graphics g, searchElement e){
            int[] coords = correctCoords((int)e.xCoord,(int)e.yCoord);
            g.fillRect(coords[0]-(int)Math.round(currentCell.dx/2),coords[1]-(int)Math.round(currentCell.dx/2),(int) currentCell.dx+1,(int) currentCell.dy+1);
            g.setColor(Color.gray);
            //vertical lines
            int[] vCoords = correctCoords((int)Math.round(e.xCoord), (int)Math.round(currentCell.xMax));
            g.drawLine(coords[0]-(int)Math.round(currentCell.dx/2), coords[1]-(int)Math.round(currentCell.dx/2), vCoords[0]-(int)Math.round(currentCell.dx/2),vCoords[1]-(int)Math.round(currentCell.dy/2));
            //horizontal lines
            int[] hCoords = correctCoords((int)Math.round(currentCell.xMax), (int)Math.round(e.yCoord));
            g.drawLine(coords[0]-(int)Math.round(currentCell.dx/2), coords[1]-(int)Math.round(currentCell.dx/2), hCoords[0]-(int)Math.round(currentCell.dx/2),hCoords[1]-(int)Math.round(currentCell.dy/2));

        }
        private void drawEdges(Graphics g, Color c){
            for(int i=0; i< currentCell.xpos.size()-1; i++) {
                int x0 = currentCell.xpos.get(i).intValue();
                int y0 = currentCell.ypos.get(i).intValue();

                int x1 = currentCell.xpos.get(i+1).intValue();
                int y1 = currentCell.ypos.get(i+1).intValue();
                //g.setColor(Color.RED);
                g.setColor(c);
                g.drawLine(x0-(int) currentCell.xMin, y0-(int) currentCell.yMin, x1-(int) currentCell.xMin, y1-(int) currentCell.yMin);
            }
            g.drawLine(currentCell.xpos.get(currentCell.xpos.size()-1).intValue()-(int) currentCell.xMin, (currentCell.ypos.get(currentCell.xpos.size()-1).intValue()-(int) currentCell.yMin)
                    ,currentCell.xpos.get(0).intValue()-(int) currentCell.xMin, currentCell.ypos.get(0).intValue()-(int) currentCell.yMin);

        }
        private Color getColor(double depth){
            Color color;
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
            return color;

        }
        private int[] correctCoords(int x, int y){
            //return new int[] {x,y};
            return new int[] {x - (int) currentCell.xMin, y - (int) currentCell.yMin};
        }
    }

}
