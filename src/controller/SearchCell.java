package controller;
import java.awt.geom.Point2D;
import java.util.ArrayList;

	public class SearchCell{
		public boolean isComplete;
		ArrayList<Double> xpos;
		ArrayList<Double> ypos;
		//double xMax, yMax, xMin, yMin;

    /**Constructor
	 * @param xpos x-positions for the polygon
	 * @param ypos y-positions for the polygon
	 * */
	public SearchCell (ArrayList<Double> xpos, ArrayList<Double> ypos){
        isComplete = false;
        this.xpos = xpos;
        this.ypos = ypos;   
	}

	/** Is the polygon convex?
	 * @return
	 * true = convex, false = not convex
	 */
	public boolean isConvex() {
		
		// TODO GÖR NÅGOT SOM FUNKAR!
		
		/*
		int n = xpos.size();
		for(int i=0;i<n;i++) {
			//point one
			double x0 = xpos.get(i%n);
			double y0 = ypos.get(i%n);
			
			//point two
			double x1 = xpos.get((i+1)%n);
			double y1 = ypos.get((i+1)%n);
			
			//point three
			double x2 = xpos.get((i+2)%n);
			double y2 = ypos.get((i+2)%n);
			
			//vector 1
			double v1x = x0-x1;
			double v1y = y0-y1;
			
			//vector 2
			double v2x = x2-x1;
			double v2y = y2-y1;
			
			
			//normalize v1,v2
			double n1 = Math.sqrt((v1x*v1x + v1y*v1y));
			double n2 = Math.sqrt((v2x*v2x + v2y*v2y));
			v1x /= n1;
			v1y /= n1;
			v2x /= n2; 
			v2y /= n2;
			
			double alpha = Math.acos(v1x*v2x + v1y*v2y);
			//System.out.println("v1x: " + v1x + "\t v1y: " + v1y + "\t v2x: " + v2x + "\t v2y: " + v2y );
			if(v1x < 0 && v2x < 0)
				alpha = Math.PI + alpha;
				
			System.out.println("angle: " + alpha / Math.PI*180);
			
		}*/
		return true;
	}
	
    /** Finds the x-value for a given y 
     * @param y
     * @param right
     * tightside / leftside
     * @return
     * 
     */
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
	
	/** Split a polygon into triangles 
	 * @param xPos
	 * Polygon x-pos
	 * @param yPos
	 * Polygon y-pos
	 * @return
	 * List of SearchCells (one for each area)
	 */
	static ArrayList<SearchCell> trianglulatePolygon(ArrayList<Double> xPos, ArrayList<Double> yPos) {
		
		ArrayList<Double> xVertex = new ArrayList<Double>();
		xVertex.addAll(xPos);
		
		ArrayList<Double> yVertex = new ArrayList<Double>();
		yVertex.addAll(yPos);
		
		ArrayList<SearchCell> list = new ArrayList<SearchCell>();
		
		//Ear clipping method
		int index = 0;
		int[] triangle = {index, index+1, index+2};
		
		while(true) {
			boolean ear = true;
			
			/*Check if triangle = ear*/
			double[] triangleX = new double[3];
			double[] triangleY = new double[3];
			for(int i=0;i<3;i++) {
				triangleX[i] = xVertex.get(triangle[i]);
				triangleY[i] = yVertex.get(triangle[i]);
			}
			
			for(int i=0; i<xVertex.size(); i++) {
				if(i == triangle[0] || i == triangle[1] || i == triangle[2]) 
					continue;
				if(insideTriangle(triangleX, triangleY, xVertex.get(i), yVertex.get(i))) {
					System.out.println("No ear");
					ear = false;
				}
			}
			
			if(!rightTurn(triangleX,triangleY)) {
				System.out.println("No ear");
				ear = false;
			}
			
			/*if triangle is an ear, remove the ear and create a SearchCell*/
			if(ear) {
				System.out.println("Ear found");
				ArrayList<Double> X = new ArrayList<Double>();
				ArrayList<Double> Y = new ArrayList<Double>();
				for(int j=0;j<3;j++) {
					X.add(xVertex.get(triangle[j]));
					Y.add(yVertex.get(triangle[j]));
				}
				list.add(new SearchCell(X,Y));
				System.out.println("Removing vertexes");
				xVertex.remove(triangle[1]);
				yVertex.remove(triangle[1]);
				
				index = triangle[1];
				System.out.println("Remaining vertexes: " + xVertex.size());
			}
			if(!ear) {
				index++;
			}
	
			if(xVertex.size() < 4) {
				System.out.println("KLAR!");
				list.add(new SearchCell(xVertex, yVertex));
				break;
			}
			
			triangle[0] = index % xVertex.size();
			triangle[1] = (index+1) % xVertex.size();
			triangle[2] = (index+2) % xVertex.size();
		}
		
		
		return list;
	}
	
	/** Check if a turn is to the right
	 * @param x
	 * x-coordinates of triangle
	 * @param y
	 * y-coordinates of triangle
	 * @return
	 * true = right turn or no turn
	 * false = left turn
	 */
	private static boolean rightTurn(double[] x, double[] y) {
		System.out.println("Calculate angle");
		double x1 = (x[1]-x[0]) * (y[2]-y[0]);
		double x2 = (x[2]-x[0]) * (y[1]-y[0]);
		double x3 = x1-x2;
		return (x3>=0);
	}
	
	/** Check if point is inside a triangle
	 * @param xpos
	 * triangle xpos
	 * @param ypos
	 * triangle ypos
	 * @param px
	 * point x
	 * @param py
	 * point y
	 * @return
	 * true/false
	 */
	private static boolean insideTriangle(double[] x, double[] y, double px, double py) {
		
		double denominator = ((y[1] - y[2])*(x[0] - x[2]) + (x[2] - x[1])*(y[0] - y[2]));
	 	double a= ((y[1] - y[2])*(px - x[2]) + (x[2] - x[1])*(py - y[2])) / denominator;
	 	double b = ((y[2] - y[0])*(px - x[2]) + (x[0] - x[2])*(py - y[2])) / denominator;
	 	double c = 1 - a - b;
	 	
	 	return 0 <= a && a <= 1 && 0 <= b && b <= 1 && 0 <= c && c <= 1;
	}
}
