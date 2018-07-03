/*
 * Copyright (c) 2016 Lars Gaidzik & Lukas Mahr
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

import java.io.Serializable;

/**
 * Created by Lukas on 05.07.2016.
 */
public class Suchdetails implements Level, Serializable {

	public final static String TAG = "Suchdetails";

	private final String datum;
	private final String timeFrom;
	private final String timeTo;

	public Suchdetails(final String datum, final String timeFrom, final String timeTo) {
		super();
		this.datum = datum;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
	}

	public final String getDate() {
		return datum;
	}

	public final String getTimeFrom() {
		return timeFrom;
	}

	public final String getTimeTo() {
		return timeTo;
	}

	@Override
	public final int getLevel() {
		return 2;
	}


}
