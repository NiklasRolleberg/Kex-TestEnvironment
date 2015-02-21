package kex2015;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class View extends JFrame implements Runnable {
	
	BufferedImage map;
	Map seaFloor;
	Boat boat;
	JPanel image;
	long dt;
	
	int size = 500;
	private boolean stop = false;
	
	public View(Map seafloor, Boat boat, long dt) {
		this.seaFloor = seafloor;
		this.boat = boat;
		this.dt = dt;
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		/**Draw a map*/
		map = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics g = map.getGraphics();
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.clearRect(0, 0, size, size);
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, size, size);
		double[] limits = seafloor.getLimits();
		
		double longStart = limits[0];
		double longStop = limits[2];
		double stepLong = (longStop + longStart) / size;
		
		double latStart = limits[1];
		double latStop = limits[3];
		double stepLat = (latStop + latStart) / size;
	
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
				}
				*/
				
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
			System.out.println("Redraw");
			
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
			
			g.drawImage(map, 0, 0, null);
			Graphics2D g2d = (Graphics2D) g;
			
			g2d.drawOval((int) (Math.random() * size), (int) (Math.random() * size), 20, 20);
			
		}
	}
}
