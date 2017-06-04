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

package de.hof.university.app.model.schedule;

import java.util.ArrayList;

import de.hof.university.app.Util.Define;
import de.hof.university.app.model.HofObject;

/**
 * Created by danie on 30.11.2016.
 */

public class Schedule extends HofObject {
	private static final long serialVersionUID = Define.serialVersionUIDv1;

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
