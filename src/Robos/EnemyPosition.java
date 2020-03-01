package Robos;

public class EnemyPosition implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private double x = 0.0;
	private double y = 0.0;
	private double bearing = 0.0;
	private String name = "";
	private double energy = 0;
	private double distance = 0;
	
	public EnemyPosition(double x, double y, double bearing, double energy, double distance, String name) {
		this.x = x;
		this.y = y;
		this.setBearing(bearing);
		this.energy = energy;
		this.distance = distance;
		this.name = name;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	
}
