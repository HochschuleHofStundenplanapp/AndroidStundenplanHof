package de.hof.university.app.model.schedule;

import java.util.ArrayList;

import de.hof.university.app.model.HofObject;

/**
 * Created by danie on 30.11.2016.
 */

public class Schedule extends HofObject {
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
