package Robos;

public class EnemyPosition implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private double x = 0.0;
	private double y = 0.0;
	private double bearing = 0.0;

	public EnemyPosition(double x, double y, double bearing) {
		this.x = x;
		this.y = y;
		this.setBearing(bearing);
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
}
