/*
 * Copyright (c) 2017 Daniel Glaser
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.hof.university.app.model.settings;

import java.util.ArrayList;
import java.util.Collections;

import de.hof.university.app.Util.Define;
import de.hof.university.app.model.HofObject;

/**
 * Created by danie on 01.12.2016.
 */

public class StudyCourses extends HofObject {
	private static final long serialVersionUID = Define.serialVersionUIDv1;

	private ArrayList<StudyCourse> courses;

	public StudyCourses() {
		this.courses = new ArrayList<>();
	}

	public ArrayList<StudyCourse> getCourses() {
		return courses;
	}

	public void setCourses(ArrayList<StudyCourse> courses) {
		Collections.sort(courses);
		this.courses = courses;
	}

}
