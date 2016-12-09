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

package de.hof.university.app.Util;

/**
 * Created by stepping on 17.07.2016.
 */
public final class Define {

	// Strings returned from the Webserverinterface from IT-Service of Hochschule Hof,
	// language-INDEPENDENT
	public static final String SCHEDULE_PARSER_LABEL = "label";     //NON-NLS
	public static final String SCHEDULE_PARSER_COMMENT = "comment";     //NON-NLS
	public static final String SCHEDULE_PARSER_GROUP = "group";     //NON-NLS
	public static final String SCHEDULE_PARSER_REASON = "reason";     //NON-NLS
	public static final String COURSE_PARSER_LABELS = "labels";     //NON-NLS
	public static final String COURSE_PARSER_COURSE = "course";     //NON-NLS
	public static final String COURSE_PARSER_SEMESTER = "semester";     //NON-NLS

	public static final String PARSER_SCHEDULE = "schedule"; // NON-NLS
	public static final String PARSER_DAY = "day"; //NON-NLS
	public static final String PARSER_TYPE = "type"; //NON-NLS
	public static final String PARSER_STARTTIME = "starttime"; //NON-NLS
	public static final String PARSER_ENDTIME = "endtime"; //NON-NLS
	public static final String PARSER_STARTDATE = "startdate"; //NON-NLS
	public static final String PARSER_ENDDATE = "enddate"; //NON-NLS
	public static final String PARSER_ROOM = "room"; //NON-NLS
	public static final String PARSER_DOCENT = "docent"; //NON-NLS

	public static final String PARSER_CHANGES = "changes"; //NON-NLS
	public static final String PARSER_TIME = "time"; //NON-NLS
	public static final String PARSER_DATE = "date"; //NON-NLS
	public static final String PARSER_ORIGNAL = "original"; //NON-NLS
	public static final String PARSER_ALTERNATIVE = "alternative"; //NON-NLS

	// Semester term: Winterterm, summerterm (Wintersemester, Sommersemester)


/*	public static final String PARSER_ = "" ; //NON-NLS
	public static final String PARSER_ = "" ; //NON-NLS
	public static final String PARSER_ = "" ; //NON-NLS
	public static final String PARSER_ = "" ; //NON-NLS
	public static final String PARSER_ = "" ; //NON-NLS
	public static final String PARSER_ = "" ; //NON-NLS
*/

	// show the fragments dealing with the location of the user
	public static final int SHOW_LOCATION_FRAGMENT = 0 ;
	// show Notenblatt and Notenfreigabe
	// TODO Weil ausblenden solange die neue Authentifizierungsmethode noch nicht funktioniert
	public static final int SHOW_NOTEN = 0 ;
	// publish notification FMC von Google verwenden
	public static final int SHOW_PUSHNOTIFICATION = 0 ;

}
