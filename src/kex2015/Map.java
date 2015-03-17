package kex2015;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Map {
	private double[][] mapData;
	private double[] startCoord = new double[2];
	private double resolution;
	private int nRows, nCols;
	
	public Map() {
		System.out.println("Nu �r det n�got som inte st�mmer");
	}
	
	public Map(String fileName) {	//double[] initCoord,  
		readCSVmap(fileName);
		//startCoord[0] = initCoord[0];
		//startCoord[1] = initCoord[1];	
		//this.resolution = resolution;		
	}
	
	/**Returns the depth at the specified x,y index*/
	public double getDepth(double x, double y){
		return mapData[(int)x][(int)y];
		/*
		int x0 = (int) x;
		int x1 = x0 +1;
		
		int y0 = (int) y;
		int y1 = y0 +1;
		
		double r0 = Math.sqrt( (x-x0)*(x-x0) + (y-y0)*(y-y0));
		double r1 = Math.sqrt( (x-x1)*(x-x1) + (y-y0)*(y-y0));
		double r2 = Math.sqrt( (x-x1)*(x-x1) + (y-y1)*(y-y1));
		double r3 = Math.sqrt( (x-x0)*(x-x0) + (y-y1)*(y-y1));
		
		double rot = Math.sqrt(2.0);
		
		try {
			double dr0 = mapData[x0][y0] * ((rot-r0) / rot);
			double dr1 = mapData[x1][y0] * ((rot-r1) / rot);
			double dr2 = mapData[x1][y1] * ((rot-r2) / rot);
			double dr3 = mapData[x0][y1] * ((rot-r3) / rot);
		return (dr0 + dr1 + dr2 + dr3);
		} catch (Exception e) {}
		return -20;
		*/
	}
	
	public double[] getLimits(){
		double[] limits = new double[4];
		limits[0] = 0;	//startCoord[0];
		limits[1] = 0;	//startCoord[1];
		limits[2] = nRows;	//limits[0] + nCols*resolution;
		limits[3] = nCols;	//limits[1] + nRows*resolution;
		return limits;
	}
	
	/**[NOT DONE] A function to convert x,y coordinates to index for lookup in the map*/
  	private int[] convertToIndex(double x, double y){
  		int[] result = {0,0};
  		return result;
  	}
	
    
    /** Read a CSV file 
     * @param fileName include file type (.csv)*/
    public void readCSVmap(String fileName){
    	String csvFile = fileName;
    	BufferedReader br1 = null;
        BufferedReader br2 = null;

        String line = "";
        String csvSplitBy = ",";


        try {
            //read once and allocate memory
            br1 = new BufferedReader(new FileReader(csvFile));
            int m = 0;
            int n = 0;
            while ((line = br1.readLine()) != null){
                m++;
                n = line.split(csvSplitBy).length;
            }
            nRows = m;
            nCols = n;	//TODO test this!!!
            mapData = new double[nRows][nCols];
            br2 = new BufferedReader(new FileReader(csvFile));

            //loop and save data
            int i = 0; int j = 0;
            while ((line = br2.readLine()) != null) {
                String[] depthValues = line.split(csvSplitBy);
                for (String s : depthValues){
                	try{
                    mapData[j][i] = Double.parseDouble(s);
                    i++;
                	}
                	catch(IndexOutOfBoundsException e){
                		System.err.println("Index out of bounds bullcrap");
                		e.printStackTrace();
                	}
                }
                i = 0;
                j++;
            }

            System.out.println("Map data read done!");
        } catch (FileNotFoundException e) {
            System.out.println("No such file, fool!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Some other shit went wrong, fool!");
            e.printStackTrace();
        } finally {
            if (br1 != null) {
                try {
                    br1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br2 != null) {
                try {
                    br2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void printMapData() {  //TODO delete this bullshit method
        int i, j = 0;
        for (double[] row : mapData){
            for (double value : row){
                System.out.println(value + " ");
            }
            System.out.println("");
        }

    }
  ///** [NOT DONE] Returns sensor data, possibly depth and front sonar in an array? */
  	/*
  	private double[] getSensorData(double x, double y){
  		double[] a = {0,0};
  		return a;
  	}*/

  	
  
    






}

