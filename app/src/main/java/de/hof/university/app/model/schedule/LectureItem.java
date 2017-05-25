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

import de.hof.university.app.BuildConfig;

/**
 * Created by larsg on 09.05.2016.
 */
public class LectureItem implements Comparable<LectureItem>, Serializable {
    //not used: private static final String date_regex = "dd-MM-yyyy HH:mm:ss";

    private final String id;
    private final String weekday;
    private final String label;
    private final String type;
    private final String style;
    private final String sp;
    private final String group;
    private final String begin;
    private final String end;
    private final String startdate;
    private final String enddate;
    private final String room;
    private final String lecturer;
    private final String comment;

    public LectureItem(final String id, final String weekday, final String label, final String type, final String style, final String sp, final String group,
                       final String begin, final String end, final String startdate, final String enddate, final String room, final String lecturer, final String comment) {
        this.id = id;
        this.weekday = weekday;
        this.label = label;
        this.type = type;
        this.style = style;
        this.sp = sp;
        this.group = group;
        this.begin = begin;
        this.end = end;
        this.startdate = startdate;
        this.enddate = enddate;
        this.room = room;
        this.lecturer = lecturer;
        this.comment = comment.replaceFirst("^- ", "");
    }

    @Override
    public String toString() {
        return "LectureItem{" +
                "id='" + id + '\'' +
                ", weekday='" + weekday + '\'' +
                ", group='" + group + '\'' +
                ", begin='" + begin + '\'' +
                ", end='" + end + '\'' +
                ", startdate='" + startdate + '\'' +
                ", enddate='" + enddate + '\'' +
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

    public final String getBegin() {
        return begin;
    }

    public final String getStartdate() {
        return startdate;
    }

    public final String getEnddate() {
        return enddate;
    }

    public final String getRoom() {
        return room;
    }

    public final String getTime() {
        return begin + " - " + end;
    }

    public final String getDetails() {
        String result = label;

        if (!sp.equals("")) {
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
        // Jahr
        if (Integer.parseInt(getStartdate().substring(6)) > Integer.parseInt(lectureItem.getStartdate().substring(6))) {
            return +1;
        } else if (Integer.parseInt(getStartdate().substring(6)) == Integer.parseInt(lectureItem.getStartdate().substring(6))) {
            // Monat
            if (Integer.parseInt(getStartdate().substring(3, 5)) > Integer.parseInt(lectureItem.getStartdate().substring(3, 5))) {
                return +1;
            } else if (Integer.parseInt(getStartdate().substring(3, 5)) == Integer.parseInt(lectureItem.getStartdate().substring(3, 5))) {
                // Tag
                if (Integer.parseInt(getStartdate().substring(0, 2)) > Integer.parseInt(lectureItem.getStartdate().substring(0, 2))) {
                    return +1;
                } else if (Integer.parseInt(getStartdate().substring(0, 2)) == Integer.parseInt(lectureItem.getStartdate().substring(0, 2))) {
                    if (Integer.parseInt(getBegin().substring(0, 2)) > Integer.parseInt(lectureItem.getBegin().substring(0, 2))) {
                        return +1;
                    } else if (Integer.parseInt(getBegin().substring(0, 2)) == Integer.parseInt(lectureItem.getBegin().substring(0, 2))) {
                        return 0;
                    }
                }
            }
        }
        else
            if ( BuildConfig.DEBUG) assert (false);

        return -1;
    }
}
