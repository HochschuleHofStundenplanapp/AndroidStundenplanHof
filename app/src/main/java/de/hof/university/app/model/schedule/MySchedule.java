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

import de.hof.university.app.util.Define;

/**
 * Created by danie on 30.11.2016.
 */

public class MySchedule extends Schedule {
	private static final long serialVersionUID = Define.serialVersionUIDv1;

	private ArrayList<String> ids;

	public MySchedule() {
		super();
		this.ids = new ArrayList<>();
	}

	public void setIds(ArrayList<String> ids) {
		this.ids = ids;
	}

	/**
	 * returns the IDs if not null, if null get the IDs from the lectures and set it
	 * @return the IDs
	 */
	public ArrayList<String> getIds() {
		// wenn IDs vorhanden
		if (ids != null) {
			return ids;
		} else {
			// falls nicht, dann hole die IDs aus den lectures
			ArrayList<String> result = new ArrayList<>();
			for ( LectureItem lectureItem : getLectures() ) {
				result.add( lectureItem.getId() );
			}
			setIds(result);
			return result;
		}
	}
}
