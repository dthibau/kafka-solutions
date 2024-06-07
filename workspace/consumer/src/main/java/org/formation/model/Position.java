package org.formation.model;

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
	public Double distance(Position origin) {

		long R = 6371; // Radius of the earth in km
		var dLat = deg2rad(origin.getLatitude() - this.latitude); // deg2rad below
		var dLon = deg2rad(origin.getLongitude() - this.longitude);
		var a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(deg2rad(this.latitude)) * Math.cos(deg2rad(origin.latitude)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c; // Distance in km

	}

	private double deg2rad(double degre) {
		return degre * (Math.PI / 180);
	}
	@Override
	public String toString() {
		return "Position [latitude=" + latitude + ", longitude=" + longitude + "]";
	}
	
}
