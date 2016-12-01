package de.hof.university.app.model.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.hof.university.app.model.SaveObject;

/**
 * Created by danie on 30.11.2016.
 */

public class Schedule extends SaveObject {
	ArrayList<LectureItem> lectures;
	String course;
	String semester;
	String termtime;

	public Schedule() {
		super();
		this.lectures = new ArrayList<>();
	}

	public ArrayList<LectureItem> getLectures() {
		return lectures;
	}

	public void setLectures(ArrayList<LectureItem> lectures) {
		this.lectures = lectures;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}

	public String getTermtime() {
		return termtime;
	}

	public void setTermtime(String termtime) {
		this.termtime = termtime;
	}
}
