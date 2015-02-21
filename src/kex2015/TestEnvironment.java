package kex2015;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TestEnvironment {
	
	Map map;
	Boat boat;
	Kex kex;
	View view;
	
	Thread boatThread;
	Thread kexThread;
	Thread viewThread;
	
	public TestEnvironment(String mapFile) {
		
		/**Create map*/
		//map = new Map("MapTest.csv");
		map = new Map(mapFile);

		
		/**Create boat*/
		boat = new Boat(map, 200);
		
		/**Create kex*/
		//polygon
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer>();
		int[] endPos = {1,3};
		kex = new Kex(boat, x, y , 0.24 , endPos , 200 );
		
		/**Create view*/
		view = new View(map, boat,100);
		
		/**create and start threads*/
		boatThread = new Thread(boat);
		kexThread = new Thread(kex);
		viewThread = new Thread(view);
		
		boatThread.start();
		kexThread.start();
		viewThread.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		kex.stop();
		boat.stop();
		view.stop();
	}
	
	/**Generate a random enviroment and save it as a csv file*/
	public static void generateRandomMap(int sizeX, int sizeY, String fileName) {
		
		//Matrix to store values
		Double[][] matrix = new Double[sizeX][sizeY];
		
		//Generate random depth
		
		
		/*idea 01 -> blev inte s� bra...
		double startX = Math.random()*Math.PI*2;
		double startY = Math.random()*Math.PI*2;
		double stepX = (Math.random()+0.5)/sizeX;
		double stepY = (Math.random()+0.5)/sizeY;
		double maxHeight = 5;
		
		for(int i = 0; i<sizeX; i++) {
			for(int j=0; j<sizeY; j++) {
				matrix[i][j] = maxHeight*Math.sin(startX + i*stepX) + maxHeight*Math.cos(startY + stepY*j);
			}
		}*/
		
		
		
		/*idea 02*/ //B�st hittils!
		for(int i = 0; i<sizeX; i++) {
			for(int j=0; j<sizeY; j++) {
				matrix[i][j] = -15.;
			}
		}
		
		//generate "islands" at radom places -> ser n�stan bra ut
		int islands = 600;
		for(int k = 0; k<islands; k++) {
			int posX = (int) (Math.random()*sizeX);
			int posY = (int) (Math.random()*sizeY);
			int r = (int)(Math.random()*(sizeX+sizeY)/10);
			for(int i = 0; i<sizeX; i++) {
				for(int j=0; j<sizeY; j++) {
					if( (posX-i)*(posX-i) + (posY-j)*(posY-j) < r*r)
						matrix[i][j] += 0.5;
				}
			}
		}
		
		//generate "squares" at radom places ->massa sm� landmassor
		/*
		int islands = 23000;
		for(int k = 0; k<islands; k++) {
			int posX = (int) (Math.random()*sizeX);
			int posY = (int) (Math.random()*sizeY);
			int width = (int)(Math.random()*(sizeX)/10);
			int height = (int)(Math.random()*(sizeY)/10);					
			for(int i = posX-(width/2); i<posX+(width/2); i++) {
				for(int j = posY-(height/2); j<posY+(height/2); j++) {
					if( i < sizeX && i >=0 && j<sizeY && j>=0)
						matrix[i][j] += 0.3;
				}
			}
		}*/

		
		/*Write file*/
		PrintWriter writer;
		
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			for(int i = 0; i<sizeX; i++) {
				StringBuilder line = new StringBuilder();
				for(int j=0; j<(sizeY-1); j++) {
					line.append(matrix[i][j] + " ,");
				}
				line.append(" " + matrix[i][sizeY-1]);
				writer.println(line.toString());
			}
			writer.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFound");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.out.println("UnsupportedEncodingException");
			e.printStackTrace();
		}
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("JAG LEVER!");
		
		generateRandomMap(100,100,"test.csv");
		
		TestEnvironment t = new TestEnvironment("test.csv");
		
	}
}
