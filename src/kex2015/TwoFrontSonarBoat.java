package kex2015;

public class TwoFrontSonarBoat extends Boat {

	public TwoFrontSonarBoat(Map map, int dt, double startLong, double startLat) {
		super(map, dt, startLong, startLat);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return
	 * 0: latitude
	 * 1: longitude
	 * 2: heading
	 * 3: speed
	 * 4: sonar 1 (depth)
	 * 5: sonar 2 (forward)
	 */
	@Override
	public double[] getSensordata() {
		
		double[] r = new double[7];
		r[0] = this.position[0];
		r[1] = this.position[1];
		r[2] = this.heading;
		r[3] = this.speed;
		r[4] = map.getDepth(this.position[0], this.position[1]);		
		r[5] = getFrontSonarData(+Math.PI/8); //right
		r[6] = getFrontSonarData(-Math.PI/8); //left

		//noise
		r[4] = r[4] + ((Math.random()-0.5)*0.2*r[4]);
		r[4] += 0.2*Math.sin(System.currentTimeMillis()/1000);

		r[5] += ((Math.random()-0.5)*0.2*r[5]);
		r[6] += ((Math.random()-0.5)*0.2*r[6]);;
		
		return r;
	}
	
private double getFrontSonarData(double angle) {
		
		double depth = map.getDepth(this.position[0], this.position[1]);
		
		double deg = 20*(Math.PI / 180);
		double dist = -depth* Math.tan(deg);
		double posLong = position[0] + dist*Math.cos(heading+angle);
		double posLat = position[1] + dist*Math.sin(heading+angle);
		double depth1 = map.getDepth(posLong, posLat);
		double ray1 = Math.sqrt(dist*dist * depth1*depth1);
		
		deg = 40*(Math.PI / 180);
		dist = -depth* Math.tan(deg);
		posLong = position[0] + dist*Math.cos(heading+angle);
		posLat = position[1] + dist*Math.sin(heading+angle);
		double depth2 = map.getDepth(posLong, posLat);
		double ray2 = Math.sqrt(dist*dist * depth2*depth2);
		
		deg = 60*(Math.PI / 180);
		dist = -depth* Math.tan(deg);
		posLong = position[0] + dist*Math.cos(heading+angle);
		posLat = position[1] + dist*Math.sin(heading+angle);
		double depth3 = map.getDepth(posLong, posLat);
		double ray3 = Math.sqrt(dist*dist * depth3*depth3);
		
		//System.out.println("Sensor: down= " + depth + "\t ray1= " + ray1 + "\t ray2= " + ray2 + "\t ray3= " + ray3);
		
		return Math.min(Math.min(ray1, ray2), ray3);
	}

}
