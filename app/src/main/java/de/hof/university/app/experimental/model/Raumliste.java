package de.hof.university.app.experimental.model;

import java.util.ArrayList;

import de.hof.university.app.model.HofObject;

/**
 * Created by danie on 01.12.2016.
 */

public class Raumliste extends HofObject {
	ArrayList<Level> raumlist;
	String timeStart;
	String timeEnd;
	String raumTyp;
	String date;

	public Raumliste() {
		this.raumlist = new ArrayList<>();
		this.timeStart = null;
		this.timeEnd = null;
		this.raumTyp = null;
		this.date = null;
	}

	public ArrayList<Level> getRaumlist() {
		return raumlist;
	}

	public void setRaumlist(ArrayList<Level> raumlist) {
		this.raumlist = raumlist;
	}

	public String getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(String timeStart) {
		this.timeStart = timeStart;
	}

	public String getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(String timeEnd) {
		this.timeEnd = timeEnd;
	}

	public String getRaumTyp() {
		return raumTyp;
	}

	public void setRaumTyp(String raumTyp) {
		this.raumTyp = raumTyp;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
