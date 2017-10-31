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

package de.hof.university.app.experimental.model;

import java.util.ArrayList;

import de.hof.university.app.model.HofObject;

/**
 * Created by danie on 01.12.2016.
 */

public class Raumliste extends HofObject {
	private ArrayList<Level> raumlist;
	private String timeStart;
	private String timeEnd;
	private String raumTyp;
	private String date;

	public Raumliste() {
		super();
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
