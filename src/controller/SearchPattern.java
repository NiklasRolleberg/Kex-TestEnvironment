package controller;

public abstract class SearchPattern implements Runnable {
	
	Kex kex;
	SearchCell region;
	double delta;
	long dt;
	AverageData data;
	XTE xte; 
	
	public SearchPattern(Kex kex, SearchCell region, double delta, long  dt) {
		this.kex = kex;
		this.region = region;
		this.delta = delta;
		this.dt = dt;
		data = new AverageData(dt/2);
		Thread dataThread = new Thread(data);
		dataThread.start();
		
		xte = new XTE(false);//false = xte turned of
		Thread xteThread = new Thread(xte);
		xteThread.start();
	}
	abstract void stop();
	
	abstract boolean followingLand();
	
	abstract boolean isDone();
	
	/**Keeps the boat on track
	 */
	class XTE implements Runnable{
		boolean on = false;
		boolean stop = false;
		
		double targetX;
		double targetY;
		
		double lastTargetX;
		double lastTargetY;
		
		XTE(boolean on) {
			this.on = on;
		}
		
		public void stop() {
			stop = true;
		}
		
		public void setWaypoint(double x, double y) {
			on = false;
			lastTargetX = targetX;
			lastTargetY = targetY;
			targetX = x;
			targetY = y;
			kex.setWaypoint(targetX,targetY);
		}
		
		/**
		 * @param startX
		 * @param startY
		 * @param stopX
		 * @param stopY
		 */
		public void setWaypoint(double startX, double startY, double stopX, double stopY) {
			on = true;
			lastTargetX = startX;
			lastTargetY = startY;
			targetX = stopX;
			targetY = stopY;
			kex.setWaypoint(stopX,stopY);
		}

		@Override
		public void run() {

			while(!stop) {
				
				if(on) {
					//System.out.println("XTE update");
					double norm = Math.sqrt((targetX-lastTargetX)*(targetX-lastTargetX) + (targetY-lastTargetY)*(targetY-lastTargetY));
					//System.out.println(norm);
					if(norm == 0)
						on = false;
					else {
						//distance to next waypoint
						double ahead = 10 + data.getSpeed();//10;
						
						if(Math.sqrt((targetX-data.posX)*(targetX-data.posX) + (targetY-data.posY)*(targetY-data.posY)) < ahead)
							kex.setWaypoint(targetX, targetY);
						else {
							double[] n = {(targetX-lastTargetX) / norm , (targetY-lastTargetY) / norm};
							double[] v ={data.getPosX() - lastTargetX ,data.getPosY() - lastTargetY};
							//u = proj_n (V)
							double sp = (v[0]*n[0] + v[1]*n[1]);
							double[] u = {sp*n[0], sp*n[1]};
							kex.setWaypoint(lastTargetX + u[0] + ahead * n[0], lastTargetY + u[1] + ahead * n[1]);
						}
					}
				}
				
				try {
					Thread.sleep(dt*5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Collects data from kex and calculates a mean value
	 * boat is assumed to be instance of "twofrontsonarboat"
	 *
	 */
	class AverageData implements Runnable{
		
		private long delay;
		private boolean stop = false;
		
		private double posX;
		private double posY;
		private double heading;
		private double speed;
		private Mean depth;
		private Mean rightSonar;
		private Mean leftSonar;
		
		AverageData(long updateDelay) {
			
			//nr = number of values to calculate mean from
			int nr = 1;
			depth = new Mean(nr);
			rightSonar = new Mean(nr);
			leftSonar = new Mean(nr);
			
			this.delay = updateDelay;
			double[] data = kex.getData();
			posX = data[0];
			posY = data[1];
			heading = data[2];
			speed = data[3];
			depth.add(data[4]);
			rightSonar.add(data[5]);
			leftSonar.add(data[6]);
		}
		
		public double getPosX() {return posX;}
		public double getPosY() {return posY;}
		public double getHeading(){return heading;}
		public double getSpeed(){return speed;}
		public double getDepth(){return depth.getValue();}
		public double getRightSonar(){return rightSonar.getValue();};
		public double getLeftSonar(){return leftSonar.getValue();}
		
		public void stop() {
			stop = true;
		}

		@Override
		public void run() {
			
			while(!stop) {
				
				double[] data = kex.getData();
				posX = data[0];
				posY = data[1];
				heading = data[2];
				speed = data[3];
				depth.add(data[4]);
				rightSonar.add(data[5]);
				leftSonar.add(data[6]);
				
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
		
		class Mean {
			
			double[] values;
			boolean empty = true;
			int n;
			
			/** Calculates a mean value of the last given values 
			 * @param number
			 * number of values to calculate mean from
			 */
			Mean(int number) {
				this.n = number;
				values = new double[number]; 
			}
			
			/**
			 * @return
			 * mean value of the last n values
			 */
			public double getValue() {
				double mean  = 0;
				for(int i=0;i<n;i++)
					mean += values[i];
				mean /=n;
				return mean;
			}
			
			public void add(double input) {
				if(empty) {
					empty = false;
					for(int i=0;i<n;i++)
						values[i] = input; //values will be weighted at fist...
				}
				else {
					//Move all values one step back, exept for the last one
					for(int i=n-2; i>=0;i--) {
						values[i+1] = values[i];
					}
					//input the new value at pos 0;
					values[0] = input;
				}
			}
		}
	}
}
