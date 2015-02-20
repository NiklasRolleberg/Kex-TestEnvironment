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
		
		//TODO Ta reda på max/min lat och long
		double longStart = 0;
		double longStop = size;
		double stepLong = (longStop + 0) / size;
		
		double latStart = 0;
		double latStop = size;
		double stepLat = (longStop + 0) / size;
		
		for(int x=0; x < size; x++) {
			for(int y=0; y < size; y++) {
				Color color;
				
				if(Math.random() < 0.5) {
					color = Color.CYAN;
				}
				else { 
					color = Color.green;
				}
				
				/*
				double longitude = longStart + stepLong*x;
				double latitude = latStart + stepLat*y;
				
				double[] data = seafloor.getSensorData(longitude, latitude);
				double depth = data[0];
				System.out.println(depth);
				if(depth < -20) {
					color = color.BLACK; 
				}
				else if(depth < -10)	{
					color = color.BLUE;
					color = color.darker();
				}
				else if(depth < -5) {
					color = color.BLUE;
				}
				else if(depth < -3) {
					color = color.BLUE;
					color = color.brighter();
				}
				else if (depth < -0) {
					color = color.CYAN;
				}
				else{
					color = color.GREEN;
				}
				*/
				g2d.setColor(color);
				g2d.drawLine(x, y, x, y);
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
