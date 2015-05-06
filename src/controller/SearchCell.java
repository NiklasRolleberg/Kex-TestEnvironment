package controller;
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
                      
            // TODO fixa
            //draw = new drawMatrix(cellList);
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
	}
