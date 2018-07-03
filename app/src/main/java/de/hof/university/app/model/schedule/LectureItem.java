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

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hof.university.app.util.Define;
import de.hof.university.app.data.DataManager;

/**
 * For one lecture
 * Created by larsg on 09.05.2016.
 */
public class LectureItem implements Comparable<LectureItem>, Serializable {
    private static final long serialVersionUID = Define.serialVersionUIDv2;

	//not used: private static final String date_regex = "dd-MM-yyyy HH:mm:ss";

    private final String id;
    private final String weekday;
    private final String label;
    private final String type;
    //private final String style;
    private final String sp;
    private final String group;
    private final Date startDate;
    private final Date endDate;
    private final String room;
    private final String lecturer;
    private final String comment;

    public LectureItem(final String id, final String weekday, final String label, final String type, final String sp, final String group,
                       final Date startDate, final Date endDate, final String room, final String lecturer, final String comment) {
	    super();
	    this.id = id;
	    this.weekday = weekday;
	    this.label = label;
	    this.type = type;
	    //this.style = style;
	    this.sp = sp;
	    this.group = group;
	    this.startDate = startDate;
	    this.endDate = endDate;
	    this.room = room;
	    this.lecturer = lecturer;
	    this.comment = comment.replaceFirst("^- ", "");
    }

    @Override
    public String toString() {
        return "LectureItem{" +
                "id='" + id + '\'' +
                ", weekday='" + weekday + '\'' +
                ", label='" + label + '\'' +
                ", type='" + type + '\'' +
                //", style='" + style + '\'' +
                ", sp='" + sp + '\'' +
                ", group='" + group + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", room='" + room + '\'' +
                ", lecturer='" + lecturer + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

	public String getId() {
		return id;
	}

	public final String getWeekday() {
		return weekday;
	}

    public final Date getStartDate() {
        return startDate;
    }

    public final Date getEndDate() {
        return endDate;
    }

	public final String getRoom() {
		return room;
	}

    public final String getTime() {
        String resultString;

        SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("HH:mm", DataManager.getInstance().getLocale());

        // Startzeit
        resultString = simpleDateFormatter.format(startDate);

        resultString += " - ";

        // Endzeit
        resultString += simpleDateFormatter.format(endDate);

        return resultString;
    }

    public final String getLabel() {
        return label;
    }

	public final String getDetails() {
		String result = label;

		if (!"".equals(sp)) {
			result += " " + sp;
		}

		// Bisher nur FWPM oder AWPM anzeigen
		if ("FWPM".equals(type) || "AWPM".equals(type)) {
			result += " (" + type + ")";
		}

		// Hier steht unter anderem "Beginn ab KW XY"
		if ((comment != null) && !comment.isEmpty()) {
			result += '\n' + comment;
		}

		if ((group != null) && !group.isEmpty()) {
			result += '\n' + group;
		}

		result += '\n' + lecturer;

		return result;
	}

	@Override
	/*
    ** Sortiert nach Sartzeit
     */
    public int compareTo(final @NonNull LectureItem lectureItem) {
        return startDate.compareTo(lectureItem.startDate);
    }

    public boolean equals(final LectureItem other) {
        return (this.id.equals(other.id)
                && this.weekday.equals(other.weekday)
                && this.label.equals(other.label)
                && this.group.equals(other.group)
                && this.startDate.toString().equals(other.startDate.toString())
                && this.endDate.toString().equals(other.endDate.toString())
                && this.room.equals(other.room)
                && this.lecturer.equals(other.lecturer)
                && this.comment.equals(other.comment));
    }
}
