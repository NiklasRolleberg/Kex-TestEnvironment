package controller;

public class MultiBeamSweepingPattern extends SearchPattern {
	
	SearchElement[][] matrix;
	
	public MultiBeamSweepingPattern(Kex kex, SearchCell region, SearchElement[][] elementMatrix, double delta, long dt) {
		super(kex, region, delta, dt);
		matrix = elementMatrix;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	boolean followingLand() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

}
