package com.example.hushcal;

import java.util.Calendar;

public class Event {

	int id;
	private String name;
	private Calendar start_time;
	private Calendar end_time;
	private String status;
	
	public Event() {
		
	}
	
	public Event(int id, String name, Calendar start_time, Calendar end_time, String status) {
		this.id = id;
		this.name = name;
		this.start_time = start_time;
		this.end_time = end_time;
		this.status = status;
	}
	
	public Event(String name, Calendar start_time, Calendar end_time, String status) {
		this.name = name;
		this.start_time = start_time;
		this.end_time = end_time;
		this.status = status;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Calendar getStartTime() {
		return this.start_time;
	}
	
	public void setStartTime(Calendar start_time) {
		this.start_time = start_time;
	}
	
	public Calendar getEndTime() {
		return this.end_time;
	}
	
	public void setEndTime(Calendar end_time) {
		this.end_time = end_time;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
}
