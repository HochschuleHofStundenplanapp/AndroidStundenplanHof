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

package de.hof.university.app.model.schedule;

/**
 * Created by larsg on 09.05.2016.
 */
public class Schedule {
    private static final String date_regex = "dd-MM-yyyy HH:mm:ss";

    private int id;
    private final String weekday;
    private final String label;
    private final String type;
    private final String group;
    private final String begin;
    private final String end;
    private final String room;
    private final String lecturer;

    public Schedule( final int id, final String weekday, final String label, final String type, final String group,
                     final String begin, final String end, final String room, final String lecturer) {
        this.id=id;
        this.weekday = weekday;
        this.label = label;
        this.type = type;
        this.group = group;
        this.begin = begin;
        this.end = end;
        this.room = room;
        this.lecturer = lecturer;
    }

    public final int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public final String getWeekday() {
        return weekday;
    }

    public final String getLabel() {
        return label;
    }

    public final String getType() {
        return type;
    }


    public final String getGroup() {
        return group;
    }

    public final String getBegin() {
        return begin;
    }

    public final String getEnd() {
        return end;
    }

    public final String getRoom() {
        return room;
    }

    public final String getLecturer() {
        return lecturer;
    }

    public final String getTime() {
        return begin +" - " + end;
    }

    public final String getDetails() {
        String result = label;

        result += '\n' +lecturer;

        if ( (group != null) && !group.isEmpty() ) {
            result += '\n' +  group ;
        }
        return result;
    }

    public final String getShortDescription() {
        return label + " ("+ begin.split(" ")[1]+ " - "+ end.split(" ")[1]+ ')';
    }
}
