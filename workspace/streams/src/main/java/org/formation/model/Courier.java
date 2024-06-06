package org.formation.model;

import java.time.LocalDateTime;

public class Courier {

	private String id;
	public Position currentPosition;
	public Position averagePosition;
	public double distance;
	
	public Courier() {
		
	}
	public Courier(String id, Position currentPosition) {
		super();
		this.id = id;
		this.currentPosition = currentPosition;
	}
	
	public void move() {
		currentPosition.move(Math.random()-0.5, Math.random()-0.5);
	}



	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Courier [id=" + id + ", currentPosition=" + currentPosition + ", distance=" + distance + "]";
	}

	
	
}
