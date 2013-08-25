package com.reviosync.hushcal;

import java.util.Calendar;

public class HCEvent {

	int id;
	private String name;
	private long start_time;
	private long end_time;
	private String status;
	
	public HCEvent() {
		
	}
	
	public HCEvent(int id, String name, long start_time, long end_time, String status) {
		this.id = id;
		this.name = name;
		this.start_time = start_time;
		this.end_time = end_time;
		this.status = status;
	}
	
	public HCEvent(String name, long start_time, long end_time, String status) {
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
	
	public long getStartTime() {
		return this.start_time;
	}
	
	public void setStartTime(long start_time) {
		this.start_time = start_time;
	}
	
	public long getEndTime() {
		return this.end_time;
	}
	
	public void setEndTime(long end_time) {
		this.end_time = end_time;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
}
