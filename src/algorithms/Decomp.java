package algorithms;
import java.awt.Polygon;
import java.util.ArrayList;
/**
 * 
 * 
 * 
 * */
public class Decomp {
	//class variables
	ArrayList<Double> polygonX;
	ArrayList<Double> polygonY;
	double targetY;
	boolean isDone;		//is scanning of the current cell done?
	
	
	
	
	/**Constructor*/
	public Decomp(ArrayList<Double> polyX, ArrayList<Double> polyY){
		polygonX = polyX;
		polygonY = polyY;
	}
	
	
	/**Set data from KEX thread*/
	public void setData(){
		
		
		
		
		
	}
	
	/**Describe me!*/
	private double findX(double y, boolean right) {
		int[] l1 = {-1,-1};
		int[] l2 = {-1,-1};
		
		int l = polygonY.size();
		for(int i=0; i < l; i++) {
			
			if(((polygonY.get((i+1)%l) > targetY) && (polygonY.get(i) < targetY))
			|| ((polygonY.get((i+1)%l) < targetY) && (polygonY.get(i) > targetY))) {
				//interpolate
				if(l1[0] == -1) {
					l1[0] = (i+1)%l;
					l1[1] = i%l;
					System.out.println("L1 set" + "\t (" + polygonX.get(l1[0]) + " , " + polygonY.get(l1[0]) + ") -> ("
														 + polygonX.get(l1[1]) + " , " + polygonY.get(l1[1]) + ")");
				}else {
					l2[0] = (i+1)%l;
					l2[1] = i%l;
					System.out.println("L2 set" + "\t (" + polygonX.get(l2[0]) + " , " + polygonY.get(l2[0]) + ") -> ("
							 + polygonX.get(l2[1]) + " , " + polygonY.get(l2[1]) + ")");
				}
			}
		}
		
		/*error return some default value*/
		if(l1[0] == -1 || l1[1] == -1 || l2[0] == -1 || l2[1] == 0) {
			double meanX = 0;
			for(int i=0;i<polygonX.size();i++) {
				meanX += polygonX.get(i);
			}
			meanX /= polygonX.size();
			return meanX;
		}
			
		
		//interpolate x
		
		double x0 = polygonX.get(l1[0]);
		double y0 = polygonY.get(l1[0]);
		
		double x1 = polygonX.get(l1[1]);
		double y1 = polygonY.get(l1[1]);
		
		//how far is y on the line
		double p = (targetY-y0) / (y1-y0);
		double l1X = (1-p)*x0 + p*x1;
		
		System.out.println("p1=" + p);
		
		x0 = polygonX.get(l2[0]);
		y0 = polygonY.get(l2[0]);
		
		x1 = polygonX.get(l2[1]);
		y1 = polygonY.get(l2[1]);
		
		//how far is y on the line
		p = (targetY-y0) / (y1-y0);
		double l2X = (1-p)*x0 + p*x1;
		System.out.println("p2=" + p);
		
		if(right) {
			return Math.max(l1X, l2X);
		}
		return Math.min(l1X, l2X);
	}
	
	
	
}
/*
//Subclass for individual cells
class Cell{}
*/