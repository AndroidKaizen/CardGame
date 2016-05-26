package com.mine.flippant.models;

public class Contact {
	long photoid;
	String phonenum;
	String name;
	String email;


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getPhotoid() {
		return photoid;
	}
	public void setPhotoid(long photoid) {
		this.photoid = photoid;
	}
	public String getPhonenum() {
		return phonenum;
	}
	public void setPhonenum(String phonenum) {
		this.phonenum = phonenum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
