package de.hof.university.app.model.settings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import de.hof.university.app.model.SaveObject;

/**
 * Created by danie on 01.12.2016.
 */

public class Courses extends SaveObject {
	private ArrayList<StudyCourse> courses;

	public Courses() {
		this.courses = new ArrayList<>();
	}

	public ArrayList<StudyCourse> getCourses() {
		return courses;
	}

	public void setCourses(ArrayList<StudyCourse> courses) {
		this.courses = courses;
	}

}
