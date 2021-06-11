package org.formation.event;

public class Position {

	private double latitude;
	private double longitude;
	
	public Position() {
		
	}
	public Position(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void move(double latitude, double longitude) {
		this.latitude += latitude;
		this.longitude += longitude;
	}
	@Override
	public String toString() {
		return "Position [latitude=" + latitude + ", longitude=" + longitude + "]";
	}
	
}
