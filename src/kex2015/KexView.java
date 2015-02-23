package kex2015;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class KexView extends JPanel implements Runnable {
	
	JFrame parent;
	Kex kex;
	ArrayList<MapElement> mapData;
	int minLong = 2;
	int maxLong= 10;
	int minLat = 2;
	int maxLat = 10;
	
	double lastLongitude = 0;
	double lastLatitude = 0;
	double dist = 10.;
	public boolean stop;
	
	
	public KexView() {
		
		mapData = new ArrayList<MapElement>();
		
		this.setPreferredSize(new Dimension(1000, 1000));
		
		parent = new JFrame();
		parent.add(this);
		parent.pack();
		//parent.setDefaultCloseOperation(EXIT_ON_CLOSE);
		parent.setVisible(true);
	}
	
	public void addData(double latitude, double longitude, double depth) {
		
		double diffX = lastLatitude -latitude;
		double diffY = lastLongitude -longitude;
		
		if(Math.sqrt(diffX*diffX + diffY*diffY) > 10) {
			
			if (latitude > maxLat)
				maxLat = (int) latitude;
			if (latitude < minLong)
				maxLat = (int) latitude;
			
			if (longitude > maxLat)
				maxLong = (int) longitude;
			if (longitude < minLong)
				maxLong = (int) longitude;	
			MapElement me = new MapElement((int)latitude, (int)longitude, depth);
			
			mapData.add(me);
		}
		
		
		
		
	}
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.RED);
		//g2d.clearRect(0, 0, maxLat-minLat, maxLong-minLong);
		g2d.clearRect(0, 0, 1000, 1000);
		for(int i=0;i<mapData.size();i++) {
			
			MapElement me = mapData.get(i);
			try{
				g2d.setColor(me.color);
				g2d.fillOval(me.latitude-5-minLat, me.longitude-5-minLong, 10, 10);
			}catch(Exception e){
			}
		}
	}
	
	@Override
	public void run() {
		
		while(true) {
			this.repaint();
			//this.setPreferredSize(new Dimension(maxLat-minLat, maxLong-minLong));
			//parent.pack();
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			
	}
	class MapElement {
		int latitude = 0;
		int longitude = 0;
		
		Color color;
		MapElement(int latitude, int longitude, double depth) {
			this.latitude = latitude;
			this.longitude = longitude;
			
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
		}
	}
}
