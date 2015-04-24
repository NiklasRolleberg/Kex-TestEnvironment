package controller;

public abstract class SearchPattern implements Runnable {
	
	Kex kex;
	Kex.SearchCell region;
	double delta;
	long dt;
	AverageData data;
	
	public SearchPattern(Kex kex, Kex.SearchCell region, double delta, long  dt) {
		this.kex = kex;
		this.region = region;
		this.delta = delta;
		this.dt = dt;
		data = new AverageData(dt/2);
		Thread dataThread = new Thread(data);
		dataThread.start();
	}
	abstract void stop();
	
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
		private double depth;
		private double rightSonar;
		private double leftSonar;
		private double heading;
		private double speed;
		
		private boolean landOnRightSide;
		
		AverageData(long updateDelay) {
			this.delay = updateDelay;
			double[] data = kex.getData();
			posX = data[0];
			posY = data[1];
			heading = data[2];
			speed = data[3];
			depth = data[4];
			rightSonar = data[5];
			leftSonar = data[6];
		}
		
		public double getPosX() {return posX;}
		public double getPosY() {return posY;}
		public double getHeading(){return heading;}
		public double getSpeed(){return heading;}
		public double getDepth(){return depth;}
		public double getRightSonar(){return rightSonar;};
		public double getLeftSonar(){return leftSonar;}
		
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
				depth = data[4];
				rightSonar = data[5];
				leftSonar = data[6];
				
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}	
	}
}
