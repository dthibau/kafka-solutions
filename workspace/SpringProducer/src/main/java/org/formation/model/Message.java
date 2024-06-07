package org.formation.model;

import java.time.LocalDateTime;
import java.util.Date;

public class Message {

	private int index;
	private Date timestamp;
	private Courier courier;
	
	public Message(int index, Date timestamp, Courier courier) {
		super();
		this.index = index;
		this.timestamp = timestamp;
		this.courier = courier;
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Courier getCourier() {
		return courier;
	}
	public void setCourier(Courier courier) {
		this.courier = courier;
	}
	
	
}
