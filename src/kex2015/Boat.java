package kex2015;

public class Boat implements Runnable {
	
	private double[] position = {0,0};
	private double heading = 0;
	private double speed = 0;
	
	private long dt = 100;
	private boolean stop = false;
	
	public Boat(long dt) {
		this.dt = dt;
		//TODO fixa saker
		
	}
	
	private void updatePos(double elapsedTime) {
		System.out.println("UpdatePosition");
		//TODO Update position of boat
	}
	
	public void stop() {
		stop = true;
	}

	@Override
	public void run() {
		
		while(!stop) {
			
			System.out.println("JAG ÄR EN BÅT!");
			
			try {
				Thread.sleep(dt);
			} catch (InterruptedException e) {
				System.err.println("Fel i Boat: " + e);
				e.printStackTrace();
			}
			
		}
		
	}

}
