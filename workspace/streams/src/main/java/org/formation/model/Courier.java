package org.formation.model;

import java.time.LocalDateTime;

public class Courier {

	private String id;
	private Position currentPosition;
	private LocalDateTime timestamp;
	
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
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "Courier [id=" + id + ", currentPosition=" + currentPosition + ", timestamp=" + timestamp + "]";
	}

	
	
}
