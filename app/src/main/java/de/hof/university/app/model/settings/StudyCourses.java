package de.hof.university.app.model.settings;

import java.util.ArrayList;

import de.hof.university.app.Util.Define;
import de.hof.university.app.model.HofObject;

/**
 * Created by danie on 01.12.2016.
 */

public class StudyCourses extends HofObject {
	private static final long serialVersionUID = Define.serialVersionUID;
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
