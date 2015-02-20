package kex2015;

public class Boat implements Runnable {
	
	private double[] position = {0,0};
	private double heading = 0;
	private double speed = 0;
	
	private long dt = 100;
	private long lastUpdate = 0;;
	private boolean stop = false;
	
	private Map map;
	
	public Boat(Map map, long dt) {
		this.dt = dt;
		//TODO fixa saker
		
		
		System.out.println("Boat created");
	}
	
	private void updatePos() {
		System.out.println("UpdatePosition");
		if(lastUpdate == 0) {
			lastUpdate = System.currentTimeMillis();
			return;
		}
		
		long elapsedTime = System.currentTimeMillis() - lastUpdate;
		//TODO Update boat position;
		
		lastUpdate = System.currentTimeMillis();
	}
	
	public void stop() {
		stop = true;
	}

	@Override
	public void run() {
		
		System.out.println("Boat running!");
		
		while(!stop) {
			
			updatePos();
			
			try {
				Thread.sleep(dt);
			} catch (InterruptedException e) {
				System.err.println("Fel i Boat: " + e);
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Boat stopped");
		
	}

}