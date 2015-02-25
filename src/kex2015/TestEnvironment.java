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
		boat = new Boat(map, 25,0.0,0.0);
		
		/**Create kex*/
		//polygon
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer>();
		int[] endPos = {1,3};
		kex = new Kex(boat, x, y , 0.24 , endPos , 100);
		
		/**Create view*/
		view = new View(map, boat,20);
		
		/**create and start threads*/
		boatThread = new Thread(boat);
		kexThread = new Thread(kex);
		viewThread = new Thread(view);
		
		boatThread.start();
		kexThread.start();
		viewThread.start();
		
		try {
			Thread.sleep(900000000);
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

                    if( (posX-i)*(posX-i) + (posY-j)*(posY-j) < r*r) {
                        matrix[i][j] += Math.random();//0.5;
                        if (matrix[i][j] > 0) {
                            matrix[i][j] += 5.0;
                        }
                    }
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
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("JAG LEVER!");
		
		//generateRandomMap(1000,1000,"test.csv");
		TestEnvironment t = new TestEnvironment("test.csv");
		
		
	}
}
