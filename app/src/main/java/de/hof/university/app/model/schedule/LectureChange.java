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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hof.university.app.util.Define;
import de.hof.university.app.data.DataManager;

/**
 * Created by larsg on 10.05.2016.
 */
public class LectureChange implements Serializable {
    private static final long serialVersionUID = Define.serialVersionUIDv2;

    private final String id;
    private final String label;
    private final String comment;
    private final String group;
    private final String reason;
    private final String text;
    private final Date begin_old;
    private final Date begin_new;
    private final String room_old;
    private final String room_new;
    private final String lecturer;

    public LectureChange(final String id, final String label, final String comment, String text, final String group, final String reason,
                         final Date begin_old, final Date begin_new, final String room_old, final String room_new, final String lecturer) {
	    super();
	    this.id = id;
	    this.label = label;
	    this.comment = comment;
	    this.group = group;
	    this.reason = reason;
	    this.text = text;
	    this.begin_old = begin_old;
	    this.begin_new = begin_new;
	    this.room_old = room_old;
	    this.room_new = room_new;
	    this.lecturer = lecturer;
    }

    @Override
    public String toString() {
        return "LectureChange{" +
                "label='" + label + '\'' +
                ", comment='" + comment + '\'' +
                ", group='" + group + '\'' +
                ", reason='" + reason + '\'' +
                ", text='" + text + '\'' +
                ", comment=´" + comment + '\'' +
                ", begin_old='" + begin_old + '\'' +
                ", begin_new='" + begin_new + '\'' +
                ", room_old='" + room_old + '\'' +
                ", room_new='" + room_new + '\'' +
                ", lecturer='" + lecturer + '\'' +
                '}';
    }

    public final String getOld() {
        String result;

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, DataManager.getInstance().getLocale());
        SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("HH:mm", DataManager.getInstance().getLocale());

        result = dateFormat.format(begin_old);
        result += " " + simpleDateFormatter.format(begin_old);

        // Raum soll immer angezeigt werden
        result += " - " + room_old;
        return result;
    }

    public final String getNew() {
        String result = "";

        if (begin_new != null) {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, DataManager.getInstance().getLocale());
            SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("HH:mm", DataManager.getInstance().getLocale());

            result = dateFormat.format(begin_new);
            result += " " + simpleDateFormatter.format(begin_new);
        }

        // Raum soll immer angezeigt werden
        result += " - " + room_new;
        return result;
    }

    public final String getDetails() {

        String result = label + '\n' +'\n'; // Fach

        result += lecturer + '\n';     // Dozent

        if (!"".equals(group)) {
            result += group + '\n';   // Übungsgruppe
        }

        /*if (!"".equals(reason)) {
            result += reason;         // Grund soll aus Datenschutzrechtlichen Gründen in den Apps nicht mehr angezeigt werden!
        }*/

        if (!"".equals(comment)){
            result += "\n" + comment;
        }



        return result;
    }

    public final String getText() {

        if (!"".equals(text)) {
            return text;
        }
        else return "";

    }

    public final String getId() {
        return this.id;
    }

    public String getLabel() {
        return label;
    }

    public Date getBegin_old() {
        return begin_old;
    }

    public Date getBegin_new() {
        return begin_new;
    }

    public String getRoom_new() {
        return room_new;
    }
}
