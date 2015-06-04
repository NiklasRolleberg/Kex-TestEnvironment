package controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
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
	
	boolean saveData = true;
	
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
        
        // TODO fundera p� varf�r det inte funkar utan detta
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
        cellList.addAll(SearchCell.triangulatePolygon(polygonX, polygonY));
        
        cellData = new ArrayList<Double>();
    	distData = new ArrayList<Double>();
    	timeData = new ArrayList<Double>();
        //addTestPolygons(temp);
	}
   

    /**Updates cellmatrix depth data*/
    public boolean updateDepthValue(double[] data){
    	
    	double xCoord = data[0];
    	double yCoord = data[1];
    	//double heading = data[2];
    	double depthValue = data[4]; 
    	//double rightSonar = data[5]; 
    	//double leftSonar = data[6];


        int ix = (int)Math.round((xCoord - xMin) / dx);
        int iy = (int)Math.round((yCoord - yMin) / dy);

        //index out of bounds fix
        if (ix >= nx){
            ix = nx-1;
        }
        if (iy >= ny){
            iy = ny-1;
        }
        if(ix<0)
        	ix = 0;
        
        if(iy<0)
        	iy = 0;
        
        if(elementMatrix[ix][iy].status == 0)
        	visitedCells++;
        
        elementMatrix[ix][iy].updateDepthData(depthValue);
        
        // Gjorde s� att b�ten fastnade p� land
        if(elementMatrix[ix][iy].accumulatedDepth > 0)
        	elementMatrix[ix][iy].status = 2;
        
      
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
            		if(elementMatrix[i][j].status != 1 && elementMatrix[i][j].status != 2){
            			elementMatrix[i][j].status = 2;
            			visitedCells++;
            		}
            	}
        	}
        }
        //System.out.println("Visited: " + elementMatrix[ix][iy].timesVisited);
        if(elementMatrix[ix][iy].timesVisited > delta*3)
        	return false;
        
        return true;
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

	
	/** Find uncovered searchElements
	 * @return 
	 * List of searchElements
	 */
    private ArrayList<SearchElement> getUncoveredElments(){
        ArrayList<SearchElement> list = new ArrayList<SearchElement>();
        int count = 0;
        for (SearchElement[] seA : elementMatrix ){
            for (SearchElement se : seA){
                if (se.status == 0){
                    count++;
                    //System.out.println("uncovered! " + count);
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
            if(saveData) {
	            distData.add(distance);
	            timeData.add(time);
	            cellData.add((100*((double)visitedCells / (double)cellsInPolygon)));
	            System.out.println("Dist: " + Math.round(distance) + "\t Complete: " + (int)(100*((double)visitedCells / (double)cellsInPolygon)) + "%\t time: " + Math.round(time)+" s");
            }
            
            
            if(!updateDepthValue(sensorData)) {
            	System.out.println("Boat is stuck!");
            	sp.stop();
            	break;
            }
            
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
		System.out.println("scanning stopped by scancell");
		sp.stop();
	}
	
	/**  true if cell is connected to a scanned cell*/
	private boolean BFS(SearchElement e) {
		
		if(e.status == 1)
			return true;

		if(e.status != 0)
			return false;
		
		for(int i=0;i<e.neighbour.size();i++) {
			if(alreadyAdded.contains(e.neighbour.get(i)))
				continue;
			
			alreadyAdded.add(e.neighbour.get(i));
			if(BFS(e.neighbour.get(i)))
				return true;
		}
		return false;	
	}
	
	/** Finds regions enclosed by land and changes status to 2*/
	private void idLand() {
		System.out.println("id land");
		alreadyAdded.clear();
		
		for(int i=0; i<nx; i++) {
			for(int j=0; j<ny; j++) {
				if(elementMatrix[i][j].status == 0) {
					alreadyAdded.clear();
					if(!BFS(elementMatrix[i][j])) {
						elementMatrix[i][j].status = 2;
						for(SearchElement s:alreadyAdded) {
							s.status = 2;
                            visitedCells++;
						}
					}
					//System.out.println(alreadyAdded.size());
				}
			}
		}
	}
    /**Identifies uncovered coasts with at least two neighbours marked as 2 and one marked as 1 with depth less than 1m
     *  and marks them as status 3
     * //TODO make sure that status 3 does not affect other parts (A* etc)
     * */
    private void idCoastElements(){
        int adjacentLand = 0;
        int adjScanned = 0;
        //int[] adjIndex = {0,2,4,6}; //only check immediate neighbours, not diagonal
        int[] adjIndex = {0,1,2,3,4,5,6,7}; //only check all neighbours
        ArrayList<SearchElement> coastElements = new ArrayList<SearchElement>();
        ArrayList<SearchElement> uncovered = getUncoveredElments();
        for (SearchElement se : uncovered){
        	
        	//Fulfix!
        	if(se.neighbour.size() != 8)
        		continue;
        	
            for (int i : adjIndex){
                if (se.neighbour.get(i).status==2){
                    adjacentLand++;
                }
                if (se.neighbour.get(i).status==1 && Math.abs(se.neighbour.get(i).getRecordedDepth()) > 1){
                    adjScanned++;
                }
            }
            if (adjacentLand>=2 && adjScanned >=1){
                coastElements.add(se);
            }
            adjacentLand = 0;
            adjScanned = 0;
        }
        System.out.println("coastElements size: " + coastElements.size());
        for (SearchElement seC : coastElements){
            seC.status = 3;
        }

    }
    
    
    /** Find the searchElement in a searchcell
     * @param c
     * SearchCell
     * @return
     * list of searchElements
     */
    private ArrayList<SearchElement> findElementsInCell(SearchCell c) {
    	ArrayList<SearchElement> temp = new ArrayList<SearchElement>();
		
		double yVal = c.minY()+1;
		while(yVal < c.maxY()) {
			double xL = c.findX(yVal, false);
			double xR = c.findX(yVal, true);
			
			while(xL<=xR) {
				int xIndex = (int) Math.round((xL- xMin) / dx);
        		int yIndex = (int) Math.round((yVal - yMin) / dy);
        		/*
        		System.out.println("--------------------------");
        		System.out.println("XL:" + xL);
        		System.out.println("XR:" + xR);
        		System.out.println("Y:" + yVal);
        		System.out.println("--------------------------");
        		*/
        		//System.out.println("Index: (" + xIndex + " , " + yIndex + ") Status:" + elementMatrix[xIndex][yIndex].status);
        		if(elementMatrix[xIndex][yIndex].status == 0) {
        			temp.add(elementMatrix[xIndex][yIndex]);
        			//System.out.println("Adding : (" + elementMatrix[xIndex][yIndex].xCoord + "), \t (" + elementMatrix[xIndex][yIndex].yCoord + ")" );
        		}
				xL+=delta;
			}
			yVal+=delta;
		}
		return temp;
    }
	
    /** Looks for cells in cellList to remove/ rework
     */
    private void reworkSearchCells() {
    	System.out.println("ReworkCells: number of cells before: " + cellList.size());
    	
       	ArrayList<SearchCell> newList = new ArrayList<SearchCell>();
    	
    	for(int index = 0;index<cellList.size();index++){
    		SearchCell current = cellList.get(index);
    		
    		//FInd searchElelemts in current cell
    		ArrayList<SearchElement> elements = findElementsInCell(current);
    		ArrayList<SearchElement> unknownElements = new ArrayList<SearchElement>();
    		for(int i=0;i<elements.size();i++) {
    			if(elements.get(i).status != 0) {
    				unknownElements.add(elements.get(i));
    			}
    		}
    		
    		System.out.println("Uncovered: " + unknownElements.size() + " \t total: " + elements.size());
    		
    		//more than 30% cleared -> remake cell
    		if((((double)unknownElements.size()) / ((double)elements.size())) > 0.3 && unknownElements.size() > 4)
    		{
    			//System.out.println("IF (1)");
    			ArrayList<Double> xIn = new ArrayList<Double>();
    			ArrayList<Double> yIn = new ArrayList<Double>();
    			
    			for(int i=0;i<unknownElements.size();i++) {
    				xIn.add(unknownElements.get(i).xCoord);
    				yIn.add(unknownElements.get(i).yCoord);
    			}
    			
    			ArrayList<ArrayList<Double>> hull = PolygonLib.findConvexHull(xIn, yIn);
    			newList.add(new SearchCell(hull.get(0), hull.get(1)));
    		}
    		else if(unknownElements.size() == elements.size()) {
    			//System.out.println("IF (2)");
    			continue; // don't add completed cells
    		}
    		else { //cell is ok
    			//System.out.println("IF (3)");
    			newList.add(current);
    		}	
    	}
    	System.out.println("ReworkCells: number of cells after: " + newList.size());
    	cellList = newList;
    }
	
	/**Turns a searchCell into triangles and removes scanned elements 
	 * @param c
	 * SearchCell
	 * @return
	 * ArrayList with new SearchCells
	 */
	private ArrayList<SearchCell> triangulateCell(SearchCell c) {
		System.out.println("TriangulateCell");
		
		ArrayList<Double> x = c.xpos;
		ArrayList<Double> y = c.ypos;
		
		ArrayList<SearchCell> tri = SearchCell.triangulatePolygon(x, y);
		
		System.out.println("Triangles: " + tri.size());
		//return tri;
		
		
		ArrayList<ArrayList<SearchElement>> newElementList = new ArrayList<ArrayList<SearchElement>>();
		
		for(int i=0;i<tri.size();i++) {
			SearchCell t = tri.get(i);
			ArrayList<SearchElement> temp = this.findElementsInCell(t);//new ArrayList<SearchElement>();
			
			System.out.println("TEMP SIZE: " + temp.size());
			if(temp.size()>1) {
				newElementList.add(temp);
			}
		}
		
		ArrayList<SearchCell> newCells = new ArrayList<SearchCell>();
		
		
		//add extra elements to boundaries!
		newElementList = extendBoundaries(newElementList);
		
		//get convex hull
		for(int i=0;i<newElementList.size();i++) {
			ArrayList<SearchElement> list = newElementList.get(i);
			ArrayList<Double> xList = new ArrayList<Double>();
			ArrayList<Double> yList = new ArrayList<Double>();
			for(SearchElement e: list) {
				xList.add(e.xCoord);
				yList.add(e.yCoord);
			}
			ArrayList<ArrayList<Double>> convexCell = PolygonLib.findConvexHull(xList,yList);
			newCells.add(new SearchCell(convexCell.get(0), convexCell.get(1)));
		}
		
		//System.out.println("cell pos: min(" + xMin + ")\t(" + yMin + ")");
		//System.out.println("cell pos: max(" + xMax + ")\t(" + yMax + ")");
		for(int i=0;i < newCells.get(0).xpos.size(); i++) {
			System.out.println("(" + newCells.get(0).xpos.get(i) + ") \t (" + newCells.get(0).ypos.get(i));
		}
		
		System.out.println("NewCells: " + newCells.size());
		
		
		//return tri;
		return newCells;
	}
	
    /**Identify new cells and add them to cellList*/
	private void idRegions(){
		
		idLand();
        idCoastElements();
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
        listOfLists = extendBoundaries(listOfLists);

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
            if(tempListX.size() > 100) {
            	ArrayList<Double> xpos = new ArrayList<Double>(convexCell.get(0).size());
            	ArrayList<Double> ypos = new ArrayList<Double>(convexCell.get(0).size());
            	for(int i=convexCell.get(0).size()-1; i>=0; i--) {
            		xpos.add(convexCell.get(0).get(i));
            		ypos.add(convexCell.get(1).get(i));
            	}
            	System.out.println("Calling triangulate function");
            	//cellList.addAll(SearchCell.trianglulatePolygon(xpos, ypos));
            	cellList.addAll(triangulateCell(new SearchCell(xpos, ypos))); // split into triangles and remove scanned elements if possible
            } else { 
            	cellList.add(new SearchCell(convexCell.get(0), convexCell.get(1)));
            }
            draw.repaint();

            xRest.clear();
            yRest.clear();
        }
        System.out.println("new Cell list size: " + cellList.size());
        draw.repaint();

    }
	
	
	/** Extends the boundaries of a set of searchelements by adding neighbours
	 * @param inList
	 * list with SearchElements
	 * @return
	 * list with SearchElements + neighbours
	 */
    private ArrayList<ArrayList<SearchElement>> extendBoundaries(ArrayList<ArrayList<SearchElement>> inList) {
        System.out.println("-------Boundaries!--------");
        System.out.println("inList size: " + inList.size());
        ArrayList<ArrayList<SearchElement>> neighbourList = new ArrayList<ArrayList<SearchElement>>(inList.size());

        for (int ci = 0; ci < inList.size(); ci++){
            neighbourList.add(new ArrayList<SearchElement>());
        }
        System.out.println("nList size: " + neighbourList.size());

        int cellIndex = 0;
        int newE = 1;
        for (ArrayList<SearchElement> subRegion : inList){
            //System.out.println("Size of region nr " + cellIndex + " : " + subRegion.size());
            for (SearchElement se : subRegion){
                for (SearchElement seN : se.neighbour){
                    if (!neighbourList.get(cellIndex).contains(seN) && !subRegion.contains(seN) && seN.status!=99){
                        //System.out.println(newE + " new elements in region " + cellIndex);
                        newE++;
                        neighbourList.get(cellIndex).add(seN);
                    }
                }
            }
            newE = 1;
            cellIndex++;
        }

        for (int ci = 0; ci<neighbourList.size(); ci++){
            //System.out.println("Size pre: " + inList.get(ci).size());
            //System.out.println("new shit: " + neighbourList.get(ci).size());
            inList.get(ci).addAll(neighbourList.get(ci));
            //System.out.println("Size post: " + inList.get(ci).size());
        }
        System.out.println("---------------");
        return inList;
    }

    
    /** Finds the closest interesting searchElement
     * @param startY 
     * x-pos
     * @param startX
     * y-pos 
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
        		ypos += this.delta/4;
    		}
    	}
    	
    	if(cellIndex == -1)
    		return null;

    	//System.out.println("new target found: " + minX + " " + minY);
    	//System.out.println("Distance: " + distance);
    	
    	return new int[] {cellIndex, minX, minY};
    }
	
    
	/** Returns the index of the current searchCell
	 * @return
	 * Index
	 */
    private int getCurrentSearchCell() {
    	
    	double xPos = boat.getPos()[0];
    	double yPos = boat.getPos()[1];
    	
    	for(int i=0;i<cellList.size(); i++) {
    		SearchCell s = cellList.get(i);
    		if(s.contains(xPos, yPos))
    			return i;
    	}
    	
    	return -1;
    }
    
    
    /** Run method- This method is called when the thread is started */
    @Override
	public void run() {
    	
    	//Window with save button
    	JFrame frame = new JFrame();
    	JButton button = new JButton("Save");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("SAVE!");
				printToFile();
			}
    	});
    	frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    	frame.add(button);
        frame.setLocation(0,550);
    	frame.pack();
    	frame.setVisible(true);
    	
    	/*
    	boat.setTargetSpeed(0);
    	try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
    	
    	
		startTime = System.currentTimeMillis();

		/**Start scanning the first cell */
		int index = getCurrentSearchCell();
		if(index == -1) {
			System.out.println("Error boat is not inside a polygon");
			System.exit(-1);
		}
			
		scanCell(cellList.get(index),((boat.getPos()[1]-cellList.get(index).minY()) < (cellList.get(index).maxY()-cellList.get(index).minY())/2));
        cellList.remove(index);
        /**Scanning of the first cell complete*/
        
        //idRegions();
      	//draw.repaint();
        //boolean a = false;
        while(true) {
        	
        	reworkSearchCells();
        	draw.repaint();
        	//cellList.clear();
        	//idRegions();
        	//draw.repaint();
        	
        	System.out.println("--------------------new regions-----------------");
        	System.out.println("regions: " + cellList.size());
        	
        	/*
        	try {
				Thread.sleep(100000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
        	
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
        			boat.setTargetSpeed(0);
        			
        			double[] data = boat.getSensordata();
        			System.out.println("DATA");
        			System.out.println("X: " + data[0]);
        			System.out.println("Y: " + data[1]);
        			System.out.println("Heading: " + data[2]);
        			System.out.println("Speed: " + data[3]);
        			System.out.println("Depth: " + data[4]);
        			
        			break;
        		}
        	}

        	/**Travel to that position*/
        	double d = gp.GO(startX, startY, target[1], target[2]);        	
        	double time = ((System.currentTimeMillis() - startTime))/1000.0;

        	System.out.println("Going to new position: (" + startX +" , " + startY + ") -> (" + target[1] + " , " + target[2] + ")");
          	if(d == -1)
    		{
        		System.out.println("Cant go to that position");
        		break;
    		} else if(saveData){ //save distance
    			distance += d;
    			distData.add(distance);
	            timeData.add(time);
	            cellData.add((100*((double)visitedCells / (double)cellsInPolygon)));
	            System.out.println("GoToPoint: " + distance);
    		}
        	
        	/**Start scanning selected cell*/
        	System.out.println("Scanning new cell");
        	SearchCell sc = cellList.get(target[0]);
        	boolean b = ((boat.getPos()[1]-sc.minY()) < (sc.maxY()-sc.minY())/2);
        	scanCell(cellList.get(target[0]),b);
        	cellList.remove(target[0]);
       
        	System.out.println("CellList size: " + cellList.size());
        }
        
        printToFile();
   	}
	
    
	/** print data to file */
	public void printToFile() {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));
			
		String fileName = "coverageData" +dateFormat.format(date) + ".csv";
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
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	
	/** This object shows what the boat knows and what is has seen*/	
	private class DrawMatrix extends JPanel {
		JFrame myFrame;

		
		public DrawMatrix(){
			myFrame = new JFrame();
            myFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            int[] dim = correctCoords((int)Math.round(xMax*1.2),(int)Math.round(yMax*1.2));
            myFrame.setPreferredSize(new Dimension(dim[0], dim[1]));
            myFrame.setLocation(520,0);
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
                    }else if (e.status == 3) {
                        g.setColor(new Color(0xB2490B));
                    }else if (e.status == 42) {
                        //System.out.println("42!");
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
        
        
        /** Gives a color corresponding to a depth
         * @param depth
         * depth
         * @return
         * color corresponding to depth
         */
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
        
        
        /**changes the coordinats from lat-long to xy in the image */
        private int[] correctCoords(int x, int y){
            return new int[] {x - (int) xMin, y - (int) yMin};
        }
    }

}
