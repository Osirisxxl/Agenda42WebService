package com.sqli.support.jepicard.agenda.model;

import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Event {
	
	private String name;
	private Date beginDate;
	private String description;
	private String place;
	private boolean alert;
	@XmlElementWrapper(name="contacts")
	@XmlElement(name="contact")
	private ArrayList<PersonInvited> invited;
	private String id;
	private int duration;
	private String ownerID;

	public Event(String ownerID, String name, Date beginDate,
			String description, String place, boolean alert, String id,
			int duration) {
		super();
		this.ownerID = ownerID;
		this.name = name;
		this.beginDate = beginDate;
		this.description = description;
		this.place = place;
		this.alert = alert;
		this.invited = new ArrayList<PersonInvited>();
		this.id = id;
		this.duration = duration;
	}

	public Event() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public boolean getAlert() {
		return alert;
	}

	public void setAlert(boolean alert) {
		this.alert = alert;
	}

	public ArrayList<PersonInvited> getInvited() {
		return invited;
	}

	public void setInvited(ArrayList<PersonInvited> invited) {
		this.invited = invited;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getOwnerID() {
		return ownerID;
	}

	public void setOwnerID(String ownerID) {
		this.ownerID = ownerID;
	}
}
