package de.hof.university.app.model.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by danie on 30.11.2016.
 */

public class Schedule implements Serializable {
	ArrayList<Object> lectures;
	Date lastSaved;

	public Schedule() {
		this.lectures = new ArrayList<>();
		lastSaved = new Date();
	}

	public ArrayList<Object> getLectures() {
		return lectures;
	}

	public void setLectures(ArrayList<Object> lectures) {
		this.lectures = lectures;
	}

	public Date getLastSaved() {
		return lastSaved;
	}

	public void setLastSaved(Date lastSaved) {
		this.lastSaved = lastSaved;
	}
}
