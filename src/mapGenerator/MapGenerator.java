package mapGenerator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import kex2015.Map;

public class MapGenerator implements ActionListener{
	
	private JTextField size;
	private String fileName;
	
	public MapGenerator(String fileName) {
		
		System.out.println("MapGen");
		this.fileName = fileName;
		
		//Grate window and content
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		JPanel topContainer = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JButton createMapButton = new JButton("Create new map");
		JButton showMapButton = new JButton("Show map");
		
		createMapButton.addActionListener(this);
		showMapButton.addActionListener(this);

		
		JTextArea sizeText = new JTextArea("size");
		sizeText.setEditable(false);
		size = new JTextField("1000");
		
		topContainer.add(createMapButton);
		topContainer.add(sizeText);
		topContainer.add(size);
		
		panel.add(topContainer,BorderLayout.NORTH);
		panel.add(showMapButton,BorderLayout.CENTER);
		
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand()  == "Create new map") {
			System.out.println("new map");
			
			//read size
			int s = Integer.parseInt(size.getText());
			if (s<=10)
				return;
			
			//generateRandomMap(s,s,fileName);
			generateContourTestMap(s,s,fileName);
			
		}
		else if(arg0.getActionCommand()  == "Show map") {
			System.out.println("show map");
			
			MapView m =  new MapView();
		}
		
	}
	
	
	/**Generate a random enviroment and save it as a csv file*/
	private void generateRandomMap(int sizeX, int sizeY, String fileName) {
		
		//Matrix to store values
		Double[][] matrix = new Double[sizeX][sizeY];
		
		//Generate depth
		for(int i = 0; i<sizeX; i++) {
			for(int j=0; j<sizeY; j++) {
				matrix[i][j] = -15.;
			}
		}
		
		//generate "islands" at radom places -> ser n�stan bra ut
		int islands = 600;
		for(int k = 0; k<islands; k++) {
			
			if(k % 12 == 0)
			{
				int pr = (int) ((k/((double) islands))*100);
				System.out.println("Generating map: " + pr + "%");
				
			}
			
			int posX = (int) (Math.random()*sizeX);
			int posY = (int) (Math.random()*sizeY);
			int r = (int)(Math.random()*(sizeX+sizeY)/10);
			
			
			for (int i = posX - r; i < posX + r; i++) {
				if (i < 0)
					continue;
				if (i >= sizeX)
					continue;
				for (int j = posY - r; j < posY + r; j++) {
					if (j < 0)
						continue;
					if (j >= sizeY)
						continue;
					if( (posX-i)*(posX-i) + (posY-j)*(posY-j) < r*r)
						matrix[i][j] += Math.random();//0.5;
				}
			}
			
		}
		
		//push the world down a bit
		/*
		islands = 1000;
		for(int k = 0; k<islands; k++) {
			int posX = (int) (Math.random()*sizeX);
			int posY = (int) (Math.random()*sizeY);
			int width = (int)(Math.random()*(sizeX)/10);
			int height = (int)(Math.random()*(sizeY)/10);					
			for(int i = posX-(width/2); i<posX+(width/2); i++) {
				for(int j = posY-(height/2); j<posY+(height/2); j++) {
					if( i < sizeX && i >=0 && j<sizeY && j>=0)
						matrix[i][j] -= 0.2;
				}
			}
		}*/
		
		//raise the land a bit to make more distinct islands  
		for(int i=1; i<sizeX-1; i++) {
			for(int j=1; j<sizeY-1; j++) {
				if(matrix[i][j] > 0) {
					matrix[i][j] += 5.0;
				}
			}
		}
		
		
		//smoth out the matrix
		for(int k = 0; k < 20; k++ ) {
			
			int pr = (int)((k/20f)*100);
			System.out.println("fixing map: " + pr + "%");
			
			for(int i=1; i<sizeX-1; i++) {
				for(int j=1; j<sizeY-1; j++) {
					
					//TODO testa att r�kna med diagonaler ocks�
					double  mean= (matrix[i+1][j]
								 +matrix[i-1][j]
							  	 +matrix[i][j+1]
								 +matrix[i][j-1])/4;
					
					matrix[i][j] += (mean-matrix[i][j]);
				}
			}
		}
		
		
		System.out.println("Generation done");
		System.out.println("Writing to file..");
		
		/*Write file*/
		PrintWriter writer;
		
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			for(int i = 0; i<sizeX; i++) {
				if((i % ((int) (sizeX/10))) == 0)
				{
					int pr = (int)((i/(sizeX/10))*10);
					System.out.println("writing map: " + pr + "%");
				}
				
				StringBuilder line = new StringBuilder();
				for(int j=0; j<(sizeY-1); j++) {
					line.append(matrix[i][j] + " ,");
				}
				line.append(" " + matrix[i][sizeY-1]);
				writer.println(line.toString());
			}
			writer.close();
			System.out.println("new map created");
			
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFound");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.out.println("UnsupportedEncodingException");
			e.printStackTrace();
		}
		
	}

	/**Generate a random enviroment and save it as a csv file*/
	private void generateContourTestMap(int sizeX, int sizeY, String fileName) {
		
		//Matrix to store values
		Double[][] matrix = new Double[sizeX][sizeY];
		
		//Generate depth
		for(int i = 0; i<sizeX; i++) {
			for(int j=0; j<sizeY; j++) {
				matrix[i][j] = 15.;
			}
		}
		
		//Generate Hole
		int r = Math.min(sizeX, sizeY);
		int posX = sizeX/2;
		int posY = sizeY/2;
		
		while (r > 4 ) {
			
			for (int i = posX - r; i < posX + r; i++) {
				if (i < 0)
					continue;
				if (i >= sizeX)
					continue;
				for (int j = posY - r; j < posY + r; j++) {
					if (j < 0)
						continue;
					if (j >= sizeY)
						continue;
					if( (posX-i)*(posX-i) + (posY-j)*(posY-j) < r*r)
						if(r > Math.min(sizeY, sizeY) / 3)
							matrix[i][j] -= 0.5;// Math.random();//0.5;
						else
							matrix[i][j] += 1;// 
				}
			}
			
			r-=10;
		}
		
		
		
		/*
		//raise the land a bit to make more distinct islands  
		for(int i=1; i<sizeX-1; i++) {
			for(int j=1; j<sizeY-1; j++) {
				if(matrix[i][j] > 0) {
					matrix[i][j] += 5.0;
				}
			}
		}
		*/
		
		//smoth out the matrix
		for(int k = 0; k < 20; k++ ) {
			
			int pr = (int)((k/20f)*100);
			System.out.println("fixing map: " + pr + "%");
			
			for(int i=1; i<sizeX-1; i++) {
				for(int j=1; j<sizeY-1; j++) {
					
					//TODO testa att r�kna med diagonaler ocks�
					double  mean= (matrix[i+1][j]
								 +matrix[i-1][j]
							  	 +matrix[i][j+1]
								 +matrix[i][j-1])/4;
					
					matrix[i][j] += (mean-matrix[i][j]);
				}
			}
		}
		
		
		System.out.println("Generation done");
		System.out.println("Writing to file..");
		
		/*Write file*/
		PrintWriter writer;
		
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			for(int i = 0; i<sizeX; i++) {
				if((i % ((int) (sizeX/10))) == 0)
				{
					int pr = (int)((i/(sizeX/10))*10);
					System.out.println("writing map: " + pr + "%");
				}
				
				StringBuilder line = new StringBuilder();
				for(int j=0; j<(sizeY-1); j++) {
					line.append(matrix[i][j] + " ,");
				}
				line.append(" " + matrix[i][sizeY-1]);
				writer.println(line.toString());
			}
			writer.close();
			System.out.println("new map created");
			
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFound");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.out.println("UnsupportedEncodingException");
			e.printStackTrace();
		}
	}


	

	
	class MapView extends JPanel implements ActionListener, MouseListener{
		
		JFrame mv_frame;
		JPanel buttonContainer;
		JPanel container;
		BufferedImage image;
		JButton readPolygon;
		JButton createPolygon;
		JToggleButton setStartPos;
		
		double longStart;
		double longStop;		
		double latStart;
		double latStop;
		
		double stepLong;
		double stepLat;
		
		/*Size of map on screen*/
		int imageSize = 700;
		
		int startX = -1;
		int startY = -1;
		
		ArrayList<Integer> polygonX;
		ArrayList<Integer> polygonY;
		
		
		MapView() {
			
			polygonX = new ArrayList<Integer>();
			polygonY = new ArrayList<Integer>();
			
			//create image
			Map map = new Map(fileName);
			/**Draw a map*/
			image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			
			Graphics2D g2d = (Graphics2D) g;
			g2d.clearRect(0, 0, imageSize, imageSize);
			g.setColor(Color.BLUE);
			g.fillRect(0, 0, imageSize, imageSize);
			
			
			double[] limits = map.getLimits();
			longStart = limits[0];
			longStop = limits[2];			
			latStart = limits[1];
			latStop = limits[3];
			stepLong = (longStop - longStart) / imageSize;
			stepLat = (latStop - latStart) / imageSize;			
		
			for(int x=0; x < imageSize; x++) {
				for(int y=0; y < imageSize; y++) {
					Color color = Color.GREEN;			
					double longitude = longStart + stepLong*x;
					double latitude = latStart + stepLat*y;				
					double depth = map.getDepth(longitude, latitude);				
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
			for(int x=5; x < imageSize; x+=75) {
				for(int y=10; y < imageSize; y+=75) {			
					double longitude = longStart + stepLong*x;
					double latitude = latStart + stepLat*y;			
					double depth = map.getDepth(longitude, latitude);
					
					String s = ""+ ((double)((int)(depth*10)))/10;
					char[] d = s.toCharArray();
					g2d.drawChars(d, 0, s.length(), x-5, y);
				}
			}
			
			
			//create JFrame and panel and buttons
			mv_frame = new JFrame();
			mv_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			buttonContainer = new JPanel();
			container = new JPanel();
			container.setLayout(new BorderLayout());
			readPolygon = new JButton("Read from file");
			createPolygon = new JButton("Save to file");
			setStartPos = new JToggleButton("Set start position");
			
			readPolygon.addActionListener(this);
			createPolygon.addActionListener(this);
			
			buttonContainer.add(readPolygon);
			buttonContainer.add(createPolygon);
			buttonContainer.add(setStartPos);
			
			
			container.add(this, BorderLayout.CENTER);
			container.add(buttonContainer, BorderLayout.SOUTH);
			
			this.setPreferredSize(new Dimension(imageSize,imageSize));
			this.addMouseListener(this);
			
			mv_frame.add(container);
			mv_frame.pack();
			mv_frame.setVisible(true);
		}
		
		
		@Override public void paint(Graphics g) {
			g.drawImage(image, 0, 0, null);
			
			if (polygonX.size() == 0)
				return;
			
			for(int i=0;i< polygonX.size()-1;i++) {
				int x0 = polygonX.get(i);
				int y0 = polygonY.get(i);
				
				int x1 = polygonX.get(i+1);
				int y1 = polygonY.get(i+1);
				g.setColor(Color.RED);
				g.drawLine(x0, y0, x1, y1);
			}
			g.setColor(Color.GRAY);
			g.drawLine(polygonX.get(polygonX.size()-1), polygonY.get(polygonX.size()-1)
									,polygonX.get(0), polygonY.get(0));
			
			if(startX != -1 && startY!= -1) {
				g.setColor(Color.GREEN);
				g.fillOval(startX-5, startY-5, 10, 10);
			}
			
		}
		
		private void read() 
		{
			polygonX.clear();
			polygonY.clear();
			
			ArrayList<Double> tempX = null;
			ArrayList<Double> tempY = null;
			
			try
			{
				//read X
				FileInputStream fileInX = new FileInputStream("polygonx.ser");
				ObjectInputStream inX = new ObjectInputStream(fileInX);
				tempX = (ArrayList<Double>) inX.readObject();
				inX.close();
				fileInX.close();
				
				for(int i=0;i<tempX.size();i++)
				{
					int xval = (int) ((tempX.get(i)-longStart)/stepLong);	
					polygonX.add(xval);	
				}	

				
				//read Y
				FileInputStream fileInY = new FileInputStream("polygony.ser");
				ObjectInputStream inY = new ObjectInputStream(fileInY);
				inY = new ObjectInputStream(fileInY);
				tempY = (ArrayList<Double>) inY.readObject();
				inY.close();
				fileInY.close();
				
				for(int i=0;i<tempY.size();i++) 
				{
					int yval = (int) ((tempY.get(i)-latStart)/stepLat);
					polygonY.add(yval);
				}
				
				//read start pos
				BufferedReader br = new BufferedReader(new FileReader("start.txt"));
		        
		        startY = (int) ((Double.parseDouble(br.readLine())-longStart)/stepLong);
		        
		        startY = (int) ((Double.parseDouble(br.readLine())-latStart)/stepLat);
		        
		        br.close();
				
			}catch(IOException i)
			{
				i.printStackTrace();
				return;
			}catch(ClassNotFoundException c)
			{
				System.out.println("class not found");
				c.printStackTrace();
				return;
			}
			//print
			System.out.println("Det kan ha funkat");
		}

		
		private void write() {
			ArrayList<Double> X =  new ArrayList<Double>();
			ArrayList<Double> Y =  new ArrayList<Double>();
			
			for(int i=0;i<polygonX.size();i++) {
				double longitude = longStart + stepLong* polygonX.get(i);
				double latitude = latStart + stepLat*polygonY.get(i);
				
				X.add(longitude);
				Y.add(latitude);
			}
			try
			{
				//write x
				FileOutputStream fileOutX = new FileOutputStream("polygonx.ser");
				ObjectOutputStream outX = new ObjectOutputStream(fileOutX);
				outX.writeObject(X);
				outX.close();
				fileOutX.close();
				System.out.println("Serialized data is saved in polygonx.ser");
	         
				//write y
				FileOutputStream fileOutY = new FileOutputStream("polygony.ser");
				ObjectOutputStream outY = new ObjectOutputStream(fileOutY);
				outY = new ObjectOutputStream(fileOutY);
				outY.writeObject(Y);
				outY.close();
				fileOutY.close();
				System.out.println("Serialized data is saved in polygony.ser");
				
				if(startX != -1 && startY != -1)
				{
					
					double longitude = longStart + stepLong* startX;
					double latitude = latStart + stepLat*startY;
					
					PrintWriter writer = new PrintWriter("start.txt", "UTF-8");
					writer.println(""+longitude);
					writer.println(""+latitude);
					writer.close();
					
					System.out.println("Start pos saved in start.txt");
				}
				
			}catch(IOException i)
			{
				i.printStackTrace();
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent a) {
			// TODO Auto-generated method stub
			if(a.getActionCommand() == "Read from file"){
				System.out.println("read file");
				read();
				super.repaint();
			}
			
			if(a.getActionCommand() == "Save to file"){
				System.out.println("write file");
				write();
			}
		}


		@Override
		public void mouseClicked(MouseEvent m) {

			System.out.println("Mouse click at: (" + m.getX() + "," + m.getY() + ")");
			
			/*Set start position*/
			if(setStartPos.isSelected()) {
				System.out.println("Selected");
				startX = m.getX();
				startY = m.getY();
			}
			/*add point to polygon*/
			else {
				polygonX.add(m.getX());
				polygonY.add(m.getY());
			}
			
			super.repaint();
			
		}


		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static void main(String[] args) {
		MapGenerator mg = new MapGenerator("test.csv");
	}
}
