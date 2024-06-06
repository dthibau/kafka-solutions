package org.formation.model;

public class Position {

	public double latitude;
	public double longitude;
	
	public Position() {
		
	}
	public Position(double latitude, double longitude) {
		super();
		this.latitude = latitude;
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
