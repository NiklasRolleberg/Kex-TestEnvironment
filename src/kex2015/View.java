package kex2015;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class View extends JFrame implements Runnable {
	
	BufferedImage map;
	Map seaFloor;
	Boat boat;
	JPanel image;
	long dt;
	
	double longStart;
	double longStop;
	double stepLong;
	
	double latStart;
	double latStop;
	double stepLat;
	
	double boatlong;
	double boatlat;
	
	int size = 500;
	private boolean stop = false;
	
	public View(Map seafloor, Boat boat, long dt, ArrayList<Double> polygonX, ArrayList<Double> polygonY) {
		this.seaFloor = seafloor;
		this.boat = boat;
		this.dt = dt;
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		//store boat pos
		double[] boatPos = boat.getPos();
		boatlong = boatPos[0];
		boatlat = boatPos[1];
		
		/**Draw a map*/
		map = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics g = map.getGraphics();
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.clearRect(0, 0, size, size);
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, size, size);
		
		/*
		double[] limits = seafloor.getLimits();
		longStart = limits[0];
		longStop = limits[2];
		stepLong = (longStop + longStart) / size;
		
		latStart = limits[1];
		latStop = limits[3];
		stepLat = (latStop + latStart) / size;*/
		
		//find min and max X,Y
		double minX = polygonX.get(0);
		double maxX = polygonX.get(0);
		double minY = polygonY.get(0);
		double maxY = polygonY.get(0);
		
		for(int i=0;i<polygonX.size();i++) {
			if (polygonX.get(i) < minX)
				minX = polygonX.get(i);
			if (polygonX.get(i) > maxX)
				maxX = polygonX.get(i);
			if (polygonY.get(i) < minY)
				minY = polygonY.get(i);
			if (polygonY.get(i) > maxY)
				maxY = polygonY.get(i);
		}
		
		//change values to get a square area if possible
		double dx = maxX-minX;
		double dy = maxY-minY;
		
		if (dx != dy) {
			if(dy > dx) {	
			}
			else {
				maxY = minY + dx;
			}
		}

		
		double[] limits = seafloor.getLimits();
		longStart = limits[0];
		longStop = limits[2];
		
		
		latStart = limits[1];
		latStop = limits[3];
		
		//Oob check
		if (minX-5 > longStart)
			longStart = minX-5;
		if (maxX+5 < longStop)
			longStop = maxX+5;
		if (minY-5 > latStart)
			latStart = minY-5;
		if (maxY+5 < latStop)
			latStop = maxY+5;

		stepLong = (longStop - longStart) / size;
		stepLat = (latStop - latStart) / size;
		
		System.out.println("View: min, max long: " + longStart + ", " + longStop);
		System.out.println("View: min, max lat: " + latStart + ", " + latStop);
		
	
		for(int x=0; x < size; x++) {
			for(int y=0; y < size; y++) {
				Color color = Color.GREEN;
			
				double longitude = longStart + stepLong*x;
				double latitude = latStart + stepLat*y;
				
				double depth = seafloor.getDepth(longitude, latitude);
				
				//System.out.println(depth);
				
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
					color = color.GREEN;
				}
				
				// more linear
				/*
				if (depth > 0) {
					color = color.GREEN;
				}
				else {
					double step = 255/21;
					int blue = (int)((-21-depth)*-step);
					int green = (int)((-21-depth)*-step);
					if(blue < 0)
						blue = 0;
					color = new Color(0, green, blue);
				}*/
				
				
				g2d.setColor(color);
				g2d.drawLine(x, y, x, y);
			}
		}
		
		/*Writh depth numbers on map*/
		g2d.setColor(Color.BLACK);
		for(int x=5; x < size; x+=75) {
			for(int y=10; y < size; y+=75) {			
				double longitude = longStart + stepLong*x;
				double latitude = latStart + stepLat*y;			
				double depth = seafloor.getDepth(longitude, latitude);
				
				String s = ""+ ((double)((int)(depth*10)))/10;
				char[] d = s.toCharArray();
				g2d.drawChars(d, 0, s.length(), x-5, y);
			}
		}
		
		/*Draw the polygon on the map */
		for(int i = 0; i < polygonX.size()+1; i++) {
			
			double long0 = polygonX.get(i%polygonX.size()).doubleValue();
			double long1 = polygonX.get((i+1)%polygonX.size()).doubleValue();
			
			double lat0 = polygonY.get(i%polygonX.size()).doubleValue();
			double lat1 = polygonY.get((i+1)%polygonX.size()).doubleValue();
			
			int x0 = (int) ((long0-longStart)/stepLong);
			int x1 = (int) ((long1-longStart)/stepLong);
			
			int y0 = (int) ((lat0-latStart)/stepLat);
			int y1 = (int) ((lat1-latStart)/stepLat);
			
			g2d.drawLine(x0,y0,x1,y1);
		}
		
		//g.translate(-100, -100);
		//g.dispose();
		
		
		
		this.image = new mapView();
		this.add(image);
		pack();
		setVisible(true);
	}
	
	public void stop() {
		stop = true;
	}

	@Override
	public void run() {
		while(!stop) {
			//System.out.println("Redraw");
			
			image.repaint();
			
			try {
				Thread.sleep(dt);
			} catch (InterruptedException e) {
				System.err.println("Fel i view");
				e.printStackTrace();
			}
		}
	}
	
	class mapView extends JPanel {
		
		mapView() {
			this.setPreferredSize(new Dimension(size,size));
			
		}
		
		public void paint(Graphics g) {
			
			Graphics2D mapgr = (Graphics2D) map.getGraphics();
			mapgr.setColor(Color.RED);
			
			double[] boatpos = boat.getPos();
			double X0 = (boatlong - longStart)/stepLong;
			double Y0 = (boatlat - latStart)/stepLat;
			
			double X1 = (boatpos[0] - longStart)/stepLong;
			double Y1 = (boatpos[1] - latStart)/stepLat;
			
			if(Math.sqrt((X1-X0)*(X1-X0)
					+ (Y1-Y0)*(Y1-Y0)) >2) {
				mapgr.drawLine((int)X0, (int) Y0, (int) X1, (int) Y1);
				boatlong = boatpos[0];
				boatlat = boatpos[1];
			}
			
			
			
			g.drawImage(map, 0, 0, null);
			Graphics2D g2d = (Graphics2D) g;
			
			
			//Draw boat
			int nPoints = 5;
			int a = 15;
			int b = 10;
			double h = boat.heading;
			int[] xPoints = {
					(int) (X1 + a*Math.cos(h)),
					(int) (X1 + b*Math.cos(h + (Math.PI/4))),
					(int) (X1 + b*Math.cos(h + (3*Math.PI/4))),
					(int) (X1 + b*Math.cos(h + (5*Math.PI/4))),
					(int) (X1 + b*Math.cos(h + (7*Math.PI/4)))};
			
			int[] yPoints = {
					(int) (Y1 + a*Math.sin(h)),
					(int) (Y1 + b*Math.sin(h + (Math.PI/4))),
					(int) (Y1 + b*Math.sin(h + (3*Math.PI/4))),
					(int) (Y1 + b*Math.sin(h + (5*Math.PI/4))),
					(int) (Y1 + b*Math.sin(h + (7*Math.PI/4)))};
			
			for(int i = 0; i < nPoints; i++) {
				if(xPoints[i] < 0)
					xPoints[i]  = 0;
				if(yPoints[i] < 0)
					yPoints[i]  = 0;
				if(xPoints[i] >= size)
					xPoints[i]  = size-1;
				if(yPoints[i] >= size)
					yPoints[i]  = size-1;
				
			}
			
			g2d.setColor(Color.RED);
			g2d.fillPolygon(xPoints, yPoints, nPoints);
			
			double[] waypoint = boat.getWaypoint();
			
			g2d.drawOval((int)((waypoint[0] - longStart)/stepLong), (int)((waypoint[1] - latStart)/stepLat), 10, 10);
		}
	}
}
