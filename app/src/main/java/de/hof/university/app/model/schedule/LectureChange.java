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

//import de.hof.university.app.R;
//import android.content.Context;

import java.io.Serializable;

/**
 * Created by larsg on 10.05.2016.
 */
public class LectureChange implements Serializable {
    private final String label;
    private final String comment;
    private final String group;
    private final String reason;
    private final String begin_old;
    private final String begin_new;
    private final String room_old;
    private final String room_new;
    private final String lecturer;

    public LectureChange(final String label, final String comment, final String group, final String reason,
                         final String begin_old, final String begin_new, final String room_old, final String room_new, final String lecturer) {
        this.label = label;
        this.comment = comment;
        this.group = group;
        this.reason = reason;
        this.begin_old = begin_old;
        this.begin_new = begin_new;
        this.room_old = room_old;
        this.room_new = room_new;
        this.lecturer = lecturer;
    }

    public final String getLabel() {
        return label;
    }

    public final String getComment() {
        return comment;
    }

    public final String getGroup() {
        return group;
    }

    public final String getReason() {
        return reason;
    }

    public final String getBegin_old() {
        return begin_old;
    }

    public final String getBegin_new() {
        return begin_new;
    }

    public final String getRoom_old() {
        return room_old;
    }

    public final String getRoom_new() {
        return room_new;
    }

    public final String getLecturer() {
        return lecturer;
    }

    @Override
    public String toString() {
        return "LectureChange{" +
                "label='" + label + '\'' +
                ", comment='" + comment + '\'' +
                ", group='" + group + '\'' +
                ", reason='" + reason + '\'' +
                ", begin_old='" + begin_old + '\'' +
                ", begin_new='" + begin_new + '\'' +
                ", room_old='" + room_old + '\'' +
                ", room_new='" + room_new + '\'' +
                ", lecturer='" + lecturer + '\'' +
                '}';
    }

    public final String getOld() {
        String result = begin_old;
        // Raum soll immer angezeigt werden
        //if (!room_old.equalsIgnoreCase(room_new)) {
        result += " - " + room_old;
        //}
        return result;
    }

    public final String getNew() {
        String result = begin_new;
        // Raum soll immer angezeigt werden
        //if (!room_old.equalsIgnoreCase(room_new)) {
        result += " - " + room_new;
        //}
        return result;
    }

    public final String getDetails() {

        String result = label + '\n'; // Fach

        result += lecturer + '\n';     // Dozent

        if ((null != group) && !group.isEmpty()) {
            result += group + '\n';   // Übungsgruppe
        }

        if ((null != reason) && !reason.isEmpty()) {
            result += reason;         // Grund
        }

        return result;
    }
}
