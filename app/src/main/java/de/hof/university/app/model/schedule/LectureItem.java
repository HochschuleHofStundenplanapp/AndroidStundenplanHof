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

import de.hof.university.app.Util.Define;
import de.hof.university.app.data.DataManager;

/**
 * Created by larsg on 09.05.2016.
 */
public class LectureItem implements Comparable<LectureItem>, Serializable {
    private static final long serialVersionUID = Define.serialVersionUIDv2;

    //not used: private static final String date_regex = "dd-MM-yyyy HH:mm:ss";

    private final String id;
    private final String weekday;
    private final String label;
    private final String type;
    private final String style;
    private final String sp;
    private final String group;
    private final Date startDate;
    private final Date endDate;
    private final String room;
    private final String lecturer;
    private final String comment;

    public LectureItem(final String id, final String weekday, final String label, final String type, final String style, final String sp, final String group,
                       final Date startDate, final Date endDate, final String room, final String lecturer, final String comment) {
        this.id = id;
        this.weekday = weekday;
        this.label = label;
        this.type = type;
        this.style = style;
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

        if (sp != null && !sp.equals("")) {
            result += " " + sp;
        }

        // Bisher nur FWPM oder AWPM anzeigen
        if (type.equals("FWPM") || type.equals("AWPM")) {
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
    public int compareTo(@NonNull LectureItem lectureItem) {
        return startDate.compareTo(lectureItem.startDate);
//        // Jahr
//        if (Integer.parseInt(getStartDate().substring(6)) > Integer.parseInt(lectureItem.getStartDate().substring(6))) {
//            return +1;
//        } else if (Integer.parseInt(getStartDate().substring(6)) == Integer.parseInt(lectureItem.getStartDate().substring(6))) {
//            // Monat
//            if (Integer.parseInt(getStartDate().substring(3, 5)) > Integer.parseInt(lectureItem.getStartDate().substring(3, 5))) {
//                return +1;
//            } else if (Integer.parseInt(getStartDate().substring(3, 5)) == Integer.parseInt(lectureItem.getStartDate().substring(3, 5))) {
//                // Tag
//                if (Integer.parseInt(getStartDate().substring(0, 2)) > Integer.parseInt(lectureItem.getStartDate().substring(0, 2))) {
//                    return +1;
//                } else if (Integer.parseInt(getStartDate().substring(0, 2)) == Integer.parseInt(lectureItem.getStartDate().substring(0, 2))) {
//                    if (Integer.parseInt(getBeginTime().substring(0, 2)) > Integer.parseInt(lectureItem.getBeginTime().substring(0, 2))) {
//                        return +1;
//                    } else if (Integer.parseInt(getBeginTime().substring(0, 2)) == Integer.parseInt(lectureItem.getBeginTime().substring(0, 2))) {
//                        return 0;
//                    }
//                }
//            }
//        }
//        else
//            if ( BuildConfig.DEBUG) assert (false);
//
//        return -1;
    }

    public boolean equals(LectureItem other) {
        if (!this.toString().equals(other.toString())) {
            return false;
        }
        return true;
    }
}
