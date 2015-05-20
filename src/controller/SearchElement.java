package controller;

import java.util.ArrayList;

/**Smallest element of the map, x and y coords, status and depth data*/
public class SearchElement{
	double xCoord;
	double yCoord;
	int status; // 0 = not scanned, 1 = scanned 2 = not accessible, 3 estimated coast?, 99 oob, 42 test
    double accumulatedDepth;
    int timesVisited;
    
    int x;
    int y;
    
    //false = not targeted before, true = targeted before
    boolean targeted = false;
    
    ArrayList<SearchElement> neighbour = new ArrayList<SearchElement>();

	public SearchElement(double x, double y, int s){
		xCoord = x;
		yCoord = y;
		status = s;
        timesVisited = 0;
	}
	
	
    void updateDepthData(double inDepth){
        if (timesVisited == 0){
            accumulatedDepth = inDepth;
            timesVisited++;
        }
        else {
            timesVisited++;
            accumulatedDepth = (accumulatedDepth + inDepth);
        }
        
        //if(status != 99)
        status = 1;
    }
    double getRecordedDepth(){
        return accumulatedDepth /timesVisited;
    }
}

