package controller;

import java.util.ArrayList;
/**Polygon computation stuff library */
public class PolygonLib {
    public static double findMax(ArrayList<Double> inList){
        Double temp = Double.MIN_VALUE;
        for (Double d : inList){
            if(d > temp){
                temp = d;
            }
        }
        return temp;

    }
    public static double findMin(ArrayList<Double> inList){
        Double temp = Double.MAX_VALUE;
        for (Double d : inList){
            if(d < temp){
                temp = d;
            }
        }
        return temp;

    }
}
