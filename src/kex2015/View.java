package kex2015;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class View extends JFrame implements Runnable {
	
	Map seaFloor;
	Boat boat;
	JPanel image;
	long dt;
	
	int size = 500;
	private boolean stop = false;
	
	public View(Map seafloor, Boat boat, long dt) {
		this.seaFloor = seafloor;
		this.boat = boat;
		this.image = new mapView();
		this.dt = dt;
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
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
			Graphics2D g2d = (Graphics2D) g;
			g2d.clearRect(0, 0, size, size);
			g.setColor(Color.BLUE);
			g.fillRect(0, 0, size, size);
			
			for(int x=0; x < size; x++) {
				for(int y=0; y < size; y++) {
					//double depth = map.getDepth(x/realSize,y/realsize); //TODO gör inte så
					Color color;
					if(Math.random() < 0.5) {
						color = Color.CYAN;
					}
					else { 
						color = Color.green;
					}
					g2d.setColor(color);
					g2d.drawLine(x, y, x, y);
				}
			}
			
		}
	}
}
