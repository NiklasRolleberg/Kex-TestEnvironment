package controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
	
	// Matrix containing all Seachelements
	SearchElement[][] elementMatrix;
	
	//number of elements and size
	int nx;
    int ny;
    double dx;
    double dy;
    double xMax, yMax, xMin, yMin; 
	
	
    int currentCellIndex;
	int[] endPos;
	
	double delta;
	long dt;

    DrawMatrix draw;
    
    SearchPattern sp;
    
    //for data log
	int cellsInPolygon = 0;
	int visitedCells = 0;
	double distance = 0;
	long startTime = 0;
	
	ArrayList<Double> cellData;
	ArrayList<Double> distData;
	ArrayList<Double> timeData;
	
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

        // (1) Create matrix + populate matrix + addneighbours + set status(0, not accessible)  
        
        // _1_calculate number of elements needed and dx,dy
        SearchCell temp = new SearchCell(polygonX,polygonY);
        double resolution = 1.0/delta;
        nx = (int)((Math.round(temp.maxX())-Math.round(temp.minX()))*resolution);
        ny = (int)((Math.round(temp.maxY())-Math.round(temp.minY()))*resolution);

        dx = (temp.maxX() - temp.minX())/nx;
        dy = (temp.maxY() - temp.minY())/ny;
        
        xMax = temp.maxX();
        yMax = temp.maxY();
        xMin = temp.minX();
        yMin = temp.minY();

        System.out.println("max x: " + xMax + ", min x: " + xMin + ", nx: " + nx + ", dx: " + dx);
		System.out.println("max y: " + yMax + ", min y: " + yMin + ", ny: " + ny + ", dy: " + dy);

        
        // _2_create and populate matrix and set status
		//initialize the 2D-array. Maybe make sure not to include oob cells at all to save memory?
		elementMatrix = new SearchElement[nx][ny];
        double xLeft, xRight;
        double xCoord = xMin;
        double yCoord = yMin;
        for (int iy = 0; iy < ny; iy++) {
            for (int ix = 0; ix < nx; ix++) {
                xLeft = temp.findX(yCoord, false);
                xRight = temp.findX(yCoord, true);
                if (xCoord <= xLeft || xCoord >= xRight) {
                    elementMatrix[ix][iy] = new SearchElement(xCoord, yCoord, 99);   //oob
                    elementMatrix[ix][iy].x = ix;
                    elementMatrix[ix][iy].y = iy;
                } else {
                    elementMatrix[ix][iy] = new SearchElement(xCoord, yCoord, 0); //in bounds
                    elementMatrix[ix][iy].x = ix;
                    elementMatrix[ix][iy].y = iy;
                    cellsInPolygon++;
                }
                xCoord += dx;
            }
            yCoord += dy;
            xCoord = xMin;
        }
        System.out.println("Matrix created" );
        
        // _3_add neighbours
        addneighbours();
        System.out.println("Neighbours added");
        
        
        //Start draw
        draw = new DrawMatrix();
        
        
        //TODO split polygon + store convex polygons
        cellList = new ArrayList<SearchCell>();
        cellList.add(temp);
        
        // TODO fixa för resultat
        //cellData = new ArrayList<Double>();
    	//distData = new ArrayList<Double>();
    	//timeData = new ArrayList<Double>();


	}
    /**adds some extra polygons to the list, for test purposes*/
    private void addTestPolygons(SearchCell initCell){

        //failed attempt at splitting the area into 2 along the y-axis
        /*ArrayList<Double> xTop, yTop, xBot, yBot;
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
        */

        //hardcoded cell for testing
        /*
        ArrayList<Double> xHard, yHard;
        xHard = new ArrayList<Double>();
        yHard = new ArrayList<Double>();
        xHard.add(430.3);
        xHard.add(330.0);
        xHard.add(230.3);
        xHard.add(300.1);

        yHard.add(230.3);
        yHard.add(330.0);
        yHard.add(330.3);
        yHard.add(200.3);

        SearchCell testCell = new SearchCell(xHard,yHard);
        cellList.add(testCell);
        */
    	/*
        ArrayList<Double> testX = new ArrayList<Double>();
        ArrayList<Double> testY = new ArrayList<Double>();
        double midX = initCell.xMin+(initCell.xMax-initCell.xMin)/2;
//        double midX = (initCell.xMax-initCell.xMin)/2;
        System.out.println("xmid = " + midX);
        double midY = initCell.yMin+(initCell.yMax-initCell.yMin)/2;
//        double midY = (initCell.yMax-initCell.yMin)/2;
	
        System.out.println("ymid = " + midY);
        double d = 50;
        testX.add(midX-d);
        testY.add(midY-d);

        testX.add(midX+d);
        testY.add(midY-d);

        testX.add(midX+d);
        testY.add(midY+d);

        testX.add(midX-d);
        testY.add(midY+d);

        cellList.add(new SearchCell(testX,testY));
	
        System.out.println("cellList size: " + cellList.size());
	*/

    }

    /**Updates cellmatrix depth data*/
    public void updateDepthValue(double[] data){
    	
    	double xCoord = data[0];
    	double yCoord = data[1];
    	double heading = data[2];
    	double depthValue = data[4]; 
    	double rightSonar = data[5]; 
    	double leftSonar = data[6];
    	
    	// TODO fixa
        int ix = (int)Math.round((xCoord - xMin) / dx);
        int iy = (int)Math.round((yCoord - yMin) / dy);

        //index out of bounds fix
        if (ix >= nx){
            ix = nx-1;
        }
        if (iy >= ny){
            iy = ny-1;
        }
        
        elementMatrix[ix][iy].updateDepthData(depthValue);
      
        if (sp.followingLand()) {
            int maxIndexX = nx;
            int maxIndexY = ny;
            
        	int[] indexX = {ix   , ix+1, ix+1 ,ix+1 ,ix   ,ix-1 ,ix-1 ,ix-1};
        	int[] indexY = {iy-1 , iy-1, iy   ,iy+1 ,iy+1 ,iy+1 ,iy   ,iy-1};
        	for(int k = 0; k < 8;k++) {
        		int i = indexX[k];
        		int j = indexY[k];
        		if(i >= 0 && i < maxIndexX && j >= 0 && j < maxIndexY)
            	{
            		if(elementMatrix[i][j].status != 1){
            			elementMatrix[i][j].status = 2;
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
	

	private void addneighbours() {
		
		//add neighbors 
		for(int i=0;i<nx;i++) {
			for(int j=0;j<ny;j++) {
				
				elementMatrix[i][j].x = i;
				elementMatrix[i][j].y = j;
				
				int[] indexX = {i   , i+1, i+1 ,i+1 ,i   ,i-1 ,i-1 ,i-1};
	        	int[] indexY = {j-1 , j-1, j   ,j+1 ,j+1 ,j+1 ,j   ,j-1};
	        	
				//int[] indexX = {i   , i+1 , i   ,i-1};
	        	//int[] indexY = {j-1 , j   , j+1 ,j  };

				
				for(int k = 0; k < 8;k++) { //8
	        		int ii = indexX[k];
	        		int jj = indexY[k];
	        		if(ii >= 0 && ii < nx && jj >= 0 && jj < ny)
	            	{
	        			elementMatrix[i][j].neighbour.add(elementMatrix[ii][jj]);
	            	}
	        	}
			}
		}		
	}
	
	private ArrayList<SearchElement> newCell(SearchElement first) {
		ArrayList<SearchElement> list = new ArrayList<SearchElement>();
		list.add(first);
		
		int i = 0;
		int n = list.size();
		
		while (i<n) {
			for(SearchElement s: list.get(i).neighbour) {
				if(list.contains(s) || s.status != 0)
					continue;
				list.add(s);
			}
			
			n = list.size();
			//System.out.println(n);
			i++;
		}
		return list;
	}
	
	@Override
	public void run() {
		
		
		//find current element
        //Kex.searchElement goal = cellList.get(0).elementMatrix[ix][iy];
		
		//GoToPoint g = new GoToPoint(this, cellList.get(0), this.delta, this.dt);
		//g.GO(5, 5, cellList.get(0).nx -5, cellList.get(0).ny-5);
		//sp = g;
		
        //Run the search pattern on the polygons
        sp = new SweepingPattern(this, cellList.get(0), this.delta, this.dt);
        //sp = new CircularPattern(this, cellList.get(0), this.delta, this.dt);
        Thread myThread = new Thread(sp);
        myThread.start();
        
        startTime = System.currentTimeMillis();
        
        double[] sensorData = boat.getSensordata();
        double lastX = sensorData[0];
        double lastY = sensorData[1];
        
		//int goalx = (int)Math.round((sensorData[0] - cellList.get(0).xMin) / cellList.get(0).dx);
        //int goaly = (int)Math.round((sensorData[1] - cellList.get(0).yMin) / cellList.get(0).dy);

        
        while(true){
            sensorData = boat.getSensordata();
            //calc distance
            double dx = lastX-sensorData[0];
            double dy = lastY-sensorData[1];
            lastX = sensorData[0];
            lastY = sensorData[1];
            distance += Math.sqrt(dx*dx + dy*dy);
            double time = ((double)(System.currentTimeMillis() - startTime))/1000.0;
            
            //false -> no data stored
            if(false) {
	            distData.add(distance);
	            timeData.add(time);
	            cellData.add((100*((double)visitedCells / (double)cellsInPolygon)));
	            System.out.println("DATA dist: " + distance + "\t Visited %: " + (int)(100*((double)visitedCells / (double)cellsInPolygon)) + "\t time: " + time);
	            
	            if(sp.isDone() || time > 700) {
	            	System.out.println("printing to file");
	            	printToFile();
	            	sp.stop();
	            	break;
	            }
            }
             
            updateDepthValue(sensorData);
            draw.repaint();
            
            if(sp.isDone()) {
            	sp.stop();
            	break;
            }
            
            try {
                Thread.sleep((long)(Math.max(500-boat.getSensordata()[3]*10, 100)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*
        System.out.println("pattern done. return to start pos");
		int startx = (int)Math.round((sensorData[0] - cellList.get(0).xMin) / cellList.get(0).dx);
        int starty = (int)Math.round((sensorData[1] - cellList.get(0).yMin) / cellList.get(0).dy);
        
        GoToPoint g = new GoToPoint(this, cellList.get(0), this.delta, this.dt);
        g.GO(startx, starty, goalx, goaly);
        */
	}
	
	/**
	 * print data to file
	 */
	private void printToFile() {
		String fileName = "coverageData.csv";
		StringBuilder l1 = new StringBuilder();
		StringBuilder l2 = new StringBuilder();
		StringBuilder l3 = new StringBuilder();
		
		for(int i=0;i<distData.size()-1;i++) {
			l1.append(cellData.get(i));
			l1.append(" ,");
			
			l2.append(timeData.get(i));
			l2.append(" ,");
			
			l3.append(distData.get(i));
			l3.append(" ,");
		}
		
		l1.append(cellData.get(distData.size()-1));
		l2.append(timeData.get(distData.size()-1));
		l3.append(distData.get(distData.size()-1));
		
		//write l1,l2,l3 to file
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.println(l1.toString());
			writer.println(l2.toString());
			writer.println(l3.toString());
			writer.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
		
	private class DrawMatrix extends JPanel {
		JFrame myFrame;

		public DrawMatrix(){
			myFrame = new JFrame();
            myFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            int[] dim = correctCoords((int)Math.round(xMax*1.2),(int)Math.round(yMax*1.2));
            myFrame.setPreferredSize(new Dimension(dim[0],dim[1]));
            myFrame.add(this);
            myFrame.pack();
            myFrame.setVisible(true);
			this.repaint();
		}

		
		@Override
		public void paint(Graphics g){
            //draw a background
            drawBackground(g);
         
            //draw elements
            for(int i=0;i<nx;i++) {
            	for(int j=0;j<ny;j++) {
            		SearchElement e = elementMatrix[i][j];
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
            	}
            }
            drawEdges(g);
        }

		/**
		 * clear background
		 * @param g
		 * graphics
		 */
        private void drawBackground(Graphics g){
            g.setColor(new Color(211, 211, 211));
            int[] c0 = correctCoords((int)Math.round(xMin),(int)Math.round(yMin));
            int[] c1 = correctCoords((int)Math.round(xMax)*2, (int) Math.round(yMax)*2);
            g.fillRect(c0[0],c0[1],c1[0],c1[1]);
        }

        /**
         * Draw a rectangle representing a seachelement
         * @param g
         * graphics
         * @param e
         * SearchElement
         */
        private void drawElements(Graphics g, SearchElement e) {
            int[] coords = correctCoords((int)e.xCoord,(int)e.yCoord);
            g.fillRect(coords[0]-(int)Math.round(dx/2),coords[1]-(int)Math.round(dx/2),(int) dx+1, (int) dy+1);
            
            //vertical lines
            g.setColor(Color.gray);
            int[] vCoords = correctCoords((int)Math.round(e.xCoord), (int) Math.round(xMax));
            g.drawLine(coords[0]-(int)Math.round(dx/2), coords[1]-(int)Math.round(dx/2), vCoords[0]-(int)Math.round(dx/2),vCoords[1]-(int)Math.round(dy/2));
            
            //horizontal lines
            int[] hCoords = correctCoords((int)Math.round(xMax), (int)Math.round(e.yCoord));
            g.drawLine(coords[0]-(int)Math.round(dx/2), coords[1]-(int)Math.round(dx/2), hCoords[0]-(int)Math.round(dx/2),hCoords[1]-(int)Math.round(dy/2));
        }

        /**
         * Draw polygon edges
         * @param g
         * graphics
         */
        private void drawEdges(Graphics g){           
        	g.setColor(Color.red);
        	for(SearchCell cell : cellList) {
        		for(int i=0;i<cell.xpos.size()-1;i++) {
        			int x0 = cell.xpos.get(i).intValue();
                    int y0 = cell.ypos.get(i).intValue();
                    int[] p1 = correctCoords(x0,y0);
                    int x1 = cell.xpos.get(i+1).intValue();
                    int y1 = cell.ypos.get(i+1).intValue();
                    int[] p2 = correctCoords(x1,y1);
                    g.drawLine(p1[0],p1[1],p2[0],p2[1]);
        		}
        		int[] p1 = correctCoords(cell.xpos.get(cell.xpos.size()-1).intValue() , cell.ypos.get(cell.xpos.size()-1).intValue());
                int[] p2 = correctCoords(cell.xpos.get(0).intValue(), cell.ypos.get(0).intValue());
                g.drawLine(p1[0],p1[1],p2[0],p2[1]);
        	}	
        }
        
        
        private Color getColor(double depth) {
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
            return new int[] {x - (int) xMin, y - (int) yMin};
        }
    }

}
