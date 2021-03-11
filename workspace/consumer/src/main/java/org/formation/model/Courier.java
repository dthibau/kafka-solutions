package org.formation.model;

public class Courier {

	private String id;
	private Position currentPosition;
	
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

	public Position getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Position currentPosition) {
		this.currentPosition = currentPosition;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "Courier [id=" + id + ", currentPosition=" + currentPosition + "]";
	}
	
	
}
