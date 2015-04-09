package algorithms;

import java.util.ArrayList;

import kex2015.Boat;

public class ContourAlgorithm extends Kex {
	
	boolean stop = false;

	public ContourAlgorithm(Boat inBoat, ArrayList<Double> x,
			ArrayList<Double> y, double delta, int[] endPos, long dt) {
		super(inBoat, x, y, delta, endPos, dt);
		// TODO Auto-generated constructor stub
	}
	
	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}catch(Exception e) {}
	}

	@Override
	public void stop() {
		stop = true;
	}

	@Override
	public void run() {
		
		double[] initialSensorData = boat.getSensordata();
	
		double targetDepth = initialSensorData[4];
		double tol = 0.1;
		
		double lastDepth = initialSensorData[4];
		
		boat.setWayPoint(initialSensorData[0] + 100, initialSensorData[1] + 100);
		boat.setTargetSpeed(3);
		
		while(!stop) {
			
			double[] sensorData = boat.getSensordata();
			double xPos = sensorData[0];
			double yPos = sensorData[1];
			double depth = sensorData[4];
			double heading = sensorData[2];
			
			//at target depth keep driving in that direction
			if(Math.abs(depth-targetDepth) < tol) {
				lastDepth = depth;
				boat.setWayPoint(xPos + Math.cos(heading) * 50, yPos + Math.sin(heading) * 50);
			} 
			//not at target depth, check which side is deeper, and turn towards/ from it
			else {
				double frontsonarDataRight;// = initialSensorData[5];
				double frontsonarDataForward;
				double frontsonarDataLeft;// = initialSensorData[5];
				double data[] = boat.getSensordata();
				
				//check depth to the right
				boat.setWayPoint(data[0] + Math.cos(heading + Math.PI/8) * 50, data[1] + Math.sin(heading + Math.PI/8) * 50);
				sleep(dt/3);
				frontsonarDataRight = boat.getSensordata()[5];
				
				//check data to the left
				data = boat.getSensordata();
				boat.setWayPoint(data[0] + Math.cos(heading - Math.PI/8) * 50, data[1] + Math.sin(heading - Math.PI/8) * 50);
				sleep(dt/3);
				frontsonarDataLeft = boat.getSensordata()[5];
				
				//check data forward
				data = boat.getSensordata();
				boat.setWayPoint(data[0] + Math.cos(heading) * 50, data[1] + Math.sin(heading) * 50);
				sleep(dt/3);
				frontsonarDataForward = boat.getSensordata()[5];
				
				System.out.println(""+frontsonarDataForward + "\t" + frontsonarDataRight + "\t" + frontsonarDataLeft);
				
				//Go towards deeper water
				if(data[4] > targetDepth) {
					System.out.println("Go to deeper water");
					//depth deepest forward
					if((frontsonarDataForward >= frontsonarDataLeft) && (frontsonarDataForward >= frontsonarDataRight)) {
						boat.setWayPoint(data[0] + Math.cos(heading) * 50, data[1] + Math.sin(heading) * 50);
						System.out.println("Forward");
					}
					
					//depth deepest to the right
					else if((frontsonarDataRight >= frontsonarDataLeft) && (frontsonarDataRight >= frontsonarDataForward)) {
						boat.setWayPoint(data[0] + Math.cos(data[2] + Math.PI/16) * 50, data[1] + Math.sin(data[2] + Math.PI/16) * 50);
						System.out.println("Right");
					}
					
					//data deepest to the left
					else if((frontsonarDataLeft >= frontsonarDataRight) && (frontsonarDataLeft >= frontsonarDataForward)) {
						boat.setWayPoint(data[0] + Math.cos(data[2] - Math.PI/16) * 50, data[1] + Math.sin(data[2] - Math.PI/16) * 50);
						System.out.println("Left");
					}
					else{ 
						System.out.println("Glömde något");
					}
				}
				
				//go towards shallower water
				else {
					
					System.out.println("Go to shallower water");
					
					//depth most shallow forward
					if((frontsonarDataForward <= frontsonarDataLeft) && (frontsonarDataForward <= frontsonarDataRight)) {
						boat.setWayPoint(data[0] + Math.cos(heading) * 50, data[1] + Math.sin(heading) * 50);
						System.out.println("Forward");
					}
					
					//depth most shallow to the right
					else if((frontsonarDataRight <= frontsonarDataLeft) && (frontsonarDataRight <= frontsonarDataForward)) {
						boat.setWayPoint(data[0] + Math.cos(data[2] - Math.PI/32) * 50, data[1] + Math.sin(data[2] - Math.PI/32) * 50);
						System.out.println("Right");
					}
					
					//data most shallow to the left
					else if((frontsonarDataLeft <= frontsonarDataRight) && (frontsonarDataLeft <= frontsonarDataForward)) {
						boat.setWayPoint(data[0] + Math.cos(data[2] + Math.PI/32) * 50, data[1] + Math.sin(data[2] + Math.PI/32) * 50);
						System.out.println("Left");
					}
					else{ 
						System.out.println("Glömde något");
					}
				}
				
				/*
				
				//turn right
				if(frontsonarDataRight > frontsonarDataLeft && (depth-targetDepth) < 0 || 
						frontsonarDataRight < frontsonarDataLeft && (depth-targetDepth) < 0) {
					boat.setWayPoint(data[0] + Math.cos(data[2] - Math.PI/16) * 50, data[1] + Math.sin(data[2] - Math.PI/16) * 50);
					System.out.println("Turn right");
				}
				//turn left
				else {
					boat.setWayPoint(data[0] + Math.cos(data[2] + Math.PI/16) * 50, data[1] + Math.sin(data[2] + Math.PI/16) * 50);
					System.out.println("turn left");
				}*/
			}
			
			
			
			sleep(dt);
		}

	}

}
