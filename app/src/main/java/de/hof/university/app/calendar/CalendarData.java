/*
 * Copyright (c) 2017 Hochschule Hof, Daniel Glaser
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


package de.hof.university.app.calendar;

import de.hof.university.app.util.Define;
import de.hof.university.app.model.HofObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Daniel on 13.05.2017.
 */

public class CalendarData extends HofObject {
    private static final long serialVersionUID = Define.serialVersionUIDv1;

    private Long calendarID;
    // besteht aus lectureID und EventID
    private final HashMap<String, ArrayList<Long>> lecturesEventIDs;
    private final HashMap<String, ArrayList<Long>> changesEventIDs;

    public CalendarData() {
	    super();
	    calendarID = null;
	    lecturesEventIDs = new HashMap<>();
	    changesEventIDs = new HashMap<>();
    }

    public Long getCalendarID() {
        return calendarID;
    }

    public void setCalendarID(Long calendarID) {
        this.calendarID = calendarID;
    }

    public HashMap<String, ArrayList<Long>> getLecturesEventIDs() {
        return lecturesEventIDs;
    }

    public HashMap<String, ArrayList<Long>> getChangesEventIDs() {
        return changesEventIDs;
    }

    /* TODO never used???
    public void setLecturesEventIDs(HashMap<String, ArrayList<Long>> lecturesEventIDs) {
        this.lecturesEventIDs = lecturesEventIDs;
    }

    public void setChangesEventIDs(HashMap<String, ArrayList<Long>> changesEventIDs) {
        this.changesEventIDs = changesEventIDs;
    }
    */
}
