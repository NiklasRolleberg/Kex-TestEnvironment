package kex2015;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Map {
	static double[][] mapData;


	public Map() {
		//TODO instantiate or static?


	}

    /** [NOT DONE] Returns sensor data, possibly depth and front sonar in an array? */
	public double[] getSensorData(double x, double y){
		double[] a = {0,0};
		return a;
	}

	/**[NOT DONE] A function to convert x,y coordinates to index for lookup in the map*/
	private int[] convertToIndex(double x, double y){
		int[] result = {0,0};
		return result;
	}
    /** Read a CSV file */
    public static void readCSVmap(){
        String csvFile = "src\\MapTest.csv";
        BufferedReader br1 = null;
        BufferedReader br2 = null;

        String line = "";
        String csvSplitBy = ",";


        try {
            //read once and allocate memory
            br1 = new BufferedReader(new FileReader(csvFile));
            int m = 0;
            int n = 0;
            BufferedReader tempBR = new BufferedReader(new FileReader(csvFile));
            while ((line = tempBR.readLine()) != null){
                m++;
                n = line.split(csvSplitBy).length;
            }
            mapData = new double[m][n];
            br2 = new BufferedReader(new FileReader(csvFile));

            //loop and save data
            int i = 0; int j = 0;
            while ((line = br2.readLine()) != null) {
                String[] depthValues = line.split(csvSplitBy);
                for (String s : depthValues){
                    mapData[i][j]=Double.parseDouble(s);
                    j++;
                }
                j = 0;
                i++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("No such file, fool!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Some other shit went wrong, fool!");
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
        System.out.println("Read done!");
    }
    public static void printMapData() {  //TODO delete this bullshit method
        int i, j = 0;
        for (double[] row : mapData){
            for (double value : row){
                System.out.println(value + " ");
            }
            System.out.println("");
        }

    }

    /**Nested class for future use? Represent each map element with an object...?!?!?*/
	public class MapElement{
		double depth;
		double x;
		double y;


		public MapElement() {
		}
	}






}

