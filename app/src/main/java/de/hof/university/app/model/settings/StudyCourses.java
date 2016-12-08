package de.hof.university.app.model.settings;

import java.util.ArrayList;

import de.hof.university.app.model.SaveObject;

/**
 * Created by danie on 01.12.2016.
 */

public class StudyCourses extends SaveObject {
	private ArrayList<StudyCourse> courses;

	public StudyCourses() {
		this.courses = new ArrayList<>();
	}

	public ArrayList<StudyCourse> getCourses() {
		return courses;
	}

	public void setCourses(ArrayList<StudyCourse> courses) {
		this.courses = courses;
	}

}
