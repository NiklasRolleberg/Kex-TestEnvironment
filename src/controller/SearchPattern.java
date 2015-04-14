package controller;

public abstract class SearchPattern implements Runnable {
	
	Kex kex;
	Object region;
	double delta;
	long dt;
	
	public SearchPattern(Kex kex, Object region, double delta, long  dt) {
		this.kex = kex;
		this.region = region;
		this.delta = delta;
		this.dt = dt;
	}
	abstract void stop();
}
