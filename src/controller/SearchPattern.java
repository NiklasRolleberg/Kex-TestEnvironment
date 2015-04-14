package controller;

public abstract class SearchPattern implements Runnable {
	
	Kex kex;
	Kex.SearchCell region;
	double delta;
	long dt;
	
	public SearchPattern(Kex kex, Kex.SearchCell region, double delta, long  dt) {
		this.kex = kex;
		this.region = region;
		this.delta = delta;
		this.dt = dt;
	}
	abstract void stop();
}
