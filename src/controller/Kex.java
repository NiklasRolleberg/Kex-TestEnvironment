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
    ArrayList<SearchElement> alreadyAdded = new ArrayList<SearchElement>();
	
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
    GoToPoint gp;
    
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
        gp = new GoToPoint(this);

        /** (1) Create matrix + populate matrix + addneighbours + set status(unknown or not accessible) */ 
        
        // _1_calculate number of elements needed and dx,dy
        SearchCell temp = new SearchCell(polygonX,polygonY);
        double resolution = 1.0/delta;
        
        xMax = temp.maxX();
        yMax = temp.maxY();
        xMin = temp.minX();
        yMin = temp.minY();
        
        nx = (int)((Math.round(xMax)-Math.round(xMin))*resolution);
        ny = (int)((Math.round(yMax)-Math.round(yMin))*resolution);

        dx = (temp.maxX() - temp.minX())/nx;
        dy = (temp.maxY() - temp.minY())/ny;
        
        // TODO fundera på varför det inte funkar utan detta
        nx+=1;
        ny+=1;

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
        
        
        /**(2) Create cell from the given polygon*/
        cellList = new ArrayList<SearchCell>();
        //cellList.add(temp);
        cellList.addAll(SearchCell.trianglulatePolygon(polygonX, polygonY));
        
        cellData = new ArrayList<Double>();
    	distData = new ArrayList<Double>();
    	timeData = new ArrayList<Double>();
        //addTestPolygons(temp);
	}
    /**adds some extra polygons to the list, for test purposes*/
    private void addTestPolygons(SearchCell initCell){



        //hardcoded cell for testing

        ArrayList<Double> xHard, yHard;
        xHard = new ArrayList<Double>();
        yHard = new ArrayList<Double>();
        double xminR, xmaxR, yminR, ymaxR;
        /*
        xmaxR = 845.7142857142858;
        xminR = 835.7142857142858;
        ymaxR = 461.92546583850924;
        yminR = 431.73913043478257;
        */
        xmaxR = 925.7142857142858;
        xminR = 875.7142857142858;
        ymaxR = 562.5465838509318;
        yminR = 492.1118012422359;



        xHard.add(xminR);
        xHard.add(xmaxR);
        xHard.add(xmaxR);
        xHard.add(xminR);

        yHard.add(yminR);
        yHard.add(yminR);
        yHard.add(ymaxR);
        yHard.add(ymaxR);

        SearchCell testCell = new SearchCell(xHard,yHard);
        cellList.add(testCell);

        /*
        ArrayList<Double> testX = new ArrayList<Double>();
        ArrayList<Double> testY = new ArrayList<Double>();
        double midX = initCell.minX()+(initCell.minX()-initCell.minX())/2;
//        double midX = (initCell.xMax-initCell.xMin)/2;
        System.out.println("xmid = " + midX);
        double midY = initCell.minY()+(initCell.minY()-initCell.minY())/2;
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
        */

        //cellList.add(new SearchCell(testX,testY));
	
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


        int ix = (int)Math.round((xCoord - xMin) / dx);
        int iy = (int)Math.round((yCoord - yMin) / dy);

        //index out of bounds fix
        if (ix >= nx){
            ix = nx-1;
        }
        if (iy >= ny){
            iy = ny-1;
        }
        
        if(elementMatrix[ix][iy].status == 0)
        	visitedCells++;
        
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
            			visitedCells++;
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
	
	/**Add neighbours to all SearchElements*/
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


    private ArrayList<SearchElement> getUncoveredElments(){
        ArrayList<SearchElement> list = new ArrayList<SearchElement>();
        int count = 0;
        for (SearchElement[] seA : elementMatrix ){
            for (SearchElement se : seA){
                if (se.status == 0){
                    count++;
                    System.out.println("uncovered! " + count);
                    list.add(se);
                }
            }
        }
        return list;
    }





	/**Identify the content of a new searchcell
	 * @param first
	 * One SearchElement in the new cell
	 * @return
	 * List of all elements in that cell
	 */
	private ArrayList<SearchElement> newCell(SearchElement first) {
		ArrayList<SearchElement> list = new ArrayList<SearchElement>();
		list.add(first);
		
		int i = 0;
		int n = list.size();
		
		while (i<n) {
			for(SearchElement s : list.get(i).neighbour) {
				if(list.contains(s) || s.status != 0){
                    continue;
                }
                    alreadyAdded.add(s);
                    list.add(s);
                }
			
			n = list.size();
			//System.out.println(n);
			i++;
		}
		return list;
	}
	
	/**Scan a cell
	 * @param c
	 * cell to be scanned 
	 * param S
	 * SearchPattern to use
	 * @param b 
	 */
	private void scanCell(SearchCell c, boolean b) {
		
		if(b)
			sp = new SweepingPattern(this, c, this.delta, this.dt);
		else
			sp = new SweepingPattern(this, c, -this.delta, this.dt);
        //sp = new CircularPattern(this, c, this.delta, this.dt);
		
        Thread myThread = new Thread(sp);
        myThread.start();

		double[] sensorData = boat.getSensordata();
        double lastX = sensorData[0];
        double lastY = sensorData[1];
		
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
	}
    /**Identify new cells and add them to cellList*/
    
	private void idRegions(){
        alreadyAdded.clear();
        ArrayList<SearchElement> uncovered = getUncoveredElments();
        ArrayList<ArrayList<SearchElement>> listOfLists = new ArrayList<ArrayList<SearchElement>>();
        for (SearchElement se : uncovered){
            if (!alreadyAdded.contains(se)){
                ArrayList<SearchElement> temp = newCell(se);
                System.out.println("temp size "+ temp.size());
                if (temp.size() > 1){
                    listOfLists.add(temp);
                }
            }
        }

        System.out.println("Nr of regions left uncovered: " + listOfLists.size());
        ArrayList<Double> xRest = new ArrayList<Double>();
        ArrayList<Double> yRest = new ArrayList<Double>();

        //add extra elements to boundaries!
        //listOfLists = extendBoundaries(listOfLists);

        for (ArrayList<SearchElement> al : listOfLists){
            for (SearchElement se : al){
                xRest.add(se.xCoord);
                yRest.add(se.yCoord);
            }

            ArrayList<Double> tempListX = new ArrayList<Double>();
            ArrayList<Double> tempListY = new ArrayList<Double>();
            tempListX.addAll(xRest);
            tempListY.addAll(yRest);

            //get convex hull
            ArrayList<ArrayList<Double>> convexCell = PolygonLib.findConvexHull(tempListX,tempListY);
            //create new cell
            SearchCell newC = new SearchCell(convexCell.get(0), convexCell.get(1));

            cellList.add(newC);
            draw.repaint();

            xRest.clear();
            yRest.clear();
        }
        System.out.println("new Cell list size: " + cellList.size());
        draw.repaint();

    }

    private ArrayList<ArrayList<SearchElement>> extendBoundaries(ArrayList<ArrayList<SearchElement>> inList) {
        System.out.println("-------Boundaries!--------");
        System.out.println("inList size: " + inList.size());
        ArrayList<ArrayList<SearchElement>> neighbourList = new ArrayList<ArrayList<SearchElement>>(inList.size());
        ArrayList<SearchElement> tempList = new ArrayList<SearchElement>();

        for (int ci = 0; ci < inList.size(); ci++){
            neighbourList.add(new ArrayList<SearchElement>());
        }
        System.out.println("nList size: " + neighbourList.size());

        int cellIndex = 0;
        int newE = 1;
        for (ArrayList<SearchElement> subRegion : inList){
            System.out.println("Size of region nr " + cellIndex + " : " + subRegion.size());
            for (SearchElement se : subRegion){
                for (SearchElement seN : se.neighbour){
                    if (!neighbourList.get(cellIndex).contains(seN) && !subRegion.contains(seN) && seN.status!=99){
                        System.out.println(newE + " new elements in region " + cellIndex);
                        newE++;
                        neighbourList.get(cellIndex).add(seN);
                        if(seN.status == 42){
                        	System.err.println("INTE BRA! DEN FINNS REDAN MED");
                        	//System.exit(0)
                        	seN.status = 0;
                        }
                        else
                        	seN.status = 42;
                        draw.repaint();
                    }
                }
            }
            newE = 1;
            cellIndex++;
        }

        for (int ci = 0; ci<neighbourList.size(); ci++){
            System.out.println("Size pre: " + inList.get(ci).size());
            System.out.println("new shit: " + neighbourList.get(ci).size());
            inList.get(ci).addAll(neighbourList.get(ci));
            System.out.println("Size post: " + inList.get(ci).size());
        }
        System.out.println("---------------");
        return inList;
    }

    /** Finds the closest searchElement 
     * @param startY 
     * @param startX 
     * @return
     * int[] { SearchCellIndex, elementIndexX, elementIndexY }
     */
    private int[] findClosest(int startX, int startY) {
    	
    	if(cellList.size() == 0) 
    		return null;
    	
    	double minDistance = Double.MAX_VALUE;
    	int cellIndex = -1;
    	int minX = -1;
    	int minY = -1;
    	
    	//compare the distance of all possible targets
    	for(int i=0;i<cellList.size();i++) {
    		SearchCell c = cellList.get(i);
    		double ypos = c.minY()+1;
    		while(ypos < c.maxY()) {
    			double xpos1 = c.findX(ypos, true);
        		double xpos2 = c.findX(ypos, false);
        		int xIndex1 = (int) Math.round((xpos1 - xMin) / dx);
        		int xIndex2 = (int) Math.round((xpos2 - xMin) / dx);
        		int yIndex = (int) Math.round((ypos - yMin) / dy);
        		
        		
        		double dist1 = gp.distance(startX, startY, xIndex1, yIndex);
        		double dist2 = gp.distance(startX, startY, xIndex2, yIndex);
        		
        		if(dist1 < minDistance && dist1 != -1) {
        			minDistance = dist1;
        			cellIndex = i;
        			minX = xIndex1;
        			minY = yIndex;
        		}
        		
        		if(dist2 < minDistance && dist2 != -1) {
        			minDistance = dist2;
        			cellIndex = i;
        			minX = xIndex2;
        			minY = yIndex;
        		}
        		ypos += this.delta;
    		}
    	}
    	
    	//return the target with the shortest distance
    	if(cellIndex == -1)
    		return null;

    	System.out.println("new target found: " + minX + " " + minY);
    	System.out.println("Distance: " + distance);
    	
    	return new int[] {cellIndex, minX, minY};
    }
	
	@Override
	public void run() {
		startTime = System.currentTimeMillis();
       
		/**Start scanning the first cell (given by operator)*/
        scanCell(cellList.get(0),true);
        cellList.remove(0);
        /**Scanning of the first cell complete*/
        
        while(true) {
        	//My position
        	int startX = (int)Math.round((boat.getPos()[0] - xMin) / dx);
        	int startY = (int)Math.round((boat.getPos()[1] - yMin) / dy);
        	if(startY >= ny)
        		startY = ny-1;
        	
        	/**Pick one cell and select start position */
        	System.out.println("Looking for closest path");
        	//find shortest distance to other cells
        	int[] target = findClosest(startX,startY);
        	if(target == null) {
        		cellList.clear();
        		idRegions();
        		target = findClosest(startX,startY);
        		if(target == null) {
        			System.out.println("Search complete");
        			break;
        		}
        	}

        	/**Travel to that position*/
        	System.out.println("Going to new position: (" + startX +" , " + startY + ") -> (" + target[1] + " , " + target[2] + ")");

        	if(!gp.GO(startX, startY, target[1], target[2]))
    		{
        		System.out.println("Cant go to that position");
        		break;
    		}
        	
        	/**Start scanning selected cell*/
        	System.out.println("Scanning new cell");
        	SearchCell sc = cellList.get(target[0]);
        	boolean b = ((boat.getPos()[1]-sc.minY()) < (sc.maxY()-sc.minY())/2);
        	scanCell(cellList.get(target[0]),b);
        	cellList.remove(target[0]);
        	System.out.println("CellList size: " + cellList.size());
        }
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
			// TODOz Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODOz Auto-generated catch block
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
                    }else if (e.status == 42) {
                        System.out.println("42!");
                        g.setColor(Color.magenta);
                    } else {
                        System.out.println("Dafuq?! Wrong status in cell read");
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
